package com.epam.ta.reportportal.info;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InfoContributorComposite implements InfoContributor {

    private final List<ExtensionContributor> infoContributors;

    @Autowired
    public InfoContributorComposite(List<ExtensionContributor> infoContributors) {
        this.infoContributors = infoContributors;
    }

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> result = new HashMap<>();
        for (ExtensionContributor e: infoContributors){
            Map<String, ?> contribute = e.contribute();
            result.putAll(contribute);
        }
        builder.withDetail("extensions", result);
    }
}
