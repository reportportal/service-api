package com.epam.ta.reportportal.core.tms.controller.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.ta.reportportal.core.tms.db.entity.TmsProductVersion;
import com.epam.ta.reportportal.core.tms.db.repository.ProductVersionRepository;
import com.epam.ta.reportportal.core.tms.dto.ProductVersionRQ;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import java.util.Optional;
import java.util.Set;

@Sql("/db/tms/tms-product-version/tms-product-version-fill.sql")
@ExtendWith(MockitoExtension.class)
class ProductVersionIntegrationTest extends BaseMvcTest {

  @Autowired
  private ProductVersionRepository productVersionRepository;

  // @Test TODO organize the tables milestone and environment
  void createVersionIntegrationTest() throws Exception {
    ProductVersionRQ request = new ProductVersionRQ(1L, "version1", "documentation1",
                                                    Set.of(2L), Set.of(2L));
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);
    mockMvc.perform(post("/project/1/tms/productversion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent)
                        .with(token(oAuthHelper.getSuperadminToken())))
                        .andExpect(status().isOk());

    Optional<TmsProductVersion> productVersion = productVersionRepository.findById(1L);
    assertTrue(productVersion.isPresent());
    assertEquals(request.id(), productVersion.get().getId());
    assertEquals(request.version(), productVersion.get().getVersion());
    assertEquals(request.documentation(), productVersion.get().getDocumentation());
  }

  //@Test TODO organize the tables milestone and environment
  void updateVersionIntegrationTest() throws Exception {
    ProductVersionRQ request = new ProductVersionRQ(3L, "versionUpdated", "docUpdated",
                                                    Set.of(1L), Set.of(1L));
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    mockMvc.perform(put("/project/3/tms/productversion/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent)
                        .with(token(oAuthHelper.getSuperadminToken())))
                .andExpect(status().isOk());

    Optional<TmsProductVersion> productVersion = productVersionRepository.findById(3L);
    assertTrue(productVersion.isPresent());
    assertEquals(request.id(), productVersion.get().getId());
    assertEquals(request.version(), productVersion.get().getVersion());
    assertEquals(request.documentation(), productVersion.get().getDocumentation());
  }

  //@Test TODO organize the tables milestone and environment
  void deleteProductVersionIntegrationTest() throws Exception {
    mockMvc.perform(delete("/project/4/tms/productversion/4")
                    .with(token(oAuthHelper.getSuperadminToken())))
            .andExpect(status().isOk());
    assertFalse(productVersionRepository.findById(4L).isPresent());
  }

  //@Test TODO organize the tables milestone and environment
  void getByIdIntegrationTest() throws Exception {
    Optional<TmsProductVersion> productVersion = productVersionRepository.findById(5L);

    mockMvc.perform(get("/project/5/tms/productversion/5")
            .with(token(oAuthHelper.getSuperadminToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(productVersion.get().getId()))
            .andExpect(jsonPath("$.version").value(productVersion.get().getVersion()))
            .andExpect(jsonPath("$.documentation").value(productVersion.get().getDocumentation()));
  }
}