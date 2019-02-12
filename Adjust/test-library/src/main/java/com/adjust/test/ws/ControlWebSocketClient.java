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
    private String testSessionId;
    private String webSocketClientId = UUID.randomUUID().toString();

    public ControlWebSocketClient(TestLibrary testLibrary, String serverUri) throws URISyntaxException {
        super(new URI(serverUri));
        this.testLibrary = testLibrary;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        debug("WS: connection opened with the server");
        ControlSignal initSignal = new ControlSignal(SignalType.INIT, this.webSocketClientId);
        send(gson.toJson(initSignal));
    }

    @Override
    public void onMessage(String message) {
        debug(String.format("WS: onMessage, message [%s]", message));
        try {
            ControlSignal incomingSignal = gson.fromJson(message, ControlSignal.class);
            this.handleIncomingSignal(incomingSignal);
        } catch (Exception ex) {
            error(String.format("WS: onMessage Error! [%s]", ex.getMessage()));
            ex.printStackTrace();
        }
    }

    private void handleIncomingSignal(ControlSignal incomingSignal) {
        if (incomingSignal.getType() == SignalType.INFO) {
            debug("WS: info from the server: " + incomingSignal.getValue());
        } else if (incomingSignal.getType() == SignalType.END_WAIT) {
            debug("WS: end wait signal recevied, reason: " + incomingSignal.getValue());
            this.testLibrary.signalEndWait(incomingSignal.getValue());
        } else if (incomingSignal.getType() == SignalType.END_CURRENT_TEST) {
            debug("WS: cancel test recevied, reason: " + incomingSignal.getValue());
            testLibrary.resetTestLibrary();
            // probably cannot be called from this thread
            // testLibrary.endTestReadNext(httpResponse);
            // TODO: ask for next test
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        debug(String.format("WS: onClose, reason [%s]", reason));
    }

    @Override
    public void onError(Exception ex) {
        debug(String.format("WS: onError []", ex.getMessage()));
    }

    public void sendInitTestSessionSignal(String testSessionId) {
        this.testSessionId = testSessionId;
        ControlSignal initSignal = new ControlSignal(SignalType.INIT_TEST_SESSION, this.testSessionId);
        send(gson.toJson(initSignal));
    }

    public void signalLastCommandExecuted(String testSessionId) {
        ControlSignal lastCommandExecutedSignal = new ControlSignal(SignalType.LAST_COMMAND_EXECUTED, testSessionId, "n/a");
        send(gson.toJson(lastCommandExecutedSignal));
    }
}
