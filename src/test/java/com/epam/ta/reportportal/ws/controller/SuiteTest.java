/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.ws.controller.suite.ActivityControllerTest;
import com.epam.ta.reportportal.ws.controller.suite.LaunchControllerTest;
import com.epam.ta.reportportal.ws.controller.suite.ProjectControllerTest;
import com.epam.ta.reportportal.ws.controller.suite.TestItemControllerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(value = { ActivityControllerTest.class, LaunchControllerTest.class, ProjectControllerTest.class,
		TestItemControllerTest.class })
public class SuiteTest {

}
