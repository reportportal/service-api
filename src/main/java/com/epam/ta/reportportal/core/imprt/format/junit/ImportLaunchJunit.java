package com.epam.ta.reportportal.core.imprt.format.junit;

import com.epam.ta.reportportal.core.launch.IFinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.IStartLaunchHandler;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class ImportLaunchJunit implements ImportLaunch {

    @Autowired
    private ImportHandlerJunit importHandlerJunit;

    @Autowired
    private IStartLaunchHandler startLaunchHandler;

    @Autowired
    private IFinishLaunchHandler finishLaunchHandler;

    @Autowired
    private LaunchRepository launchRepository;

    @Autowired
    @Qualifier("saveLogsTaskExecutor")
    private TaskExecutor taskExecutor;

    @Override
    public String importLaunch(String projectId, String userName, MultipartFile file) {
        String launchId = startLaunch(projectId, userName, file.getOriginalFilename());
        importHandlerJunit.setProjectId(projectId);
        importHandlerJunit.setUserName(userName);
        importHandlerJunit.setLaunchId(launchId);
        try {
            processFile(file);
        } catch (IOException e) {
            throw new ReportPortalException(e.getMessage());
        }
        finishLaunch(launchId, projectId, userName);
        return launchId;
    }

    private void processFile(MultipartFile file) throws IOException {
        File tmp = File.createTempFile(file.getName(), ".zip");
        file.transferTo(tmp);
        try (ZipFile zipFile = new ZipFile(tmp)){
            Predicate<ZipEntry> isFile = zipEntry -> !zipEntry.isDirectory();
            Predicate<ZipEntry> isXml = zipEntry -> zipEntry.getName().matches(".*xml");
            zipFile.stream()
                    .filter(isFile.and(isXml))
                    .forEach(it -> processZipEntry(zipFile, it));
        }
    }

    private void processZipEntry(ZipFile zipFile, ZipEntry entry) {
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(zipFile.getInputStream(entry), importHandlerJunit);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new ReportPortalException("Problem with processing file " + entry.getName());
        }
    }

    private String startLaunch(String projectId, String userName, String launchName) {
        StartLaunchRQ startLaunchRQ = new StartLaunchRQ();
        //initial start time for launch, will be counted while parsing files
        startLaunchRQ.setStartTime(new Date(0));
        startLaunchRQ.setName(launchName);
        startLaunchRQ.setMode(Mode.DEFAULT);
        return startLaunchHandler.startLaunch(userName, projectId, startLaunchRQ).getId();
    }

    private void finishLaunch(String launchId, String projectId, String userName) {
        FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
        finishExecutionRQ.setEndTime(ImportHandlerJunit.getEndLaunchTime());
        finishLaunchHandler.finishLaunch(launchId, finishExecutionRQ, projectId, userName);
        Launch launch = launchRepository.findOne(launchId);
        launch.setStartTime(ImportHandlerJunit.getStartLaunchTime());
        launchRepository.save(launch);
    }
}
