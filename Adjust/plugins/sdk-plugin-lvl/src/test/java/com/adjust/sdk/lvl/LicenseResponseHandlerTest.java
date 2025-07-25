package com.adjust.sdk.lvl;

import com.adjust.sdk.ILogger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class LicenseResponseHandlerTest {

    @Mock LicenseRawCallback mockCallback;
    @Mock ILogger mockLogger;

    private LicenseResponseHandler handler;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        handler = new LicenseResponseHandler(mockCallback, mockLogger, 3);
    }

    @Test
    public void testLicensed_response_callsOnSuccess() {
        handler.handleResponse(0, "signedData", "signature", 0);

        verify(mockCallback).onLicenseDataReceived(0, "signedData", "signature");
        verifyNoMoreInteractions(mockCallback);
    }

    @Test
    public void testLicensed_missingSignature_callsError() {
        handler.handleResponse(0, null, null, 0);

        verify(mockLogger).error(contains("missing signed data"));
        verify(mockCallback).onError(0);
    }

    @Test
    public void testNotLicensed_callsErrorDirectly() {
        handler.handleResponse(1, null, null, 0);

        verify(mockCallback).onError(1);
        verify(mockLogger).warn(contains("[LicenseVerification] License check failed: "));
    }

    @Test
    public void testTemporaryError_retriesBelowMax() {
        boolean shouldRetry = handler.handleResponse(4, null, null, 2);

        verify(mockLogger).warn(contains("Retry attempt [3]"), eq(4));
        verify(mockCallback, never()).onError(anyInt());
        assert(shouldRetry);
    }

    @Test
    public void testTemporaryError_retriesExceeded_callsError() {
        boolean shouldRetry = handler.handleResponse(5, null, null, 3);

        verify(mockLogger).error(contains("after max retries"), eq(5));
        verify(mockCallback).onError(5);
        assert(!shouldRetry);
    }

    @Test
    public void testUnexpectedCode_callsError() {
        handler.handleResponse(999, "X", "Y", 0);

        verify(mockLogger).error(contains("Unexpected response code"), eq(999));
        verify(mockCallback).onError(999);
    }
}
