package com.epam.ta.reportportal.core.tms.controller.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseVersion;
import com.epam.ta.reportportal.core.tms.db.repository.TestCaseRepository;
import com.epam.ta.reportportal.core.tms.dto.AttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.TestCaseRQ;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import java.util.List;
import java.util.Optional;

@Sql("/db/tms/tms-product-version/tms-test-case-fill.sql")
@ExtendWith(MockitoExtension.class)
class TestCaseIntegrationTest extends BaseMvcTest {

  @Autowired
  private TestCaseRepository testCaseRepository;

  @Test
  void getTestCaseByIdIntegrationTest() throws Exception {
    Optional<TmsTestCase> testCase = testCaseRepository.findById(3L);
    TmsTestCaseVersion[] versionArray = testCase.get().getVersions()
                                        .toArray(new TmsTestCaseVersion[0]);

    mockMvc.perform(get("/project/3/tms/testcase/3")
            .with(token(oAuthHelper.getSuperadminToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(testCase.get().getId()))
            .andExpect(jsonPath("$.name").value(testCase.get().getName()))
            .andExpect(jsonPath("$.description").value(testCase.get().getDescription()))
            .andExpect(jsonPath("$.testCaseVersions[0].id").value(versionArray[0].getId()))
            .andExpect(jsonPath("$.testCaseVersions[0].name").value(versionArray[0].getName()))
            .andExpect(jsonPath("$.testCaseVersions[0].isDefault").value(versionArray[0]
                                                                            .isDefault()))
            .andExpect(jsonPath("$.testCaseVersions[0].isDraft").value(versionArray[0].isDraft()));
  }

  @Test
  void createTestCaseIntegrationTest() throws Exception {
    AttributeRQ attribute = new AttributeRQ("key", "value");
    TestCaseRQ requestData = new TestCaseRQ("name_create", "value_create", 3L, List.of(attribute));
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(requestData);

    mockMvc.perform(post("/project/3/tms/testcase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent)
                        .with(token(oAuthHelper.getSuperadminToken())))
                        .andExpect(status().isOk());

    Optional<TmsTestCase> testCase = testCaseRepository.findById(1L);
    assertTrue(testCase.isPresent());
    assertEquals(requestData.name(), testCase.get().getName());
    assertEquals(requestData.description(), testCase.get().getDescription());
  }
}
