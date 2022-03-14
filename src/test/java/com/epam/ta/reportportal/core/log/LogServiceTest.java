package com.epam.ta.reportportal.core.log;

import com.epam.ta.reportportal.elastic.dao.LogMessageRepository;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.log.LogMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogServiceTest {

//    @Mock
//    private LogMessageRepository logMessageRepository;
//    @InjectMocks
//    private LogService logService;
//
//    private Log log;
//    private Long logId;
//    private LocalDateTime logTime;
//    private String logMessageText;
//    private LocalDateTime lastModified;
//    private Integer logLevel;
//    private TestItem testItem;
//    private Attachment attachment;
//    private Long itemId;
//    private LogMessage logMessage;
//    private Long projectId;
//
//    @BeforeEach
//    public void init() {
//        logId = 1L;
//        logTime = LocalDateTime.of(2021, 12, 1, 1, 1, 1);
//        logMessageText = "Log message text";
//        lastModified = LocalDateTime.of(2021, 12,  1, 1, 2, 1);
//        logLevel = 1;
//        itemId = 2L;
//        testItem = new TestItem(itemId);
//        attachment = new Attachment();
//        projectId = 3L;
//
//        log = new Log(logId, logTime, logMessageText, lastModified, logLevel, testItem, attachment);
//        log.setProjectId(projectId);
//
//        logMessage = new LogMessage(logId, logTime, logMessageText, itemId, null, projectId);
//    }
//
//    @Test
//    void saveLogMessageToElasticSearch() {
//        when(logMessageRepository.save(logMessage)).thenReturn(logMessage);
//
//        logService.saveLogMessageToElasticSearch(log);
//
////        assertEquals(logMessage, logMessageResult);
//    }
//
//    @Test
//    void saveLogMessageListToElasticSearch() {
//        List<Log> logList = List.of(log);
//        when(logMessageRepository.saveAll(List.of(logMessage))).thenReturn(List.of(logMessage));
//
//        logService.saveLogMessageListToElasticSearch(logList);
//
////        assertEquals(List.of(logMessage), logMessageList);
//    }
}