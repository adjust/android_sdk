package com.adjust.sdk.oaid;

import java.util.Map;

public interface OnOaidReadListener {
    void onOaidRead(String oaid);
    void onFail(String message);
}
