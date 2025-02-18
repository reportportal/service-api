package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.model.ProductVersion;
import com.epam.ta.reportportal.core.tms.dto.ProductVersionRS;
import org.springframework.stereotype.Component;

@Component
public class ProductVersionMapper implements DtoMapper<ProductVersion, ProductVersionRS> {

    @Override
    public ProductVersionRS convert(ProductVersion productVersion) {
        return new ProductVersionRS(productVersion.getId(),
                productVersion.getVersion(),
                productVersion.getDocumentation(),
                productVersion.getTestPlans(),
                productVersion.getMilestones());

    }
}
