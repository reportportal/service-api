/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.imprt.impl;

import static com.epam.ta.reportportal.core.imprt.FileExtensionConstant.XML_EXTENSION;
import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.imprt.impl.junit.XunitParseJob;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.model.launch.LaunchImportRQ;
import com.epam.reportportal.rules.exception.ErrorType;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.inject.Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class XmlImportStrategy extends AbstractImportStrategy {

  @Autowired
  private Provider<XunitParseJob> xmlParseJobProvider;

  @Override
  public String importLaunch(MembershipDetails membershipDetails, ReportPortalUser user,
      File file, String baseUrl, LaunchImportRQ rq) {
    try {
      return processXmlFile(file, membershipDetails, user, baseUrl, rq);
    } finally {
      try {
        ofNullable(file).ifPresent(File::delete);
      } catch (Exception e) {
        LOGGER.error("File '{}' was not successfully deleted.", file.getName(), e);
      }
    }
  }

  private String processXmlFile(File xml, MembershipDetails membershipDetails,
      ReportPortalUser user, String baseUrl, LaunchImportRQ rq) {
    //copy of the launch's id to use it in catch block if something goes wrong
    String savedLaunchId = null;
    try (InputStream xmlStream = new FileInputStream(xml)) {
      String launchId = startLaunch(membershipDetails, user,
          xml.getName().substring(0, xml.getName().indexOf("." + XML_EXTENSION)), rq
      );
      savedLaunchId = launchId;
      XunitParseJob job = xmlParseJobProvider.get()
          .withParameters(membershipDetails, launchId, user, xmlStream,
              isSkippedNotIssue(rq.getAttributes())
          );
      ParseResults parseResults = job.call();
      finishLaunch(launchId, membershipDetails, user, parseResults, baseUrl);
      return launchId;
    } catch (Exception e) {
      updateBrokenLaunch(savedLaunchId);
      throw new ReportPortalException(ErrorType.IMPORT_FILE_ERROR, cleanMessage(e));
    }
  }
}
