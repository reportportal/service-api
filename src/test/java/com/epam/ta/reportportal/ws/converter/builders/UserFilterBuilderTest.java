package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.entity.filter.FilterSort;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.model.filter.Order;
import com.epam.ta.reportportal.ws.model.filter.UpdateUserFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.UserFilterCondition;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class UserFilterBuilderTest {

	@Test
	void userFilterBuilder() {
		final UpdateUserFilterRQ request = new UpdateUserFilterRQ();
		final String name = "name";
		request.setName(name);
		final String objectType = "Launch";
		request.setObjectType(objectType);
		request.setConditions(Sets.newHashSet(new UserFilterCondition("name", "eq", "value")));
		final Order order = new Order();
		order.setIsAsc(false);
		order.setSortingColumnName("column");
		request.setOrders(Collections.singletonList(order));
		final String description = "description";
		request.setDescription(description);
		final boolean share = true;
		request.setShare(share);
		final String owner = "owner";
		final Long projectId = 1L;

		final UserFilter userFilter = new UserFilterBuilder().addFilterRq(request).addOwner(owner).addProject(projectId).get();

		assertEquals(name, userFilter.getName());
		assertEquals(description, userFilter.getDescription());
		assertEquals(share, userFilter.isShared());
		assertEquals(owner, userFilter.getOwner());
		assertEquals(projectId, userFilter.getProject().getId());
		assertEquals(Launch.class, userFilter.getTargetClass().getClassObject());
		assertThat(userFilter.getFilterCondition()).containsExactlyInAnyOrder(FilterCondition.builder().eq("name", "value").build());
		final FilterSort filterSort = new FilterSort();
		filterSort.setDirection(Sort.Direction.DESC);
		filterSort.setField("column");
		assertThat(userFilter.getFilterSorts()).containsExactlyInAnyOrder(filterSort);
	}
}