package com.epam.ta.reportportal.ws.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ORGANIZATION_MANAGER;

import com.epam.reportportal.api.OrganizationGroupApi;
import com.epam.reportportal.api.model.CreateGroupRequest;
import com.epam.reportportal.api.model.CreateOrgGroupRequest;
import com.epam.reportportal.api.model.GroupInfo;
import com.epam.reportportal.api.model.GroupPage;
import com.epam.reportportal.api.model.OrgGroupPage;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.group.GroupExtensionPoint;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller for handling organization group-related requests.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@RestController
public class OrganizationGroupController implements OrganizationGroupApi {

  private final PluginManager pluginManager;

  /**
   * Constructor for the {@link OrganizationGroupController} class.
   *
   * @param pluginManager Plugin manager
   */
  @Autowired
  public OrganizationGroupController(PluginManager pluginManager) {
    this.pluginManager = pluginManager;
  }

  @Override
  @PreAuthorize(ORGANIZATION_MANAGER)
  public ResponseEntity<GroupInfo> createOrgGroup(Long orgId, CreateOrgGroupRequest request) {
    var group = getGroupExtension().createGroup(
        toCreateGroupRequest(request, orgId),
        getPrincipal().getUserId()
    );
    return new ResponseEntity<>(group, HttpStatus.CREATED);
  }

  @Override
  @PreAuthorize(ORGANIZATION_MANAGER)
  public ResponseEntity<OrgGroupPage> getOrgGroups(
      Long orgId,
      Integer offset,
      Integer limit,
      String order,
      String sort
  ) {
    var groupPage = getGroupExtension().getGroups(offset, limit, order, sort, orgId);
    return ResponseEntity.ok(toOrgGroupPage(groupPage));
  }

  private GroupExtensionPoint getGroupExtension() {
    return pluginManager.getExtensions(GroupExtensionPoint.class)
        .stream()
        .findFirst()
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED)
        );
  }

  private ReportPortalUser getPrincipal() {
    return (ReportPortalUser) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();
  }

  private CreateGroupRequest toCreateGroupRequest(CreateOrgGroupRequest request, Long orgId) {
    return CreateGroupRequest.builder()
        .name(request.getName())
        .slug(request.getSlug())
        .orgId(orgId)
        .build();
  }

  private OrgGroupPage toOrgGroupPage(GroupPage groupPage) {
    return OrgGroupPage.builder()
        .items(groupPage.getItems())
        .offset(groupPage.getOffset())
        .limit(groupPage.getLimit())
        .totalCount(groupPage.getTotalCount())
        .sort(groupPage.getSort())
        .order(groupPage.getOrder())
        .build();
  }
}
