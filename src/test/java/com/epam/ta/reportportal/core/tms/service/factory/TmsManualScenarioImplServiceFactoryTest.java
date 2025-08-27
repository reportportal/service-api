package com.epam.ta.reportportal.core.tms.service.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioType;
import com.epam.ta.reportportal.core.tms.service.TmsManualScenarioImplService;
import com.epam.ta.reportportal.core.tms.service.TmsStepsManualScenarioImplService;
import com.epam.ta.reportportal.core.tms.service.TmsTextManualScenarioImplService;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsManualScenarioImplServiceFactoryTest {

  @Mock
  private TmsTextManualScenarioImplService textManualScenarioService;

  @Mock
  private TmsStepsManualScenarioImplService stepsManualScenarioService;

  private TmsManualScenarioImplServiceFactory factory;

  @BeforeEach
  void setUp() {
    when(textManualScenarioService.getTmsManualScenarioType()).thenReturn(
        TmsManualScenarioType.TEXT);
    when(stepsManualScenarioService.getTmsManualScenarioType()).thenReturn(
        TmsManualScenarioType.STEPS);

    factory = new TmsManualScenarioImplServiceFactory(
        Arrays.asList(textManualScenarioService, stepsManualScenarioService));
  }

  @Test
  void shouldReturnTextManualScenarioService() {
    // When
    var service = factory.getTmsManualScenarioService(TmsManualScenarioType.TEXT);

    // Then
    assertThat(service).isEqualTo(textManualScenarioService);
  }

  @Test
  void shouldReturnStepsManualScenarioService() {
    // When
    var service = factory.getTmsManualScenarioService(TmsManualScenarioType.STEPS);

    // Then
    assertThat(service).isEqualTo(stepsManualScenarioService);
  }

  @Test
  void shouldReturnTextManualScenarioServiceWithEntityEnum() {
    // When
    var service = factory.getTmsManualScenarioService(
        com.epam.ta.reportportal.core.tms.db.entity.enums.TmsManualScenarioType.TEXT);

    // Then
    assertThat(service).isEqualTo(textManualScenarioService);
  }

  @Test
  void shouldReturnStepsManualScenarioServiceWithEntityEnum() {
    // When
    var service = factory.getTmsManualScenarioService(
        com.epam.ta.reportportal.core.tms.db.entity.enums.TmsManualScenarioType.STEPS);

    // Then
    assertThat(service).isEqualTo(stepsManualScenarioService);
  }

  @Test
  void shouldReturnAllServices() {
    // When
    var services = factory.getTmsManualScenarioImplServices();

    // Then
    assertThat(services)
        .isNotNull()
        .isNotEmpty()
        .hasSize(2)
        .contains(stepsManualScenarioService, textManualScenarioService);
  }

  @Test
  void shouldReturnSameServiceForBothEnumTypes() {
    // When
    var serviceFromDto = factory.getTmsManualScenarioService(TmsManualScenarioType.TEXT);
    var serviceFromEntity = factory.getTmsManualScenarioService(
        com.epam.ta.reportportal.core.tms.db.entity.enums.TmsManualScenarioType.TEXT);

    // Then
    assertThat(serviceFromDto).isEqualTo(serviceFromEntity);
    assertThat(serviceFromDto).isEqualTo(textManualScenarioService);
  }

  @Test
  void shouldReturnSameStepsServiceForBothEnumTypes() {
    // When
    var serviceFromDto = factory.getTmsManualScenarioService(TmsManualScenarioType.STEPS);
    var serviceFromEntity = factory.getTmsManualScenarioService(
        com.epam.ta.reportportal.core.tms.db.entity.enums.TmsManualScenarioType.STEPS);

    // Then
    assertThat(serviceFromDto).isEqualTo(serviceFromEntity);
    assertThat(serviceFromDto).isEqualTo(stepsManualScenarioService);
  }
}
