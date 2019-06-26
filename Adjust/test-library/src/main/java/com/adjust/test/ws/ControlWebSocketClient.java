package com.adjust.test.ws;

import com.adjust.test.TestLibrary;
import com.google.gson.Gson;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import static com.adjust.test.Utils.debug;
import static com.adjust.test.Utils.error;

/**
 * com.adjust.test.ws
 * Created by 2beens on 07.02.19.
 */
public class ControlWebSocketClient extends WebSocketClient {
    private TestLibrary testLibrary;
    private Gson gson = new Gson();

    public ControlWebSocketClient(TestLibrary testLibrary, String serverUri) throws URISyntaxException {
        super(new URI(serverUri));
        this.testLibrary = testLibrary;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        debug("[WebSocket] connection opened with the server");
    }

    @Override
    public void onMessage(String message) {
        //debug(String.format("[WebSocket] onMessage, message [%s]", message));
        ControlSignal incomingSignal = this.parseControlSignal(message);
        this.handleIncomingSignal(incomingSignal);
    }

    private ControlSignal parseControlSignal(String message) {
        ControlSignal incomingSignal;
        try {
            incomingSignal = gson.fromJson(message, ControlSignal.class);
        } catch (Exception ex) {
            if (message == null) {
                message = "null";
            }
            error(String.format("[WebSocket] onMessage Error! Cannot parse message [%s]. Details: [%s]", message, ex.getMessage()));
            ex.printStackTrace();
            incomingSignal = new ControlSignal(SignalType.UNKNOWN);
        }
        return incomingSignal;
    }

    private void handleIncomingSignal(ControlSignal incomingSignal) {
        if (incomingSignal.getType() == SignalType.INFO) {
            debug("[WebSocket] info from the server: " + incomingSignal.getValue());
        } else if (incomingSignal.getType() == SignalType.END_WAIT) {
            debug("[WebSocket] end wait signal recevied, reason: " + incomingSignal.getValue());
            this.testLibrary.signalEndWait(incomingSignal.getValue());
        } else if (incomingSignal.getType() == SignalType.CANCEL_CURRENT_TEST) {
            debug("[WebSocket] cancel test recevied, reason: " + incomingSignal.getValue());
            testLibrary.cancelTestAndGetNext();
        } else {
            debug("[WebSocket] unknown signal received by the server. Value: " + incomingSignal.getValue());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        debug(String.format("[WebSocket] onClose, code [%d], reason [%s]", code, reason));
    }

    @Override
    public void onError(Exception ex) {
        debug(String.format("[WebSocket] onError [%s]", ex.getMessage()));
    }

    public void sendInitTestSessionSignal(String testSessionId) {
        ControlSignal initSignal = new ControlSignal(SignalType.INIT_TEST_SESSION, testSessionId);
        send(gson.toJson(initSignal));
    }
}
