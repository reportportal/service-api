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

import com.epam.ta.reportportal.filesystem.DataEncoder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Dzianis_Shybeka
 */
public class DataEncoderTest {

	private DataEncoder encoder;

	@Before
	public void setUp() throws Exception {

		encoder = new DataEncoder();
	}

	@Test
	public void encode_decode_with_empty_input() throws Exception {
		//  given:
		String input = "";

		//  when:
		String encoded = encoder.encode(input);

		//  then:
		assertTrue(encoded.isEmpty());

		//	and:

		//	when:
		String decoded = encoder.decode(encoded);

		//	then:
		assertTrue(decoded.isEmpty());
	}

	@Test
	public void encode_decode_with_null_input() throws Exception {
		//  given:
		String input = null;

		//  when:
		String encoded = encoder.encode(input);

		//  then:
		assertNull(encoded);

		//	and:

		//	when:
		String decoded = encoder.decode(encoded);

		//	then:
		assertNull(decoded);
	}

	@Test
	public void encode_decode() throws Exception {
		//  given:
		String input = "/data/path/file.ext";

		//  when:
		String encoded = encoder.encode(input);

		//  then:
		assertFalse(encoded.isEmpty());

		//	and:

		//	when:
		String decoded = encoder.decode(encoded);

		//	then:
		assertFalse(decoded.isEmpty());
		assertEquals(input, decoded);
	}
}
