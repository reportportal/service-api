package com.epam.ta.reportportal.demodata.service.generator;

import com.epam.ta.reportportal.demodata.model.DemoItemMetadata;
import com.epam.ta.reportportal.demodata.model.RootMetaData;
import com.epam.ta.reportportal.demodata.service.DemoDataTestItemService;
import com.epam.ta.reportportal.demodata.service.DemoLogsService;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.IntStream;

@Service
public class SuiteWithRetriesGenerator extends DefaultSuiteGenerator {

	private static final int RETRIES_COUNT = 3;

	@Autowired
	public SuiteWithRetriesGenerator(DemoDataTestItemService demoDataTestItemService, DemoLogsService demoLogsService) {
		super(demoDataTestItemService, demoLogsService);
	}

	@Override
	protected void createStep(DemoItemMetadata stepMetaData, RootMetaData rootMetaData) {
		super.createStep(stepMetaData, rootMetaData);
		if (stepMetaData.getStatus() != StatusEnum.PASSED) {
			generateRetries(stepMetaData, rootMetaData);
		}
	}

	private void generateRetries(final DemoItemMetadata metadata, RootMetaData rootMetaData) {
		IntStream.range(0, RETRIES_COUNT).forEach(i -> {
			final DemoItemMetadata retryMetaData = getMetadata(metadata.getName(),
					metadata.getType(),
					metadata.getStatus(),
					metadata.getParentId()
			).withIssue(metadata.getIssue()).withRetry(true);
			super.createStep(retryMetaData, rootMetaData);
		});
	}
}
