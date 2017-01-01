package com.epam.ta.reportportal.ws.rabbit;

import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.List;

/**
 * @author Konstantin Antipin
 */
@Component
public class ReportingStartupService {

    @Autowired
    @Qualifier("reportingListenerContainers")
    private List<AbstractMessageListenerContainer> listenerContainers;

    @PostConstruct
    public void init() {
        for (AbstractMessageListenerContainer listenerContainer : listenerContainers) {
            listenerContainer.start();
        }
    }

}
