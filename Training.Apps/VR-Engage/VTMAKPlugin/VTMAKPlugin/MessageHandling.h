#pragma once

#include "VrEngageCommon.pb.h"

#include <google/protobuf/message.h>

using namespace vrengage::pb;

/// <summary>
/// Handles an incoming request by calling the appropriate handling function 
/// based on the type of the message's payload.
/// </summary>
/// <param name='msg'>
/// The incoming message to handle. Can't be null. Will be deleted before returning.
/// </param>
/// <returns>
/// The response to the incoming request which should be sent to the client.
/// </returns>
const google::protobuf::Message *routeRequest(const VrEngageMessage *msg);
