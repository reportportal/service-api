package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.ws.model.externalsystem.ExternalSystemResource;

import java.util.function.Function;

public final class ExternalSystemConverter {

    private ExternalSystemConverter() {
        //static only
    }

    public static final Function<ExternalSystem, ExternalSystemResource> TO_RESOURCE = model ->
            {
                ExternalSystemResource resource = new ExternalSystemResource();
                resource.setSystemId(model.getId());
                resource.setUrl(model.getUrl());
                resource.setProjectRef(model.getProjectRef());
                resource.setProject(model.getProject());
                resource.setExternalSystemType(model.getExternalSystemType().name());
                resource.setExternalSystemAuth(model.getExternalSystemAuth().name());
                resource.setUsername(model.getUsername());
                resource.setAccessKey(model.getAccessKey());
                resource.setDomain(model.getDomain());
                resource.setFields(model.getFields());
                return resource;
            };
}
