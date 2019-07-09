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
