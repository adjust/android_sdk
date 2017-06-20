package com.adjust.testlibrary;

import android.os.SystemClock;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.adjust.testlibrary.Constants.BASE_PATH_HEADER;
import static com.adjust.testlibrary.Constants.TEST_LIBRARY_CLASSNAME;
import static com.adjust.testlibrary.Constants.TEST_SCRIPT_HEADER;
import static com.adjust.testlibrary.Constants.TEST_SESSION_END_HEADER;
import static com.adjust.testlibrary.Constants.WAIT_FOR_CONTROL;
import static com.adjust.testlibrary.Constants.WAIT_FOR_SLEEP;
import static com.adjust.testlibrary.Utils.debug;
import static com.adjust.testlibrary.UtilsNetworking.sendPostI;


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
    String currentTest;
    String currentBasePath;
    Gson gson = new Gson();
    BlockingQueue<String> waitControlQueue;
    Map<String, String> infoToServer;

    String testNames = null;
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
    private void resetTest() {
        clearTest();

        waitControlQueue = new LinkedBlockingQueue<String>();
        controlChannel = new ControlChannel(this);
    }

    public void setTests(String testNames) {
        this.testNames = testNames;
    }

    public void doNotExitAfterEnd() {
        this.exitAfterEnd = false;
    }

    public void initTestSession(final String clientSdk) {
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

    public void sendInfoToServer() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                sendInfoToServerI();
            }
        });
    }


    void readHeaders(final UtilsNetworking.HttpResponse httpResponse) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                readHeadersI(httpResponse);
            }
        });
    }

    private void sendTestSessionI(String clientSdk) {
        UtilsNetworking.HttpResponse httpResponse = sendPostI("/init_session", clientSdk, testNames);
        if (httpResponse == null) {
            return;
        }

        readHeadersI(httpResponse);
    }

    private void sendInfoToServerI() {
        debug("sendInfoToServerI called");
        UtilsNetworking.HttpResponse httpResponse = sendPostI(Utils.appendBasePath(currentBasePath, "/test_info"), null, infoToServer);
        infoToServer = null;
        if (httpResponse == null) {
            return;
        }

        readHeadersI(httpResponse);
    }

    public void readHeadersI(UtilsNetworking.HttpResponse httpResponse) {
        if (httpResponse.headerFields.containsKey(TEST_SESSION_END_HEADER)) {
            teardown(false);
            debug("TestSessionEnd received");
            if (exitAfterEnd) {
                exit();
            }
            return;
        }

        if (httpResponse.headerFields.containsKey(BASE_PATH_HEADER)) {
            currentBasePath = httpResponse.headerFields.get(BASE_PATH_HEADER).get(0);
        }

        if (httpResponse.headerFields.containsKey(TEST_SCRIPT_HEADER)) {
            currentTest = httpResponse.headerFields.get(TEST_SCRIPT_HEADER).get(0);
            resetTest();

            List<TestCommand> testCommands = Arrays.asList(gson.fromJson(httpResponse.response, TestCommand[].class));
            try {
                execTestCommandsI(testCommands);
            } catch (InterruptedException e) {
                debug("InterruptedException thrown %s", e.getMessage());
            }
        }
    }

    private void execTestCommandsI(List<TestCommand> testCommands) throws InterruptedException {
        debug("testCommands: %s", testCommands);

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
                commandJsonListener.executeCommand(testCommand.className, testCommand.functionName, gson.toJson(testCommand.params));
            } else if (commandRawJsonListener != null) {
                commandRawJsonListener.executeCommand(gson.toJson(testCommand));
            }
            long timeAfter = System.nanoTime();
            long timeElapsedMillis = TimeUnit.NANOSECONDS.toMillis(timeAfter - timeBefore);
            debug("time after %s.%s: %d", testCommand.className, testCommand.functionName, timeAfter);
            debug("time elapsed %s.%s in milli seconds: %d", testCommand.className, testCommand.functionName, timeElapsedMillis);
        }
    }

    private void executeTestLibraryCommandI(TestCommand testCommand) throws InterruptedException {
        switch (testCommand.functionName) {
            case "end_test": endTestI(); break;
            case "wait": waitI(testCommand.params); break;
            case "exit": exit(); break;
        }
    }

    private void endTestI() {
        UtilsNetworking.HttpResponse httpResponse = sendPostI(Utils.appendBasePath(currentBasePath, "/end_test"));
        if (httpResponse == null) {
            if (exitAfterEnd) {
                exit();
            }
            return;
        }

        readHeadersI(httpResponse);
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
