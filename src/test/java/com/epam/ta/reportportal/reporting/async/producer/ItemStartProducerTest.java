package com.epam.ta.reportportal.reporting.async.producer;

import static com.epam.ta.reportportal.OrganizationUtil.TEST_PROJECT_KEY;
import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.util.MembershipUtils.rpUserToMembership;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.reporting.StartTestItemRQ;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;

@ExtendWith(MockitoExtension.class)
class ItemStartProducerTest {

  @Mock
  AmqpTemplate amqpTemplate;

  @InjectMocks
  ItemStartProducer itemStartProducer;

  @Test
  void startRootItem() {
    StartTestItemRQ request = new StartTestItemRQ();
    ReportPortalUser user = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.EDITOR,
        1L);

    itemStartProducer.startRootItem(user, rpUserToMembership(user),
        request);
    verify(amqpTemplate).convertAndSend(any(), any(), any(), any());
  }

  @Test
  void startChildItem() {
    StartTestItemRQ request = new StartTestItemRQ();
    request.setLaunchUuid(UUID.randomUUID().toString());
    ReportPortalUser user = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.EDITOR,
        1L);

    itemStartProducer.startChildItem(user, rpUserToMembership(user),
        request, "123");
    verify(amqpTemplate).convertAndSend(any(), any(), any(), any());
  }
}
