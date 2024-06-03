package com.amazon.parser;

import com.amazon.parser.wrappers.StackHandler;
import com.amazon.parser.data.Input;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.List;

@Ignore
public class HandlerPermissionGetterTests {

    @Mock StackHandler stackHandler;

    @Before
    public void setUp() {

    }

    @Test
    public void validInputTest() throws IOException {
        String inputPath = "/Users/alexark/inputs";
        HandlerPermissionGetter handlerPermissionGetter = new HandlerPermissionGetter();
        List<Input> inputs = handlerPermissionGetter.getInputs(inputPath);
        handlerPermissionGetter.processInputs(inputs,stackHandler,stackHandler,stackHandler);
    }
}
