package com.epam.reportportal.base.core.tms.service;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.BAD_REQUEST_ERROR;

import com.epam.reportportal.base.core.tms.dto.TmsUserSettingsRQ;
import com.epam.reportportal.base.core.tms.dto.TmsUserSettingsRS;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsUserSettingsRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsUserSettings;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsUserSettingsId;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class TmsUserSettingsServiceImpl implements TmsUserSettingsService {

  static final int MAX_STORED_KEYS = 200;
  static final int MAX_VALUE_LENGTH = 4096;

  private static final String VALUE_TOO_LONG =
      "Value of the setting '%s' is longer than %d characters";
  private static final String TOO_MANY_KEYS =
      "No more than %d settings can be stored per user and project";

  private final TmsUserSettingsRepository tmsUserSettingsRepository;
  private final JsonMapper objectMapper;

  @Override
  @Transactional(readOnly = true)
  public TmsUserSettingsRS get(long projectId, long userId) {
    return new TmsUserSettingsRS(loadSettings(projectId, userId));
  }

  @Override
  @Transactional
  public TmsUserSettingsRS update(long projectId, long userId, TmsUserSettingsRQ request) {
    var values = new LinkedHashMap<String, Object>();
    List<String> removedKeys = new ArrayList<>();

    request.settings().forEach((key, value) -> {
      if (Objects.isNull(value)) {
        removedKeys.add(key);
      } else {
        if (objectMapper.writeValueAsString(value).length() > MAX_VALUE_LENGTH) {
          throw new ReportPortalException(BAD_REQUEST_ERROR,
              VALUE_TOO_LONG.formatted(key, MAX_VALUE_LENGTH));
        }
        values.put(key, value);
      }
    });

    tmsUserSettingsRepository.upsertSettings(userId, projectId,
        objectMapper.writeValueAsString(values), objectMapper.writeValueAsString(removedKeys));

    var settings = loadSettings(projectId, userId);
    if (settings.size() > MAX_STORED_KEYS) {
      throw new ReportPortalException(BAD_REQUEST_ERROR, TOO_MANY_KEYS.formatted(MAX_STORED_KEYS));
    }
    return new TmsUserSettingsRS(settings);
  }

  private Map<String, Object> loadSettings(long projectId, long userId) {
    return tmsUserSettingsRepository.findById(new TmsUserSettingsId(userId, projectId))
        .map(TmsUserSettings::getSettings)
        .filter(Objects::nonNull)
        .orElseGet(Map::of);
  }
}
