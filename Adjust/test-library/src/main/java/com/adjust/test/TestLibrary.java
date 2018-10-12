package com.adjust.test;

import android.os.SystemClock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.adjust.test.Constants.TEST_LIBRARY_CLASSNAME;
import static com.adjust.test.Constants.WAIT_FOR_CONTROL;
import static com.adjust.test.Constants.WAIT_FOR_SLEEP;
import static com.adjust.test.Utils.debug;
import static com.adjust.test.UtilsNetworking.sendPostI;


/**
 * Created by nonelse on 09.03.17.
 */

public class TestLibrary {
    static String baseUrl;
    ExecutorService executor;
    IOnExitListener onExitListener;
    ICommandListener commandListener;
    ICommandJsonListener commandJsonListener;
    ICommandRawJsonListener commandRawJsonListener;
    ControlChannel controlChannel;
    String currentTestName;
    String currentBasePath;
    Gson gson = new Gson();
    BlockingQueue<String> waitControlQueue;
    Map<String, String> infoToServer;

    StringBuilder testNames = new StringBuilder();
    boolean exitAfterEnd = true;

    public TestLibrary(String baseUrl, ICommandRawJsonListener commandRawJsonListener) {
        this(baseUrl);
        this.commandRawJsonListener = commandRawJsonListener;
    }

    public TestLibrary(String baseUrl, ICommandJsonListener commandJsonListener) {
        this(baseUrl);
        this.commandJsonListener = commandJsonListener;
    }

    public TestLibrary(String baseUrl, ICommandListener commandListener) {
        this(baseUrl);
        this.commandListener = commandListener;
    }

    private TestLibrary(String baseUrl) {
        this.baseUrl = baseUrl;
        debug("base url: %s", baseUrl);
    }

    // resets test library to initial state
    void resetTestLibrary() {
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

        clearTest();
    }

    // clear for each test
    private void clearTest() {
        if (waitControlQueue != null) {
            waitControlQueue.clear();
        }
        waitControlQueue = null;
        if (controlChannel != null) {
            controlChannel.teardown();
        }
        controlChannel = null;
        infoToServer = null;
    }

    // reset for each test
    private void resetForNextTest() {
        clearTest();

        waitControlQueue = new LinkedBlockingQueue<String>();
        controlChannel = new ControlChannel(this);
    }

    public void addTestDirectory(String testDir) {
        this.testNames.append(testDir);

        if(!testDir.endsWith("/") || !testDir.endsWith("/;")) {
            this.testNames.append("/");
        }

        if(!testDir.endsWith(";")) {
            this.testNames.append(";");
        }
    }

    public void addTest(String testName) {
        this.testNames.append(testName);

        if(!testName.endsWith(";")) {
            this.testNames.append(";");
        }
    }

    public void doNotExitAfterEnd() {
        this.exitAfterEnd = false;
    }

    public void startTestSession(final String clientSdk) {
        resetTestLibrary();

        executor.submit(new Runnable() {
            @Override
            public void run() {
                sendTestSessionI(clientSdk);
            }
        });
    }

    public void setOnExitListener(IOnExitListener onExitListener) {
        this.onExitListener = onExitListener;
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

    void readResponse(final UtilsNetworking.HttpResponse httpResponse) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                readResponseI(httpResponse);
            }
        });
    }

    private void sendTestSessionI(String clientSdk) {
        UtilsNetworking.HttpResponse httpResponse = sendPostI("/init_session", clientSdk, testNames.toString());
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

    private void execTestCommandsI(List<TestCommand> testCommands) throws InterruptedException {
        debug("testCommands: %s", testCommands);
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        for (TestCommand testCommand : testCommands) {
            if (Thread.interrupted()) {
                debug("Thread interrupted");
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

            if (TEST_LIBRARY_CLASSNAME.equals(testCommand.className)) {
                executeTestLibraryCommandI(testCommand);
                long timeAfter = System.nanoTime();
                long timeElapsedMillis = TimeUnit.NANOSECONDS.toMillis(timeAfter - timeBefore);
                debug("time after %s %s: %d", testCommand.className, testCommand.functionName, timeAfter);
                debug("time elapsed %s %s in milli seconds: %d", testCommand.className, testCommand.functionName, timeElapsedMillis);

                continue;
            }
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
            case "endTestReadNext": endTestReadNext(); break;
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
        resetForNextTest();
    }

    private void endTestReadNext() {
        // send end test request
        UtilsNetworking.HttpResponse httpResponse = sendPostI(Utils.appendBasePath(currentBasePath, "/end_test_read_next"));
        // and process the next in the response
        readResponseI(httpResponse);
    }

    private void endTestSessionI() {
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
