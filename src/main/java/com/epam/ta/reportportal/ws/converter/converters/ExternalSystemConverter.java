package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.AuthType;
import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.database.entity.item.issue.ExternalSystemType;
import com.epam.ta.reportportal.ws.model.externalsystem.CreateExternalSystemRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.ExternalSystemResource;
import org.jasypt.util.text.BasicTextEncryptor;

import java.util.function.Function;

public final class ExternalSystemConverter {

    private ExternalSystemConverter() {
        //static only
    }

    public static final Function<ExternalSystem, ExternalSystemResource> TO_RESOURCE = model -> {
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
    
    public static final Function<CreateExternalSystemRQ, ExternalSystem> TO_MODEL = request -> {
        ExternalSystem externalSystem = new ExternalSystem();
        externalSystem.setExternalSystemType(ExternalSystemType.findByName(request.getExternalSystemType()).orElse(null));
        externalSystem.setUrl(request.getUrl());
        externalSystem.setExternalSystemAuth(AuthType.findByName(request.getExternalSystemAuth()));
        externalSystem.setProject(request.getProject());
        externalSystem.setUsername(request.getUsername());
        String encryptedPass = new BasicTextEncryptor().encrypt(request.getPassword());
        externalSystem.setPassword(encryptedPass);
        externalSystem.setAccessKey(request.getAccessKey());
        externalSystem.setDomain(request.getDomain());
        return externalSystem;
    };
}
