/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

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
        when(dataStorageService.saveData(any(BinaryData.class),
                    anyString(), anyMap())).thenReturn("not null");
        saveBinData.withBinaryData(BIN_DATA)
                   .withProject(PROJECT_NAME)
                   .withFilename(FILE_NAME)
                   .withLog(LOG)
                   .run();
        verify(logRepository, times(1))
                   .save(LOG);
        verify(dataStorageService, times(2))
                   .saveData(any(BinaryData.class), anyString(), anyMap());
        }

    @Test
    public void runTestWithContentTypeNotEqualsImage() {
        String contentType = "binary";
        BinaryData binData = new BinaryData(contentType, LENGHT_FOR_BIN_DATA, INPUT_STREAM);
        saveBinData.withBinaryData(binData)
                   .withProject(PROJECT_NAME)
                   .withLog(LOG)
                   .run();
        verify(logRepository, times(1))
                   .save(LOG);
    }


}
