package com.epam.ta.reportportal.core.tms.controller;

import com.epam.ta.reportportal.core.tms.dto.ProductVersionRQ;
import com.epam.ta.reportportal.core.tms.dto.ProductVersionRS;
import com.epam.ta.reportportal.core.tms.service.ProductVersionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/project/{projectId}/tms/productversion")
@Tag(name = "Product Version", description = "Product Version API collection")
@RequiredArgsConstructor
public class ProductVersionController {

    private final ProductVersionService productVersionService;

    @GetMapping("/{productVersionId}")
    ProductVersionRS getById(@PathVariable("projectId") final long projectId,
                             @PathVariable("productVersionId") final long productVersionId) {
        return productVersionService.getById(projectId, productVersionId);
    }

    @PostMapping
    ProductVersionRS createVersion(@PathVariable("projectId") final long projectId,
                                   @RequestBody final ProductVersionRQ inputDto) {
        return productVersionService.create(projectId, inputDto);
    }

    @PutMapping("/{productVersionId}")
    ProductVersionRS updateVersion(@PathVariable("projectId") final long projectId,
                                   @PathVariable("productVersionId") final long productVersionId,
                                   @RequestBody final ProductVersionRQ inputDto) {
        return productVersionService.update(projectId, productVersionId,inputDto);
    }

    @DeleteMapping("/{productVersionId}")
    void deleteVersion(@PathVariable("projectId") final long projectId,
                       @PathVariable("productVersionId") final long productVersionId) {
        productVersionService.delete(projectId, productVersionId);
    }

}
