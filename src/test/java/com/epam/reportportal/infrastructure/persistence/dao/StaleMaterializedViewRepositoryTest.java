package com.epam.reportportal.infrastructure.persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.ws.BaseMvcTest;
import com.epam.reportportal.infrastructure.persistence.entity.materialized.StaleMaterializedView;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class StaleMaterializedViewRepositoryTest extends BaseMvcTest {

  @Autowired
  private StaleMaterializedViewRepository staleMaterializedViewRepository;

  @Test
  void shouldInsertAndSetId() {

    final StaleMaterializedView staleMaterializedView = new StaleMaterializedView();
    staleMaterializedView.setName("test");
    staleMaterializedView.setCreationDate(Instant.now());

    final StaleMaterializedView result = staleMaterializedViewRepository.insert(
        staleMaterializedView);

    assertNotNull(staleMaterializedView.getId());
    assertNotNull(result.getId());
    assertEquals(result.getId(), staleMaterializedView.getId());

    final Optional<StaleMaterializedView> found = staleMaterializedViewRepository.findById(1L);
    assertTrue(found.isPresent());

    final Optional<StaleMaterializedView> notFound = staleMaterializedViewRepository.findById(2L);
    assertTrue(notFound.isEmpty());

  }

}
