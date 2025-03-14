package com.epam.ta.reportportal.core.tms.controller.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.ta.reportportal.core.tms.db.repository.TmsDatasetRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsDatasetRQ;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;

@Sql("/db/tms/dataset/tms_dataset_fill.sql")
class TmsDatasetIntegrationTest extends BaseMvcTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  @Autowired
  private TmsDatasetRepository tmsDatasetRepository;

  @Test
  @SneakyThrows
  void createDatasetIntegrationTest() {
    var requestPayload = new TmsDatasetRQ();
    requestPayload.setName("Dataset1");
    var jsonPayload = objectMapper.writeValueAsString(requestPayload);

    mockMvc
        .perform(post("/project/31/tms/dataset")
            .contentType("application/json")
            .content(jsonPayload)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Dataset1")); // Assert

    // Verify the dataset was saved in the repository
    var tmsDataset = tmsDatasetRepository.findByName("Dataset1");

    assertTrue(tmsDataset.isPresent());
    assertEquals("Dataset1", tmsDataset.get().getName());
    assertEquals(31L, tmsDataset.get().getProject().getId());
  }

  @Test
  @SneakyThrows
  void getDatasetsByProjectIdIntegrationTest() {
    mockMvc
        .perform(get("/project/31/tms/dataset")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("Dataset10001"))
        .andExpect(jsonPath("$[1].name").value("Dataset10002"));
  }

  @Test
  @SneakyThrows
  void getDatasetByIdIntegrationTest() {
    var tmsDataset = tmsDatasetRepository.findById(10001L);

    // Act & Assert
    mockMvc
        .perform(get("/project/31/tms/dataset/10001")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(tmsDataset.get().getId()))
        .andExpect(jsonPath("$.name").value(tmsDataset.get().getName()));
  }

  @Test
  @SneakyThrows
  void updateDatasetIntegrationTest() {
    var updatedPayload = new TmsDatasetRQ();
    updatedPayload.setName("UpdatedDataset");

    var jsonPayload = objectMapper.writeValueAsString(updatedPayload);

    mockMvc
        .perform(put("/project/31/tms/dataset/10001")
            .contentType("application/json")
            .content(jsonPayload)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("UpdatedDataset"));

    var tmsDataset = tmsDatasetRepository.findById(10001L);
    assertTrue(tmsDataset.isPresent());
    assertEquals("UpdatedDataset", tmsDataset.get().getName());
  }

  @Test
  @SneakyThrows
  void patchDatasetIntegrationTest() {
    var patchPayload = new TmsDatasetRQ();
    patchPayload.setName("PatchedDataset");
    var jsonPayload = objectMapper.writeValueAsString(patchPayload);

    mockMvc
        .perform(patch("/project/31/tms/dataset/10001")
            .contentType("application/json")
            .content(jsonPayload)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("PatchedDataset"));

    var tmsDataset = tmsDatasetRepository.findById(10001L);
    assertTrue(tmsDataset.isPresent());
    assertEquals("PatchedDataset", tmsDataset.get().getName());
  }

  @Test
  @SneakyThrows
  void deleteDatasetIntegrationTest() {
    mockMvc
        .perform(delete("/project/31/tms/dataset/10001")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    var tmsDataset = tmsDatasetRepository.findById(10001L);
    assertTrue(tmsDataset.isEmpty());
  }

  @Test
  void uploadDatasetFromFileIntegrationTest() throws Exception {
    var fileContent = "name,key,value\nDataset20,Key2,Value2\nDataset30,Key3,Value3";
    var mockMultipartFile = new MockMultipartFile(
        "file",         // Name of the file in the FormData (matches the controller @RequestPart)
        "data.csv",     // The actual file name
        MediaType.MULTIPART_FORM_DATA_VALUE, // Content type of the file
        fileContent.getBytes()      // Content of the file as bytes
    );
    mockMvc
        .perform(multipart("/project/32/tms/dataset/upload")
            .file(mockMultipartFile)
            .contentType(MediaType.MULTIPART_FORM_DATA) // Specify multipart content type
            .header("Content-Disposition", "form-data; name=\"file\"; filename=\"data.csv\"")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("Dataset20"))
        .andExpect(jsonPath("$[1].name").value("Dataset30"));
  }
}
