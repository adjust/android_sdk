package com.adjust.sdk.google.lvl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.content.ServiceConnection;
import android.os.RemoteException;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.google.lvl.LicenseChecker;
import com.adjust.sdk.google.lvl.LicenseRawCallback;
import com.android.vending.licensing.ILicenseResultListener;
import com.android.vending.licensing.ILicensingService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33) // or 34, must be <= 34
public class LicenseCheckerIntegrationTest {

    private Context context;
    private LicenseRawCallback mockCallback;
    private ILogger mockLogger;
    private LicenseChecker licenseChecker;

    @Before
    public void setUp() {
        context = mock(Context.class);
        mockCallback = mock(LicenseRawCallback.class);
        mockLogger = mock(ILogger.class);
        licenseChecker = new LicenseChecker(context, mockCallback, mockLogger, System.currentTimeMillis());
    }

    @Test
    public void testSuccessfulLicenseCheck_shouldCallCallback() {
        doAnswer(invocation -> {
            ServiceConnection conn = invocation.getArgument(1);
            IBinder binder = new ILicensingService.Stub() {
                @Override
                public void checkLicense(long nonce, String packageName, ILicenseResultListener listener) throws RemoteException {
                    listener.verifyLicense(0, "signedData", "signature");
                }
            };
            conn.onServiceConnected(new ComponentName("com.android.vending", "LicensingService"), binder);
            return true;
        }).when(context).bindService(any(Intent.class), any(), anyInt());

        licenseChecker.checkAccess();
        verify(mockCallback, timeout(1000)).onLicenseDataReceived(eq(0), eq("signedData"), eq("signature"));
    }
}
