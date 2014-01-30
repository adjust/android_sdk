package com.adeven.adjustio.test;

import android.app.Activity;
import android.content.Context;
import android.test.mock.MockContext;

public class MockActivity extends Activity {

	@Override
	public Context getApplicationContext()
	{
		MockContext mockContext = new MockContext();
		
		return mockContext;
	}
}
