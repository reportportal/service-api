package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.ws.model.log.LogResource;

import java.util.Optional;
import java.util.function.Function;

public final class LogConverter {

    private LogConverter() {
        //static only
    }

    public static final Function<Log, LogResource> TO_RESOURCE = model -> {
        LogResource resource = new LogResource();
        if (Optional.ofNullable(model).isPresent()) {
            resource.setIdLog(model.getId());
            resource.setMessage(Optional.ofNullable(model.getLogMsg()).orElse("NULL"));
            resource.setLogTime(model.getLogTime());
            if (Optional.ofNullable(model.getBinaryContent()).isPresent()){
                LogResource.BinaryContent binaryContent = new LogResource.BinaryContent();
                binaryContent.setBinaryDataId(model.getBinaryContent().getBinaryDataId());
                binaryContent.setContentType(model.getBinaryContent().getContentType());
                binaryContent.setThumbnailId(model.getBinaryContent().getThumbnailId());
                resource.setBinaryContent(binaryContent);
            }
            resource.setTestItem(model.getTestItemRef());
            if (Optional.ofNullable(model.getLevel()).isPresent()){
                resource.setLevel(model.getLevel().toString());
            }
        }
        return resource;
    };

}
