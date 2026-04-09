package com.epam.ta.reportportal.core.launch.repository;

import com.epam.ta.reportportal.entity.launch.Launch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LaunchModifiedRepository extends JpaRepository<Launch, Long> {

  @Modifying
  @Query(value = """
      INSERT INTO launches_modified (launch_id)
      VALUES (:launchId)
      ON CONFLICT (launch_id) DO UPDATE SET created_at = clock_timestamp()
      """, nativeQuery = true)
  void insertIfAbsent(@Param("launchId") Long launchId);
}
