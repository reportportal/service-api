package com.epam.ta.reportportal.generator;

import org.jooq.util.DefaultGeneratorStrategy;
import org.jooq.util.Definition;

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
