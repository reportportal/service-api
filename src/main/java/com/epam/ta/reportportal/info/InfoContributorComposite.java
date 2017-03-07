package com.epam.ta.reportportal.info;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class InfoContributorComposite implements InfoContributor {

    private final List<ExtensionContributor> infoContributors;

    @Autowired
    public InfoContributorComposite(List<ExtensionContributor> infoContributors) {
        this.infoContributors = infoContributors;
    }

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("extensions", infoContributors.stream()
                        .map(ExtensionContributor::contribute).flatMap(map -> map.entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }
}
