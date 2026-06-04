package com.epam.reportportal.base.core.tms.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.epam.reportportal.base.core.tms.controller.TmsTestCaseAttributeController;
import com.epam.reportportal.base.core.tms.dto.GetAttributesByTestCaseIdsRQ;
import com.epam.reportportal.base.core.tms.dto.TmsAttributeRS;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseAttributeService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.model.Page;
import com.epam.reportportal.base.util.ProjectExtractor;
import com.epam.reportportal.base.ws.resolver.OffsetArgumentResolver;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

class TmsTestCaseAttributeControllerTest {

  private static final long PROJECT_ID = 1L;
  private static final String PROJECT_KEY = "test_project";

  @Mock
  private TmsTestCaseAttributeService tmsTestCaseAttributeService;

  @Mock
  private ProjectExtractor projectExtractor;

  @InjectMocks
  private TmsTestCaseAttributeController tmsTestCaseAttributeController;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private ReportPortalUser testUser;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    objectMapper = new ObjectMapper();

    testUser = ReportPortalUser.userBuilder()
        .withUserName("testUser")
        .withPassword("password")
        .withUserId(1L)
        .withActive(true)
        .withAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
        .build();

    mockMvc = standaloneSetup(tmsTestCaseAttributeController)
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
            new OffsetArgumentResolver())
        .build();

    var membershipDetails = MembershipDetails.builder()
        .withProjectId(PROJECT_ID)
        .withProjectKey(PROJECT_KEY)
        .build();
    given(projectExtractor.extractMembershipDetails(eq(testUser), anyString()))
        .willReturn(membershipDetails);
  }

  @Test
  void shouldGetAttributesByTestCaseIds() throws Exception {
    // Given
    var request = GetAttributesByTestCaseIdsRQ.builder()
        .testCaseIds(List.of(101L, 205L, 317L))
        .build();
    var jsonContent = objectMapper.writeValueAsString(request);

    var attr1 = createAttributeRS(1L, "browser", "Chrome");
    var attr2 = createAttributeRS(2L, "priority", "High");
    var pageMetadata = new Page.PageMetadata(20, 1, 2, 1);
    var responsePage = new Page<>(List.of(attr1, attr2), pageMetadata);

    given(tmsTestCaseAttributeService.getAttributesByTestCaseIds(
        eq(PROJECT_ID), eq(List.of(101L, 205L, 317L)), any(Pageable.class)))
        .willReturn(responsePage);

    // When/Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-case/attribute", PROJECT_KEY)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].id").value(1L))
        .andExpect(jsonPath("$.content[0].key").value("browser"))
        .andExpect(jsonPath("$.content[0].value").value("Chrome"))
        .andExpect(jsonPath("$.content[1].id").value(2L))
        .andExpect(jsonPath("$.content[1].key").value("priority"))
        .andExpect(jsonPath("$.content[1].value").value("High"))
        .andExpect(jsonPath("$.page.totalElements").value(2));

    verify(tmsTestCaseAttributeService).getAttributesByTestCaseIds(
        eq(PROJECT_ID), eq(List.of(101L, 205L, 317L)), any(Pageable.class));
  }

  @Test
  void shouldGetAttributesByTestCaseIds_WhenSingleTestCaseId() throws Exception {
    // Given
    var request = GetAttributesByTestCaseIdsRQ.builder()
        .testCaseIds(List.of(101L))
        .build();
    var jsonContent = objectMapper.writeValueAsString(request);

    var attr1 = createAttributeRS(1L, "os", "Linux");
    var pageMetadata = new Page.PageMetadata(20, 1, 1, 1);
    var responsePage = new Page<>(List.of(attr1), pageMetadata);

    given(tmsTestCaseAttributeService.getAttributesByTestCaseIds(
        eq(PROJECT_ID), eq(List.of(101L)), any(Pageable.class)))
        .willReturn(responsePage);

    // When/Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-case/attribute", PROJECT_KEY)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].id").value(1L))
        .andExpect(jsonPath("$.content[0].key").value("os"))
        .andExpect(jsonPath("$.content[0].value").value("Linux"));

    verify(tmsTestCaseAttributeService).getAttributesByTestCaseIds(
        eq(PROJECT_ID), eq(List.of(101L)), any(Pageable.class));
  }

  @Test
  void shouldGetAttributesByTestCaseIds_WhenEmptyResult() throws Exception {
    // Given
    var request = GetAttributesByTestCaseIdsRQ.builder()
        .testCaseIds(List.of(999L))
        .build();
    var jsonContent = objectMapper.writeValueAsString(request);

    var pageMetadata = new Page.PageMetadata(20, 1, 0, 0);
    var responsePage = new Page<TmsAttributeRS>(Collections.emptyList(), pageMetadata);

    given(tmsTestCaseAttributeService.getAttributesByTestCaseIds(
        eq(PROJECT_ID), eq(List.of(999L)), any(Pageable.class)))
        .willReturn(responsePage);

    // When/Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-case/attribute", PROJECT_KEY)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.page.totalElements").value(0));

    verify(tmsTestCaseAttributeService).getAttributesByTestCaseIds(
        eq(PROJECT_ID), eq(List.of(999L)), any(Pageable.class));
  }

  @Test
  void shouldExtractProjectId() throws Exception {
    // Given
    var request = GetAttributesByTestCaseIdsRQ.builder()
        .testCaseIds(List.of(1L))
        .build();
    var jsonContent = objectMapper.writeValueAsString(request);

    var pageMetadata = new Page.PageMetadata(20, 1, 0, 0);
    var responsePage = new Page<TmsAttributeRS>(Collections.emptyList(), pageMetadata);

    given(tmsTestCaseAttributeService.getAttributesByTestCaseIds(
        eq(PROJECT_ID), any(), any(Pageable.class)))
        .willReturn(responsePage);

    // When/Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-case/attribute", PROJECT_KEY)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseAttributeService).getAttributesByTestCaseIds(
        eq(PROJECT_ID), any(), any(Pageable.class));
  }

  @Test
  void shouldWorkWithDifferentProjectKey() throws Exception {
    // Given
    var differentProjectKey = "another_project";
    var differentProjectId = 42L;
    var request = GetAttributesByTestCaseIdsRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .build();
    var jsonContent = objectMapper.writeValueAsString(request);

    var differentMembershipDetails = MembershipDetails.builder()
        .withProjectId(differentProjectId)
        .withProjectKey(differentProjectKey)
        .build();
    given(projectExtractor.extractMembershipDetails(eq(testUser), eq(differentProjectKey)))
        .willReturn(differentMembershipDetails);

    var pageMetadata = new Page.PageMetadata(20, 1, 0, 0);
    var responsePage = new Page<TmsAttributeRS>(Collections.emptyList(), pageMetadata);

    given(tmsTestCaseAttributeService.getAttributesByTestCaseIds(
        eq(differentProjectId), any(), any(Pageable.class)))
        .willReturn(responsePage);

    // When/Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-case/attribute", differentProjectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), eq(differentProjectKey));
    verify(tmsTestCaseAttributeService).getAttributesByTestCaseIds(
        eq(differentProjectId), any(), any(Pageable.class));
  }

  private TmsAttributeRS createAttributeRS(Long id, String key, String value) {
    var rs = new TmsAttributeRS();
    rs.setId(id);
    rs.setKey(key);
    rs.setValue(value);
    return rs;
  }
}
