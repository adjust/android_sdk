var TestLibrary = {
    startTestSession: function(clientSdk) {
        if (TestLibraryBridge) {
            var baseUrl = "https://10.0.2.2:8443";
            var gdprUrl = "https://10.0.2.2:8443";
            TestLibrary.commandExecutor = new CommandExecutor(baseUrl, gdprUrl);
            TestLibraryBridge.startTestSession(clientSdk);
        }
    },
    addTestDirectory: function(testDir) {
        console.log('TestLibrary addTestDirectory: ' + testDir);
        if (TestLibraryBridge) {
            TestLibraryBridge.addTestDirectory(testDir);
        }
    },
    addTest: function(testName) {
        console.log('TestLibrary, addTest: ' + testName);
        if (TestLibraryBridge) {
            TestLibraryBridge.addTest(testName);
        }
    },
    addInfoToSend: function(key, value) {
        if (TestLibraryBridge) {
            TestLibraryBridge.addInfoToSend(key, value);
        }
    },
    sendInfoToServer: function(basePath) {
        if (TestLibraryBridge) {
            TestLibraryBridge.sendInfoToServer(basePath);
        }
    },

    adjust_commandRawJsonListenerCallback: function (order, commandJson) {
        var className = commandJson.className;
        var methodName = commandJson.functionName;
        var jsonParameters = commandJson.params;
        TestLibrary.commandExecutor.scheduleCommand(className, methodName, jsonParameters, order);
    },

    setTestOptions: function (testOptions) {
        var testOptionsString = JSON.stringify(testOptions);
        if (TestLibraryBridge) {
            TestLibraryBridge.setTestOptions(testOptionsString);
        }
    },
};
