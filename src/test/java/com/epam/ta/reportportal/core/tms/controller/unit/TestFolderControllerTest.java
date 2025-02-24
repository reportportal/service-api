package com.epam.ta.reportportal.core.tms.controller.unit;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.epam.ta.reportportal.core.tms.controller.TestFolderController;
import com.epam.ta.reportportal.core.tms.dto.TestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TestFolderRS;
import com.epam.ta.reportportal.core.tms.service.TestFolderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.List;

public class TestFolderControllerTest {

  @Mock
  private TestFolderService testFolderService;

  @InjectMocks
  private TestFolderController testFolderController;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    mockMvc = standaloneSetup(testFolderController).build();
  }

  @Test
  public void testCreateTestFolder() throws Exception {
    long projectId = 1L;
    TestFolderRQ request = new TestFolderRQ("name", "doc");
    TestFolderRS expectedResponse = new TestFolderRS(1L, "name", "doc");
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    given(testFolderService.createFolder(projectId, request)).willReturn(expectedResponse);

    mockMvc.perform(post("/project/{projectId}/tms/folder", projectId)
                        .contentType("application/json")
                        .content(jsonContent))
                .andExpect(status().isOk());

    verify(testFolderService).createFolder(projectId, request);
  }

  @Test
  public void testUpdateTestFolder() throws Exception {
    long projectId = 1L;
    long folderId = 2L;
    TestFolderRQ request = new TestFolderRQ("name", "doc");
    TestFolderRS expectedResponse = new TestFolderRS(1L, "name", "doc");
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    given(testFolderService.updateFolder(projectId, folderId, request))
             .willReturn(expectedResponse);

    mockMvc.perform(put("/project/{projectId}/tms/folder/{folderId}", projectId, folderId)
                        .contentType("application/json")
                        .content(jsonContent))
                .andExpect(status().isOk());

    verify(testFolderService).updateFolder(projectId, folderId, request);
  }

  @Test
  public void testGetTestFolderById() throws Exception {
    long projectId = 1L;
    long folderId = 2L;
    TestFolderRS expectedResponse = new TestFolderRS(1L, "name", "doc");

    given(testFolderService.getFolderById(folderId)).willReturn(expectedResponse);

    mockMvc.perform(get("/project/{projectId}/tms/folder/{folderId}", projectId, folderId))
                .andExpect(status().isOk());

    verify(testFolderService).getFolderById(folderId);
  }

  @Test
  public void testGetTestFolderByProjectId() throws Exception {
    long projectId = 1L;
    TestFolderRS expectedResponse1 = new TestFolderRS(1L, "name", "doc");
    TestFolderRS expectedResponse2 = new TestFolderRS(1L, "name", "doc");
    List<TestFolderRS> expectedResponse = Arrays.asList(expectedResponse1, expectedResponse2);

    given(testFolderService.getFolderByProjectID(projectId)).willReturn(expectedResponse);

    mockMvc.perform(get("/project/{projectId}/tms/folder/", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

    verify(testFolderService).getFolderByProjectID(projectId);
  }
}
