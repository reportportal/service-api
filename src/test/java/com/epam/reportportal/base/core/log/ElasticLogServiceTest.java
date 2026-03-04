package com.epam.reportportal.base.core.log;

import static com.epam.reportportal.base.core.configs.rabbit.BackgroundProcessingConfiguration.LOG_MESSAGE_SAVING_ROUTING_KEY;
import static com.epam.reportportal.base.core.configs.rabbit.BackgroundProcessingConfiguration.PROCESSING_EXCHANGE_NAME;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.LogFull;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.LogMessage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;

@ExtendWith(MockitoExtension.class)
class ElasticLogServiceTest {

  @Mock
  private AmqpTemplate amqpTemplate;

  @InjectMocks
  private ElasticLogService elasticLogService;

  private LogFull logFull;

  private LogMessage logMessage;

  @BeforeEach
  public void setUp() {
    Long itemId = 1L;
    Long launchId = 1L;
    logFull = new LogFull();
    logFull.setTestItem(new TestItem(itemId));
    logFull.setLaunch(new Launch(launchId));

    logMessage = new LogMessage(logFull.getId(), logFull.getLogTime(), logFull.getLogMessage(),
        itemId, launchId, logFull.getProjectId());
  }

  @Test
  void saveLogMessage() {
    elasticLogService.saveLogMessage(logFull, logFull.getLaunch().getId());

    verify(amqpTemplate, times(1)).convertAndSend(eq(PROCESSING_EXCHANGE_NAME),
        eq(LOG_MESSAGE_SAVING_ROUTING_KEY), eq(logMessage));
  }

  @Test
  void saveLogMessageList() {
    elasticLogService.saveLogMessageList(List.of(logFull), logFull.getLaunch().getId());

    verify(amqpTemplate, times(1)).convertAndSend(eq(PROCESSING_EXCHANGE_NAME),
        eq(LOG_MESSAGE_SAVING_ROUTING_KEY), eq(logMessage));
  }
}
