package com.epam.reportportal.base.core.tms.sync.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoteAttachment {
    private String id;
    private String filename;
    private String mimeType;
    private Long size;
    private String contentUrl;
}
