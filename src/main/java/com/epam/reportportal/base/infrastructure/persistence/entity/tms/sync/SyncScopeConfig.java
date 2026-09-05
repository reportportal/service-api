package com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync;

import com.epam.reportportal.base.infrastructure.persistence.commons.JsonbUserType;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SyncScopeConfig extends JsonbUserType<SyncScopeConfig> implements Serializable {
    private String remoteFolderId;
    private Long localFolderId;

    @Override
    public Class<SyncScopeConfig> returnedClass() {
        return SyncScopeConfig.class;
    }
}