/*
 *
 *  * Copyright (C) 2018 EPAM Systems
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.epam.ta.reportportal.store.filesystem;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author Dzianis_Shybeka
 * <p>
 * Class to encode/decode values to url compatible format
 */
@Component
public class DataEncoder {

	/**
	 * Encode provided data.
	 *
	 * @param data
	 * @return
	 */
	public String encode(String data) {

		return StringUtils.isEmpty(data) ?
				data :
				Base64.getUrlEncoder().withoutPadding().encodeToString(data.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Decode provided data.
	 *
	 * @param data
	 * @return
	 */
	public String decode(String data) {

		return StringUtils.isEmpty(data) ? data : new String(Base64.getUrlDecoder().decode(data), StandardCharsets.UTF_8);
	}
}
