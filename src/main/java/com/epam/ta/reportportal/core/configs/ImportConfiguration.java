package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.core.imprt.format.async.AsyncXmlImportHandler;
import com.epam.ta.reportportal.core.imprt.format.async.XmlParseJob;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class ImportConfiguration {

    @Bean(name = "xmlParseJob")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public XmlParseJob xmlParseJob() {
        return new XmlParseJob();
    }

    @Bean(name = "asyncImportHandler")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public AsyncXmlImportHandler asyncImportHandler(){
        return new AsyncXmlImportHandler();
    }
}
