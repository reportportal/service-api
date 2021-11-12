package com.epam.ta.reportportal.demodata.service.generator;

import com.epam.ta.reportportal.demodata.model.DemoItemMetadata;
import com.epam.ta.reportportal.demodata.model.RootMetaData;
import com.epam.ta.reportportal.demodata.model.Test;
import com.epam.ta.reportportal.demodata.service.DemoDataTestItemService;
import com.epam.ta.reportportal.demodata.service.DemoLogsService;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.entity.enums.TestItemTypeEnum.STEP;

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

	@Override
	protected String startTest(String suiteId, RootMetaData rootMetaData, Test test, StatusEnum testStatus) {
		final String testId = super.startTest(suiteId, rootMetaData, test, testStatus);
		final DemoItemMetadata stepParentMetadata = getMetadata(test.getName(), STEP, testStatus, testId);
		return demoDataTestItemService.startTestItem(stepParentMetadata, rootMetaData);
	}

}
