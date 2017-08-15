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

package com.epam.ta.reportportal.ws.converter.builders;

/**
 * Generic builder holder
 *
 * @param <T>
 * @author Andrei Varabyeu
 */
@Deprecated
public abstract class Builder<T> {

    private T object;

    public Builder() {
        object = initObject();
    }

    /**
     * Builds object
     *
     * @return Built object
     */
    public T build() {
        T toReturn = object;
        object = initObject();
        return toReturn;
    }

    protected abstract T initObject();

    protected T getObject() {
        return object;
    }
}