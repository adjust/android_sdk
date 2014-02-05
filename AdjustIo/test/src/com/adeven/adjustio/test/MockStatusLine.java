package com.adeven.adjustio.test;

import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;

public class MockStatusLine implements StatusLine {

	public MockStatusLine() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getStatusCode() {
		return HttpStatus.SC_OK;
	}

	@Override
	public ProtocolVersion getProtocolVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getReasonPhrase() {
		// TODO Auto-generated method stub
		return null;
	}

}
