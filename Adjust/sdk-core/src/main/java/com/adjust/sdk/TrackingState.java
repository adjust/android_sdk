package com.adjust.sdk;

public enum TrackingState {
    OPTED_OUT(1),
    THIRD_PARTY_SHARING_DISABLED(2);

    private int value;

    TrackingState(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
