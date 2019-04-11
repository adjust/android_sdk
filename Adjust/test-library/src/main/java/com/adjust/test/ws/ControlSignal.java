package com.adjust.test.ws;

import static com.adjust.test.Constants.*;

/**
 * com.adjust.test.ws
 * Created by 2beens on 08.02.19.
 */
public class ControlSignal {
    private String type;
    private String value;

    ControlSignal(SignalType type) {
        this.type = getSignalTypeString(type);
        this.value = "n/a";
    }

    ControlSignal(SignalType type, String value) {
        this.type = getSignalTypeString(type);
        this.value = value;
    }

    public SignalType getType() {
        return getSignalTypeByString(type);
    }

    public String getValue() {
        return this.value;
    }

    private String getSignalTypeString(SignalType signalType) {
        switch (signalType) {
            case INFO:                  return SIGNAL_INFO;
            case INIT_TEST_SESSION:     return SIGNAL_INIT_TEST_SESSION;
            case END_WAIT:              return SIGNAL_END_WAIT;
            case CANCEL_CURRENT_TEST:   return SIGNAL_CANCEL_CURRENT_TEST;
            default:                    return "unknown";
        }
    }

    private SignalType getSignalTypeByString(String signalType) {
        switch (signalType) {
            case SIGNAL_INFO:                   return SignalType.INFO;
            case SIGNAL_INIT_TEST_SESSION:      return SignalType.INIT_TEST_SESSION;
            case SIGNAL_END_WAIT:               return SignalType.END_WAIT;
            case SIGNAL_CANCEL_CURRENT_TEST:    return SignalType.CANCEL_CURRENT_TEST;
            default:                            return SignalType.UNKNOWN;
        }
    }
}
