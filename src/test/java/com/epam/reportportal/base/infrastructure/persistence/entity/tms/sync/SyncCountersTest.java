package com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class SyncCountersTest {

  private final JsonMapper jsonMapper = JsonMapper.builder().build();

  @Test
  @DisplayName("Should serialize only public sync statistics")
  void serializeWhenCountersProvidedShouldContainOnlyPublicStatistics() {
    // Given
    var counters = new SyncCounters(4, 2, 1, 1);

    // When
    var json = jsonMapper.readTree(jsonMapper.writeValueAsString(counters));

    // Then
    assertThat(json.propertyNames()).containsExactlyInAnyOrder("processed", "imported", "skipped", "failed");
    assertThat(json.get("processed").asInt()).isEqualTo(4);
    assertThat(json.get("imported").asInt()).isEqualTo(2);
    assertThat(json.get("skipped").asInt()).isEqualTo(1);
    assertThat(json.get("failed").asInt()).isEqualTo(1);
  }
}
