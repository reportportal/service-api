package com.epam.ta.reportportal.core.tms.service.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioRQ.TmsManualScenarioType;
import com.epam.ta.reportportal.core.tms.service.TmsManualScenarioService;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsManualScenarioServiceFactoryTest {

  @Mock
  private TmsManualScenarioService textManualScenarioService;

  @Mock
  private TmsManualScenarioService stepsManualScenarioService;

  private TmsManualScenarioServiceFactory factory;

  @BeforeEach
  void setUp() {
    when(textManualScenarioService.getTmsManualScenarioType()).thenReturn(
        TmsManualScenarioType.TEXT);
    when(stepsManualScenarioService.getTmsManualScenarioType()).thenReturn(
        TmsManualScenarioType.STEPS);

    factory = new TmsManualScenarioServiceFactory(
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
  void shouldReturnAllServices() {
    var services = factory.getTmsManualScenarioServices();

    assertThat(services)
        .isNotNull()
        .isNotEmpty()
        .hasSize(2)
        .contains(stepsManualScenarioService, textManualScenarioService);
  }
}
