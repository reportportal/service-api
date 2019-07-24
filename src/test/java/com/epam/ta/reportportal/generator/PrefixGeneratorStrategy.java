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

package com.epam.ta.reportportal.generator;

import org.jooq.codegen.DefaultGeneratorStrategy;
import org.jooq.meta.Definition;

/**
 * Strategy for generating JOOQ entities
 *
 * @author Yauheni_Martynau
 */
public class PrefixGeneratorStrategy extends DefaultGeneratorStrategy {

	@Override
	public String getJavaClassName(Definition definition, Mode mode) {
		String className = super.getJavaClassName(definition, mode);

		return "J" + className;
	}
}
