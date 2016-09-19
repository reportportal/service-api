/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

package com.epam.ta.reportportal.core.favorites.impl;

import static com.epam.ta.reportportal.commons.Preconditions.*;
import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.core.favorites.IAddToFavoritesHandler;
import com.epam.ta.reportportal.database.dao.DashboardRepository;
import com.epam.ta.reportportal.database.dao.FavoriteResourceRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.ReportPortalRepository;
import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.Project.UserConfig;
import com.epam.ta.reportportal.database.entity.favorite.FavoriteResource;
import com.epam.ta.reportportal.database.entity.sharing.AclPermissions;
import com.epam.ta.reportportal.database.entity.sharing.Shareable;
import com.epam.ta.reportportal.util.LazyReference;
import com.epam.ta.reportportal.ws.converter.DashboardResourceAssembler;
import com.epam.ta.reportportal.ws.converter.builders.FavoriteResourceBuilder;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import com.epam.ta.reportportal.ws.model.favorites.AddFavoriteResourceRQ;
import com.epam.ta.reportportal.ws.model.favorites.FavoriteResourceTypes;

/**
 * Default implementation of {@link IAddToFavoritesHandler}
 * 
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class AddToFavoritesHandler implements IAddToFavoritesHandler {

	@Autowired
	private FavoriteResourceRepository favoriteResourceRepository;

	@Autowired
	private DashboardRepository dashboardRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private DashboardResourceAssembler resourceAssembler;

	@Resource(name = "favoritesRepositories")
	private Map<FavoriteResourceTypes, ReportPortalRepository<? extends Shareable, String>> repositories;

	@Autowired
	@Qualifier("favoriteResourceBuilder.reference")
	private LazyReference<FavoriteResourceBuilder> favoriteResourceBuilder;

	@Override
	public DashboardResource add(AddFavoriteResourceRQ addFavoriteResourceRQ, String userName, String projectName) {
		validate(addFavoriteResourceRQ, userName, projectName);
		FavoriteResource favoriteResource = favoriteResourceBuilder.get().addProject(projectName).addUser(userName)
				.addCreateRQ(addFavoriteResourceRQ).build();
		favoriteResourceRepository.save(favoriteResource);

		Dashboard dashboard = dashboardRepository.findOne(addFavoriteResourceRQ.getResourceId());
		return resourceAssembler.toResource(dashboard);
	}

	/**
	 * Validate is addFavoriteResourceRQ contains correct resource type and
	 * resource id, and is saving favorite resource have already exist.
	 */
	private void validate(AddFavoriteResourceRQ addFavoriteResourceRQ, String userName, String projectName) {
		Project project = projectRepository.findOne(projectName);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);

		UserConfig assigned = project.getUsers().get(userName);
		expect(assigned, notNull()).verify(ACCESS_DENIED, formattedSupplier("User '{}' not assigned to target project", userName));

		ReportPortalRepository<? extends Shareable, String> repository = repositories.get(addFavoriteResourceRQ.getType());
		expect(repository, notNull()).verify(UNABLE_ADD_TO_FAVORITE,
				formattedSupplier("Unknown resource type '{}'.", addFavoriteResourceRQ.getType()));

		Shareable shareable = repository.findOne(addFavoriteResourceRQ.getResourceId());

		expect(shareable, notNull()).verify(UNABLE_ADD_TO_FAVORITE,
				formattedSupplier("Incorrect resource id '{}'.", addFavoriteResourceRQ.getResourceId()));

		expect(shareable.getAcl().getEntries(), contains(hasACLPermission(projectName, AclPermissions.READ))).verify(UNABLE_ADD_TO_FAVORITE,
				formattedSupplier("Resource with id '{}' isn't shared to project.", addFavoriteResourceRQ.getResourceId(), projectName));
		expect(shareable.getAcl().getOwnerUserId(), not(equalTo(userName.toLowerCase()))).verify(UNABLE_ADD_TO_FAVORITE,
				formattedSupplier("User can't add own resource to favorites"));

		List<FavoriteResource> resources = favoriteResourceRepository.findByFilter(
				Utils.getUniqueFavoriteFilter(userName, addFavoriteResourceRQ.getType().name(), addFavoriteResourceRQ.getResourceId()));
		expect(resources, not(NOT_EMPTY_COLLECTION)).verify(UNABLE_ADD_TO_FAVORITE,
				formattedSupplier("Favorite resource with resource id '{}', type '{}' have already exist for user '{}'.",
						addFavoriteResourceRQ.getResourceId(), addFavoriteResourceRQ.getType(), userName));
	}
}