package com.epam.ta.reportportal.core.imprt.impl.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.core.log.CreateLogHandler;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.item.ItemCreatedRS;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayDeque;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xml.sax.Attributes;

@ExtendWith(MockitoExtension.class)
public class XunitImportHandlerTest {

  @Spy
  @InjectMocks
  private XunitImportHandler xunitImportHandler;

  @Mock
  private StartTestItemHandler startTestItemHandler;

  @Mock
  private FinishTestItemHandler finishTestItemHandler;

  @Mock
  private CreateLogHandler createLogHandler;

  private ReportPortalUser user;

  private ReportPortalUser.ProjectDetails projectDetails;

  @Captor
  private ArgumentCaptor<StartTestItemRQ> startTestItemRQArgumentCaptor;

  @Captor
  private ArgumentCaptor<SaveLogRQ> saveLogRQArgumentCaptor;
  private static final String TEST_SUITE = "testsuite";

  private static final String TEST_CASE = "testcase";

  private static final String ATTR_NAME = "attribute";

  private static final String TIMESTAMP = "1690210345";

  private static final String ISO_DATE = "2008-06-03T11:05:30";

  private static final String DURATION = "300000";

  private static final String LAUNCH_ID = "94e83a8c-862c-4b72-95d0-f42665c90e7b";

  private static final String ITEM_UUID = "749be655-6f8c-4afb-b050-5b8721a6e311";

  @BeforeEach
  public void setUp() {
    projectDetails = mock(ReportPortalUser.ProjectDetails.class);
    user = mock(ReportPortalUser.class);

    xunitImportHandler.startDocument();
    xunitImportHandler.withParameters(projectDetails, LAUNCH_ID, user, false);
  }

  @Test
  public void whenStartElement_andQnameIsTestSuite_andItemUuidsAreEmpty_andStartTimeNotNull_thenStartRootItem() {
    String qName = TEST_SUITE;
    String suiteTimestamp = "1700210345";
    Attributes attributes = mock(Attributes.class);
    when(attributes.getValue(XunitReportTag.ATTR_NAME.getValue())).thenReturn(ATTR_NAME);
    when(attributes.getValue(XunitReportTag.TIMESTAMP.getValue())).thenReturn(TIMESTAMP);

    LocalDateTime startSuiteTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(suiteTimestamp)),
            ZoneId.systemDefault()
        );

    LocalDateTime startItemTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(TIMESTAMP)),
            ZoneId.systemDefault()
        );

    setStartSuiteTime(xunitImportHandler, startSuiteTime);

    ItemCreatedRS itemCreatedRS = mock(ItemCreatedRS.class);

    when(itemCreatedRS.getId()).thenReturn(ITEM_UUID);

    when(startTestItemHandler.startRootItem(eq(user), eq(projectDetails), any())).thenReturn(
        itemCreatedRS);

    xunitImportHandler.startElement("", "", qName, attributes);
    verify(startTestItemHandler).startRootItem(
        eq(user), eq(projectDetails), startTestItemRQArgumentCaptor.capture());
    StartTestItemRQ startTestItemRQ = startTestItemRQArgumentCaptor.getValue();
    assertEquals(startTestItemRQ.getLaunchUuid(), LAUNCH_ID);
    assertEquals(startTestItemRQ.getStartTime(), EntityUtils.TO_DATE.apply(startItemTime));
    assertEquals(startTestItemRQ.getType(), TestItemTypeEnum.TEST.name());
    assertEquals(startTestItemRQ.getName(), ATTR_NAME);

  }

  @Test
  public void whenStartElement_andQnameIsTestSuite_andItemUuidsAreEmpty_andStartTimeIsIso_thenStartRootItem() {
    String qName = TEST_SUITE;
    Attributes attributes = mock(Attributes.class);
    when(attributes.getValue(XunitReportTag.ATTR_NAME.getValue())).thenReturn(ATTR_NAME);
    when(attributes.getValue(XunitReportTag.TIMESTAMP.getValue())).thenReturn(ISO_DATE);

    DateTimeFormatter formatter =
        new DateTimeFormatterBuilder().appendOptional(DateTimeFormatter.RFC_1123_DATE_TIME)
            .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME).optionalStart().appendZoneId()
            .optionalEnd().optionalStart().appendLiteral(' ').parseCaseSensitive().appendZoneId()
            .optionalEnd().toFormatter();
    LocalDateTime startItemTime = LocalDateTime.parse(ISO_DATE, formatter);

    LocalDateTime startSuiteTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(TIMESTAMP)),
            ZoneId.systemDefault()
        );

    setStartItemTime(xunitImportHandler, startSuiteTime);

    ItemCreatedRS itemCreatedRS = mock(ItemCreatedRS.class);

    when(itemCreatedRS.getId()).thenReturn(ITEM_UUID);

    when(startTestItemHandler.startRootItem(eq(user), eq(projectDetails), any())).thenReturn(
        itemCreatedRS);

    xunitImportHandler.startElement("", "", qName, attributes);
    verify(startTestItemHandler).startRootItem(
        eq(user), eq(projectDetails), startTestItemRQArgumentCaptor.capture());
    StartTestItemRQ startTestItemRQ = startTestItemRQArgumentCaptor.getValue();
    assertEquals(startTestItemRQ.getLaunchUuid(), LAUNCH_ID);
    assertEquals(startTestItemRQ.getStartTime(), EntityUtils.TO_DATE.apply(startItemTime));
    assertEquals(startTestItemRQ.getType(), TestItemTypeEnum.TEST.name());
    assertEquals(startTestItemRQ.getName(), ATTR_NAME);

  }

  @Test
  public void whenStartElement_andQnameIsTestSuite_andItemUuidsAreNotEmpty_andStartTimeNotNull_thenStartTestItem() {
    String qName = TEST_SUITE;
    Attributes attributes = mock(Attributes.class);
    when(attributes.getValue(XunitReportTag.ATTR_NAME.getValue())).thenReturn(ATTR_NAME);

    LocalDateTime startItemTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(TIMESTAMP)),
            ZoneId.systemDefault()
        );

    setStartItemTime(xunitImportHandler, startItemTime);

    String parentId = "749be655-6f8c-4afb-b050-5b8721a6e311";

    setItemUuids(xunitImportHandler, List.of(parentId));

    ItemCreatedRS itemCreatedRS = mock(ItemCreatedRS.class);

    when(itemCreatedRS.getId()).thenReturn(ITEM_UUID);

    when(startTestItemHandler.startChildItem(eq(user), eq(projectDetails), any(),
        eq(parentId)
    )).thenReturn(itemCreatedRS);

    xunitImportHandler.startElement("", "", qName, attributes);
    verify(startTestItemHandler).startChildItem(
        eq(user), eq(projectDetails), startTestItemRQArgumentCaptor.capture(), eq(parentId));
    StartTestItemRQ startTestItemRQ = startTestItemRQArgumentCaptor.getValue();
    assertEquals(startTestItemRQ.getLaunchUuid(), LAUNCH_ID);
    assertEquals(startTestItemRQ.getStartTime(), EntityUtils.TO_DATE.apply(startItemTime));
    assertEquals(startTestItemRQ.getType(), TestItemTypeEnum.TEST.name());
    assertEquals(startTestItemRQ.getName(), ATTR_NAME);

  }

  @Test
  public void whenStartElement_andQnameIsTestCase_thenStartStepItem() {
    String qName = TEST_CASE;

    Attributes attributes = mock(Attributes.class);
    when(attributes.getValue(XunitReportTag.ATTR_NAME.getValue())).thenReturn(ATTR_NAME);
    when(attributes.getValue(XunitReportTag.START_TIME.getValue())).thenReturn(TIMESTAMP);
    when(attributes.getValue(XunitReportTag.ATTR_TIME.getValue())).thenReturn(DURATION);

    LocalDateTime startItemTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(TIMESTAMP)),
            ZoneId.systemDefault()
        );

    setStartItemTime(xunitImportHandler, startItemTime);

    String parentId = "749be655-6f8c-4afb-b050-5b8721a6e311";

    setItemUuids(xunitImportHandler, List.of(parentId));

    ItemCreatedRS itemCreatedRS = mock(ItemCreatedRS.class);

    when(itemCreatedRS.getId()).thenReturn(ITEM_UUID);

    when(startTestItemHandler.startChildItem(eq(user), eq(projectDetails), any(),
        eq(parentId)
    )).thenReturn(itemCreatedRS);

    xunitImportHandler.startElement("", "", qName, attributes);
    verify(startTestItemHandler).startChildItem(
        eq(user), eq(projectDetails), startTestItemRQArgumentCaptor.capture(), eq(parentId));
    StartTestItemRQ startTestItemRQ = startTestItemRQArgumentCaptor.getValue();
    assertEquals(startTestItemRQ.getLaunchUuid(), LAUNCH_ID);
    assertEquals(startTestItemRQ.getStartTime(), EntityUtils.TO_DATE.apply(startItemTime));
    assertEquals(startTestItemRQ.getType(), TestItemTypeEnum.STEP.name());
    assertEquals(startTestItemRQ.getName(), ATTR_NAME);

  }

  @Test
  public void whenStartElement_andQnameIsFailure_thenStatusIsFailed() {
    // Given
    String startElementQName = "failure";
    Attributes attributes = mock(Attributes.class);
    String endElementQName = "system-err";

    // When
    xunitImportHandler.startElement("", "", startElementQName, attributes);
    xunitImportHandler.characters(new char[] { 'F', 'a', 'i', 'l', 'e', 'd' }, 0, 6);
    xunitImportHandler.endElement("", "", endElementQName);

    // Then
    // Verify that the createLogHandler.createLog() method is called with the expected SaveLogRQ
    verify(createLogHandler).createLog(saveLogRQArgumentCaptor.capture(), any(), any());

    SaveLogRQ capturedSaveLogRQ = saveLogRQArgumentCaptor.getValue();
    assertEquals(LogLevel.ERROR.name(), capturedSaveLogRQ.getLevel(),
        "The log level should be ERROR when handling the FAILURE tag"
    );
  }

  @Test
  public void whenStartElement_andQnameIsError_thenStatusIsFailed() {
    // Given
    String startElementQName = "error";
    Attributes attributes = mock(Attributes.class);
    String endElementQName = "system-err";

    // When
    xunitImportHandler.startElement("", "", startElementQName, attributes);
    xunitImportHandler.characters(new char[] { 'E', 'r', 'r', 'o', 'r' }, 0, 5);
    xunitImportHandler.endElement("", "", endElementQName);

    // Then
    // Verify that the createLogHandler.createLog() method is called with the expected SaveLogRQ
    verify(createLogHandler).createLog(saveLogRQArgumentCaptor.capture(), any(), any());

    SaveLogRQ capturedSaveLogRQ = saveLogRQArgumentCaptor.getValue();
    assertEquals(LogLevel.ERROR.name(), capturedSaveLogRQ.getLevel(),
        "The log level should be ERROR when handling the ERROR tag"
    );
  }

  @Test
  public void whenStartElement_andQnameIsSkipped_thenStatusIsSkipped() {
    // Given
    String startElementQName = "skipped";
    Attributes attributes = mock(Attributes.class);
    String endElementQName = "system-out";

    // When
    xunitImportHandler.startElement("", "", startElementQName, attributes);
    xunitImportHandler.characters(
        new char[] { 'S', 'k', 'i', 'p', 'p', 'e', 'd' }, 0, 7); // Simulate skipped log message
    xunitImportHandler.endElement("", "", endElementQName);

    // Then
    // Verify that the createLogHandler.createLog() method is called with the expected SaveLogRQ
    verify(createLogHandler).createLog(saveLogRQArgumentCaptor.capture(), any(), any());

    SaveLogRQ capturedSaveLogRQ = saveLogRQArgumentCaptor.getValue();
    assertEquals(LogLevel.INFO.name(), capturedSaveLogRQ.getLevel(),
        "The log level should be INFO when handling the SKIPPED tag"
    );
  }

  @Test
  public void whenStartElement_andQnameIsWarning_thenStatusIsWarning() {
    // Given
    String startElementQName = "warning";
    Attributes attributes = mock(Attributes.class);
    String endElementQName = "warning";

    // When
    xunitImportHandler.startElement("", "", startElementQName, attributes);
    xunitImportHandler.characters(
        new char[] { 'W', 'a', 'r', 'n', 'i', 'n', 'g' }, 0, 7); // Simulate warning log message
    xunitImportHandler.endElement("", "", endElementQName);

    // Then
    // Verify that the createLogHandler.createLog() method is called with the expected SaveLogRQ
    verify(createLogHandler).createLog(saveLogRQArgumentCaptor.capture(), any(), any());

    SaveLogRQ capturedSaveLogRQ = saveLogRQArgumentCaptor.getValue();
    assertEquals(LogLevel.WARN.name(), capturedSaveLogRQ.getLevel(),
        "The log level should be WARN when handling the WARNING tag"
    );
  }

  private void setStartSuiteTime(XunitImportHandler xunitImportHandler,
      LocalDateTime startSuiteTime) {
    try {
      Field startSuiteTimeField = XunitImportHandler.class.getDeclaredField("startSuiteTime");
      startSuiteTimeField.setAccessible(true);
      startSuiteTimeField.set(xunitImportHandler, startSuiteTime);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Failed to set startSuiteTime field", e);
    }
  }

  private void setStartItemTime(XunitImportHandler xunitImportHandler,
      LocalDateTime startItemTime) {
    try {
      Field startSuiteTimeField = XunitImportHandler.class.getDeclaredField("startItemTime");
      startSuiteTimeField.setAccessible(true);
      startSuiteTimeField.set(xunitImportHandler, startItemTime);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Failed to set startItemTime field", e);
    }
  }

  private void setItemUuids(XunitImportHandler xunitImportHandler, List<String> itemUuids) {
    try {
      Field itemUuidsField = XunitImportHandler.class.getDeclaredField("itemUuids");
      itemUuidsField.setAccessible(true);
      itemUuidsField.set(xunitImportHandler, new ArrayDeque<>(itemUuids));
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Failed to set itemUuids field", e);
    }
  }
}
