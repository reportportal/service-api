/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.launch.export;

import com.epam.ta.reportportal.core.jasper.GetJasperReportHandler;
import com.epam.ta.reportportal.core.jasper.ReportFormat;
import com.epam.ta.reportportal.core.jasper.constants.LaunchReportConstants;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.user.User;
import java.util.Collection;
import java.util.Map;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Service responsible for generating launch reports using JasperReports.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class LaunchReportService {

  private final GetJasperReportHandler<Launch> reportHandler;
  private final UserRepository userRepository;

  public LaunchReportService(
      @Qualifier("launchJasperReportHandler") GetJasperReportHandler<Launch> reportHandler,
      UserRepository userRepository
  ) {
    this.reportHandler = reportHandler;
    this.userRepository = userRepository;
  }

  /**
   * Generates a report for the given launch and test items in the specified format.
   *
   * @param launch    the launch to generate the report for
   * @param testItems the test items to include in the report
   * @param username  the fallback username if launch owner is not found
   * @param format    the format of the report
   * @return the generated report as a byte array
   */
  public byte[] generateReport(Launch launch, Collection<TestItemPojo> testItems, String username,
      ReportFormat format) {
    Map<String, Object> params = reportHandler.convertParams(launch);
    String owner = userRepository.findById(launch.getUserId())
        .map(User::getFullName)
        .orElse(username);
    params.put(LaunchReportConstants.OWNER, owner);
    params.put(LaunchReportConstants.TEST_ITEMS, testItems);
    if (!ReportFormat.PDF.equals(format)) {
      params.put(JRParameter.IS_IGNORE_PAGINATION, true);
    }
    JasperPrint jasperPrint = reportHandler.getJasperPrint(params, new JREmptyDataSource());
    return reportHandler.exportReportBytes(format, jasperPrint);
  }

  public ReportFormat resolveFormat(String format) {
    return reportHandler.getReportFormat(format);
  }

}
