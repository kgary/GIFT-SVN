package mil.arl.gift.gateway.interop.Steelartt;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.course.InteropInputs;
import generated.course.Nvpair;
import generated.course.UnityInteropInputs;
import generated.gateway.Unity;
import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.Siman;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.SimanTypeEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.ta.state.TrainingAppState;
import mil.arl.gift.gateway.GatewayModule;
import mil.arl.gift.gateway.interop.unity.UnityInterface;
import mil.arl.gift.gateway.interop.AbstractInteropInterface;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.embedded.message.codec.EmbeddedAppMessageEncoder;
import mil.arl.gift.net.socket.AsyncSocketHandler;


public class SteelArttSocket extends SteelArttInteropTemplate {

    /** The __logger for the class */
    private static final Logger __logger = LoggerFactory.getLogger(SteelArttSocket.class);

    /**
     * The socket handler that recieves competency message data from the unity app over socket.
     */
    private AsyncSocketHandler __dataSocketHandler;

    public SteelArttSocket(String displayName) {
        super(displayName);
    }
    

    @Override
    public void cleanup() {
        super.cleanup();
        _closeSocketHandler(__dataSocketHandler);
    }

    @Override
    protected void _establishConnection() throws IOException{
        super._establishConnection();

        if (__dataSocketHandler == null) {
            __dataSocketHandler = _createSocket(__dataSocketHandler,2);
        }

        _connectSocketHandler(__dataSocketHandler);
    }

    @Override
    protected void _createSocketOrConsumers(){
            super._createSocketOrConsumers();
            __dataSocketHandler = _createSocket(__dataSocketHandler,2);
    }

    protected void _disconnectSocketHandlerOrKafka(AsyncSocketHandler socketHandler){
        // This method will disconnect the "data" socket handler.
        _disconnectSocketHandler(__dataSocketHandler);
    }    

}
