package com.epam.reportportal.base.core.tms.service;

public interface TmsDisplayIdService {

  String generateTestCaseDisplayId(Long projectId);

  String generateTestPlanDisplayId(Long projectId);

  String generateMilestoneDisplayId(Long projectId);

  String generateManualLaunchDisplayId(Long projectId);
}
