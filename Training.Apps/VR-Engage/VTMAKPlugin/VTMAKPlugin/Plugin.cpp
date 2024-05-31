#include "Logger.h"
#include "MessageHandling.h"
#include "Plugin.h"
#include "PluginProperties.h"
#include "ProtobufSocketStream.h"
#include "VrEngageCommon.pb.h"
#include "VrEngageEnvironment.pb.h"
#include "VrEngageLOS.pb.h"

#include <thread>
#include <vrfcgf/vrfPluginExtension.h>
#include <vrfutil/scenario.h>
#include <WinSock2.h>

using namespace vrengage::pb;

/// <summary>
/// The constant name used to identify the anonymous object we create in order 
/// to run custom scripts.
/// </summary>
std::string EXECUTOR_NAME = "GIFT_global_executor";

/// <summary>
/// The API entry point used to query and affect the VR-Forces simulation 
/// engine.
/// </summary>
DtCgf *cgf = nullptr;

/// <summary>
/// The socket which listens for incoming client connections.
/// </summary>
SOCKET listeningSocket = INVALID_SOCKET;

/// <summary>
/// The thread which handles incoming client requests and provides responses 
/// to them.
/// </summary>
std::thread *socketHandler = nullptr;

/// <summary>
/// A boolean flag that indicates whether or not the plugin is currently 
/// running.
/// </summary>
boolean isRunning;

/// <summary>
/// The function that is called when a scenario has been successfully
/// loaded.
/// </summary>
/// <param name='scenario'>A reference to the scenario that was loaded.</param>
/// <param name='usrData'>
/// Not clear on what this is from the VR-Forces docs. Should always be null.
/// </param>
void onScenarioLoaded(const DtScenario &scenario, void *usrData);

/// <summary>
/// Establishes a socket that listens for incoming traffic from GIFT.
/// </summary>
SOCKET establishSocket();

/// <summary>
/// The method that <see cref='socketHandler' /> runs.
/// </summary>
void handleSocketRequests();

extern "C" {
	DT_VRF_DLL_PLUGIN void DtPluginInformation(DtVrfPluginInformation &info) {
		// Immediately configure the logger
		Logger::getInstance().setLogLevel(PluginProperties::getInstance()->getLogLevel());

		LOG(DEBUG) << "DtPluginInformation(DtVrfPluginInformation&)" << std::endl;

		// Populate metadata about the program
		info.pluginName = "GIFT Interop";
		info.pluginVersion = "2020.1.16";
		info.pluginDescription = "Provides integration with the GIFT Framework";
		info.pluginCreator = "Dignitas Technologies";
	}

	DT_VRF_DLL_PLUGIN bool DtInitializeVrfPlugin(DtCgf *cgf, DtVrfPluginInformation &info) {
		LOG(DEBUG) << "DtInitializeVrfPlugin(" << cgf << ", DtVrfPluginInformation&)" << std::endl;

		// Save a reference to the cgf
		::cgf = cgf;

		// Create the socket connection.
		listeningSocket = establishSocket();
		if (listeningSocket == INVALID_SOCKET) {
			LOG(ERR) << "Plugin failed to load because a listening socket could not be established." << std::endl;
			return false;
		}

		// Create the thread that will handle messages from the socket.
		socketHandler = new std::thread(handleSocketRequests);
		socketHandler->detach();

		return true;
	}

	DT_VRF_DLL_PLUGIN bool DtPostInitializeVrfPlugin(DtCgf *cgf) {
		// Create the global executor
		LOG(ALL) << "Creating the global executor '" << EXECUTOR_NAME << "'." << std::endl;
		cgf->addPostLoadScenarioCallback(onScenarioLoaded, nullptr);
		return true;
	}

	DT_VRF_DLL_PLUGIN void DtUnloadVrfPlugin() {
		LOG(DEBUG) << "DtUnloadVrfPlugin()";

		isRunning = false;

		Logger::getInstance().close();

		// Clean up the sockets
		WSACleanup();
		closesocket(listeningSocket);

		// Cleanup Protobuf
		google::protobuf::ShutdownProtobufLibrary();
	}
}

void onScenarioLoaded(const DtScenario &scenario, void *usrData) {
	LOG(DEBUG) << "onScenarioLoaded(DtScenario&, " << usrData << ")" << std::endl;
	cgf->createWaypoint(DtVector(0, 0, 0), EXECUTOR_NAME);
}

SOCKET establishSocket() {
	LOG(DEBUG) << "establishSocket()" << std::endl;

	WSAData wsaData;
	struct addrinfo *addr = nullptr;
	struct addrinfo hints;

	int startupResult = WSAStartup(MAKEWORD(2, 2), &wsaData);
	if (startupResult != 0) {
		LOG(ERR) << "WSAStartup failed with error: " << WSAGetLastError() << std::endl;
		listeningSocket = INVALID_SOCKET;
		return INVALID_SOCKET;
	}

	ZeroMemory(&hints, sizeof(hints));
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_protocol = IPPROTO_TCP;
	hints.ai_flags = AI_PASSIVE;

	const int &portNumber = PluginProperties::getInstance()->getPort();
	std::stringstream portStrStream;
	portStrStream << portNumber;

	// Query the address info of a socket meeting the criteria in the 'hints' variable
	LOG(INFO) << "Intializing the socket with port " << portNumber << "." << std::endl;
	int getAddrInfoResult = getaddrinfo(NULL, portStrStream.str().c_str(), &hints, &addr);
	if (getAddrInfoResult != 0) {
		LOG(ERR) << "Function getaddrinfo failed with error " << WSAGetLastError() << "." << std::endl;
		WSACleanup();
		listeningSocket = INVALID_SOCKET;
		return listeningSocket;
	}

	// Acquire the socket described by the addr variable
	listeningSocket = socket(addr->ai_family, addr->ai_socktype, addr->ai_protocol);
	if (listeningSocket == INVALID_SOCKET) {
		LOG(ERR) << "Acquiring socket failed with error " << WSAGetLastError() << "." << std::endl;
		freeaddrinfo(addr);
		WSACleanup();
		return listeningSocket;
	}

	// Setup the socket that was acquired
	int bindResult = bind(listeningSocket, addr->ai_addr, (int)addr->ai_addrlen);
	if (bindResult == SOCKET_ERROR) {
		LOG(ERR) << "Socket binding failed with error " << WSAGetLastError() << ". Try changing the port number to a different value for both the GIFT VT MAK plugin and the Gateway module plugin." << std::endl;
		closesocket(listeningSocket);
		listeningSocket = INVALID_SOCKET;
		WSACleanup();
		freeaddrinfo(addr);
		return listeningSocket;
	}

	freeaddrinfo(addr);

	int listenResult = listen(listeningSocket, SOMAXCONN);
	if (listenResult == SOCKET_ERROR) {
		LOG(ERR) << "The listeningSocket failed to list with error " << WSAGetLastError() << "." << std::endl;
		closesocket(listeningSocket);
		listeningSocket = INVALID_SOCKET;
		WSACleanup();
	}

	return listeningSocket;
}

void handleSocketRequests() {
	LOG(DEBUG) << "handleSocketRequests()" << std::endl;

	while (isRunning) {

		// Wait for a client to connect
		LOG(ALL) << "Waiting for an incoming socket connection..." << std::endl;
		auto acceptedSocket = accept(listeningSocket, nullptr, nullptr);

		// If no valid socket was received, log the error and wait for a socket again
		if (acceptedSocket == INVALID_SOCKET) {
			LOG(ERR) << "Listening socket failed to accept with error: " << WSAGetLastError() << "." << std::endl;
			continue;
		}

		LOG(ALL) << "Socket connection received!" << std::endl;
		ProtobufSocketStream clientSocket(acceptedSocket);

		do {
			auto msg = clientSocket.readMessage();
			if (msg == nullptr) {
				break;
			}

			// Handle the message
			const auto response = routeRequest(msg);
			clientSocket.sendMessageToClient(response);
		} while (isRunning);
	}
}
