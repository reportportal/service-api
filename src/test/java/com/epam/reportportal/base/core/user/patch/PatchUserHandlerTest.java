package com.epam.reportportal.base.core.user.patch;

import static com.epam.reportportal.api.model.OperationType.ADD;
import static com.epam.reportportal.api.model.OperationType.REMOVE;
import static com.epam.reportportal.api.model.OperationType.REPLACE;
import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.api.model.PatchOperation;
import com.epam.reportportal.base.core.user.UserMutationService;
import com.epam.reportportal.base.core.user.UserService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserType;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.SecurityContextUtils;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PatchUserHandlerTest {

  private static final String UPSA_IMMUTABLE_MESSAGE = "UPSA users cannot be updated.";

  @Mock
  private UserService userService;

  @Mock
  private UserMutationService userMutationService;

  @InjectMocks
  private PatchUserHandler patchUserHandler;

  private MockedStatic<SecurityContextUtils> mockedSecurityContextUtils;

  private User targetUser;
  private ReportPortalUser principalUser;

  @BeforeEach
  void setUp() {
    mockedSecurityContextUtils = mockStatic(SecurityContextUtils.class);

    targetUser = new User();
    targetUser.setId(2L);
    targetUser.setUserType(UserType.INTERNAL);
    targetUser.setEmail("target@example.com");
    targetUser.setFullName("Target User");
    targetUser.setRole(UserRole.USER);
    targetUser.setActive(true);
    targetUser.setExternalId("external_target");

    principalUser = getRpUser("owner", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.EDITOR, 1L);
  }

  @AfterEach
  void tearDown() {
    mockedSecurityContextUtils.close();
  }

  private void mockPrincipal(ReportPortalUser principal, boolean isAdmin) {
    mockedSecurityContextUtils.when(SecurityContextUtils::getPrincipal).thenReturn(principal);
    mockedSecurityContextUtils.when(SecurityContextUtils::isAdminRole).thenReturn(isAdmin);
  }

  // --- Test Cases for Profile Owner (INTERNAL, non-UPSA) ---

  @Test
  @DisplayName("Profile owner (INTERNAL) updates own email - Should delegate to UserMutationService")
  void profileOwnerInternalUpdatesOwnEmailShouldSucceed() {
    targetUser.setId(principalUser.getUserId());
    mockPrincipal(principalUser, false);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("/email");
    op.setValue("new_email@example.com");
    patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op));

    verify(userMutationService).updateEmail(targetUser, "new_email@example.com", principalUser);
  }

  @Test
  @DisplayName("Profile owner (INTERNAL) updates own full_name - Should delegate to UserMutationService")
  void profileOwnerInternalUpdatesOwnFullNameShouldSucceed() {
    targetUser.setId(principalUser.getUserId());
    mockPrincipal(principalUser, false);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("/full_name");
    op.setValue("New Full Name");
    patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op));

    verify(userMutationService).updateFullName(targetUser, "New Full Name", principalUser);
  }

  @Test
  @DisplayName("Profile owner (INTERNAL) updates own role - Should fail with ACCESS_DENIED")
  void profileOwnerInternalUpdatesOwnRoleShouldFail() {
    targetUser.setId(principalUser.getUserId());
    mockPrincipal(principalUser, false);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("/instance_role");
    op.setValue(UserRole.ADMINISTRATOR.name());
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op)));

    assertEquals(ErrorType.ACCESS_DENIED, exception.getErrorType());
    assertEquals(
        "You do not have enough permissions. You can only update your own email and full name.",
        exception.getMessage());
  }

  @Test
  @DisplayName("Profile owner (INTERNAL) updates another user's email - Should fail with ACCESS_DENIED")
  void profileOwnerInternalUpdatesAnotherUserEmailShouldFail() {
    mockPrincipal(principalUser, false);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("/email");
    op.setValue("another@example.com");
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op)));

    assertEquals(ErrorType.ACCESS_DENIED, exception.getErrorType());
    assertEquals("You do not have enough permissions. You are not allowed to update this user's profile.",
        exception.getMessage());
  }

  // --- Test Cases for Admin ---

  @Test
  @DisplayName("Admin updates another user's (INTERNAL) email - Should delegate to UserMutationService")
  void adminUpdatesAnotherUserInternalEmailShouldSucceed() {
    mockPrincipal(principalUser, true);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("/email");
    op.setValue("admin_changed@example.com");
    patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op));

    verify(userMutationService).updateEmail(targetUser, "admin_changed@example.com", principalUser);
  }

  @Test
  @DisplayName("Admin updates another user's (INTERNAL) role - Should delegate to UserMutationService")
  void adminUpdatesAnotherUserInternalRoleShouldSucceed() {
    mockPrincipal(principalUser, true);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("/instance_role");
    op.setValue(UserRole.ADMINISTRATOR.name());
    patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op));

    verify(userMutationService).updateInstanceRole(targetUser, UserRole.ADMINISTRATOR.name(), principalUser);
  }

  @Test
  @DisplayName("Admin updates own email - Should delegate to UserMutationService")
  void adminUpdatesOwnEmailShouldSucceed() {
    targetUser.setId(principalUser.getUserId());
    mockPrincipal(principalUser, true);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("/email");
    op.setValue("admin_new@example.com");
    patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op));

    verify(userMutationService).updateEmail(targetUser, "admin_new@example.com", principalUser);
  }

  // --- Test Cases for UPSA Users ---

  @Test
  @DisplayName("Profile owner (UPSA) updates own email - Should fail with ACCESS_DENIED")
  void profileOwnerUpsaUpdatesOwnEmailShouldFail() {
    targetUser.setId(principalUser.getUserId());
    targetUser.setUserType(UserType.UPSA);
    mockPrincipal(principalUser, false);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);
    doThrow(new ReportPortalException(ErrorType.ACCESS_DENIED, UPSA_IMMUTABLE_MESSAGE))
        .when(userMutationService).validateUserUpdatable(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("/email");
    op.setValue("upsa_new@example.com");
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op)));

    assertEquals(ErrorType.ACCESS_DENIED, exception.getErrorType());
    assertTrue(exception.getMessage().contains(UPSA_IMMUTABLE_MESSAGE));
  }

  @Test
  @DisplayName("Admin updates UPSA user's email - Should fail with ACCESS_DENIED")
  void adminUpdatesUpsaUserEmailShouldFail() {
    targetUser.setUserType(UserType.UPSA);
    mockPrincipal(principalUser, true);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);
    doThrow(new ReportPortalException(ErrorType.ACCESS_DENIED, UPSA_IMMUTABLE_MESSAGE))
        .when(userMutationService).validateUserUpdatable(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("/email");
    op.setValue("admin_upsa_new@example.com");
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op)));

    assertEquals(ErrorType.ACCESS_DENIED, exception.getErrorType());
    assertTrue(exception.getMessage().contains(UPSA_IMMUTABLE_MESSAGE));
  }

  @Test
  @DisplayName("Admin updates UPSA user's external_id (REPLACE) - Should fail with ACCESS_DENIED")
  void adminUpdatesUpsaUserExternalIdReplaceShouldFail() {
    targetUser.setUserType(UserType.UPSA);
    mockPrincipal(principalUser, true);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);
    doThrow(new ReportPortalException(ErrorType.ACCESS_DENIED, UPSA_IMMUTABLE_MESSAGE))
        .when(userMutationService).validateUserUpdatable(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("/external_id");
    op.setValue("new_external_id");
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op)));

    assertEquals(ErrorType.ACCESS_DENIED, exception.getErrorType());
    assertTrue(exception.getMessage().contains(UPSA_IMMUTABLE_MESSAGE));
    verify(userMutationService, never()).updateExternalId(any(), any());
  }

  @Test
  @DisplayName("Admin updates UPSA user's external_id (ADD) - Should fail with ACCESS_DENIED")
  void adminUpdatesUpsaUserExternalIdAddShouldFail() {
    targetUser.setUserType(UserType.UPSA);
    targetUser.setExternalId(null);
    mockPrincipal(principalUser, true);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);
    doThrow(new ReportPortalException(ErrorType.ACCESS_DENIED, UPSA_IMMUTABLE_MESSAGE))
        .when(userMutationService).validateUserUpdatable(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(ADD);
    op.setPath("/external_id");
    op.setValue("added_external_id");
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op)));

    assertEquals(ErrorType.ACCESS_DENIED, exception.getErrorType());
    assertTrue(exception.getMessage().contains(UPSA_IMMUTABLE_MESSAGE));
    verify(userMutationService, never()).updateExternalId(any(), any());
  }

  @Test
  @DisplayName("Admin updates UPSA user's external_id (REMOVE) - Should fail with ACCESS_DENIED")
  void adminUpdatesUpsaUserExternalIdRemoveShouldFail() {
    targetUser.setUserType(UserType.UPSA);
    targetUser.setExternalId("existing_external_id");
    mockPrincipal(principalUser, true);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);
    doThrow(new ReportPortalException(ErrorType.ACCESS_DENIED, UPSA_IMMUTABLE_MESSAGE))
        .when(userMutationService).validateUserUpdatable(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REMOVE);
    op.setPath("/external_id");
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op)));

    assertEquals(ErrorType.ACCESS_DENIED, exception.getErrorType());
    assertTrue(exception.getMessage().contains(UPSA_IMMUTABLE_MESSAGE));
  }

  // --- Unified Error Messages ---

  @Test
  @DisplayName("UPSA user updating own role fails with immutable user error")
  void upsaUserUpdatesOwnRoleShouldFailWithImmutableError() {
    targetUser.setId(principalUser.getUserId());
    targetUser.setUserType(UserType.UPSA);
    mockPrincipal(principalUser, false);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);
    doThrow(new ReportPortalException(ErrorType.ACCESS_DENIED, UPSA_IMMUTABLE_MESSAGE))
        .when(userMutationService).validateUserUpdatable(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("/instance_role");
    op.setValue(UserRole.ADMINISTRATOR.name());
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op)));

    assertEquals(ErrorType.ACCESS_DENIED, exception.getErrorType());
    assertTrue(exception.getMessage().contains(UPSA_IMMUTABLE_MESSAGE));
  }

  // --- Other Operations ---

  @Test
  @DisplayName("Unsupported operation type - Should throw IllegalArgumentException")
  void unsupportedOperationTypeShouldThrowException() {
    targetUser.setId(principalUser.getUserId());
    mockPrincipal(principalUser, false);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(null);
    op.setPath("/email");
    op.setValue("test@example.com");
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op)));

    assertEquals("Unexpected operation: null", exception.getMessage());
  }

  @Test
  @DisplayName("Unsupported path for ADD operation - Should throw IllegalArgumentException")
  void unsupportedPathForAddOperationShouldThrowException() {
    targetUser.setId(principalUser.getUserId());
    mockPrincipal(principalUser, true);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(ADD);
    op.setPath("/email");
    op.setValue("test@example.com");
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op)));

    assertEquals("Unexpected path: '/email'", exception.getMessage());
  }

  @Test
  @DisplayName("Unsupported path for REMOVE operation - Should throw IllegalArgumentException")
  void unsupportedPathForRemoveOperationShouldThrowException() {
    targetUser.setId(principalUser.getUserId());
    mockPrincipal(principalUser, true);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REMOVE);
    op.setPath("/email");
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op)));

    assertEquals("Unexpected path: '/email'", exception.getMessage());
  }

  @Test
  @DisplayName("Unsupported path for REPLACE operation by admin - Should throw IllegalArgumentException")
  void unsupportedPathForReplaceOperationShouldThrowException() {
    targetUser.setId(principalUser.getUserId());
    mockPrincipal(principalUser, true);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("unsupported_path");
    op.setValue("value");
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op)));

    assertEquals("Unexpected path: 'unsupported_path'", exception.getMessage());
  }

  @Test
  @DisplayName("Unsupported path for REPLACE by non-admin own profile - Should fail with ACCESS_DENIED")
  void unsupportedPathForReplaceByNonAdminOwnProfileShouldFail() {
    targetUser.setId(principalUser.getUserId());
    mockPrincipal(principalUser, false);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("unsupported_path");
    op.setValue("value");
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op)));

    assertEquals(ErrorType.ACCESS_DENIED, exception.getErrorType());
    assertEquals("You do not have enough permissions. You can only update your own email and full name.",
        exception.getMessage());
  }

  // --- Test Cases for External Users (LDAP, SCIM, SAML, GITHUB) ---

  @Test
  @DisplayName("External user (LDAP, instance_role=USER) updates own email - Should delegate to UserMutationService")
  void externalUserLdapWithUserRoleUpdatesOwnEmailShouldSucceed() {
    targetUser.setId(principalUser.getUserId());
    targetUser.setUserType(UserType.LDAP);
    targetUser.setRole(UserRole.USER);
    mockPrincipal(principalUser, false);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("/email");
    op.setValue("new@example.com");
    patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op));

    verify(userMutationService).updateEmail(targetUser, "new@example.com", principalUser);
  }

  @Test
  @DisplayName("External user (SCIM, instance_role=USER) updates own full_name - Should delegate to UserMutationService")
  void externalUserScimWithUserRoleUpdatesOwnFullNameShouldSucceed() {
    targetUser.setId(principalUser.getUserId());
    targetUser.setUserType(UserType.SCIM);
    targetUser.setRole(UserRole.USER);
    mockPrincipal(principalUser, false);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("/full_name");
    op.setValue("New Name");
    patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op));

    verify(userMutationService).updateFullName(targetUser, "New Name", principalUser);
  }

  @Test
  @DisplayName("Admin updates valid account_type to INTERNAL - Should delegate to UserMutationService")
  void adminUpdatesValidAccountTypeToInternalShouldSucceed() {
    mockPrincipal(principalUser, true);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("/account_type");
    op.setValue("INTERNAL");
    patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op));

    verify(userMutationService).updateAccountType(targetUser, "INTERNAL");
  }

  @Test
  @DisplayName("Admin updates valid account_type to SCIM - Should delegate to UserMutationService")
  void adminUpdatesValidAccountTypeToScimShouldSucceed() {
    mockPrincipal(principalUser, true);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("/account_type");
    op.setValue("SCIM");
    patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op));

    verify(userMutationService).updateAccountType(targetUser, "SCIM");
  }
}
