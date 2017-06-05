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

package com.epam.ta.reportportal.exception;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;

/**
 * Created by Andrey_Ivanov1 on 05-Jun-17.
 */

@RunWith(SpringJUnit4ClassRunner.class)
public class PermissionNotDefinedExceptionTest {

    private final String MESSAGE = "PermissionNotDefinedException appeared";
    private final long EXPECTED_SERIAL_VERSION_UID =
            (long) ReflectionTestUtils.getField(PermissionNotDefinedException.class, "serialVersionUID");

    @Test(expected = PermissionNotDefinedException.class)
    public void permissionNotDefinedExceptionLocalTest() {
            try {
                throw new PermissionNotDefinedException(MESSAGE);
            } catch (PermissionNotDefinedException e) {
                assertEquals(MESSAGE, e.getMessage());
                long serialVersionUID = (long) ReflectionTestUtils.getField(e, "serialVersionUID");
                assertEquals(EXPECTED_SERIAL_VERSION_UID, serialVersionUID);
                throw e;
            }
    }



}