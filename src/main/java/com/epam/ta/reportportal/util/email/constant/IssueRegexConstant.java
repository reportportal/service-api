/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.util.email.constant;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class IssueRegexConstant {

	public static final String PRODUCT_BUG_ISSUE_REGEX = "^statistics\\$defects\\$product_bug\\$((?!total$).)+.*$";
	public static final String NO_DEFECT_ISSUE_REGEX = "^statistics\\$defects\\$no_defect\\$((?!total$).)+.*$";
	public static final String SYSTEM_ISSUE_REGEX = "^statistics\\$defects\\$system_issue\\$((?!total$).)+.*$";
	public static final String AUTOMATION_BUG_ISSUE_REGEX = "^statistics\\$defects\\$automation_bug\\$((?!total$).)+.*$";
	public static final String TO_INVESTIGATE_ISSUE_REGEX = "^statistics\\$defects\\$to_investigate\\$((?!total$).)+.*$";

	private IssueRegexConstant() {
		//static only
	}

}
