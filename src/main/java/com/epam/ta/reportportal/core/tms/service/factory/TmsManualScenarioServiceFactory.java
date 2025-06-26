package com.epam.ta.reportportal.core.tms.service.factory;

import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioRQ.TmsManualScenarioType;
import com.epam.ta.reportportal.core.tms.service.TmsManualScenarioService;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class TmsManualScenarioServiceFactory {

  private final Map<TmsManualScenarioType, TmsManualScenarioService> tmsManualScenarioServices;

  public TmsManualScenarioServiceFactory(
      List<TmsManualScenarioService> tmsManualScenarioServices) {
    this.tmsManualScenarioServices = tmsManualScenarioServices
        .stream()
        .collect(Collectors.toMap(
            TmsManualScenarioService::getTmsManualScenarioType,
            Function.identity()
        ));
  }

  public TmsManualScenarioService getTmsManualScenarioService(TmsManualScenarioType tmsManualScenarioType) {
    var tmsManualScenarioService = tmsManualScenarioServices.get(tmsManualScenarioType);
    if (Objects.isNull(tmsManualScenarioService)) {
      throw new UnsupportedOperationException("Unsupported tmsManualScenarioType.");
    } else {
      return tmsManualScenarioService;
    }
  }

  public Collection<TmsManualScenarioService> getTmsManualScenarioServices() {
    return tmsManualScenarioServices.values();
  }
}
