package com.adjust.sdk.google.lvl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.content.ServiceConnection;
import android.os.Parcel;
import android.os.RemoteException;

import com.adjust.sdk.ILogger;

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
            IBinder binder = new Binder() {
                private static final String SERVICE_DESCRIPTOR = "com.android.vending.licensing.ILicensingService";
                private static final String LISTENER_DESCRIPTOR = "com.android.vending.licensing.ILicenseResultListener";

                @Override
                protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
                    if (code == IBinder.INTERFACE_TRANSACTION) {
                        if (reply != null) {
                            reply.writeString(SERVICE_DESCRIPTOR);
                        }
                        return true;
                    }

                    if (code == IBinder.FIRST_CALL_TRANSACTION) {
                        data.enforceInterface(SERVICE_DESCRIPTOR);
                        long nonce = data.readLong();
                        String packageName = data.readString();
                        IBinder listenerBinder = data.readStrongBinder();

                        Parcel response = Parcel.obtain();
                        try {
                            response.writeInterfaceToken(LISTENER_DESCRIPTOR);
                            response.writeInt(0);
                            response.writeString("signedData");
                            response.writeString("signature");
                            listenerBinder.transact(IBinder.FIRST_CALL_TRANSACTION, response, null, IBinder.FLAG_ONEWAY);
                        } finally {
                            response.recycle();
                        }
                        return true;
                    }
                    return super.onTransact(code, data, reply, flags);
                }
            };
            conn.onServiceConnected(new ComponentName("com.android.vending", "LicensingService"), binder);
            return true;
        }).when(context).bindService(any(Intent.class), any(), anyInt());

        licenseChecker.checkAccess();
        verify(mockCallback, timeout(1000)).onLicenseDataReceived(eq(0), eq("signedData"), eq("signature"));
    }
}
