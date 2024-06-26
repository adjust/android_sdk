package com.adjust.test;

/**
 * Created by nonelse on 09.03.17.
 */
public interface Constants {
    int ONE_SECOND  = 1000;
    int ONE_MINUTE  = 60 * ONE_SECOND;
    String ENCODING = "UTF-8";

    String LOGTAG                 = "TestLibrary";
    String TEST_LIBRARY_CLASSNAME = "TestLibrary";
    String WAIT_FOR_CONTROL       = "control";
    String WAIT_FOR_SLEEP         = "sleep";
    String TEST_SESSION_ID_HEADER = "Test-Session-Id";

    // web socket values
    String SIGNAL_INFO                = "info";
    String SIGNAL_INIT_TEST_SESSION   = "init-test-session";
    String SIGNAL_END_WAIT            = "end-wait";
    String SIGNAL_CANCEL_CURRENT_TEST = "cancel-current-test";
}
