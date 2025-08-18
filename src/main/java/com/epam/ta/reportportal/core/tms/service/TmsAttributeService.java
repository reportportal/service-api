package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsAttribute;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

public interface TmsAttributeService {

  Map<String, TmsAttribute> getTmsAttributes(@NotEmpty List<TmsAttributeRQ> attributes);
}
