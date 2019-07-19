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

package com.epam.ta.reportportal.auth.permissions;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * * Specifies list of permission names to be assigned to some
 * {@link com.epam.ta.reportportal.auth.permissions.Permission} implementation<br>
 * <b>BE AWARE</b> that each permissions should be marked with
 * {@link com.epam.ta.reportportal.auth.permissions.LookupPermission} annotation
 * to be assigned to some permission name. Without this permission will be
 * ignored by {@link org.springframework.security.access.PermissionEvaluator}
 *
 * @author Andrei Varabyeu
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface LookupPermission {
	String[] value();
}