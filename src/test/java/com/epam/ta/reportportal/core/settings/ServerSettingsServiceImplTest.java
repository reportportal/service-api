package com.epam.ta.reportportal.core.settings;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.SettingsUpdatedEvent;
import com.epam.ta.reportportal.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.entity.ServerSettings;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.model.settings.UpdateSettingsRq;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServerSettingsServiceImplTest {

  @Mock
  private ServerSettingsRegistry registry;
  @Mock
  private ServerSettingsRepository repository;
  @Mock
  private MessageBus messageBus;
  @Mock
  private ServerSettingHandler handler;

  @InjectMocks
  private ServerSettingsServiceImpl service;

  @Test
  void updateServerSettingsWhenHandlerPresentShouldValidatePersistInvokeHandlerAndPublishEvent() {
    // given
    final ReportPortalUser rpUser = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.MEMBER, 1L);
    var serverSettings = new ServerSettings();
    serverSettings.setKey("server.password.min.length");
    serverSettings.setValue("8");
    when(repository.findByKey(serverSettings.getKey())).thenReturn(Optional.of(serverSettings));
    when(registry.getHandler(serverSettings.getKey())).thenReturn(Optional.of(handler));

    var updateSettingsRq = new UpdateSettingsRq();
    updateSettingsRq.setKey(serverSettings.getKey());
    updateSettingsRq.setValue("12");

    // when
    OperationCompletionRS rs = service.updateServerSettings(updateSettingsRq, rpUser);

    // then
    assertEquals("Server Settings were successfully updated.", rs.getResultMessage());
    verify(handler).validate("12");
    verify(repository).save(serverSettings);
    verify(handler).handle("12");

    ArgumentCaptor<SettingsUpdatedEvent> captor = ArgumentCaptor.forClass(SettingsUpdatedEvent.class);
    verify(messageBus).publishActivity(captor.capture());
  }
}


