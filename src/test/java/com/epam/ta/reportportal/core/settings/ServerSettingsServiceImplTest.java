package com.epam.ta.reportportal.core.settings;

import static com.epam.ta.reportportal.entity.ServerSettingsConstants.ANALYTICS_CONFIG_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.SettingsUpdatedEvent;
import com.epam.ta.reportportal.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.entity.ServerSettings;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.model.settings.AnalyticsResource;
import com.epam.ta.reportportal.model.settings.ServerSettingsResource;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class ServerSettingsServiceImplTest {

  @Mock
  private ServerSettingsRegistry settingsRegistry;

  @Mock
  private ServerSettingsRepository serverSettingsRepository;

  @Mock
  private MessageBus messageBus;

  @InjectMocks
  private ServerSettingsServiceImpl serverSettingsService;

  @Captor
  private ArgumentCaptor<ServerSettings> settingsCaptor;

  @Captor
  private ArgumentCaptor<SettingsUpdatedEvent> eventCaptor;

  private ReportPortalUser testUser;
  private AnalyticsResource analyticsResource;

  @BeforeEach
  void setUp() {
    testUser = ReportPortalUser.userBuilder()
        .withUserName("testUser")
        .withPassword("test")
        .withAuthorities(Sets.newHashSet(new SimpleGrantedAuthority(UserRole.USER.getAuthority())))
        .withUserId(1L)
        .withEmail("test@email.com")
        .withUserRole(UserRole.USER)
        .build();

    analyticsResource = new AnalyticsResource();
    analyticsResource.setType("all");
    analyticsResource.setEnabled(true);
  }

  @Test
  void saveAnalyticsSettings_WhenUpdatingServerAnalyticsAll_ShouldUpdateSettingsAndSendEvent() {
    // given
    String analyticsType = "all";
    String expectedKey = ANALYTICS_CONFIG_PREFIX + analyticsType;
    analyticsResource.setType(analyticsType);
    analyticsResource.setEnabled(true);
    ServerSettings existingAnalyticsSetting = createServerSettings(expectedKey, "false");
    List<ServerSettings> existingSettings = Arrays.asList(
        existingAnalyticsSetting,
        createServerSettings("server.session.expiration", "86400000"),
        createServerSettings("server.users.sso", "false")
    );

    when(serverSettingsRepository.selectServerSettings()).thenReturn(existingSettings);
    when(serverSettingsRepository.save(any(ServerSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // when
    OperationCompletionRS result = serverSettingsService.saveAnalyticsSettings(analyticsResource, testUser);

    // then
    assertNotNull(result);
    assertEquals("Server Settings were successfully updated.", result.getResultMessage());
    verify(serverSettingsRepository).save(settingsCaptor.capture());
    ServerSettings savedSettings = settingsCaptor.getValue();
    assertEquals(expectedKey, savedSettings.getKey());
    assertEquals("true", savedSettings.getValue());

    verify(messageBus).publishActivity(eventCaptor.capture());
    SettingsUpdatedEvent event = eventCaptor.getValue();
    assertEquals(testUser.getUserId(), event.getUserId());
    assertEquals(testUser.getUsername(), event.getUserLogin());
    
    // Verify the before and after settings in the event
    ServerSettingsResource beforeSettings = event.getBefore();
    ServerSettingsResource afterSettings = event.getAfter();
    
    assertNotNull(beforeSettings);
    assertNotNull(afterSettings);
    assertEquals(expectedKey, beforeSettings.getKey());
    assertEquals(expectedKey, afterSettings.getKey());
    assertEquals("false", beforeSettings.getValue());
    assertEquals("true", afterSettings.getValue());
  }

  private ServerSettings createServerSettings(String key, String value) {
    ServerSettings settings = new ServerSettings();
    settings.setKey(key);
    settings.setValue(value);
    return settings;
  }
}