package com.epam.ta.reportportal.core.tms.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.epam.ta.reportportal.core.tms.controller.TmsDatasetController;
import com.epam.ta.reportportal.core.tms.dto.TmsDatasetRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsDatasetRS;
import com.epam.ta.reportportal.core.tms.service.TmsDatasetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
class TmsDatasetControllerTest {

  @Mock
  private TmsDatasetService tmsDatasetService;

  @InjectMocks
  private TmsDatasetController tmsDatasetController;

  private MockMvc mockMvc;

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc = standaloneSetup(tmsDatasetController).build();
    objectMapper = new ObjectMapper();
  }

  @Test
  void shouldCreateDataset() throws Exception {
    long projectId = 1L;
    var datasetRQ = new TmsDatasetRQ("Dataset1", List.of());
    var expectedResponse = new TmsDatasetRS(1L, "Dataset1", List.of());

    var jsonContent = objectMapper.writeValueAsString(datasetRQ);

    given(tmsDatasetService.create(projectId, datasetRQ)).willReturn(expectedResponse);

    mockMvc
        .perform(post("/project/{projectId}/tms/dataset", projectId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(expectedResponse.getId()))
        .andExpect(jsonPath("$.name").value(expectedResponse.getName()));

    verify(tmsDatasetService).create(projectId, datasetRQ);
  }

  @Test
  void shouldUploadDatasetFromFile() throws Exception {
    long projectId = 1L;
    var dataset1 = new TmsDatasetRS(1L, "Dataset1", List.of());
    var dataset2 = new TmsDatasetRS(2L, "Dataset2", List.of());
    var mockResponse = Arrays.asList(dataset1, dataset2);

    given(tmsDatasetService.uploadFromFile(eq(projectId), any())).willReturn(mockResponse);

    mockMvc
        .perform(multipart("/project/{projectId}/tms/dataset/upload", projectId)
            .file("file", "mockFileContent".getBytes())
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
    verify(tmsDatasetService).uploadFromFile(eq(projectId), any());
  }

  @Test
  void shouldGetDatasetsByProjectId() throws Exception {
    long projectId = 1L;
    var dataset1 = new TmsDatasetRS(1L, "Dataset1", List.of());
    var dataset2 = new TmsDatasetRS(2L, "Dataset2", List.of());
    var mockResponse = Arrays.asList(dataset1, dataset2);

    given(tmsDatasetService.getByProjectId(projectId)).willReturn(mockResponse);

    mockMvc
        .perform(get("/project/{projectId}/tms/dataset", projectId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
    verify(tmsDatasetService).getByProjectId(projectId);
  }

  @Test
  void shouldGetDatasetById() throws Exception {
    long projectId = 1L;
    long datasetId = 100L;
    var mockResponse = new TmsDatasetRS(100L, "Dataset100", List.of());

    given(tmsDatasetService.getById(projectId, datasetId)).willReturn(mockResponse);

    mockMvc
        .perform(get("/project/{projectId}/tms/dataset/{datasetId}", projectId, datasetId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(mockResponse.getId()))
        .andExpect(jsonPath("$.name").value(mockResponse.getName()));
    verify(tmsDatasetService).getById(projectId, datasetId);
  }

  @Test
  void shouldUpdateDataset() throws Exception {
    long projectId = 1L;
    long datasetId = 100L;
    var requestData = new TmsDatasetRQ("UpdatedDataset", List.of());
    var expectedResponse = new TmsDatasetRS(100L, "UpdatedDataset", List.of());

    var jsonContent = objectMapper.writeValueAsString(requestData);

    given(tmsDatasetService.update(eq(projectId), eq(datasetId), eq(requestData)))
        .willReturn(expectedResponse);

    mockMvc
        .perform(put("/project/{projectId}/tms/dataset/{datasetId}", projectId, datasetId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(expectedResponse.getId()))
        .andExpect(jsonPath("$.name").value(expectedResponse.getName()));

    verify(tmsDatasetService).update(projectId, datasetId, requestData);
  }

  @Test
  void shouldPatchDataset() throws Exception {
    long projectId = 1L;
    long datasetId = 100L;
    var requestData = new TmsDatasetRQ("PatchedDataset", List.of());
    var expectedResponse = new TmsDatasetRS(100L, "PatchedDataset", List.of());

    var jsonContent = objectMapper.writeValueAsString(requestData);

    given(tmsDatasetService.patch(eq(projectId), eq(datasetId), eq(requestData)))
        .willReturn(expectedResponse);

    mockMvc
        .perform(patch("/project/{projectId}/tms/dataset/{datasetId}", projectId, datasetId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(expectedResponse.getId()))
        .andExpect(jsonPath("$.name").value(expectedResponse.getName()));

    verify(tmsDatasetService).patch(projectId, datasetId, requestData);
  }

  @Test
  void shouldDeleteDataset() throws Exception {
    long projectId = 1L;
    long datasetId = 100L;

    mockMvc
        .perform(
            delete("/project/{projectId}/tms/dataset/{datasetId}", projectId, datasetId))
        .andExpect(status().isOk());
    verify(tmsDatasetService).delete(projectId, datasetId);
  }
}
