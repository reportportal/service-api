package com.epam.ta.reportportal.util.email.constant;

/**
 * @author Ivan Budaev
 */
public final class IssueRegexConstant {

	public static final String PRODUCT_BUG_ISSUE_REGEX = "^statistics\\$product_bug\\$((?!total$).)+.*$";
	public static final String NO_DEFECT_ISSUE_REGEX = "^statistics\\$no_defect\\$((?!total$).)+.*$";
	public static final String SYSTEM_ISSUE_REGEX = "^statistics\\$system_issue\\$((?!total$).)+.*$";
	public static final String AUTOMATION_BUG_ISSUE_REGEX = "^statistics\\$automation_bug\\$((?!total$).)+.*$";
	public static final String TO_INVESTIGATE_ISSUE_REGEX = "^statistics\\$to_investigate\\$((?!total$).)+.*$";

	private IssueRegexConstant() {
		//static only
	}

}
