package com.epam.reportportal.base.core.tms.sync.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoteTestCase {
    private String id;
    private String name;
    private String description;
    private String priority;
    private List<String> labels;
    private List<String> requirements;
    private String steps;
    private String expectedResults;
    private String folderId;
    private Instant updatedAt;
    private List<RemoteAttachment> attachments;
}
