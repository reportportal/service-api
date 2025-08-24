package com.epam.ta.reportportal.core.tms.service;

import static com.epam.reportportal.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.db.entity.TmsAttribute;
import com.epam.ta.reportportal.core.tms.db.repository.TmsAttributeRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsAttributeMapper;
import jakarta.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsAttributeServiceImpl implements TmsAttributeService {

  private static final String TMS_ATTRIBUTES_WITH_IDS = "TMS Attributes with IDs %s";

  private final TmsAttributeRepository tmsAttributeRepository;
  private final TmsAttributeMapper tmsAttributeMapper;

  @Override
  @Transactional
  public Map<String, TmsAttribute> getTmsAttributes(@NotEmpty List<TmsAttributeRQ> attributes) {
    Map<String, TmsAttribute> result = new HashMap<>();

    var idsToFind = attributes.stream()
        .map(TmsAttributeRQ::getId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();

    if (!idsToFind.isEmpty()) {
      var existingAttributes = tmsAttributeRepository.findAllById(idsToFind);

      if (existingAttributes.size() != idsToFind.size()) {
        var foundIds = existingAttributes.stream()
            .map(TmsAttribute::getId)
            .collect(Collectors.toSet());

        var missingIds = idsToFind.stream()
            .filter(id -> !foundIds.contains(id))
            .toList();

        throw new ReportPortalException(
            NOT_FOUND, TMS_ATTRIBUTES_WITH_IDS.formatted(missingIds));
      }

      existingAttributes.forEach(attr -> result.put("id:" + attr.getId(), attr));
    }

    var keysToProcess = attributes.stream()
        .filter(attr -> Objects.isNull(attr.getId()) && StringUtils.isNotBlank(attr.getKey()))
        .map(TmsAttributeRQ::getKey)
        .distinct()
        .toList();

    if (!keysToProcess.isEmpty()) {
      var existingByKey = tmsAttributeRepository.findAllByKeyIn(keysToProcess);
      existingByKey.forEach(attr -> result.put("key:" + attr.getKey(), attr));

      var existingKeys = existingByKey.stream()
          .map(TmsAttribute::getKey)
          .collect(Collectors.toSet());

      var keysToCreate = keysToProcess.stream()
          .filter(key -> !existingKeys.contains(key))
          .toList();

      if (!keysToCreate.isEmpty()) {
        var newAttributes = keysToCreate.stream()
            .map(tmsAttributeMapper::createTmsAttribute)
            .toList();

        var savedAttributes = tmsAttributeRepository.saveAll(newAttributes);
        savedAttributes.forEach(attr -> result.put("key:" + attr.getKey(), attr));
      }
    }

    return result;
  }
}
