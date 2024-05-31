package com.amazon.parser;

import com.amazon.parser.wrappers.StackHandler;
import com.amazon.parser.data.Input;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class HandlerPermissionGetterTests {
    @Ignore
    @Test
    public void validInputTest() throws IOException {
        String inputPath = "/Users/alexark/inputs";
        HandlerPermissionGetter handlerPermissionGetter = new HandlerPermissionGetter();
        List<Input> inputs = handlerPermissionGetter.getInputs(inputPath);
        StackHandler stackHandler = new StackHandler("us-east-1", null);
        handlerPermissionGetter.processInputs(inputs,stackHandler,stackHandler,stackHandler);
    }
}
