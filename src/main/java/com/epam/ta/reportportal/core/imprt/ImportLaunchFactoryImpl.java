package com.epam.ta.reportportal.core.imprt;

import com.epam.ta.reportportal.core.imprt.format.async.AsyncImportLaunchJunit;
import com.epam.ta.reportportal.core.imprt.format.async.ImportLaunch;
import com.epam.ta.reportportal.core.imprt.format.async.ImportType;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ImportLaunchFactoryImpl implements ImportLaunchFactory, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private static final Map<ImportType, Class<? extends ImportLaunch>> MAPPING =
            ImmutableMap.<ImportType, Class<? extends ImportLaunch>>builder()
            .put(ImportType.JUNIT, AsyncImportLaunchJunit.class)
            .build();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public ImportLaunch getImportLaunch(ImportType type) {
        return applicationContext.getBean(MAPPING.get(type));
    }
}
