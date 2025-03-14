package com.epam.ta.reportportal.core.tms.controller.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolder;
import com.epam.ta.reportportal.core.tms.db.repository.TestFolderRepository;
import com.epam.ta.reportportal.core.tms.dto.TestFolderRQ;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql("/db/tms/tms-product-version/tms-test-folder-fill.sql")
@ExtendWith(MockitoExtension.class)
class TestFolderIntegrationTest extends BaseMvcTest {

  @Autowired
  private TestFolderRepository testFolderRepository;

  @Test
  void createTestFolderIntegrationTest() throws Exception {
    TestFolderRQ request = new TestFolderRQ("name_create", "description_create");
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    mockMvc.perform(post("/project/31/tms/folder")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
    Optional<TmsTestFolder> folder = testFolderRepository.findById(1L);
    assertTrue(folder.isPresent());
    assertEquals(request.name(), folder.get().getName());
    assertEquals(request.description(), folder.get().getDescription());
    assertEquals(31L, folder.get().getProjectId());
  }

  @Test
  void updateTestFolderIntegrationTest() throws Exception {
    TestFolderRQ request = new TestFolderRQ("name_updated", "description_updated");
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    mockMvc.perform(put("/project/31/tms/folder/3")
            .contentType("application/json")
            .content(jsonContent)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    Optional<TmsTestFolder> folder = testFolderRepository.findById(3L);
    assertTrue(folder.isPresent());
    assertEquals(request.name(), folder.get().getName());
    assertEquals(request.description(), folder.get().getDescription());
    assertEquals(31L, folder.get().getProjectId());
  }

  @Test
  void getTestFolderByIdIntegrationTest() throws Exception {
    Optional<TmsTestFolder> folder = testFolderRepository.findById(4L);

    mockMvc.perform(get("/project/31/tms/folder/4")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(folder.get().getId()))
        .andExpect(jsonPath("$.name").value(folder.get().getName()))
        .andExpect(jsonPath("$.description").value(folder.get().getDescription()));
  }

  @Test
  void testGetTestFolderByProjectId() throws Exception {
    Optional<TmsTestFolder> folder = testFolderRepository.findById(5L);

    mockMvc.perform(get("/project/35/tms/folder/")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.[0].id").value(folder.get().getId()))
        .andExpect(jsonPath("$.[0].name").value(folder.get().getName()))
        .andExpect(jsonPath("$.[0].description").value(folder.get().getDescription()));
  }
}
