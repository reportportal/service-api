/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */ 
/*
 * This file is part of Report Portal.
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.core.filter.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.database.dao.UserFilterRepository;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.CriteriaHolder;
import com.epam.ta.reportportal.database.search.CriteriaMap;
import com.epam.ta.reportportal.database.search.CriteriaMapFactory;
import com.epam.ta.reportportal.database.search.QueryBuilder;
import com.epam.ta.reportportal.database.search.QueryBuilder.ComplexSearchCriteria;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.filter.UserFilterEntity;

/**
 * Contains validations for {@link UserFilter}s objects.
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class UserFilterValidationService {

	@Autowired
	private UserFilterRepository userFilterRepository;

	@Autowired
	private CriteriaMapFactory criteriaMapFactory;

	/**
	 * Check is user filter name unique for specified user under specified project, if name isn't
	 * unique throws exception.
	 *
	 * @param userName
	 * @param filterName
	 * @param projectName
	 * @throws UserFilterAlreadyExistException
	 */
	public void isFilterNameUnique(String userName, String filterName, String projectName) {
		UserFilter existingFilter = userFilterRepository.findOneByName(userName, filterName, projectName);
		BusinessRule.expect(existingFilter, Predicates.isNull()).verify(ErrorType.USER_FILTER_ALREADY_EXISTS, filterName, userName,
				projectName);
	}

	/**
	 * Validate is {@link UserFilterEntity} contains correct data:<br>
	 * name of condition known to ws;<br>
	 * names of filtering fields known to ws.<br>
	 *
	 * @param type
	 * @param filterEntities
	 */
	public Set<UserFilterEntity> validateUserFilterEntities(Class<?> type, Set<UserFilterEntity> filterEntities) {
		Set<UserFilterEntity> remastered = new HashSet<>();
		CriteriaMap<?> ctriteriaMap = criteriaMapFactory.getCriteriaMap(type);
		synchronized (this) {
			for (Iterator<UserFilterEntity> it = filterEntities.iterator(); it.hasNext();) {
				UserFilterEntity userFilterEntity = it.next();
				ComplexSearchCriteria filterCriteria = QueryBuilder.filterSearchCriteriaPreProcessor(userFilterEntity.getFilteringField());
				Optional<CriteriaHolder> holderOptional = ctriteriaMap.getCriteriaHolderUnchecked(filterCriteria.getGlobalSearchCriteria());

				BusinessRule.expect(holderOptional, Preconditions.IS_PRESENT).verify(ErrorType.INCORRECT_FILTER_PARAMETERS,
						Suppliers.formattedSupplier("Filter parameter {} is not defined", filterCriteria.getGlobalSearchCriteria()));

				CriteriaHolder updated = null;
				boolean reload = false;
				if (holderOptional.get().getQueryCriteria().contains(IssueCounter.DEFECTS_FOR_DB)) {
					/* Re-master search criteria */
					String rebuildedSearchCriteria = filterCriteriaRebuilder(userFilterEntity.getFilteringField());

					/* Re-master entity */
					UserFilterEntity newEntity = new UserFilterEntity(rebuildedSearchCriteria, userFilterEntity.getCondition(),
							userFilterEntity.getValue(), userFilterEntity.getIsNegative());

					/* Remove 'invalid' entity and paste new one */
					it.remove();
					remastered.add(newEntity);
					reload = true;
					updated = new CriteriaHolder(holderOptional.get().getFilterCriteria(), rebuildedSearchCriteria, Integer.class,
							holderOptional.get().isReference());
				}

				BusinessRule.expect(holderOptional.isPresent(), Predicates.equalTo(Boolean.TRUE)).verify(
						ErrorType.BAD_SAVE_USER_FILTER_REQUEST,
						Suppliers.formattedSupplier("Filtering field '{}' is unknown.", userFilterEntity.getFilteringField()));
				Condition condition = Condition.findByMarker(userFilterEntity.getCondition());
				BusinessRule.expect(condition, Predicates.notNull()).verify(ErrorType.BAD_SAVE_USER_FILTER_REQUEST,
						Suppliers.formattedSupplier("Incorrect filter's entity condition '{}'.", userFilterEntity.getCondition()));
				if (!reload) {
					condition.validate(holderOptional.get(), userFilterEntity.getValue(), userFilterEntity.getIsNegative(),
							ErrorType.BAD_SAVE_USER_FILTER_REQUEST);
					// check is values have correct type(is it possible to cast user
					// filter entity value to to type specified in criteria holder)
					condition.castValue(holderOptional.get(), userFilterEntity.getValue(), ErrorType.BAD_SAVE_USER_FILTER_REQUEST);
				} else {
					condition.validate(updated, userFilterEntity.getValue(), userFilterEntity.getIsNegative(),
							ErrorType.BAD_SAVE_USER_FILTER_REQUEST);
					// check is values have correct type(is it possible to cast user
					// filter entity value to to type specified in criteria holder)
					condition.castValue(updated, userFilterEntity.getValue(), ErrorType.BAD_SAVE_USER_FILTER_REQUEST);
				}
			}
		}
		filterEntities.addAll(remastered);
		return filterEntities;
	}

	/**
	 * Check is sorting column name known to ws.
	 */
	public void validateSortingColumnName(Class<?> type, String sortingColumnName) {
		BusinessRule.expect(criteriaMapFactory.getCriteriaMap(type).getCriteriaHolderUnchecked(sortingColumnName).isPresent(),
				Predicates.equalTo(Boolean.TRUE)).verify(ErrorType.BAD_SAVE_USER_FILTER_REQUEST,
						Suppliers.formattedSupplier("Column for sorting with name '{}' is unknown.", sortingColumnName));
	}

	/**
	 * If input filter criteria contains defects reference, then it should be validated on
	 * containing defect sub-type additional extension. If there is no defect sub-type, then TOTAL
	 * reference will be used.<br>
	 *
	 * <b>NOTE:</b> should be used after check on containing <i>statistics.issueCounter</i> criteria
	 * part.
	 *
	 * @param input
	 *            - input criteria
	 * @return String - processed criteria
	 */
	private static String filterCriteriaRebuilder(String input) {
		String[] split = input.split("\\$");
		StringBuilder builder = new StringBuilder(input);
		/* If this is defect type filter without sub-type */
		if (split.length <= 3)
			builder.append("$").append(IssueCounter.GROUP_TOTAL);
		return builder.toString();
	}
}