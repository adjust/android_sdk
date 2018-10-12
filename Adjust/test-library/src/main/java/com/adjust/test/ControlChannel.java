package com.adjust.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.adjust.test.Constants.TEST_CANCELTEST_HEADER;
import static com.adjust.test.Constants.TEST_ENDWAIT_HEADER;
import static com.adjust.test.Utils.debug;
import static com.adjust.test.UtilsNetworking.sendPostI;

/**
 * Created by nonelse on 21.03.17.
 */

public class ControlChannel {
    private static final String CONTROL_START_PATH = "/control_start";
    private static final String CONTROL_CONTINUE_PATH = "/control_continue";

    ExecutorService controlChannelExecutor = Executors.newCachedThreadPool();
    TestLibrary testLibrary;
    private boolean closed = false;

    public ControlChannel(TestLibrary testLibrary) {
        this.testLibrary = testLibrary;
        sendControlRequest(CONTROL_START_PATH);
    }

    public void teardown() {
        if (controlChannelExecutor != null) {
            debug("controlChannelExecutor shutdown");
            controlChannelExecutor.shutdown();
        }

        closed = true;
        controlChannelExecutor = null;
    }

    private void sendControlRequest(final String controlPath) {
        controlChannelExecutor.submit(new Runnable() {
            @Override
            public void run() {
                long timeBefore = System.nanoTime();
                debug("time before wait: %d", timeBefore);

                UtilsNetworking.HttpResponse httpResponse = sendPostI(
                        Utils.appendBasePath(testLibrary.currentBasePath, controlPath));

                long timeAfter = System.nanoTime();
                long timeElapsedMillis = TimeUnit.NANOSECONDS.toMillis(timeAfter - timeBefore);
                debug("time after wait: %d", timeAfter);
                debug("time elapsed waiting in milli seconds: %d", timeElapsedMillis);

                readControlHeaders(httpResponse);
            }
        });
    }

    void readControlHeaders(UtilsNetworking.HttpResponse httpResponse) {
        if (closed) {
            debug("control channel already closed");
            return;
        }
        if (httpResponse.headerFields.containsKey(TEST_CANCELTEST_HEADER)) {
            debug("Test canceled due to %s", httpResponse.headerFields.get(TEST_CANCELTEST_HEADER).get(0));
            testLibrary.resetTestLibrary();
            testLibrary.readResponse(httpResponse);
        }
        if (httpResponse.headerFields.containsKey(TEST_ENDWAIT_HEADER)) {
            String waitEndReason = httpResponse.headerFields.get(TEST_ENDWAIT_HEADER).get(0);
            sendControlRequest(CONTROL_CONTINUE_PATH);
            endWait(waitEndReason);
        }
    }

    void endWait(String waitEndReason) {
        debug("End wait from control channel due to %s", waitEndReason);
        testLibrary.waitControlQueue.offer(waitEndReason);
        debug("Wait ended from control channel due to %s", waitEndReason);
    }
}
