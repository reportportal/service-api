package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

/**
 * @author Konstantin Antipin
 */
@ExtendWith(MockitoExtension.class)
class TestItemAsyncControllerTest {

	@Mock
	StartTestItemHandler startTestItemHandler;

	@Mock
	FinishTestItemHandler finishTestItemHandler;

	@InjectMocks
	TestItemAsyncController testItemAsyncController;

	@Test
	void startRootItem() {
		ReportPortalUser user = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

		StartTestItemRQ startTestItemRQ = new StartTestItemRQ();

		ArgumentCaptor<ReportPortalUser> userArgumentCaptor = ArgumentCaptor.forClass(ReportPortalUser.class);
		ArgumentCaptor<ReportPortalUser.ProjectDetails> projectDetailsArgumentCaptor = ArgumentCaptor.forClass(ReportPortalUser.ProjectDetails.class);
		ArgumentCaptor<StartTestItemRQ> requestArgumentCaptor = ArgumentCaptor.forClass(StartTestItemRQ.class);

		testItemAsyncController.startRootItem("test_project", user, startTestItemRQ);
		verify(startTestItemHandler).startRootItem(userArgumentCaptor.capture(),
				projectDetailsArgumentCaptor.capture(),
				requestArgumentCaptor.capture()
		);
		assertEquals(user, userArgumentCaptor.getValue());
		assertEquals(user.getProjectDetails().get("test_project"), projectDetailsArgumentCaptor.getValue());
		assertEquals(startTestItemRQ, requestArgumentCaptor.getValue());
	}

	@Test
	void startChildItem() {
		ReportPortalUser user = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

		StartTestItemRQ startTestItemRQ = new StartTestItemRQ();
		String parentItem = "parent";

		ArgumentCaptor<ReportPortalUser> userArgumentCaptor = ArgumentCaptor.forClass(ReportPortalUser.class);
		ArgumentCaptor<ReportPortalUser.ProjectDetails> projectDetailsArgumentCaptor = ArgumentCaptor.forClass(ReportPortalUser.ProjectDetails.class);
		ArgumentCaptor<StartTestItemRQ> requestArgumentCaptor = ArgumentCaptor.forClass(StartTestItemRQ.class);
		ArgumentCaptor<String> parentArgumentCaptor = ArgumentCaptor.forClass(String.class);

		testItemAsyncController.startChildItem("test_project", user, parentItem, startTestItemRQ);
		verify(startTestItemHandler).startChildItem(userArgumentCaptor.capture(),
				projectDetailsArgumentCaptor.capture(),
				requestArgumentCaptor.capture(),
				parentArgumentCaptor.capture()
		);
		assertEquals(user, userArgumentCaptor.getValue());
		assertEquals(user.getProjectDetails().get("test_project"), projectDetailsArgumentCaptor.getValue());
		assertEquals(startTestItemRQ, requestArgumentCaptor.getValue());
	}

	@Test
	void finishTestItem() {
		ReportPortalUser user = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

		FinishTestItemRQ finishTestItemRQ = new FinishTestItemRQ();
		String testItemId = UUID.randomUUID().toString();
		finishTestItemRQ.setLaunchUuid(UUID.randomUUID().toString());

		ArgumentCaptor<ReportPortalUser> userArgumentCaptor = ArgumentCaptor.forClass(ReportPortalUser.class);
		ArgumentCaptor<ReportPortalUser.ProjectDetails> projectDetailsArgumentCaptor = ArgumentCaptor.forClass(ReportPortalUser.ProjectDetails.class);
		ArgumentCaptor<String> testItemCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<FinishTestItemRQ> requestArgumentCaptor = ArgumentCaptor.forClass(FinishTestItemRQ.class);

		testItemAsyncController.finishTestItem("test_project", user, testItemId, finishTestItemRQ);
		verify(finishTestItemHandler).finishTestItem(userArgumentCaptor.capture(),
				projectDetailsArgumentCaptor.capture(),
				testItemCaptor.capture(),
				requestArgumentCaptor.capture()
		);
		assertEquals(user, userArgumentCaptor.getValue());
		assertEquals(user.getProjectDetails().get("test_project"), projectDetailsArgumentCaptor.getValue());
		assertEquals(finishTestItemRQ, requestArgumentCaptor.getValue());
	}

	@Test
	void finishTestItemWithoutLaunchUuid() {
		ReportPortalUser user = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

		FinishTestItemRQ finishTestItemRQ = new FinishTestItemRQ();
		String testItemId = UUID.randomUUID().toString();

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> testItemAsyncController.finishTestItem("test_project", user, testItemId, finishTestItemRQ)
		);
		assertEquals(
				"Error in handled Request. Please, check specified parameters: 'Launch UUID should not be null or empty.'",
				exception.getMessage()
		);
	}
}