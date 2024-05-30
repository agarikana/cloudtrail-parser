package org.example;

import org.example.data.Input;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class HandlerPermissionGetterTests {

    @Test
    public void validInputTest() throws IOException {
        String inputPath = "/Users/alexark/inputs";

        List<Input> inputs = HandlerPermissionGetter.getInputs(inputPath);

    }
}
