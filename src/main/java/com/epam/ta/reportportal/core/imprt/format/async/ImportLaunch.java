package com.epam.ta.reportportal.core.imprt.format.async;

import org.springframework.web.multipart.MultipartFile;

public interface ImportLaunch {
    String importLaunch(String projectId, String userName, MultipartFile file);
}
