package com.epam.reportportal.infrastructure.persistence.dao.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.lang.Nullable;

@Converter(autoApply = true)
public class JpaInstantConverter implements AttributeConverter<Instant, Timestamp> {

  @Nullable
  @Override
  public Timestamp convertToDatabaseColumn(Instant instant) {
    return instant == null ? null
        : Timestamp.valueOf(LocalDateTime.ofInstant(instant, ZoneOffset.UTC));
  }

  @Nullable
  @Override
  public Instant convertToEntityAttribute(Timestamp timestamp) {
    return timestamp == null ? null : timestamp.toInstant();
  }
}
