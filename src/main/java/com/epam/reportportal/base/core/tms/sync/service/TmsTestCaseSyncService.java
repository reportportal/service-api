package com.epam.reportportal.base.core.tms.sync.service;

import com.epam.reportportal.base.core.tms.sync.TmsSyncConnector;
import com.epam.reportportal.base.core.tms.sync.dto.RemoteTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import java.util.List;

public interface TmsTestCaseSyncService {

  void processTestCaseBatch(Long jobId,
      Long projectId,
      TmsSyncConnector<Integration> connector,
      Integration integration,
      List<RemoteTestCase> batch,
      Long localFolderId
  );
}
