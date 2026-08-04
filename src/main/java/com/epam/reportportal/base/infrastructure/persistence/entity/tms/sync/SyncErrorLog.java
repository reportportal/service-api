package com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync;

import com.epam.reportportal.base.infrastructure.persistence.commons.JsonbUserType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
public class SyncErrorLog extends JsonbUserType<SyncErrorLog> implements Serializable {
    
    @Builder.Default
    private List<SyncError> errors = new ArrayList<>();

    @Override
    public Class<SyncErrorLog> returnedClass() {
        return SyncErrorLog.class;
    }
}
