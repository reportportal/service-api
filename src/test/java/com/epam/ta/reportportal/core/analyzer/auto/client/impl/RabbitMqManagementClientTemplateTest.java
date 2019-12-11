package com.epam.ta.reportportal.core.analyzer.auto.client.impl;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.rabbitmq.http.client.Client;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.epam.ta.reportportal.core.analyzer.auto.client.impl.AnalyzerUtils.ANALYZER_KEY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RabbitMqManagementClientTemplateTest {

    @Mock
    private Client rabbitClient;

    @InjectMocks
    private RabbitMqManagementClientTemplate template;

    @Test
    public void testReportPortalExceptionOnGetExchanges() {
        when(rabbitClient.getExchanges(ANALYZER_KEY)).thenReturn(null);

        assertThatThrownBy(() -> template.getAnalyzerExchangesInfo()).isInstanceOf(ReportPortalException.class);
    }
}