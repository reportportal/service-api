package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.ProductVersionRQ;
import com.epam.reportportal.base.core.tms.dto.TmsProductVersionRS;

public interface ProductVersionService extends CrudService<ProductVersionRQ,
    TmsProductVersionRS, Long> {

}
