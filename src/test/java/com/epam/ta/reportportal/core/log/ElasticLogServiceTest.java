package com.epam.ta.reportportal.core.log;

import static com.epam.ta.reportportal.core.configs.rabbit.BackgroundProcessingConfiguration.LOG_MESSAGE_SAVING_ROUTING_KEY;
import static com.epam.ta.reportportal.core.configs.rabbit.BackgroundProcessingConfiguration.PROCESSING_EXCHANGE_NAME;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.log.LogMessage;
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

  private Log log;

  private LogMessage logMessage;

  @BeforeEach
  public void setUp() {
    Long itemId = 1L;
    Long launchId = 1L;
    log = new Log();
    log.setTestItem(new TestItem(itemId));
    log.setLaunch(new Launch(launchId));

    logMessage = new LogMessage(log.getId(), log.getLogTime(), log.getLogMessage(), itemId,
        launchId, log.getProjectId());
  }

  @Test
  void saveLogMessage() {
    elasticLogService.saveLogMessage(log, log.getLaunch().getId());

    verify(amqpTemplate, times(1)).convertAndSend(eq(PROCESSING_EXCHANGE_NAME),
        eq(LOG_MESSAGE_SAVING_ROUTING_KEY), eq(logMessage));
  }

  @Test
  void saveLogMessageList() {
    elasticLogService.saveLogMessageList(List.of(log), log.getLaunch().getId());

    verify(amqpTemplate, times(1)).convertAndSend(eq(PROCESSING_EXCHANGE_NAME),
        eq(LOG_MESSAGE_SAVING_ROUTING_KEY), eq(logMessage));
  }
}