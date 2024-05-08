package com.adjust.sdk.oaid;

import java.util.Map;

public interface OnOaidReadListener {
    void onOaidRead(Map<String, String> oaidParameters);
    void onFail(String message);
}
