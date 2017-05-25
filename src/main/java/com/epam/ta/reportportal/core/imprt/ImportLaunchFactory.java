package com.epam.ta.reportportal.core.imprt;

import com.epam.ta.reportportal.core.imprt.format.junit.ImportLaunch;
import com.epam.ta.reportportal.core.imprt.format.junit.ImportType;

/**
 * Created by Pavel_Bortnik on 5/25/2017.
 */
public interface ImportLaunchFactory {
    ImportLaunch getImportLaunch(ImportType type);
}
