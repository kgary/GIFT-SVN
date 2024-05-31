#include "Logger.h"
#include "ProtobufSocketStream.h"

#include <google/protobuf/util/delimited_message_util.h>
#include <sstream>

using namespace google::protobuf::io;
using namespace google::protobuf::util;

ProtobufSocketStream::ProtobufSocketStream(SOCKET socket) : socket(socket) {}

ProtobufSocketStream::~ProtobufSocketStream() {
	LOG(DEBUG) << "~ProtobufSocketStream()" << std::endl;

	const auto shutdownResult = shutdown(socket, SD_SEND);
	if (shutdownResult == SOCKET_ERROR) {
		LOG(ERR) << "There was an error shutting down the clientSocket. " << WSAGetLastError() << std::endl;
	}

	const auto closeResult = closesocket(socket);
	if (closeResult == SOCKET_ERROR) {
		LOG(ERR) << "There was an error closing the clientSocket. " << WSAGetLastError() << std::endl;
	}
}

int ProtobufSocketStream::Read(void *buffer, int size) {
	LOG(DEBUG) << "Read(" << buffer << ", " << size << ")" << std::endl;

	return recv(this->socket, (char*)buffer, size, 0);
}

VrEngageMessage *ProtobufSocketStream::readMessage() {
	auto msg = new VrEngageMessage();
	google::protobuf::io::CopyingInputStreamAdaptor streamAdaptor(this);
	auto result = ParseDelimitedFromZeroCopyStream(msg, &streamAdaptor, nullptr);
	if (result) {
		return msg;
	} else {
		delete msg;
		return nullptr;
	}
}

void ProtobufSocketStream::disconnect() {
	LOG(DEBUG) << "disconnect()" << std::endl;

	if (this->socket == INVALID_SOCKET) {
		return;
	}
}

void ProtobufSocketStream::sendMessageToClient(const google::protobuf::Message *response) {
	LOG(DEBUG) << "sendMessageToClient(" << response->ShortDebugString() << ")" << std::endl;

	// Build the Any payload
	LOG(ALL) << "Building the message to send." << std::endl;
	auto anyPayload = new google::protobuf::Any();
	anyPayload->PackFrom(*response);

	// Build the message to send
	VrEngageMessage msg;
	msg.set_allocated_payload(anyPayload);

	// Serialize the response
	LOG(ALL) << "Serializing the message to send." << std::endl;
	std::stringstream stringStream;
	google::protobuf::util::SerializeDelimitedToOstream(msg, &stringStream);
	const std::string output = stringStream.str();
	const char *bytesToSend = output.c_str();
	const int responseSize = output.length();

	// Send the response to the client
	LOG(ALL) << "Sending the response." << std::endl;
	const auto sendResult = send(this->socket, bytesToSend, responseSize, 0);
	if (sendResult == SOCKET_ERROR) {
		LOG(ERR) << "There was an error while sending a response. " << WSAGetLastError() << std::endl;
	} else {
		LOG(ALL) << "Sent " << sendResult << " bytes to the client as a response" << std::endl;
	}

	// Delete the provided payload which was wrapped
	delete response;
}
