package com.epam.ta.reportportal.core.launch.changes;

import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.RetentionPolicyEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import java.util.Set;

public record LaunchFieldsSnapshot(
    String description,
    LaunchModeEnum mode,
    StatusEnum status,
    RetentionPolicyEnum retentionPolicy,
    Set<AttributeSnapshot> attributes
) {

}
