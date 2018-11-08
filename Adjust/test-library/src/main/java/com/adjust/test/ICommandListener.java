package com.adjust.test;

import java.util.List;
import java.util.Map;

/**
 * Created by nonelse on 09.03.17.
 */

public interface ICommandListener {
    void executeCommand(String className, String methodName, Map<String, List<String>> parameters);
}
