package com.epam.ta.reportportal.ws.controller;

import com.epam.reportportal.extension.organizations.OrganizationsExtensionPoint;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import org.pf4j.Extension;

@Extension
public class OrgPlugin implements OrganizationsExtensionPoint {

  @Override
  public Object createOrganization(String name, ReportPortalUser reportPortalUser) {
    // custom logic etc
    return orgRepository.createOrganization(name);
  }

  @Override
  public void updateOrganization(String organization, ReportPortalUser reportPortalUser) {
    // custom logic etc
    return orgRepository.updateOrganization(organization);
  }
}
