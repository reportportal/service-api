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
public class SyncCounters extends JsonbUserType<SyncCounters> implements Serializable {
    private int total;
    private int processed;
    private int failed;

    @Override
    public Class<SyncCounters> returnedClass() {
        return SyncCounters.class;
    }
}