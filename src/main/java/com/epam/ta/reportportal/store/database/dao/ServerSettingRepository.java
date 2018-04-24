package com.epam.ta.reportportal.store.database.dao;

import com.epam.ta.reportportal.store.database.entity.ServerSettings;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.stream.Stream;

public interface ServerSettingRepository extends ReportPortalRepository<ServerSettings, Long> {

	@Query("from ServerSettings ss")
	Stream<ServerSettings> streamAll();

	Optional<ServerSettings> findByKey(String key);
}
