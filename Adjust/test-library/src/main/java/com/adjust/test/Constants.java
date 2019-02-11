package com.adjust.test;

/**
 * Created by nonelse on 09.03.17.
 */

public interface Constants {
    int ONE_SECOND = 1000;
    int ONE_MINUTE = 60 * ONE_SECOND;

    String ENCODING = "UTF-8";

    String LOGTAG = "TestLibrary";
    String TEST_SCRIPT_HEADER = "TestScript";
    String BASE_PATH_HEADER = "BasePath";
    String TEST_SESSION_END_HEADER = "TestSessionEnd";
    String TEST_CANCELTEST_HEADER = "CancelTest";
    String TEST_ENDWAIT_HEADER = "EndWait";
    String TEST_LIBRARY_CLASSNAME = "TestLibrary";
    String WAIT_FOR_CONTROL = "control";
    String WAIT_FOR_SLEEP = "sleep";

    // web socket values
    final String SIGNAL_INFO =                  "info";
    final String SIGNAL_INIT =                  "init";
    final String SIGNAL_INIT_TEST_SESSION =     "init-test-session";
    final String SIGNAL_END_WAIT =              "end-wait";
    final String SIGNAL_LAST_COMMAND_EXECUTED = "last-command-executed";
    final String SIGNAL_END_CURRENT_TEST =      "end-current-test";
}
