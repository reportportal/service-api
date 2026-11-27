package com.epam.reportportal.base.reporting.async.producer;


import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static com.epam.reportportal.base.infrastructure.persistence.util.MembershipUtils.rpUserToMembership;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.reporting.StartTestItemRQ;
import com.epam.reportportal.base.reporting.async.config.MessageHeaders;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitOperations.OperationsCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class ItemStartProducerTest {

  @Mock
  RabbitTemplate amqpTemplate;

  @InjectMocks
  ItemStartProducer itemStartProducer;

  @Test
  void startRootItem() {
    StartTestItemRQ request = new StartTestItemRQ();
    ReportPortalUser user = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.EDITOR,
        1L);

    itemStartProducer.startRootItem(user, rpUserToMembership(user),
        request);
    verify(amqpTemplate).invoke(any());
  }

  @Test
  void startChildItem() {
    StartTestItemRQ request = new StartTestItemRQ();
    request.setLaunchUuid(UUID.randomUUID().toString());
    ReportPortalUser user = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.EDITOR,
        1L);

    itemStartProducer.startChildItem(user, rpUserToMembership(user),
        request, "123");
    verify(amqpTemplate).invoke(any());
  }

  @Test
  @SuppressWarnings("unchecked")
  void startItemSendsItsUuidInItemIdHeader() {
    when(amqpTemplate.invoke(any(OperationsCallback.class))).thenAnswer(
        invocation -> ((OperationsCallback<Object>) invocation.getArgument(0)).doInRabbit(amqpTemplate));
    StartTestItemRQ rootRequest = new StartTestItemRQ();
    rootRequest.setLaunchUuid(UUID.randomUUID().toString());
    StartTestItemRQ childRequest = new StartTestItemRQ();
    childRequest.setLaunchUuid(rootRequest.getLaunchUuid());
    ReportPortalUser user = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.EDITOR,
        1L);

    itemStartProducer.startRootItem(user, rpUserToMembership(user), rootRequest);
    itemStartProducer.startChildItem(user, rpUserToMembership(user), childRequest, "parent-uuid");

    ArgumentCaptor<MessagePostProcessor> postProcessors = ArgumentCaptor.forClass(MessagePostProcessor.class);
    verify(amqpTemplate, times(2)).convertAndSend(anyString(), anyString(),
        any(StartTestItemRQ.class), postProcessors.capture());
    MessageProperties root = postProcess(postProcessors.getAllValues().get(0));
    MessageProperties child = postProcess(postProcessors.getAllValues().get(1));
    assertEquals(rootRequest.getUuid(), root.getHeader(MessageHeaders.ITEM_ID));
    assertEquals("", root.getHeader(MessageHeaders.PARENT_ITEM_ID));
    assertEquals(childRequest.getUuid(), child.getHeader(MessageHeaders.ITEM_ID));
    assertEquals("parent-uuid", child.getHeader(MessageHeaders.PARENT_ITEM_ID));
  }

  private static MessageProperties postProcess(MessagePostProcessor postProcessor) {
    return postProcessor.postProcessMessage(new Message(new byte[0], new MessageProperties()))
        .getMessageProperties();
  }
}
