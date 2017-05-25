package com.epam.ta.reportportal.core.imprt;

import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.web.multipart.MultipartFile;

public interface ImportLaunchHandler {
    OperationCompletionRS importLaunch(String projectId, String userName, String format, MultipartFile file);
}