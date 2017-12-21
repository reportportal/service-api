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

package com.epam.ta.reportportal.core.configs;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Encrypt beans configuration for password values
 *
 * @author Andrei_Ramanchuk
 */
@Configuration
public class EncryptConfiguration {

	private static final String DEFAULT_PASS = "reportportal";

	@Bean(name = "basicEncryptor")
	public BasicTextEncryptor getBasicEncrypt() {
		BasicTextEncryptor basic = new BasicTextEncryptor();
		basic.setPassword(DEFAULT_PASS);
		return basic;
	}

	@Bean(name = "strongEncryptor")
	public StandardPBEStringEncryptor getStrongEncryptor() {
		StandardPBEStringEncryptor strong = new StandardPBEStringEncryptor();
		strong.setPassword(DEFAULT_PASS);
		strong.setAlgorithm("PBEWithMD5AndTripleDES");
		return strong;
	}
}