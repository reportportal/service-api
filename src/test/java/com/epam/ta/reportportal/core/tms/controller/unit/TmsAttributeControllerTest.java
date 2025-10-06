package com.epam.ta.reportportal.core.tms.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.tms.controller.TmsAttributeController;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRS;
import com.epam.ta.reportportal.core.tms.service.TmsAttributeService;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.ws.resolver.FilterCriteriaResolver;
import com.epam.ta.reportportal.ws.resolver.OffsetArgumentResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class TmsAttributeControllerTest {

  @Mock
  private TmsAttributeService tmsAttributeService;

  @Mock
  private Page<TmsAttributeRS> mockPage;

  @InjectMocks
  private TmsAttributeController tmsAttributeController;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    objectMapper = new ObjectMapper();

    // Configure MockMvc with a custom argument resolver for Pageable
    mockMvc = standaloneSetup(tmsAttributeController)
        .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
          @Override
          public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().equals(Pageable.class);
          }

          @Override
          public Object resolveArgument(MethodParameter parameter,
              ModelAndViewContainer mavContainer,
              NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return Pageable.unpaged();
          }
        },
        new OffsetArgumentResolver(),
        new FilterCriteriaResolver())
        .build();
  }

  @Test
  void shouldCreateAttribute() throws Exception {
    // Given
    var attributeRequest = createAttributeRequest();
    var attributeResponse = createAttributeResponse();
    var jsonContent = objectMapper.writeValueAsString(attributeRequest);

    given(tmsAttributeService.create(any(TmsAttributeRQ.class))).willReturn(attributeResponse);

    // When/Then
    mockMvc.perform(post("/v1/tms/attribute")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.key").value("test-key"));

    verify(tmsAttributeService).create(any(TmsAttributeRQ.class));
  }

  @Test
  void shouldPatchAttribute() throws Exception {
    // Given
    var attributeId = 1L;
    var attributeRequest = createAttributeRequest();
    var updatedResponse = createUpdatedAttributeResponse();
    var jsonContent = objectMapper.writeValueAsString(attributeRequest);

    given(tmsAttributeService.patch(eq(attributeId), any(TmsAttributeRQ.class))).willReturn(updatedResponse);

    // When/Then
    mockMvc.perform(patch("/v1/tms/attribute/{attributeId}", attributeId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(attributeId))
        .andExpect(jsonPath("$.key").value("updated-key"));

    verify(tmsAttributeService).patch(eq(attributeId), any(TmsAttributeRQ.class));
  }

  @Test
  void shouldGetAllAttributes() throws Exception {
    // Given
    given(tmsAttributeService.getAll(any(Filter.class), any(Pageable.class))).willReturn(mockPage);

    // When/Then
    mockMvc.perform(get("/v1/tms/attribute")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(tmsAttributeService).getAll(any(Filter.class), any(Pageable.class));
  }

  @Test
  void shouldGetAttributeById() throws Exception {
    // Given
    var attributeId = 1L;
    var attributeResponse = createAttributeResponse();

    given(tmsAttributeService.getById(attributeId)).willReturn(attributeResponse);

    // When/Then
    mockMvc.perform(get("/v1/tms/attribute/{attributeId}", attributeId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(attributeId))
        .andExpect(jsonPath("$.key").value("test-key"));

    verify(tmsAttributeService).getById(attributeId);
  }

  @Test
  void shouldCreateAttributeWithNullKey() throws Exception {
    // Given
    var requestWithNullKey = TmsAttributeRQ.builder()
        .key(null)
        .build();
    var responseWithNullKey = createAttributeResponseWithKey(null);
    var jsonContent = objectMapper.writeValueAsString(requestWithNullKey);

    given(tmsAttributeService.create(any(TmsAttributeRQ.class))).willReturn(responseWithNullKey);

    // When/Then
    mockMvc.perform(post("/v1/tms/attribute")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1L));

    verify(tmsAttributeService).create(any(TmsAttributeRQ.class));
  }

  @Test
  void shouldPatchAttributeWithNullKey() throws Exception {
    // Given
    var attributeId = 1L;
    var requestWithNullKey = TmsAttributeRQ.builder()
        .key(null)
        .build();
    var responseWithNullKey = createAttributeResponseWithKey(null);
    var jsonContent = objectMapper.writeValueAsString(requestWithNullKey);

    given(tmsAttributeService.patch(eq(attributeId), any(TmsAttributeRQ.class))).willReturn(responseWithNullKey);

    // When/Then
    mockMvc.perform(patch("/v1/tms/attribute/{attributeId}", attributeId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(attributeId));

    verify(tmsAttributeService).patch(eq(attributeId), any(TmsAttributeRQ.class));
  }

  @Test
  void shouldCallServiceWithCorrectParametersForCreate() throws Exception {
    // Given
    var customRequest = TmsAttributeRQ.builder()
        .key("custom-key")
        .build();
    var attributeResponse = createAttributeResponse();
    var jsonContent = objectMapper.writeValueAsString(customRequest);

    given(tmsAttributeService.create(any(TmsAttributeRQ.class))).willReturn(attributeResponse);

    // When/Then
    mockMvc.perform(post("/v1/tms/attribute")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isCreated());

    verify(tmsAttributeService).create(any(TmsAttributeRQ.class));
  }

  @Test
  void shouldCallServiceWithCorrectParametersForPatch() throws Exception {
    // Given
    var attributeId = 999L;
    var customRequest = TmsAttributeRQ.builder()
        .key("custom-key")
        .build();
    var attributeResponse = createAttributeResponse();
    var jsonContent = objectMapper.writeValueAsString(customRequest);

    given(tmsAttributeService.patch(eq(attributeId), any(TmsAttributeRQ.class))).willReturn(attributeResponse);

    // When/Then
    mockMvc.perform(patch("/v1/tms/attribute/{attributeId}", attributeId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(tmsAttributeService).patch(eq(attributeId), any(TmsAttributeRQ.class));
  }

  @Test
  void shouldCallServiceWithCorrectParametersForGetById() throws Exception {
    // Given
    var attributeId = 999L;
    var attributeResponse = createAttributeResponse();

    given(tmsAttributeService.getById(attributeId)).willReturn(attributeResponse);

    // When/Then
    mockMvc.perform(get("/v1/tms/attribute/{attributeId}", attributeId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(tmsAttributeService).getById(eq(attributeId));
  }

  @Test
  void shouldCallServiceWithCorrectParametersForGetAll() throws Exception {
    // Given
    given(tmsAttributeService.getAll(any(Filter.class), any(Pageable.class))).willReturn(mockPage);

    // When/Then
    mockMvc.perform(get("/v1/tms/attribute")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(tmsAttributeService).getAll(any(Filter.class), any(Pageable.class));
  }

  @Test
  void shouldGetAllAttributesWithCustomPagination() throws Exception {
    // Given
    given(tmsAttributeService.getAll(any(Filter.class), any(Pageable.class))).willReturn(mockPage);

    // When/Then
    mockMvc.perform(get("/v1/tms/attribute")
            .param("offset", "0")
            .param("limit", "10")
            .param("sort", "key,desc")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(tmsAttributeService).getAll(any(Filter.class), any(Pageable.class));
  }

  @Test
  void shouldGetAllAttributesWhenEmpty() throws Exception {
    // Given
    given(tmsAttributeService.getAll(any(Filter.class), any(Pageable.class))).willReturn(mockPage);

    // When/Then
    mockMvc.perform(get("/v1/tms/attribute")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(tmsAttributeService).getAll(any(Filter.class), any(Pageable.class));
  }

  @Test
  void shouldHandleMultipleCallsToCreate() throws Exception {
    // Given
    var firstRequest = TmsAttributeRQ.builder().key("first").build();
    var secondRequest = TmsAttributeRQ.builder().key("second").build();
    var firstResponse = createAttributeResponseWithKey("first");
    var secondResponse = createAttributeResponseWithKey("second");
    var firstJsonContent = objectMapper.writeValueAsString(firstRequest);
    var secondJsonContent = objectMapper.writeValueAsString(secondRequest);

    given(tmsAttributeService.create(any(TmsAttributeRQ.class)))
        .willReturn(firstResponse)
        .willReturn(secondResponse);

    // When/Then
    mockMvc.perform(post("/v1/tms/attribute")
            .contentType(MediaType.APPLICATION_JSON)
            .content(firstJsonContent))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.key").value("first"));

    mockMvc.perform(post("/v1/tms/attribute")
            .contentType(MediaType.APPLICATION_JSON)
            .content(secondJsonContent))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.key").value("second"));

    verify(tmsAttributeService, times(2)).create(any(TmsAttributeRQ.class));
  }

  @Test
  void shouldHandleMultipleCallsToPatch() throws Exception {
    // Given
    var firstAttributeId = 1L;
    var secondAttributeId = 2L;
    var attributeRequest = createAttributeRequest();
    var firstResponse = createAttributeResponseWithKey("first");
    var secondResponse = createAttributeResponseWithKey("second");
    var jsonContent = objectMapper.writeValueAsString(attributeRequest);

    given(tmsAttributeService.patch(eq(firstAttributeId), any(TmsAttributeRQ.class))).willReturn(firstResponse);
    given(tmsAttributeService.patch(eq(secondAttributeId), any(TmsAttributeRQ.class))).willReturn(secondResponse);

    // When/Then
    mockMvc.perform(patch("/v1/tms/attribute/{attributeId}", firstAttributeId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.key").value("first"));

    mockMvc.perform(patch("/v1/tms/attribute/{attributeId}", secondAttributeId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.key").value("second"));

    verify(tmsAttributeService).patch(eq(firstAttributeId), any(TmsAttributeRQ.class));
    verify(tmsAttributeService).patch(eq(secondAttributeId), any(TmsAttributeRQ.class));
  }

  @Test
  void shouldCreateAttributeWithComplexData() throws Exception {
    // Given
    var complexRequest = TmsAttributeRQ.builder()
        .key("complex-key-with-special-chars-@#$%")
        .build();
    var complexResponse = createAttributeResponseWithKey("complex-key-with-special-chars-@#$%");
    var jsonContent = objectMapper.writeValueAsString(complexRequest);

    given(tmsAttributeService.create(any(TmsAttributeRQ.class))).willReturn(complexResponse);

    // When/Then
    mockMvc.perform(post("/v1/tms/attribute")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.key").value("complex-key-with-special-chars-@#$%"));

    verify(tmsAttributeService).create(any(TmsAttributeRQ.class));
  }

  @Test
  void shouldPatchAttributeWithComplexData() throws Exception {
    // Given
    var attributeId = 1L;
    var complexRequest = TmsAttributeRQ.builder()
        .key("updated-complex-key-123")
        .build();
    var complexResponse = createAttributeResponseWithKey("updated-complex-key-123");
    var jsonContent = objectMapper.writeValueAsString(complexRequest);

    given(tmsAttributeService.patch(eq(attributeId), any(TmsAttributeRQ.class))).willReturn(complexResponse);

    // When/Then
    mockMvc.perform(patch("/v1/tms/attribute/{attributeId}", attributeId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(attributeId))
        .andExpect(jsonPath("$.key").value("updated-complex-key-123"));

    verify(tmsAttributeService).patch(eq(attributeId), any(TmsAttributeRQ.class));
  }

  // Helper methods
  private TmsAttributeRQ createAttributeRequest() {
    return TmsAttributeRQ.builder()
        .key("test-key")
        .build();
  }

  private TmsAttributeRS createAttributeResponse() {
    var response = new TmsAttributeRS();
    response.setId(1L);
    response.setKey("test-key");
    return response;
  }

  private TmsAttributeRS createUpdatedAttributeResponse() {
    var response = new TmsAttributeRS();
    response.setId(1L);
    response.setKey("updated-key");
    return response;
  }

  private TmsAttributeRS createAttributeResponseWithKey(String key) {
    var response = new TmsAttributeRS();
    response.setId(1L);
    response.setKey(key);
    return response;
  }
}
