package com.epam.ta.reportportal.demodata.service.generator;

import com.epam.ta.reportportal.demodata.model.DemoItemMetadata;
import com.epam.ta.reportportal.demodata.model.RootMetaData;
import com.epam.ta.reportportal.demodata.service.DemoDataTestItemService;
import com.epam.ta.reportportal.demodata.service.DemoLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SuiteWithNestedStepsGenerator extends DefaultSuiteGenerator {

	@Autowired
	public SuiteWithNestedStepsGenerator(DemoDataTestItemService demoDataTestItemService, DemoLogsService demoLogsService) {
		super(demoDataTestItemService, demoLogsService);
	}

	@Override
	protected void createStep(DemoItemMetadata stepMetaData, RootMetaData rootMetaData) {
		super.createStep(stepMetaData.withNested(true), rootMetaData);
	}
}
