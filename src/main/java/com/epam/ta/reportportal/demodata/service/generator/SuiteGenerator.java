package com.epam.ta.reportportal.demodata.service.generator;

import com.epam.ta.reportportal.demodata.model.RootMetaData;
import com.epam.ta.reportportal.demodata.model.Suite;

public interface SuiteGenerator {

	void generateSuites(Suite suite, RootMetaData rootMetaData);

}
