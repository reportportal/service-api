package com.epam.ta.reportportal.core.imprt.format.async;

import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.core.launch.IFinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.IStartLaunchHandler;
import com.epam.ta.reportportal.core.log.ICreateLogHandler;
import com.epam.ta.reportportal.database.entity.LogLevel;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.TestItemType;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.Optional;

import static com.epam.ta.reportportal.core.imprt.format.async.JunitReportTag.*;

public class AsyncXmlImportHandler extends DefaultHandler {

    @Autowired
    private IStartLaunchHandler startLaunchHandler;

    @Autowired
    private IFinishLaunchHandler finishLaunchHandler;

    @Autowired
    private StartTestItemHandler startTestItemHandler;

    @Autowired
    private FinishTestItemHandler finishTestItemHandler;

    @Autowired
    private ICreateLogHandler createLogHandler;

    private String projectId;
    private String userName;
    private String launchId;

    private Deque<String> itemsIds;
    private Status status;
    private StringBuilder message;
    private LocalDateTime time;
    private long currentDuration;

    private static long fullDuration;
    private static LocalDateTime startLaunchTime;

    @Override
    public void startDocument() throws SAXException {
        itemsIds = new ArrayDeque<>();
        message = new StringBuilder();
        startLaunchTime = LocalDateTime.now();
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (fromString(qName)) {
            case TESTSUITE:
                startRootItem(attributes.getValue(ATTR_NAME.getValue()), attributes.getValue(TIMESTAMP.getValue()));
                break;
            case TESTCASE:
                startTestItem(attributes.getValue(ATTR_NAME.getValue()), attributes.getValue(ATTR_TIME.getValue()));
                break;
            case FAILURE:
                message = new StringBuilder("");
                status = Status.FAILED;
                break;
            case SKIPPED:
                message = new StringBuilder("");
                status = Status.SKIPPED;
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (fromString(qName)) {
            case TESTSUITE:
                finishRootItem();
                break;
            case TESTCASE:
                finishTestItem();
                break;
            case SKIPPED:
                attachLog(LogLevel.ERROR);
                break;
            case FAILURE:
                attachLog(LogLevel.ERROR);
                break;
            case SYSTEM_OUT:
                attachLog(LogLevel.DEBUG);
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        message.append(new String(ch, start, length));
    }

    private void attachLog(LogLevel logLevel) {
        if (null != message && message.length() != 0) {
            SaveLogRQ saveLogRQ = new SaveLogRQ();
            saveLogRQ.setLevel(logLevel.name());
            saveLogRQ.setLogTime(toDate(time));
            saveLogRQ.setMessage(message.toString());
            saveLogRQ.setTestItemId(itemsIds.getFirst());
            createLogHandler.createLog(saveLogRQ, null, null, projectId);
        }
    }

    private void startRootItem(String name, String timestamp) {
        if (null != timestamp) {
            time = LocalDateTime.parse(timestamp);
            if (startLaunchTime.isAfter(time)) {
                startLaunchTime = LocalDateTime.of(time.toLocalDate(), time.toLocalTime());
            }
        } else {
            time = LocalDateTime.now();
        }
        StartTestItemRQ rq = new StartTestItemRQ();
        rq.setLaunchId(launchId);
        rq.setStartTime(toDate(time));
        rq.setType(TestItemType.TEST.name());
        rq.setName(name);
        String id = startTestItemHandler.startRootItem(projectId, rq).getId();
        itemsIds.push(id);
    }

    private void startTestItem(String name, String duration) {
        StartTestItemRQ rq = new StartTestItemRQ();
        rq.setLaunchId(launchId);
        rq.setStartTime(toDate(time));
        rq.setType(TestItemType.STEP.name());
        rq.setName(name);
        String id = startTestItemHandler.startChildItem(rq, itemsIds.peekLast()).getId();
        currentDuration = toMillis(duration);
        itemsIds.push(id);
    }

    private void finishRootItem() {
        FinishTestItemRQ rq = new FinishTestItemRQ();
        rq.setEndTime(toDate(time));
        rq.setStatus(Optional.ofNullable(status).orElse(Status.PASSED).name());
        finishTestItemHandler.finishTestItem(itemsIds.poll(), rq, userName);
        status = null;
    }

    private void finishTestItem() {
        FinishTestItemRQ rq = new FinishTestItemRQ();
        time = time.plus(currentDuration, ChronoUnit.MILLIS);
        fullDuration += currentDuration;
        rq.setEndTime(toDate(time));
        rq.setStatus(Optional.ofNullable(status).orElse(Status.PASSED).name());
        finishTestItemHandler.finishTestItem(itemsIds.poll(), rq, userName);
        status = null;
    }

    AsyncXmlImportHandler withParameters(String projectId, String launchId, String user){
        this.projectId = projectId;
        this.launchId = launchId;
        this.userName = user;
        return this;
    }

    private static Date toDate(LocalDateTime startTime) {
        return Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private long toMillis(String duration) {
        Double value = Double.valueOf(duration) * 1000;
        return value.longValue();
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setLaunchId(String launchId) {
        this.launchId = launchId;
    }

    public String getLaunchId() {
        return launchId;
    }

    static Date getEndLaunchTime() {
        return toDate(startLaunchTime.plus(fullDuration, ChronoUnit.MILLIS));
    }

    static Date getStartLaunchTime() {
        return toDate(startLaunchTime);
    }
}
