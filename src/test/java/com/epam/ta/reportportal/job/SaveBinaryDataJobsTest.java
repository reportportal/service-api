package com.epam.ta.reportportal.job;

import com.epam.reportportal.commons.Thumbnailator;
import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.BinaryData;
import com.epam.ta.reportportal.database.DataStorage;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.entity.Log;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SaveBinaryDataJobsTest extends BaseTest {

    @Mock
    private DataStorage dataStorageService;

    @Mock
    private BinaryData binaryData;

    @Mock
    private LogRepository logRepository;

    @Mock
    private Thumbnailator thumbnailator;

    @InjectMocks
    private SaveBinaryDataJob saveBinData = new SaveBinaryDataJob();

    private final String CONTENT_TYPE = "image";
    private final String PROJECT_NAME = "project";
    private final String FILE_NAME = "filename.fln";
    private final Long LENGHT_FOR_BIN_DATA = 1L;
    private final Log LOG = new Log();
    private final InputStream INPUT_STREAM = new ByteArrayInputStream(CONTENT_TYPE.getBytes(StandardCharsets.UTF_8));
    private final BinaryData BIN_DATA = new BinaryData(CONTENT_TYPE, LENGHT_FOR_BIN_DATA, INPUT_STREAM);

    @Test
    public void runTestWithContentTypeEqualsImage() throws IOException {
        byte[] byteArr = {116, 101};
        when(binaryData.getContentType()).thenReturn(CONTENT_TYPE);
        when(thumbnailator.createThumbnail(any(byte[].class))).thenReturn(byteArr);
        when(dataStorageService.saveData(any(BinaryData.class), anyString(), anyMap())).thenReturn("not null");
        saveBinData.withBinaryData(BIN_DATA).withProject(PROJECT_NAME).withFilename(FILE_NAME).withLog(LOG).run();
        verify(logRepository, times(1)).save(LOG);
        verify(dataStorageService, times(2)).saveData(any(BinaryData.class), anyString(), anyMap());
        }

    @Test
    public void runTestWithContentTypeNotEqualsImage() {
        String contentType = "binary";
        BinaryData binData = new BinaryData(contentType, LENGHT_FOR_BIN_DATA, INPUT_STREAM);
        saveBinData.withBinaryData(binData).withProject(PROJECT_NAME).withLog(LOG).run();
        verify(logRepository, times(1)).save(LOG);
    }


}
