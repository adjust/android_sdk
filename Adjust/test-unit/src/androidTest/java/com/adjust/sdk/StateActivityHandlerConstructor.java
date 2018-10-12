package com.adjust.sdk;

import com.adjust.sdk.ActivityHandler;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.IRunActivityHandler;
import com.adjust.sdk.LogLevel;

import java.util.List;

/**
 * Created by pfms on 09/08/2016.
 */
public class StateActivityHandlerConstructor {
    AdjustConfig config;
    boolean startEnabled = true;
    boolean isToUpdatePackages = false;

    StateActivityHandlerConstructor(AdjustConfig config) {
        this.config = config;
    }
}
