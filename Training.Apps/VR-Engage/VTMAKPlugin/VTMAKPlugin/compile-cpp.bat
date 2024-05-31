@ECHO OFF
::This compiles the C++ files needed to use GIFT's protobuff messages in VR-Engage

set CompileSrc=%~dp0..\..\..\..\GIFT\config\gateway\externalApplications\VR-Engage
set CompileOut=%~dp0
set BaseDir=%CompileSrc%\..\..\..\..
set ProtoCompileExe=%BaseDir%\external\protobuf\bin\protoc

%ProtoCompileExe% -I=%CompileSrc% --cpp_out=%CompileOut% %CompileSrc%\VrEngageCommon.proto %CompileSrc%\VrEngageEnvironment.proto %CompileSrc%\VrEngageLOS.proto