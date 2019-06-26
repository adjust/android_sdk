package com.adjust.test;

import android.os.SystemClock;

import com.adjust.test.ws.ControlWebSocketClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.adjust.test.Constants.ONE_SECOND;
import static com.adjust.test.Constants.TEST_LIBRARY_CLASSNAME;
import static com.adjust.test.Constants.TEST_SESSION_ID_HEADER;
import static com.adjust.test.Constants.WAIT_FOR_CONTROL;
import static com.adjust.test.Constants.WAIT_FOR_SLEEP;
import static com.adjust.test.Utils.debug;
import static com.adjust.test.Utils.error;
import static com.adjust.test.UtilsNetworking.sendPostI;


/**
 * Created by nonelse on 09.03.17.
 */

public class TestLibrary {
    static String baseUrl;
    static String controlUrl;
    private Gson gson = new Gson();
    private BlockingQueue<String> waitControlQueue;
    private ControlWebSocketClient controlClient;
    private ExecutorService executor;
    private IOnExitListener onExitListener;
    private ICommandListener commandListener;
    private ICommandJsonListener commandJsonListener;
    private ICommandRawJsonListener commandRawJsonListener;
    private String currentTestName;
    private String currentBasePath;
    private Map<String, String> infoToServer;
    private String testSessionId;
    private StringBuilder currentTestNames = new StringBuilder();
    private boolean exitAfterEnd = true;

    public TestLibrary(String baseUrl, String controlUrl, ICommandRawJsonListener commandRawJsonListener) {
        this(baseUrl, controlUrl);
        this.commandRawJsonListener = commandRawJsonListener;
    }

    public TestLibrary(String baseUrl, String controlUrl, ICommandJsonListener commandJsonListener) {
        this(baseUrl, controlUrl);
        this.commandJsonListener = commandJsonListener;
    }

    public TestLibrary(String baseUrl, String controlUrl, ICommandListener commandListener) {
        this(baseUrl, controlUrl);
        this.commandListener = commandListener;
    }

    private TestLibrary(String baseUrl, String controlUrl) {
        this.baseUrl = baseUrl;
        this.controlUrl = controlUrl;
        debug("> base url: \t%s", baseUrl);
        debug("> control url: \t%s", controlUrl);
        this.initializeWebSocket(controlUrl);
    }

    private void initializeWebSocket(String controlUrl) {
        try {
            this.controlClient = new ControlWebSocketClient(this, controlUrl);
            this.controlClient.connect();
            debug(" ---> control web socket client, connection state: " + this.controlClient.getReadyState());
        } catch (URISyntaxException e) {
            debug(String.format("Error, cannot create/connect with server web socket: [%s]", e.getMessage()));
            e.printStackTrace();
        }
    }

    // resets test library to initial state
    private void resetTestLibrary() {
        teardown(true);
        executor = Executors.newCachedThreadPool();
        waitControlQueue = new LinkedBlockingQueue<String>();
    }

    // clears test library
    private void teardown(boolean shutdownNow) {
        if (executor != null) {
            if (shutdownNow) {
                debug("test library executor shutdownNow");
                executor.shutdownNow();
            } else {
                debug("test library executor shutdown");
                executor.shutdown();
            }
        }
        executor = null;
        infoToServer = null;
        if (waitControlQueue != null) {
            waitControlQueue.clear();
        }
        waitControlQueue = null;
    }

    public void startTestSession(final String clientSdk) {
        resetTestLibrary();

        // reconnect web socket client if disconnected
        if (!this.controlClient.isOpen()) {
            debug("reconnecting web socket client ...");
            this.initializeWebSocket(controlUrl);
            // wait for WS to reconnect
            SystemClock.sleep(ONE_SECOND);
        }

        executor.submit(new Runnable() {
            @Override
            public void run() {
                startTestSessionI(clientSdk);
            }
        });
    }

    public void setOnExitListener(IOnExitListener onExitListener) {
        this.onExitListener = onExitListener;
    }

    public void signalEndWait(String reason) {
        this.waitControlQueue.offer(reason);
    }

    public void cancelTestAndGetNext() {
        resetTestLibrary();
        executor.submit(new Runnable() {
            @Override
            public void run() {
            UtilsNetworking.HttpResponse httpResponse = sendPostI(Utils.appendBasePath(currentBasePath, "/end_test_read_next"));
            readResponseI(httpResponse);
            }
        });
    }

    public void addInfoToSend(String key, String value) {
        if (infoToServer == null) {
            infoToServer = new HashMap<String, String>();
        }
        infoToServer.put(key, value);
    }

    public void sendInfoToServer(final String basePath) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                sendInfoToServerI(basePath);
            }
        });
    }

    private void startTestSessionI(String clientSdk) {
        UtilsNetworking.HttpResponse httpResponse = sendPostI("/init_session", clientSdk, currentTestNames.toString());
        this.testSessionId = httpResponse.headerFields.get(TEST_SESSION_ID_HEADER).get(0);
        // set test session ID on the web socket object in Test Server, so it can be uniquely identified
        this.controlClient.sendInitTestSessionSignal(this.testSessionId);
        debug("starting new test session with ID: " + this.testSessionId);
        readResponseI(httpResponse);
    }

    private void sendInfoToServerI(String basePath) {
        UtilsNetworking.HttpResponse httpResponse = sendPostI(Utils.appendBasePath(basePath, "/test_info"), null, infoToServer);
        infoToServer = null;
        readResponseI(httpResponse);
    }

    public void readResponseI(UtilsNetworking.HttpResponse httpResponse) {
        if (httpResponse == null) {
            debug("httpResponse is null");
            return;
        }

        List<TestCommand> testCommands = Arrays.asList(gson.fromJson(httpResponse.response, TestCommand[].class));
        try {
            execTestCommandsI(testCommands);
        } catch (InterruptedException e) {
            debug("InterruptedException thrown %s", e.getMessage());
        }
    }

    public void addTestDirectory(String testDir) {
        this.currentTestNames.append(testDir);

        if(!testDir.endsWith("/") || !testDir.endsWith("/;")) {
            this.currentTestNames.append("/");
        }

        if(!testDir.endsWith(";")) {
            this.currentTestNames.append(";");
        }
    }

    public void addTest(String testName) {
        this.currentTestNames.append(testName);

        if(!testName.endsWith(";")) {
            this.currentTestNames.append(";");
        }
    }

    public void doNotExitAfterEnd() {
        this.exitAfterEnd = false;
    }

    private void execTestCommandsI(List<TestCommand> testCommands) throws InterruptedException {
        debug("testCommands: %s", testCommands);
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        for (TestCommand testCommand : testCommands) {
            if (Thread.interrupted()) {
                error("Thread interrupted");
                return;
            }
            debug("ClassName: %s", testCommand.className);
            debug("FunctionName: %s", testCommand.functionName);
            debug("Params:");
            if (testCommand.params != null && testCommand.params.size() > 0) {
                for (Map.Entry<String, List<String>> entry : testCommand.params.entrySet()) {
                    debug("\t%s: %s", entry.getKey(), entry.getValue());
                }
            }
            long timeBefore = System.nanoTime();
            debug("time before %s %s: %d", testCommand.className, testCommand.functionName, timeBefore);

            // execute TestLibrary command
            if (TEST_LIBRARY_CLASSNAME.equals(testCommand.className)) {
                executeTestLibraryCommandI(testCommand);
                long timeAfter = System.nanoTime();
                long timeElapsedMillis = TimeUnit.NANOSECONDS.toMillis(timeAfter - timeBefore);
                debug("time after %s %s: %d", testCommand.className, testCommand.functionName, timeAfter);
                debug("time elapsed %s %s in milli seconds: %d", testCommand.className, testCommand.functionName, timeElapsedMillis);
                continue;
            }

            // execute Adjust command
            if (commandListener != null) {
                commandListener.executeCommand(testCommand.className, testCommand.functionName, testCommand.params);
            } else if (commandJsonListener != null) {
                String toJsonParams = gson.toJson(testCommand.params);
                debug("commandJsonListener test command params toJson: %s", toJsonParams);
                commandJsonListener.executeCommand(testCommand.className, testCommand.functionName, toJsonParams);
            } else if (commandRawJsonListener != null) {
                String toJsonCommand = gson.toJson(testCommand);
                debug("commandRawJsonListener test command toJson: %s", toJsonCommand);
                commandRawJsonListener.executeCommand(toJsonCommand);
            }

            long timeAfter = System.nanoTime();
            long timeElapsedMillis = TimeUnit.NANOSECONDS.toMillis(timeAfter - timeBefore);
            debug("time after %s.%s: %d", testCommand.className, testCommand.functionName, timeAfter);
            debug("time elapsed %s.%s in milli seconds: %d", testCommand.className, testCommand.functionName, timeElapsedMillis);
        }
    }

    private void executeTestLibraryCommandI(TestCommand testCommand) throws InterruptedException {
        switch (testCommand.functionName) {
            case "resetTest": resetTestI(testCommand.params); break;
            case "endTestReadNext": endTestReadNextI(); break;
            case "endTestSession": endTestSessionI(); break;
            case "wait": waitI(testCommand.params); break;
            case "exit": exit(); break;
        }
    }

    private void resetTestI(Map<String, List<String>> params) {
        if (params.containsKey("basePath")) {
            currentBasePath = params.get("basePath").get(0);
            debug("current base path %s", currentBasePath);
        }
        if (params.containsKey("testName")) {
            currentTestName = params.get("testName").get(0);
            debug("current test name %s", currentTestName);
        }

        if (waitControlQueue != null) {
            waitControlQueue.clear();
        }
        infoToServer = null;
        waitControlQueue = new LinkedBlockingQueue<String>();
    }

    private void endTestReadNextI() {
        UtilsNetworking.HttpResponse httpResponse = sendPostI(Utils.appendBasePath(currentBasePath, "/end_test_read_next"));
        readResponseI(httpResponse);
    }

    private void endTestSessionI() {
        debug(" ---> test session ended!");
        teardown(false);
        if (exitAfterEnd) {
            exit();
        }
    }

    private void waitI(Map<String, List<String>> params) throws InterruptedException {
        if (params.containsKey(WAIT_FOR_CONTROL)) {
            String waitExpectedReason = params.get(WAIT_FOR_CONTROL).get(0);
            debug("wait for %s", waitExpectedReason);
            String endReason = waitControlQueue.take();
            debug("wait ended due to %s", endReason);
        }
        if (params.containsKey(WAIT_FOR_SLEEP)) {
            long millisToSleep = Long.parseLong(params.get(WAIT_FOR_SLEEP).get(0));
            debug("sleep for %s", millisToSleep);
            SystemClock.sleep(millisToSleep);
            debug("sleep ended");
        }
    }

    private void exit() {
        if(onExitListener != null){
            onExitListener.onExit();
        }
        System.exit(0);
    }
}
