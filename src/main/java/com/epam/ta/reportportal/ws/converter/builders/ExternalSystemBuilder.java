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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.database.entity.AuthType;
import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.ws.model.externalsystem.CreateExternalSystemRQ;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Builder for {@link ExternalSystem} entity
 *
 * @author Andrei_Ramanchuk
 */
@Service
@Scope("prototype")
public class ExternalSystemBuilder extends Builder<ExternalSystem> {

	@Autowired
	private BasicTextEncryptor simpleEncryptor;

	public ExternalSystemBuilder addExternalSystem(CreateExternalSystemRQ request, String rpProject) {
		// TODO null check
		getObject().setExternalSystemType(request.getExternalSystemType());
		getObject().setUrl(request.getUrl());
		getObject().setExternalSystemAuth(AuthType.findByName(request.getExternalSystemAuth()));
		getObject().setProjectRef(rpProject);

		getObject().setProject(request.getProject());
		getObject().setUsername(request.getUsername());
		String encryptedPass = simpleEncryptor.encrypt(request.getPassword());
		getObject().setPassword(encryptedPass);
		getObject().setAccessKey(request.getAccessKey());
		getObject().setDomain(request.getDomain());
		return this;
	}

	@Override
	protected ExternalSystem initObject() {
		return new ExternalSystem();
	}
}