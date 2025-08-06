package com.epam.ta.reportportal.core.tms.service.factory;

import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioType;
import com.epam.ta.reportportal.core.tms.service.TmsManualScenarioImplService;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class TmsManualScenarioImplServiceFactory {

  private final Map<TmsManualScenarioType, TmsManualScenarioImplService> tmsManualScenarioImplServices;

  public TmsManualScenarioImplServiceFactory(
      List<TmsManualScenarioImplService> tmsManualScenarioImplServices) {
    this.tmsManualScenarioImplServices = tmsManualScenarioImplServices
        .stream()
        .collect(Collectors.toMap(
            TmsManualScenarioImplService::getTmsManualScenarioType,
            Function.identity()
        ));
  }

  public TmsManualScenarioImplService getTmsManualScenarioService(
      TmsManualScenarioType tmsManualScenarioType) {
    var tmsManualScenarioService = tmsManualScenarioImplServices.get(tmsManualScenarioType);
    if (Objects.isNull(tmsManualScenarioService)) {
      throw new UnsupportedOperationException("Unsupported tmsManualScenarioType.");
    } else {
      return tmsManualScenarioService;
    }
  }

  public Collection<TmsManualScenarioImplService> getTmsManualScenarioImplServices() {
    return tmsManualScenarioImplServices.values();
  }
}
