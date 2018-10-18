package com.epam.ta.reportportal.core.events.activity;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Pavel Bortnik
 */
public enum ActivityAction {

	CREATE_DASHBOARD("create_dashboard"),
	UPDATE_DASHBOARD("update_dashboard"),
	DELETE_DASHBOARD("delete_dashboard"),
	CREATE_WIDGET("create_widget"),
	UPDATE_WIDGET("update_widget"),
	DELETE_WIDGET("delete_widget"),
	CREATE_FILTER("create_filter"),
	UPDATE_FILTER("update_filter"),
	DELETE_FILTER("delete_filter"),
	ANALYZE_ITEM("analyze_item"),
	CREATE_DEFECT("create_defect"),
	UPDATE_DEFECT("update_defect"),
	DELETE_DEFECT("delete_defect"),
	CREATE_BTS("create_bts"),
	UPDATE_BTS("update_bts"),
	DELETE_BTS("delete_bts"),
	START_LAUNCH("start_launch"),
	FINISH_LAUNCH("finish_launch"),
	DELETE_LAUNCH("delete_launch"),
	UPDATE_PROJECT("update_project"),
	UPDATE_ANALYZER("update_analyzer"),
	POST_ISSUE("post_issue"),
	LINK_ISSUE("link_issue"),
	LINK_ISSUE_AA("link_issue_aa"),
	UNLINK_ISSUE("unlink_issue"),
	UPDATE_ITEM("update_item"),
	CREATE_USER("create_user"),
	DELETE_INDEX("delete_index"),
	GENERATE_INDEX("generate_index"),
	START_IMPORT("start_import"),
	FINISH_IMPORT("finish_import"),
	DELETE_EXTERNAL_SYSTEM("delete_external_system");

	private String value;

	ActivityAction(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static Optional<ActivityAction> fromString(String string) {
		return Optional.ofNullable(string).flatMap(str -> Arrays.stream(values()).filter(it -> it.value.equals(str)).findAny());
	}
}