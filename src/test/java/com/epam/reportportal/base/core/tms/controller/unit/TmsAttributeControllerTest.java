package com.epam.reportportal.base.core.tms.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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

import com.epam.reportportal.base.core.tms.controller.TmsAttributeController;
import com.epam.reportportal.base.core.tms.dto.TmsAttributeRQ;
import com.epam.reportportal.base.core.tms.dto.TmsAttributeRS;
import com.epam.reportportal.base.core.tms.service.TmsAttributeService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.model.Page;
import com.epam.reportportal.base.util.ProjectExtractor;
import com.epam.reportportal.base.ws.resolver.FilterCriteriaResolver;
import com.epam.reportportal.base.ws.resolver.OffsetArgumentResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class TmsAttributeControllerTest {

  private final long projectId = 1L;
  private final String projectKey = "test_project";

  @Mock
  private TmsAttributeService tmsAttributeService;

  @Mock
  private ProjectExtractor projectExtractor;

  @Mock
  private Page<TmsAttributeRS> mockPage;

  @InjectMocks
  private TmsAttributeController tmsAttributeController;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private ReportPortalUser testUser;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    objectMapper = new ObjectMapper();

    // Create a test user
    testUser = ReportPortalUser.userBuilder()
        .withUserName("testUser")
        .withPassword("password")
        .withUserId(1L)
        .withActive(true)
        .withAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
        .build();

    // Configure MockMvc with custom argument resolvers
    mockMvc = standaloneSetup(tmsAttributeController)
        .setCustomArgumentResolvers(
            new HandlerMethodArgumentResolver() {
              @Override
              public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterAnnotation(AuthenticationPrincipal.class) != null;
              }

              @Override
              public Object resolveArgument(MethodParameter parameter,
                  ModelAndViewContainer mavContainer,
                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return testUser;
              }
            },
            new HandlerMethodArgumentResolver() {
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

    // Setup the project extractor mock to return a MembershipDetails with the projectId
    var membershipDetails = MembershipDetails.builder()
        .withProjectId(projectId)
        .withProjectKey(projectKey)
        .build();
    given(projectExtractor.extractMembershipDetails(eq(testUser), anyString()))
        .willReturn(membershipDetails);
  }

  @Test
  void shouldCreateAttribute() throws Exception {
    // Given
    var attributeRequest = createAttributeRequest();
    var attributeResponse = createAttributeResponse();
    var jsonContent = objectMapper.writeValueAsString(attributeRequest);

    given(tmsAttributeService.create(eq(projectId), any(TmsAttributeRQ.class)))
        .willReturn(attributeResponse);

    // When/Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/attribute", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.key").value("test-key"))
        .andExpect(jsonPath("$.value").value("test-value"));

    verify(tmsAttributeService).create(eq(projectId), any(TmsAttributeRQ.class));
  }

  @Test
  void shouldPatchAttribute() throws Exception {
    // Given
    var attributeId = 1L;
    var attributeRequest = createAttributeRequest();
    var updatedResponse = createUpdatedAttributeResponse();
    var jsonContent = objectMapper.writeValueAsString(attributeRequest);

    given(tmsAttributeService.patch(eq(projectId), eq(attributeId), any(TmsAttributeRQ.class)))
        .willReturn(updatedResponse);

    // When/Then
    mockMvc.perform(patch("/v1/project/{projectKey}/tms/attribute/{attributeId}", projectKey, attributeId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(attributeId))
        .andExpect(jsonPath("$.key").value("updated-key"))
        .andExpect(jsonPath("$.value").value("updated-value"));

    verify(tmsAttributeService).patch(eq(projectId), eq(attributeId), any(TmsAttributeRQ.class));
  }

  @Test
  void shouldGetAllAttributes() throws Exception {
    // Given
    given(tmsAttributeService.getAll(eq(projectId), any(Filter.class), any(Pageable.class)))
        .willReturn(mockPage);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute", projectKey)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(tmsAttributeService).getAll(eq(projectId), any(Filter.class), any(Pageable.class));
  }

  @Test
  void shouldGetAttributeById() throws Exception {
    // Given
    var attributeId = 1L;
    var attributeResponse = createAttributeResponse();

    given(tmsAttributeService.getById(eq(projectId), eq(attributeId)))
        .willReturn(attributeResponse);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/{attributeId}", projectKey, attributeId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(attributeId))
        .andExpect(jsonPath("$.key").value("test-key"))
        .andExpect(jsonPath("$.value").value("test-value"));

    verify(tmsAttributeService).getById(eq(projectId), eq(attributeId));
  }

  @Test
  void shouldGetAllKeys() throws Exception {
    // Given
    var keys = Arrays.asList("key1", "key2", "key3");
    given(tmsAttributeService.getKeysByCriteria(eq(projectId), isNull()))
        .willReturn(keys);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/key", projectKey)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect(jsonPath("$[0]").value("key1"))
        .andExpect(jsonPath("$[1]").value("key2"))
        .andExpect(jsonPath("$[2]").value("key3"));

    verify(tmsAttributeService).getKeysByCriteria(eq(projectId), isNull());
  }

  @Test
  void shouldGetAllKeysWithSearchParameter() throws Exception {
    // Given
    var searchTerm = "test";
    var keys = Arrays.asList("test-key1", "test-key2");
    given(tmsAttributeService.getKeysByCriteria(eq(projectId), eq(searchTerm)))
        .willReturn(keys);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/key", projectKey)
            .param("search", searchTerm)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0]").value("test-key1"))
        .andExpect(jsonPath("$[1]").value("test-key2"));

    verify(tmsAttributeService).getKeysByCriteria(eq(projectId), eq(searchTerm));
  }

  @Test
  void shouldGetAllKeysWhenEmpty() throws Exception {
    // Given
    given(tmsAttributeService.getKeysByCriteria(eq(projectId), isNull()))
        .willReturn(Collections.emptyList());

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/key", projectKey)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));

    verify(tmsAttributeService).getKeysByCriteria(eq(projectId), isNull());
  }

  @Test
  void shouldGetAllValues() throws Exception {
    // Given
    var values = Arrays.asList("value1", "value2", "value3");
    given(tmsAttributeService.getValuesByCriteria(eq(projectId), isNull()))
        .willReturn(values);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/value", projectKey)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect(jsonPath("$[0]").value("value1"))
        .andExpect(jsonPath("$[1]").value("value2"))
        .andExpect(jsonPath("$[2]").value("value3"));

    verify(tmsAttributeService).getValuesByCriteria(eq(projectId), isNull());
  }

  @Test
  void shouldGetAllValuesWithSearchParameter() throws Exception {
    // Given
    var searchTerm = "prod";
    var values = Arrays.asList("production", "product");
    given(tmsAttributeService.getValuesByCriteria(eq(projectId), eq(searchTerm)))
        .willReturn(values);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/value", projectKey)
            .param("search", searchTerm)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0]").value("production"))
        .andExpect(jsonPath("$[1]").value("product"));

    verify(tmsAttributeService).getValuesByCriteria(eq(projectId), eq(searchTerm));
  }

  @Test
  void shouldGetAllValuesWhenEmpty() throws Exception {
    // Given
    given(tmsAttributeService.getValuesByCriteria(eq(projectId), isNull()))
        .willReturn(Collections.emptyList());

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/value", projectKey)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));

    verify(tmsAttributeService).getValuesByCriteria(eq(projectId), isNull());
  }

  @Test
  void shouldCreateAttributeWithNullKey() throws Exception {
    // Given
    var requestWithNullKey = TmsAttributeRQ.builder()
        .key(null)
        .value("test-value")
        .build();
    var responseWithNullKey = createAttributeResponseWithKeyAndValue(null, "test-value");
    var jsonContent = objectMapper.writeValueAsString(requestWithNullKey);

    given(tmsAttributeService.create(eq(projectId), any(TmsAttributeRQ.class)))
        .willReturn(responseWithNullKey);

    // When/Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/attribute", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.value").value("test-value"));

    verify(tmsAttributeService).create(eq(projectId), any(TmsAttributeRQ.class));
  }

  @Test
  void shouldCreateAttributeWithNullValue() throws Exception {
    // Given
    var requestWithNullValue = TmsAttributeRQ.builder()
        .key("test-key")
        .value(null)
        .build();
    var responseWithNullValue = createAttributeResponseWithKeyAndValue("test-key", null);
    var jsonContent = objectMapper.writeValueAsString(requestWithNullValue);

    given(tmsAttributeService.create(eq(projectId), any(TmsAttributeRQ.class)))
        .willReturn(responseWithNullValue);

    // When/Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/attribute", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.key").value("test-key"));

    verify(tmsAttributeService).create(eq(projectId), any(TmsAttributeRQ.class));
  }

  @Test
  void shouldPatchAttributeWithNullKey() throws Exception {
    // Given
    var attributeId = 1L;
    var requestWithNullKey = TmsAttributeRQ.builder()
        .key(null)
        .value("updated-value")
        .build();
    var responseWithNullKey = createAttributeResponseWithKeyAndValue(null, "updated-value");
    var jsonContent = objectMapper.writeValueAsString(requestWithNullKey);

    given(tmsAttributeService.patch(eq(projectId), eq(attributeId), any(TmsAttributeRQ.class)))
        .willReturn(responseWithNullKey);

    // When/Then
    mockMvc.perform(patch("/v1/project/{projectKey}/tms/attribute/{attributeId}", projectKey, attributeId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(attributeId))
        .andExpect(jsonPath("$.value").value("updated-value"));

    verify(tmsAttributeService).patch(eq(projectId), eq(attributeId), any(TmsAttributeRQ.class));
  }

  @Test
  void shouldCallServiceWithCorrectParametersForCreate() throws Exception {
    // Given
    var customRequest = TmsAttributeRQ.builder()
        .key("custom-key")
        .value("custom-value")
        .build();
    var attributeResponse = createAttributeResponse();
    var jsonContent = objectMapper.writeValueAsString(customRequest);

    given(tmsAttributeService.create(eq(projectId), any(TmsAttributeRQ.class)))
        .willReturn(attributeResponse);

    // When/Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/attribute", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isCreated());

    verify(tmsAttributeService).create(eq(projectId), any(TmsAttributeRQ.class));
  }

  @Test
  void shouldCallServiceWithCorrectParametersForPatch() throws Exception {
    // Given
    var attributeId = 999L;
    var customRequest = TmsAttributeRQ.builder()
        .key("custom-key")
        .value("custom-value")
        .build();
    var attributeResponse = createAttributeResponse();
    var jsonContent = objectMapper.writeValueAsString(customRequest);

    given(tmsAttributeService.patch(eq(projectId), eq(attributeId), any(TmsAttributeRQ.class)))
        .willReturn(attributeResponse);

    // When/Then
    mockMvc.perform(patch("/v1/project/{projectKey}/tms/attribute/{attributeId}", projectKey, attributeId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(tmsAttributeService).patch(eq(projectId), eq(attributeId), any(TmsAttributeRQ.class));
  }

  @Test
  void shouldCallServiceWithCorrectParametersForGetById() throws Exception {
    // Given
    var attributeId = 999L;
    var attributeResponse = createAttributeResponse();

    given(tmsAttributeService.getById(eq(projectId), eq(attributeId)))
        .willReturn(attributeResponse);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/{attributeId}", projectKey, attributeId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(tmsAttributeService).getById(eq(projectId), eq(attributeId));
  }

  @Test
  void shouldCallServiceWithCorrectParametersForGetAll() throws Exception {
    // Given
    given(tmsAttributeService.getAll(eq(projectId), any(Filter.class), any(Pageable.class)))
        .willReturn(mockPage);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute", projectKey)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(tmsAttributeService).getAll(eq(projectId), any(Filter.class), any(Pageable.class));
  }

  @Test
  void shouldGetAllAttributesWithCustomPagination() throws Exception {
    // Given
    given(tmsAttributeService.getAll(eq(projectId), any(Filter.class), any(Pageable.class)))
        .willReturn(mockPage);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute", projectKey)
            .param("offset", "0")
            .param("limit", "10")
            .param("sort", "key,desc")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(tmsAttributeService).getAll(eq(projectId), any(Filter.class), any(Pageable.class));
  }

  @Test
  void shouldGetAllAttributesWhenEmpty() throws Exception {
    // Given
    given(tmsAttributeService.getAll(eq(projectId), any(Filter.class), any(Pageable.class)))
        .willReturn(mockPage);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute", projectKey)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(tmsAttributeService).getAll(eq(projectId), any(Filter.class), any(Pageable.class));
  }

  @Test
  void shouldHandleMultipleCallsToCreate() throws Exception {
    // Given
    var firstRequest = TmsAttributeRQ.builder().key("first").value("first-value").build();
    var secondRequest = TmsAttributeRQ.builder().key("second").value("second-value").build();
    var firstResponse = createAttributeResponseWithKeyAndValue("first", "first-value");
    var secondResponse = createAttributeResponseWithKeyAndValue("second", "second-value");
    var firstJsonContent = objectMapper.writeValueAsString(firstRequest);
    var secondJsonContent = objectMapper.writeValueAsString(secondRequest);

    given(tmsAttributeService.create(eq(projectId), any(TmsAttributeRQ.class)))
        .willReturn(firstResponse)
        .willReturn(secondResponse);

    // When/Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/attribute", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(firstJsonContent))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.key").value("first"))
        .andExpect(jsonPath("$.value").value("first-value"));

    mockMvc.perform(post("/v1/project/{projectKey}/tms/attribute", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(secondJsonContent))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.key").value("second"))
        .andExpect(jsonPath("$.value").value("second-value"));

    verify(tmsAttributeService, times(2)).create(eq(projectId), any(TmsAttributeRQ.class));
  }

  @Test
  void shouldHandleMultipleCallsToPatch() throws Exception {
    // Given
    var firstAttributeId = 1L;
    var secondAttributeId = 2L;
    var attributeRequest = createAttributeRequest();
    var firstResponse = createAttributeResponseWithKeyAndValue("first", "first-value");
    var secondResponse = createAttributeResponseWithKeyAndValue("second", "second-value");
    var jsonContent = objectMapper.writeValueAsString(attributeRequest);

    given(tmsAttributeService.patch(eq(projectId), eq(firstAttributeId), any(TmsAttributeRQ.class)))
        .willReturn(firstResponse);
    given(tmsAttributeService.patch(eq(projectId), eq(secondAttributeId), any(TmsAttributeRQ.class)))
        .willReturn(secondResponse);

    // When/Then
    mockMvc.perform(patch("/v1/project/{projectKey}/tms/attribute/{attributeId}", projectKey, firstAttributeId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.key").value("first"))
        .andExpect(jsonPath("$.value").value("first-value"));

    mockMvc.perform(patch("/v1/project/{projectKey}/tms/attribute/{attributeId}", projectKey, secondAttributeId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.key").value("second"))
        .andExpect(jsonPath("$.value").value("second-value"));

    verify(tmsAttributeService).patch(eq(projectId), eq(firstAttributeId), any(TmsAttributeRQ.class));
    verify(tmsAttributeService).patch(eq(projectId), eq(secondAttributeId), any(TmsAttributeRQ.class));
  }

  @Test
  void shouldCreateAttributeWithComplexData() throws Exception {
    // Given
    var complexRequest = TmsAttributeRQ.builder()
        .key("complex-key-with-special-chars-@#$%")
        .value("complex-value-with-unicode-\u00E9\u00F1")
        .build();
    var complexResponse = createAttributeResponseWithKeyAndValue(
        "complex-key-with-special-chars-@#$%",
        "complex-value-with-unicode-\u00E9\u00F1"
    );
    var jsonContent = objectMapper.writeValueAsString(complexRequest);

    given(tmsAttributeService.create(eq(projectId), any(TmsAttributeRQ.class)))
        .willReturn(complexResponse);

    // When/Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/attribute", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.key").value("complex-key-with-special-chars-@#$%"))
        .andExpect(jsonPath("$.value").value("complex-value-with-unicode-\u00E9\u00F1"));

    verify(tmsAttributeService).create(eq(projectId), any(TmsAttributeRQ.class));
  }

  @Test
  void shouldPatchAttributeWithComplexData() throws Exception {
    // Given
    var attributeId = 1L;
    var complexRequest = TmsAttributeRQ.builder()
        .key("updated-complex-key-123")
        .value("updated-complex-value-456")
        .build();
    var complexResponse = createAttributeResponseWithKeyAndValue(
        "updated-complex-key-123",
        "updated-complex-value-456"
    );
    var jsonContent = objectMapper.writeValueAsString(complexRequest);

    given(tmsAttributeService.patch(eq(projectId), eq(attributeId), any(TmsAttributeRQ.class)))
        .willReturn(complexResponse);

    // When/Then
    mockMvc.perform(patch("/v1/project/{projectKey}/tms/attribute/{attributeId}", projectKey, attributeId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(attributeId))
        .andExpect(jsonPath("$.key").value("updated-complex-key-123"))
        .andExpect(jsonPath("$.value").value("updated-complex-value-456"));

    verify(tmsAttributeService).patch(eq(projectId), eq(attributeId), any(TmsAttributeRQ.class));
  }

  @Test
  void shouldWorkWithDifferentProjectKey() throws Exception {
    // Given
    var differentProjectKey = "another_project";
    var differentProjectId = 2L;
    var attributeRequest = createAttributeRequest();
    var attributeResponse = createAttributeResponse();
    var jsonContent = objectMapper.writeValueAsString(attributeRequest);

    var differentMembershipDetails = MembershipDetails.builder()
        .withProjectId(differentProjectId)
        .withProjectKey(differentProjectKey)
        .build();
    given(projectExtractor.extractMembershipDetails(eq(testUser), eq(differentProjectKey)))
        .willReturn(differentMembershipDetails);

    given(tmsAttributeService.create(eq(differentProjectId), any(TmsAttributeRQ.class)))
        .willReturn(attributeResponse);

    // When/Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/attribute", differentProjectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.key").value("test-key"))
        .andExpect(jsonPath("$.value").value("test-value"));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), eq(differentProjectKey));
    verify(tmsAttributeService).create(eq(differentProjectId), any(TmsAttributeRQ.class));
  }

  @Test
  void shouldExtractProjectIdForGetAll() throws Exception {
    // Given
    given(tmsAttributeService.getAll(eq(projectId), any(Filter.class), any(Pageable.class)))
        .willReturn(mockPage);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute", projectKey)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsAttributeService).getAll(eq(projectId), any(Filter.class), any(Pageable.class));
  }

  @Test
  void shouldExtractProjectIdForGetById() throws Exception {
    // Given
    var attributeId = 1L;
    var attributeResponse = createAttributeResponse();

    given(tmsAttributeService.getById(eq(projectId), eq(attributeId)))
        .willReturn(attributeResponse);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/{attributeId}", projectKey, attributeId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsAttributeService).getById(eq(projectId), eq(attributeId));
  }

  @Test
  void shouldExtractProjectIdForGetAllKeys() throws Exception {
    // Given
    var keys = Arrays.asList("key1", "key2");
    given(tmsAttributeService.getKeysByCriteria(eq(projectId), isNull()))
        .willReturn(keys);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/key", projectKey)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsAttributeService).getKeysByCriteria(eq(projectId), isNull());
  }

  @Test
  void shouldExtractProjectIdForGetAllValues() throws Exception {
    // Given
    var values = Arrays.asList("value1", "value2");
    given(tmsAttributeService.getValuesByCriteria(eq(projectId), isNull()))
        .willReturn(values);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/value", projectKey)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsAttributeService).getValuesByCriteria(eq(projectId), isNull());
  }

  @Test
  void shouldGetKeysWithEmptySearchParameter() throws Exception {
    // Given
    var keys = Arrays.asList("key1", "key2");
    given(tmsAttributeService.getKeysByCriteria(eq(projectId), eq("")))
        .willReturn(keys);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/key", projectKey)
            .param("search", "")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2));

    verify(tmsAttributeService).getKeysByCriteria(eq(projectId), eq(""));
  }

  @Test
  void shouldGetValuesWithEmptySearchParameter() throws Exception {
    // Given
    var values = Arrays.asList("value1", "value2");
    given(tmsAttributeService.getValuesByCriteria(eq(projectId), eq("")))
        .willReturn(values);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/value", projectKey)
            .param("search", "")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2));

    verify(tmsAttributeService).getValuesByCriteria(eq(projectId), eq(""));
  }

  @Test
  void shouldHandleMultipleCallsToGetKeys() throws Exception {
    // Given
    var firstSearch = "test";
    var secondSearch = "prod";
    var firstKeys = Arrays.asList("test-key1", "test-key2");
    var secondKeys = Arrays.asList("prod-key1");

    given(tmsAttributeService.getKeysByCriteria(eq(projectId), eq(firstSearch)))
        .willReturn(firstKeys);
    given(tmsAttributeService.getKeysByCriteria(eq(projectId), eq(secondSearch)))
        .willReturn(secondKeys);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/key", projectKey)
            .param("search", firstSearch)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));

    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/key", projectKey)
            .param("search", secondSearch)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));

    verify(tmsAttributeService).getKeysByCriteria(eq(projectId), eq(firstSearch));
    verify(tmsAttributeService).getKeysByCriteria(eq(projectId), eq(secondSearch));
  }

  @Test
  void shouldHandleMultipleCallsToGetValues() throws Exception {
    // Given
    var firstSearch = "dev";
    var secondSearch = "prod";
    var firstValues = Arrays.asList("development", "devops");
    var secondValues = Arrays.asList("production", "product");

    given(tmsAttributeService.getValuesByCriteria(eq(projectId), eq(firstSearch)))
        .willReturn(firstValues);
    given(tmsAttributeService.getValuesByCriteria(eq(projectId), eq(secondSearch)))
        .willReturn(secondValues);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/value", projectKey)
            .param("search", firstSearch)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));

    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/value", projectKey)
            .param("search", secondSearch)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));

    verify(tmsAttributeService).getValuesByCriteria(eq(projectId), eq(firstSearch));
    verify(tmsAttributeService).getValuesByCriteria(eq(projectId), eq(secondSearch));
  }

  @Test
  void shouldGetKeysWithSpecialCharactersInSearch() throws Exception {
    // Given
    var searchTerm = "test@#$%";
    var keys = List.of("test@#$%-key");
    given(tmsAttributeService.getKeysByCriteria(eq(projectId), eq(searchTerm)))
        .willReturn(keys);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/key", projectKey)
            .param("search", searchTerm)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0]").value("test@#$%-key"));

    verify(tmsAttributeService).getKeysByCriteria(eq(projectId), eq(searchTerm));
  }

  @Test
  void shouldGetValuesWithSpecialCharactersInSearch() throws Exception {
    // Given
    var searchTerm = "value@#$%";
    var values = List.of("value@#$%-1");
    given(tmsAttributeService.getValuesByCriteria(eq(projectId), eq(searchTerm)))
        .willReturn(values);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/attribute/value", projectKey)
            .param("search", searchTerm)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0]").value("value@#$%-1"));

    verify(tmsAttributeService).getValuesByCriteria(eq(projectId), eq(searchTerm));
  }

  // Helper methods
  private TmsAttributeRQ createAttributeRequest() {
    return TmsAttributeRQ.builder()
        .key("test-key")
        .value("test-value")
        .build();
  }

  private TmsAttributeRS createAttributeResponse() {
    var response = new TmsAttributeRS();
    response.setId(1L);
    response.setKey("test-key");
    response.setValue("test-value");
    return response;
  }

  private TmsAttributeRS createUpdatedAttributeResponse() {
    var response = new TmsAttributeRS();
    response.setId(1L);
    response.setKey("updated-key");
    response.setValue("updated-value");
    return response;
  }

  private TmsAttributeRS createAttributeResponseWithKeyAndValue(String key, String value) {
    var response = new TmsAttributeRS();
    response.setId(1L);
    response.setKey(key);
    response.setValue(value);
    return response;
  }
}
