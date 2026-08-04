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
public class RemoteFolder {
    private String id;
    private String name;
    private String parentId;
    private List<String> testCaseIds;
}
