package com.adeven.adjustio.test;

import static com.adeven.adjustio.Constants.LOGTAG;

import java.util.*;

import android.util.Log;

import com.adeven.adjustio.Logger;

public class MockLogger implements Logger {

	private StringBuffer logBuffer;
	private Map<Integer, List<String>> logMap;
	private List<String> logList;
	
	public MockLogger() {
		logBuffer = new StringBuffer();
		logList = new ArrayList<String>();
		logMap = new HashMap<Integer, List<String>>();
		logMap.put(LogLevel.ASSERT.getAndroidLogLevel(), new ArrayList<String>());
		logMap.put(LogLevel.DEBUG.getAndroidLogLevel(), new ArrayList<String>());
		logMap.put(LogLevel.ERROR.getAndroidLogLevel(), new ArrayList<String>());
		logMap.put(LogLevel.INFO.getAndroidLogLevel(), new ArrayList<String>());
		logMap.put(LogLevel.VERBOSE.getAndroidLogLevel(), new ArrayList<String>());
		logMap.put(LogLevel.WARN.getAndroidLogLevel(), new ArrayList<String>());
		//  logging test level == 1
		logMap.put(1, new ArrayList<String>());
	}
	
	@Override
	public String toString() {
		String logging = logBuffer.toString();
		//Log.v("TestLogger ", logging);
		return logging;
	}
	
	@Override
	public void setLogLevel(LogLevel logLevel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLogLevelString(String logLevelString) {
		// TODO Auto-generated method stub

	}
	
	private void logMessage(String message, Integer iLoglevel, String messagePrefix) {
		logBuffer.append(messagePrefix + message);
		Log.d(messagePrefix, message);
		logList.add(message);
		
		List<String> prefixedList = logMap.get(iLoglevel);
		prefixedList.add(message);
	}

	@Override
	public void verbose(String message) {
		logMessage(message, LogLevel.VERBOSE.getAndroidLogLevel(), "v ");
	}

	@Override
	public void debug(String message) {
		logMessage(message, LogLevel.DEBUG.getAndroidLogLevel(), "d ");
	}

	@Override
	public void info(String message) {
		logMessage(message, LogLevel.INFO.getAndroidLogLevel(), "i ");
	}

	@Override
	public void warn(String message) {
		logMessage(message, LogLevel.WARN.getAndroidLogLevel(), "w ");
	}

	@Override
	public void error(String message) {
		logMessage(message, LogLevel.ERROR.getAndroidLogLevel(), "e ");
	}

	@Override
	public void Assert(String message) {
		logMessage(message, LogLevel.ASSERT.getAndroidLogLevel(), "a ");
	}
	
	public void test(String message) {
		logMessage(message, 1, "t ");
	}
	
	public Boolean hasAnyError()
	{
		return !logMap.get(LogLevel.ERROR).isEmpty();
	}
	
	private Boolean listContainsMessage(List<String> list, String beginsWith) {
		String sList = Arrays.toString(list.toArray());
		for (String log : list) {
			if (log.startsWith(beginsWith)) {
				test(log + " found");
				return true;
			}
		}
		test(beginsWith + " is not in " + sList);
		return false;
	}

	public Boolean containsMessage(String beginsWith) {
		return listContainsMessage(logList, beginsWith);
	}
	
	public Boolean containsMessage(LogLevel level, String beginsWith) {
		List<String> messageList = logMap.get(level.getAndroidLogLevel());
		return listContainsMessage(messageList, beginsWith);	
	}
	
	public Boolean containsTestMessage(String beginsWith) {
		List<String> testMessages = logMap.get(1);
		return listContainsMessage(testMessages, beginsWith);
	}
}
