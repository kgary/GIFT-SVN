#pragma once

#include <google/protobuf/io/zero_copy_stream_impl_lite.h>
#include <WinSock2.h>

#include "VrEngageCommon.pb.h"

using namespace vrengage::pb;

/// <summary>
/// A class that wraps around the WinSock api to conveniently send and receive 
/// protobuf messages.
/// </summary>
class ProtobufSocketStream : public google::protobuf::io::CopyingInputStream {
public:
	/// <summary>
	/// Initializes a <see cref='ProtobufSocketStream'/> with a provided 
	/// <see cref='SOCKET'/> used to read and write protobuf messages.
	/// </summary>
	/// <param name='socket'>
	/// The <see cref='SOCKET'/> used to read and write protobuf messages.
	/// </param>
	ProtobufSocketStream(SOCKET socket);

	/// <summary>
	/// Destructor that cleans up resources associated with the socket.
	/// </summary>
	virtual ~ProtobufSocketStream();

	/// <summary>
	/// Performs the read on the socket.
	/// </summary>
	/// <param name='buffer'>The buffer to populate with the read data.</param>
	/// <param name='size'>The size of the <paramref name='buffer'/>.</param>
	/// <returns>The number of read bytes.</returns>
	virtual int Read(void *buffer, int size);

	/// <summary>
	/// Reads and parses a protobuf message from the socket.
	/// </summary>
	/// <returns> The incoming message. Null if no message was read.</returns>
	VrEngageMessage *readMessage();

	/// <summary>
	/// Serializes and sends a provided <see cref='google::protobuf::Message'/>
	/// to the client.
	/// </summary>
	/// <param name='msg'>
	/// The <see cref='google::protobuf::Message'/> to send to the client.
	/// </param>
	void sendMessageToClient(const google::protobuf::Message *msg);
private:
	/// <summary>The socket which is read and written from.</summary>
	const SOCKET socket;
};
