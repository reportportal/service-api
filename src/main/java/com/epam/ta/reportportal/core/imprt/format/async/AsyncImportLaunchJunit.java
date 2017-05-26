package com.epam.ta.reportportal.core.imprt.format.async;

import com.epam.ta.reportportal.core.launch.IFinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.IStartLaunchHandler;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class AsyncImportLaunchJunit implements ImportLaunch {

    @Autowired
    private Provider<XmlParseJob> xmlParseJobProvider;

    @Autowired
    private IStartLaunchHandler startLaunchHandler;

    @Autowired
    private IFinishLaunchHandler finishLaunchHandler;

    @Autowired
    private LaunchRepository launchRepository;

    private static final ExecutorService service = Executors.newFixedThreadPool(5);

    private static final String XML_REGEX = ".*xml";

    private final static Predicate<ZipEntry> isFile = zipEntry -> !zipEntry.isDirectory();
    private final static Predicate<ZipEntry> isXml = zipEntry -> zipEntry.getName().matches(XML_REGEX);

    @Override
    public String importLaunch(String projectId, String userName, MultipartFile file) {
        try {
            File tmp = File.createTempFile(file.getName(), ".zip");
            file.transferTo(tmp);
            String launchId = startLaunch(projectId, userName, file.getOriginalFilename());
            processZipFile(tmp, projectId, userName, launchId);
            finishLaunch(launchId, projectId, userName);
            return launchId;
        } catch (IOException e) {
            throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, file.getName(), e);
        }
    }

    private void processZipFile(File zip, String projectId, String userName, String launchId) throws IOException {
        try (ZipFile zipFile = new ZipFile(zip)){
            CompletableFuture[] futures = zipFile.stream().filter(isFile.and(isXml)).map(zipEntry -> {
                try {
                    XmlParseJob job = xmlParseJobProvider.get().withParameters(projectId, launchId, userName, zipFile.getInputStream(zipEntry));
                    return CompletableFuture.runAsync(job, service);
                } catch (IOException e) {
                    throw new ReportPortalException("There was a problem while parsing file : " + zipEntry.getName(), e);
                }
            }).toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(futures).get(5, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new ReportPortalException("There was a problem while importing", e);
        }
    }

    private String startLaunch(String projectId, String userName, String launchName) {
        StartLaunchRQ startLaunchRQ = new StartLaunchRQ();
        startLaunchRQ.setStartTime(new Date(0));
        startLaunchRQ.setName(launchName);
        startLaunchRQ.setMode(Mode.DEFAULT);
        return startLaunchHandler.startLaunch(userName, projectId, startLaunchRQ).getId();
    }

    private void finishLaunch(String launchId, String projectId, String userName) {
        FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
        finishExecutionRQ.setEndTime(AsyncXmlImportHandler.getEndLaunchTime());
        finishLaunchHandler.finishLaunch(launchId, finishExecutionRQ, projectId, userName);
        Launch launch = launchRepository.findOne(launchId);
        launch.setStartTime(AsyncXmlImportHandler.getStartLaunchTime());
        launchRepository.save(launch);
    }
}
