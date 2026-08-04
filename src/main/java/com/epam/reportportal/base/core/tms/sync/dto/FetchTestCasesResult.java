package com.epam.reportportal.base.core.tms.sync.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FetchTestCasesResult {

    private int totalCount;
    private List<RemoteTestCase> testCases;
    private boolean hasMore;
}

