package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseGenerationMetadata;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsTestCaseGenerationMetadataRepository extends JpaRepository<TmsTestCaseGenerationMetadata, Long> {

  Optional<TmsTestCaseGenerationMetadata> findByTestCaseVersionId(Long testCaseVersionId);

  List<TmsTestCaseGenerationMetadata> findByTestCaseVersionIdIn(Collection<Long> testCaseVersionIds);
}
