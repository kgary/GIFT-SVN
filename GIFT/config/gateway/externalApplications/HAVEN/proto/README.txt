The PROTO files contained in GIFT/config/gateway/externalApplications/HAVEN/proto are
provided by the SE Sandbox development team in order to interface with the SE Sandbox 
software. GIFT should not be modifying these files, to ensure they match the
definitions in SE Sandbox.

These PROTO files are designed with the assumption of being in a folder called "proto",
and the protobuf compiler will not work correctly with them if they are not. If the
location of these files are changed, they still need to be placed in a folder called
"proto" at their new location.