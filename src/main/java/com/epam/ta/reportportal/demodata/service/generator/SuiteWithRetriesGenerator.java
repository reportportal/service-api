package com.epam.ta.reportportal.demodata.service.generator;

import com.epam.ta.reportportal.demodata.model.DemoItemMetadata;
import com.epam.ta.reportportal.demodata.model.RootMetaData;
import com.epam.ta.reportportal.demodata.service.DemoDataTestItemService;
import com.epam.ta.reportportal.demodata.service.DemoLogsService;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.IntStream;

import static com.epam.ta.reportportal.entity.enums.StatusEnum.FAILED;

@Service
public class SuiteWithRetriesGenerator extends DefaultSuiteGenerator {

	private static final int RETRIES_COUNT = 3;

	@Autowired
	public SuiteWithRetriesGenerator(DemoDataTestItemService demoDataTestItemService, DemoLogsService demoLogsService) {
		super(demoDataTestItemService, demoLogsService);
	}

	@Override
	protected void createStep(int logsCount, RootMetaData rootMetaData, StatusEnum stepStatus, DemoItemMetadata stepMetaData) {
		super.createStep(logsCount, rootMetaData, stepStatus, stepMetaData);
		if (stepStatus != StatusEnum.PASSED) {
			generateRetries(stepMetaData, rootMetaData);
		}
	}

	private void generateRetries(final DemoItemMetadata metadata, RootMetaData rootMetaData) {
		IntStream.range(0, RETRIES_COUNT).forEach(i -> {
			final DemoItemMetadata retryMetaData = getMetadata(metadata.getName(), metadata.getType(), metadata.getParentId()).withIssue(
					metadata.getIssue()).withRetry(true);
			super.createStep(STEP_LOGS_COUNT, rootMetaData, FAILED, retryMetaData);
		});
	}
}
