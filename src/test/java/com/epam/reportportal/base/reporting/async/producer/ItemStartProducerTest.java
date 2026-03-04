package com.epam.reportportal.base.reporting.async.producer;


import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static com.epam.reportportal.base.infrastructure.persistence.util.MembershipUtils.rpUserToMembership;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.reporting.StartTestItemRQ;
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
