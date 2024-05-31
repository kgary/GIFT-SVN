@ECHO OFF

REM This script will serve as the basis for launching the Gateway Module in server mode.

set BaseDir=.\
set BinDir=%BaseDir%\bin
set ExternalLibsDir=%BaseDir%\external
set JavaHome=%ExternalLibsDir%\jre-11

set Class="mil.arl.gift.gateway.GatewayModule"
set JavaClasspath="%BinDir%\gift-common.jar;%BinDir%\gift-gateway-remote.jar;%ExternalLibsDir%\slf4j\*.jar;%ExternalLibsDir%\slf4j\*;%ExternalLibsDir%\jsonsimple\json_simple-1.1.jar;%ExternalLibsDir%\activemq\activemq-all-5.18.3.jar;%ExternalLibsDir%\hibernate\antlr-2.7.6.jar;%ExternalLibsDir%\hibernate\hibernate3.jar;%ExternalLibsDir%\hibernate\junit.jar;%ExternalLibsDir%\hibernate\c3p0-0.9.1.2.jar;%ExternalLibsDir%\hibernate\hibernate-jpa-2.0-api-1.0.0.Final.jar;%ExternalLibsDir%\hibernate\cglib-2.2.jar;%ExternalLibsDir%\hibernate\javassist-3.28.0.GA.jar;%ExternalLibsDir%\hibernate\slf4j-api.jar;%ExternalLibsDir%\hibernate\commons-collections-3.1.jar;%ExternalLibsDir%\hibernate\jpa-api-2.0-cr-1.jar;%ExternalLibsDir%\hibernate\dom4j-2.1.0.jar;%ExternalLibsDir%\hibernate\jta-1.1.jar;%ExternalLibsDir%\jacob\jacob.jar;%ExternalLibsDir%\vecmath.jar;%ExternalLibsDir%\jdis.jar;%BinDir%\jaxb_generated.jar;%BinDir%\protobuf_generated.jar;%BinDir%\protobuf_generated_external_apps.jar;%ExternalLibsDir%\commons-lang-2.4.jar;%ExternalLibsDir%\commons-io-2.3.jar;%ExternalLibsDir%\vecmath.jar;%ExternalLibsDir%\jna-3.5.1.jar;%ExternalLibsDir%\jna-platform-3.5.1.jar;%ExternalLibsDir%\apache-xmlrpc-3.1.3\lib\commons-logging-1.1.jar;%ExternalLibsDir%\apache-xmlrpc-3.1.3\lib\ws-commons-util-1.0.2.jar;%ExternalLibsDir%\apache-xmlrpc-3.1.3\lib\xmlrpc-client-3.1.3.jar;%ExternalLibsDir%\apache-xmlrpc-3.1.3\lib\xmlrpc-common-3.1.3.jar;%ExternalLibsDir%\apache-xmlrpc-3.1.3\lib\xmlrpc-server-3.1.3.jar;%ExternalLibsDir%\rabbitmq-java-client-bin-3.5.4\commons-cli-1.1.jar;%ExternalLibsDir%\rabbitmq-java-client-bin-3.5.4\commons-io-1.2.jar;%ExternalLibsDir%\rabbitmq-java-client-bin-3.5.4\rabbitmq-client.jar;%ExternalLibsDir%\protobuf\bin\protobuf-java-3.7.0.jar;%ExternalLibsDir%\commons-compress-1.4.1.jar;%ExternalLibsDir%\xmleditor.jar;%ExternalLibsDir%\gift-native-x86.jar;%ExternalLibsDir%\gift-native-x64.jar"
set LibraryPathExtension="%ExternalLibsDir%\jacob"
set ClasspathExtension="%ExternalLibsDir%\jacob\*;%BinDir%\gift-gateway-remote.jar;%ExternalLibsDir%\apache-xmlrpc-3.1.3\lib\*;%ExternalLibsDir%\CJWizards-0.2.jar;%ExternalLibsDir%\rabbitmq-java-client-bin-3.5.4\*;%ExternalLibsDir%\protobuf\bin\*;%ExternalLibsDir%\grpc\*;%ExternalLibsDir%\perfmark\*;%ExternalLibsDir%\opencensus\*;%ExternalLibsDir%\guava\*;%BinDir%\protobuf_generated.jar;%BinDir%\protobuf_generated_external_apps.jar;%ExternalLibsDir%\javafx-sdk-11.0.2\lib\*;%JavaHome%\jaxb-ri\mod\*;%BinDir%\legacyJsonMessageCodec.jar"
set AddExports=--add-exports=java.desktop/java.awt.dnd.peer=javafx.swing --add-exports=java.desktop/sun.awt=javafx.swing --add-exports=java.desktop/sun.awt.dnd=javafx.swing --add-exports=java.desktop/sun.swing=javafx.swing --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.prefs/java.util.prefs=ALL-UNNAMED

echo Starting...
set Command=%JavaHome%\bin\javaw -classpath "%JavaClasspath%;%ClasspathExtension%" %AddExports% -Djava.library.path=%LibraryPathExtension% -Djava.io.tmpdir=%GIFT_TEMP% %Class% %*
start "Gateway Module" %Command%

REM following is for debugging purposes
REM echo %Command%
REM pause

exit