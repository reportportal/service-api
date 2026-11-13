package com.epam.reportportal.base.core.tms.sync;

import com.epam.reportportal.base.core.tms.enums.TmsSyncProvider;
import com.epam.reportportal.base.core.tms.sync.dto.FetchTestCasesResult;
import com.epam.reportportal.base.core.tms.sync.dto.RemoteFolder;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

public interface TmsSyncConnector<T> {

    TmsSyncProvider getSupportedProvider();

    void validateConfig(T config);

    List<RemoteFolder> fetchFolderTree(T config, String rootFolderId);

    FetchTestCasesResult fetchTestCases(T config, RemoteFolder folder, Instant since, int offset, int limit);

    InputStream downloadAttachment(T config, String contentUrl);
}
