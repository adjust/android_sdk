package com.adeven.adjustio.test;

import java.util.*;

import com.adeven.adjustio.Logger;

public class TestLogger implements Logger {

	private StringBuffer logBuffer;
	private Map<LogLevel, List<String>> logMap;
	private List<String> logList;
	
	public TestLogger() {
		logBuffer = new StringBuffer();
		logList = new ArrayList<String>();
		logMap = new HashMap<Logger.LogLevel, List<String>>();
		logMap.put(LogLevel.ASSERT, new ArrayList<String>());
		logMap.put(LogLevel.DEBUG, new ArrayList<String>());
		logMap.put(LogLevel.ERROR, new ArrayList<String>());
		logMap.put(LogLevel.INFO, new ArrayList<String>());
		logMap.put(LogLevel.VERBOSE, new ArrayList<String>());
		logMap.put(LogLevel.WARN, new ArrayList<String>());
	}
	
	@Override
	public String toString() {
		return logBuffer.toString();
	}
	
	@Override
	public void setLogLevel(LogLevel logLevel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLogLevelString(String logLevelString) {
		// TODO Auto-generated method stub

	}
	
	private void logMessage(String message, LogLevel loglevel, String messagePrefix) {
		logBuffer.append(messagePrefix + message);
		logList.add(message);
		
		List<String> prefixedList = logMap.get(loglevel);
		prefixedList.add(message);
	}

	@Override
	public void verbose(String message) {
		logMessage(message, LogLevel.VERBOSE, "v ");
	}

	@Override
	public void debug(String message) {
		logMessage(message, LogLevel.DEBUG, "d ");
	}

	@Override
	public void info(String message) {
		logMessage(message, LogLevel.INFO, "i ");
	}

	@Override
	public void warn(String message) {
		logMessage(message, LogLevel.WARN, "w ");
	}

	@Override
	public void error(String message) {
		logMessage(message, LogLevel.ERROR, "e ");
	}

	@Override
	public void Assert(String message) {
		logMessage(message, LogLevel.ASSERT, "a ");
	}
	
	public Boolean hasAnyError()
	{
		return !logMap.get(LogLevel.ERROR).isEmpty();
	}

	public Boolean containsMessage(String beginsWith) {
		for (String log : logList) {
			if (log.startsWith(beginsWith)) {
				return true;
			}
		}
		return false;
	}
	
	public Boolean containsMessage(LogLevel level, String beginsWith) {
		List<String> errorList = logMap.get(level);
		for (String error : errorList) {
			if (error.startsWith(beginsWith)) {
				return true;
			}
		}
		return false;
	}
}
