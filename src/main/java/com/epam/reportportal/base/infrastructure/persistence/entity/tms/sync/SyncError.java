package com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncError implements Serializable {
    private String externalId;
    private String message;
    private String stackTrace;
}
