/*
 * Copyright 2022 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.widget.content.updater.validator;

import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.reportportal.rules.exception.ReportPortalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.LAUNCH_NAME_FIELD;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Andrei Piankouski
 */
public class TopTestCasesContentValidatorTest {
    private WidgetValidatorStrategy topTestCasesContentValidator;

    @BeforeEach
    public void setUp() {
        topTestCasesContentValidator = new TopTestCasesContentValidator();
    }

    @Test
    public void testValidateWithNullContentField() {
        Exception exception = assertThrows(ReportPortalException.class,
                () -> topTestCasesContentValidator.validate(null, new HashMap<>(), new WidgetOptions(), 5)
        );

        String expectedMessage = "Error in handled Request. Please, check specified parameters: 'Content fields should not be empty'";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testValidateWithLimitExceed() {
        List<String> contentFields = new ArrayList<>();
        contentFields.add("contentField");

        Exception exception = assertThrows(ReportPortalException.class,
                () -> topTestCasesContentValidator.validate(contentFields, new HashMap<>(), null, 101)
        );

        String expectedMessage = "Unable to load widget content. Widget properties contain errors: Items count should have value from 2 to 100.";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

}
