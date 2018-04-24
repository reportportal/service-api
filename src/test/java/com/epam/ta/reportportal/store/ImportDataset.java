package com.epam.ta.reportportal.store;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ImportDataset {
	String value();
}
