package com.epam.reportportal.core.user.patch;

import static com.epam.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.reportportal.api.model.OperationType.ADD;
import static com.epam.reportportal.api.model.OperationType.REMOVE;
import static com.epam.reportportal.api.model.OperationType.REPLACE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.epam.reportportal.api.model.PatchOperation;
import com.epam.reportportal.core.user.UserService;
import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserType;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.util.SecurityContextUtils;
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

  @Mock
  private UserService userService;

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
  @DisplayName("Profile owner (INTERNAL) updates own email - Should succeed")
  void profileOwnerInternalUpdatesOwnEmailShouldSucceed() {
    targetUser.setId(principalUser.getUserId()); // Make target user the principal
    mockPrincipal(principalUser, false);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("email");
    op.setValue("new_email@example.com");
    patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op));

    assertEquals("new_email@example.com", targetUser.getEmail());
  }

  @Test
  @DisplayName("Profile owner (INTERNAL) updates own full_name - Should succeed")
  void profileOwnerInternalUpdatesOwnFullNameShouldSucceed() {
    targetUser.setId(principalUser.getUserId()); // Make target user the principal
    mockPrincipal(principalUser, false);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("full_name");
    op.setValue("New Full Name");
    patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op));

    assertEquals("New Full Name", targetUser.getFullName());
  }

  @Test
  @DisplayName("Profile owner (INTERNAL) updates own role - Should fail with ACCESS_DENIED")
  void profileOwnerInternalUpdatesOwnRoleShouldFail() {
    targetUser.setId(principalUser.getUserId()); // Make target user the principal
    mockPrincipal(principalUser, false);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("role");
    op.setValue(UserRole.ADMINISTRATOR.name());
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op)));

    assertEquals(ErrorType.ACCESS_DENIED, exception.getErrorType());
    assertEquals(
        "You do not have enough permissions. You can only update your own email and full name. Other fields can only be changed by an administrator for you.",
        exception.getMessage());
  }

  @Test
  @DisplayName("Profile owner (INTERNAL) updates another user's email - Should fail with ACCESS_DENIED")
  void profileOwnerInternalUpdatesAnotherUserEmailShouldFail() {
    mockPrincipal(principalUser, false); // Principal is not admin
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("email");
    op.setValue("another@example.com");
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op)));

    assertEquals(ErrorType.ACCESS_DENIED, exception.getErrorType());
    assertEquals("You do not have enough permissions. You are not allowed to update this user's profile.",
        exception.getMessage());
  }

  // --- Test Cases for Admin ---

  @Test
  @DisplayName("Admin updates another user's (INTERNAL) email - Should succeed")
  void adminUpdatesAnotherUserInternalEmailShouldSucceed() {
    mockPrincipal(principalUser, true); // Principal is admin
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("email");
    op.setValue("admin_changed@example.com");
    patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op));

    assertEquals("admin_changed@example.com", targetUser.getEmail());
  }

  @Test
  @DisplayName("Admin updates another user's (INTERNAL) role - Should succeed")
  void adminUpdatesAnotherUserInternalRoleShouldSucceed() {
    mockPrincipal(principalUser, true); // Principal is admin
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("role");
    op.setValue(UserRole.ADMINISTRATOR.name());
    patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op));

    assertEquals(UserRole.ADMINISTRATOR, targetUser.getRole());
  }

  @Test
  @DisplayName("Admin updates own role - Should fail with ACCESS_DENIED")
  void adminUpdatesOwnRoleShouldFail() {
    targetUser.setId(principalUser.getUserId()); // Admin is updating their own profile
    mockPrincipal(principalUser, true); // Principal is admin
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("role");
    op.setValue(UserRole.ADMINISTRATOR.name());
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op)));

    assertEquals(ErrorType.ACCESS_DENIED, exception.getErrorType());
    assertEquals(
        "You do not have enough permissions. You can only update your own email and full name. Other fields can only be changed by an administrator for you.",
        exception.getMessage());
  }

  // --- Test Cases for UPSA Users ---

  @Test
  @DisplayName("Profile owner (UPSA) updates own email - Should fail with ACCESS_DENIED")
  void profileOwnerUpsaUpdatesOwnEmailShouldFail() {
    targetUser.setId(principalUser.getUserId()); // Make target user the principal
    targetUser.setUserType(UserType.UPSA);
    mockPrincipal(principalUser, false); // Principal is not admin
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("email");
    op.setValue("upsa_new@example.com");
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op)));

    assertEquals(ErrorType.ACCESS_DENIED, exception.getErrorType());
    assertEquals("You do not have enough permissions. You are not allowed to update this user's profile.",
        exception.getMessage());
  }

  @Test
  @DisplayName("Admin updates UPSA user's email - Should fail with ACCESS_DENIED")
  void adminUpdatesUpsaUserEmailShouldFail() {
    targetUser.setUserType(UserType.UPSA);
    mockPrincipal(principalUser, true); // Principal is admin
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("email");
    op.setValue("admin_upsa_new@example.com");
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op)));

    assertEquals(ErrorType.ACCESS_DENIED, exception.getErrorType());
    assertEquals("You do not have enough permissions. Email and full name of UPSA users cannot be updated.",
        exception.getMessage());
  }

  @Test
  @DisplayName("Admin updates UPSA user's external_id (REPLACE) - Should succeed")
  void adminUpdatesUpsaUserExternalIdReplaceShouldSucceed() {
    targetUser.setUserType(UserType.UPSA);
    mockPrincipal(principalUser, true); // Principal is admin
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("external_id");
    op.setValue("new_external_id");
    patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op));

    assertEquals("new_external_id", targetUser.getExternalId());
  }

  @Test
  @DisplayName("Admin updates UPSA user's external_id (ADD) - Should succeed")
  void adminUpdatesUpsaUserExternalIdAddShouldSucceed() {
    targetUser.setUserType(UserType.UPSA);
    targetUser.setExternalId(null); // Ensure it's null for ADD operation
    mockPrincipal(principalUser, true); // Principal is admin
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(ADD);
    op.setPath("external_id");
    op.setValue("added_external_id");
    patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op));

    assertEquals("added_external_id", targetUser.getExternalId());
  }

  @Test
  @DisplayName("Admin updates UPSA user's external_id (REMOVE) - Should succeed")
  void adminUpdatesUpsaUserExternalIdRemoveShouldSucceed() {
    targetUser.setUserType(UserType.UPSA);
    targetUser.setExternalId("existing_external_id");
    mockPrincipal(principalUser, true); // Principal is admin
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REMOVE);
    op.setPath("external_id");
    patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op));

    assertNull(targetUser.getExternalId());
  }

  // --- Other Operations ---

  @Test
  @DisplayName("Unsupported operation type - Should throw IllegalArgumentException")
  void unsupportedOperationTypeShouldThrowException() {
    targetUser.setId(principalUser.getUserId());
    mockPrincipal(principalUser, false);
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(null); // Null operation type
    op.setPath("email");
    op.setValue("test@example.com");
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op)));

    assertEquals("Unexpected operation: null", exception.getMessage());
  }

  @Test
  @DisplayName("Unsupported path for ADD operation - Should throw UnsupportedOperationException")
  void unsupportedPathForAddOperationShouldThrowException() {
    targetUser.setId(principalUser.getUserId());
    mockPrincipal(principalUser, true); // Admin can do more, but not this
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(ADD);
    op.setPath("email");
    op.setValue("test@example.com");
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op)));

    assertEquals("Unexpected path: 'email'", exception.getMessage());
  }

  @Test
  @DisplayName("Unsupported path for REMOVE operation - Should throw UnsupportedOperationException")
  void unsupportedPathForRemoveOperationShouldThrowException() {
    targetUser.setId(principalUser.getUserId());
    mockPrincipal(principalUser, true); // Admin can do more, but not this
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REMOVE);
    op.setPath("email");
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op)));

    assertEquals("Unexpected path: 'email'", exception.getMessage());
  }

  @Test
  @DisplayName("Unsupported path for REPLACE operation - Should throw UnsupportedOperationException")
  void unsupportedPathForReplaceOperationShouldThrowException() {
    targetUser.setId(principalUser.getUserId());
    mockPrincipal(principalUser, true); // Admin can do more, but not this
    when(userService.findById(targetUser.getId())).thenReturn(targetUser);

    PatchOperation op = new PatchOperation();
    op.setOp(REPLACE);
    op.setPath("unsupported_field");
    op.setValue("value");
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> patchUserHandler.patchUser(targetUser.getId(), Collections.singletonList(op)));

    assertEquals(
        "You do not have enough permissions. You can only update your own email and full name. Other fields can only be changed by an administrator for you.",
        exception.getMessage());
  }
}
