package com.epam.ta.reportportal.store.database.dao;

import java.util.List;

/**
 * @author Yauheni_Martynau
 */
public interface LaunchTagRepositoryCustom {

	List<String> getTags(String projectName, String value);
}
