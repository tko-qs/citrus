/*
 * Copyright 2006-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.validation.matcher.core;

import java.util.Arrays;

import com.consol.citrus.UnitTestSupport;
import com.consol.citrus.exceptions.ValidationException;
import org.testng.annotations.Test;

public class TrimAllWhitespacesValidationMatcherTest extends UnitTestSupport {

    private TrimAllWhitespacesValidationMatcher matcher = new TrimAllWhitespacesValidationMatcher();

    @Test
    public void testValidateSuccess() {
        matcher.validate("field", "This is a value", Arrays.asList("Thisisavalue"), context);
        matcher.validate("field", " This is a value ", Arrays.asList("Thisisavalue"), context);
        matcher.validate("field", "    This is a value    ", Arrays.asList("Thisisavalue"), context);
        matcher.validate("field", "    This is a value    ", Arrays.asList("This is a value    "), context);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testValidateError() {
        matcher.validate("field", " This is a value ", Arrays.asList("This is a wrong value"), context);
    }
}
