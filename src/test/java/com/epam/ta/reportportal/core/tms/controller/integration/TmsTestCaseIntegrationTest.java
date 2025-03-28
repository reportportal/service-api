package com.epam.ta.reportportal.core.tms.controller.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestCaseRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * Integration tests for TestCaseController
 */
@Sql("/db/tms/tms-test-case/tms-test-case-fill.sql")
@ExtendWith(MockitoExtension.class)
public class TmsTestCaseIntegrationTest extends BaseMvcTest {

    @Autowired
    private TmsTestCaseRepository testCaseRepository;

    @Test
    void createTestCaseIntegrationTest() throws Exception {
        // Given
        TmsTestCaseAttributeRQ attribute = new TmsTestCaseAttributeRQ();
        attribute.setValue("value3");
        attribute.setAttributeId(3L);

        TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
        testCaseRQ.setName("Test Case 3");
        testCaseRQ.setDescription("Description for test case 3");
        testCaseRQ.setTestFolderId(3L);
        testCaseRQ.setTags(List.of(attribute));

        ObjectMapper mapper = new ObjectMapper();
        String jsonContent = mapper.writeValueAsString(testCaseRQ);

        // When
        mockMvc.perform(post("/project/3/tms/test-case")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
            .andExpect(status().isOk());

        // Then
        Optional<TmsTestCase> testCase = testCaseRepository.findById(1L);

        assertTrue(testCase.isPresent());
        assertEquals(testCaseRQ.getName(), testCase.get().getName());
        assertEquals(testCaseRQ.getDescription(), testCase.get().getDescription());
        assertEquals(testCaseRQ.getTestFolderId(), testCase.get().getTestFolder().getId());
    }

    @Test
    void getTestCaseByIdIntegrationTest() throws Exception {
        // Given
        Optional<TmsTestCase> testCase = testCaseRepository.findById(4L);

        // When/Then
        mockMvc.perform(get("/project/4/tms/test-case/4")
                .with(token(oAuthHelper.getSuperadminToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(testCase.get().getId()))
            .andExpect(jsonPath("$.name").value(testCase.get().getName()))
            .andExpect(jsonPath("$.description").value(testCase.get().getDescription()));
    }

    @Test
    void getTestCaseByProjectIdIntegrationTest() throws Exception {
        // Given
        Optional<TmsTestCase> testCase = testCaseRepository.findById(4L);

        // When/Then
        mockMvc.perform(get("/project/4/tms/test-case")
                .with(token(oAuthHelper.getSuperadminToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(testCase.get().getId()))
            .andExpect(jsonPath("$[0].name").value(testCase.get().getName()))
            .andExpect(jsonPath("$[0].description").value(testCase.get().getDescription()));
    }

    @Test
    void updateTestCaseIntegrationTest() throws Exception {
        // Given
        TmsTestCaseAttributeRQ attribute = new TmsTestCaseAttributeRQ();
        attribute.setValue("value4");
        attribute.setAttributeId(4L);

        TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
        testCaseRQ.setName("Updated Test Case 5");
        testCaseRQ.setDescription("Updated description for test case 5");
        testCaseRQ.setTestFolderId(5L);
        testCaseRQ.setTags(List.of(attribute));

        ObjectMapper mapper = new ObjectMapper();
        String jsonContent = mapper.writeValueAsString(testCaseRQ);

        // When
        mockMvc.perform(put("/project/5/tms/test-case/5")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
            .andExpect(status().isOk());

        // Then
        Optional<TmsTestCase> testCase = testCaseRepository.findById(5L);

        assertTrue(testCase.isPresent());
        assertEquals(testCaseRQ.getName(), testCase.get().getName());
        assertEquals(testCaseRQ.getDescription(), testCase.get().getDescription());
        assertEquals(testCaseRQ.getTestFolderId(), testCase.get().getTestFolder().getId());
    }

    @Test
    void patchTestCaseIntegrationTest() throws Exception {
        // Given
        TmsTestCaseAttributeRQ attribute = new TmsTestCaseAttributeRQ();
        attribute.setValue("value6");
        attribute.setAttributeId(6L);
        TmsTestCaseRQ testCaseRQ = new TmsTestCaseRQ();
        testCaseRQ.setName("Patched Test Case 6");
        testCaseRQ.setDescription("Patched description for test case 6");
        testCaseRQ.setTestFolderId(6L);
        testCaseRQ.setTags(List.of(attribute));

        ObjectMapper mapper = new ObjectMapper();
        String jsonContent = mapper.writeValueAsString(testCaseRQ);

        // When
        mockMvc.perform(patch("/project/6/tms/test-case/6")
                .contentType("application/json")
                .content(jsonContent)
                .with(token(oAuthHelper.getSuperadminToken())))
            .andExpect(status().isOk());

        // Then
        Optional<TmsTestCase> testCase = testCaseRepository.findById(6L);

        assertTrue(testCase.isPresent());
        assertEquals(testCaseRQ.getName(), testCase.get().getName());
        assertEquals(testCaseRQ.getDescription(), testCase.get().getDescription());
        assertEquals(testCaseRQ.getTestFolderId(), testCase.get().getTestFolder().getId());
    }
}
