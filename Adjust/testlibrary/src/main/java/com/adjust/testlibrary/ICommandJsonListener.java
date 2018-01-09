package com.adjust.testlibrary;

/**
 * Created by nonelse on 10.03.17.
 */

public interface ICommandJsonListener {
    void executeCommand(String className, String methodName, String jsonParameters);
}
