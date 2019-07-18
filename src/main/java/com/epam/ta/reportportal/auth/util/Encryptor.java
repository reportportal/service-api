/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.auth.util;

import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Andrei Varabyeu
 */
@Component
public class Encryptor implements InitializingBean {

	@Value("${rp.auth.encryptor.password:reportportal}")
	private String password;

	private BasicTextEncryptor textEncryptor;

	/**
	 * Encrypts string
	 *
	 * @param str String to be encrypted
	 * @return Encrypted string
	 */
	public String encrypt(String str) {
		return this.textEncryptor.encrypt(str);
	}

	/**
	 * Decrypts string
	 *
	 * @param str String to be decrypted
	 * @return Decrypted string
	 */
	public String decrypt(String str) {
		return this.textEncryptor.decrypt(str);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(password);
	}
}
