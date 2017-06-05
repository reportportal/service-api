package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.filter.SelectionParameters;
import com.epam.ta.reportportal.ws.model.filter.UserFilterEntity;
import com.epam.ta.reportportal.ws.model.filter.UserFilterResource;
import com.google.common.collect.Sets;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public final class UserFilterConverter {

    private UserFilterConverter() {
        //static only
    }

    public static final Function<UserFilter, UserFilterResource> TO_RESOURCE = filter -> {
        UserFilterResource resource = new UserFilterResource();
        if (Optional.ofNullable(filter).isPresent()) {
            resource.setFilterId(filter.getId());
            resource.setName(filter.getName());
            resource.setDescription(filter.getDescription());
            Filter f = filter.getFilter();
            if (Optional.ofNullable(f).isPresent()) {
                resource.setObjectType(f.getTarget().getSimpleName().toLowerCase());
                resource.setEntities(UserFilterConverter.TO_ENTITIES.apply(f));
            }
            if (Optional.ofNullable(filter.getSelectionOptions()).isPresent()) {
                SelectionParameters selectionParameters = new SelectionParameters();
                selectionParameters.setSortingColumnName(filter.getSelectionOptions().getSortingColumnName());
                selectionParameters.setIsAsc(filter.getSelectionOptions().isAsc());
                selectionParameters.setPageNumber(filter.getSelectionOptions().getPageNumber());
                resource.setSelectionParameters(selectionParameters);
            }
            if (Optional.ofNullable(filter.getAcl()).isPresent()) {
                resource.setOwner(filter.getAcl().getOwnerUserId());
                resource.setShare(!filter.getAcl().getEntries().isEmpty());
            }
        }
        return resource;
    };

    /**
     * Transform Set<{@link Filter}> to Set<{@link UserFilterEntity}>
     *
     * @param filterEntities
     * @return Set<ComplexFilterEntity>
     */
    private static final Function<Filter, Set<UserFilterEntity>> TO_ENTITIES = filter -> {
        Set<UserFilterEntity> result = Sets.newLinkedHashSet();
        filter.getFilterConditions().forEach(condition -> {
            UserFilterEntity userFilterEntity = new UserFilterEntity();
            userFilterEntity.setCondition(Optional.ofNullable(condition.getCondition().getMarker())
                    .orElse(null));
            userFilterEntity.setIsNegative(condition.isNegative());
            userFilterEntity.setValue(condition.getValue());
            userFilterEntity.setFilteringField(condition.getSearchCriteria());
            result.add(userFilterEntity);
        });
        return result;
    };

}
