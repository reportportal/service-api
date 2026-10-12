package com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.PreparedStatement;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.postgresql.util.PGobject;

class SyncCountersTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void shouldSerializeOnlyPublicSyncStatistics() throws Exception {
    var counters = new SyncCounters(4, 2, 1, 1);

    var serializedCounters = objectMapper.readTree(objectMapper.writeValueAsString(counters));

    assertTrue(serializedCounters.has("processed"));
    assertTrue(serializedCounters.has("imported"));
    assertTrue(serializedCounters.has("skipped"));
    assertTrue(serializedCounters.has("failed"));
    assertFalse(serializedCounters.has("total"));
    assertFalse(serializedCounters.has("sqlType"));
  }

  @Test
  void shouldNotPersistSqlTypeIntoJsonbColumn() throws Exception {
    var counters = new SyncCounters(4, 2, 1, 1);
    var statement = mock(PreparedStatement.class);

    counters.nullSafeSet(statement, counters, 1, null);

    var captor = ArgumentCaptor.forClass(Object.class);
    verify(statement).setObject(any(Integer.class), captor.capture());
    var json = objectMapper.readTree(((PGobject) captor.getValue()).getValue());
    assertEquals(4, json.get("processed").asInt());
    assertEquals(2, json.get("imported").asInt());
    assertEquals(1, json.get("skipped").asInt());
    assertEquals(1, json.get("failed").asInt());
    assertFalse(json.has("sqlType"));
  }
}
