/*
 * Copyright 2017 EPAM Systems
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

package com.epam.ta.reportportal.store.commons;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

/**
 * @author Pavel Bortnik
 */
abstract public class JsonbType implements UserType {

	private final ObjectMapper mapper;

	public JsonbType() {
		mapper = new ObjectMapper();
		mapper.enableDefaultTyping();
	}

	@Override
	public int[] sqlTypes() {
		return new int[] { Types.JAVA_OBJECT };
	}

	@Override
	abstract public Class<?> returnedClass();

	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		return Objects.equals(x, y);
	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		return Objects.hashCode(x);
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
		if (rs.getObject(names[0]) == null) {
			return null;
		}

		PGobject pgObject = (PGobject) rs.getObject(names[0]);

		try {
			return mapper.readValue(pgObject.getValue(), this.returnedClass());
		} catch (Exception e) {
			throw new ReportPortalException("Failed to convert String to Invoice: " + e.getMessage(), e);
		}
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
			throws HibernateException, SQLException {
		if (value == null) {
			st.setNull(index, Types.OTHER);
			return;
		}
		try {
			PGobject pGobject = new PGobject();
			pGobject.setType("json");
			pGobject.setValue(mapper.writeValueAsString(value));
			st.setObject(index, pGobject);
		} catch (final Exception ex) {
			throw new ReportPortalException("Failed to convert Invoice to String: " + ex.getMessage(), ex);
		}

	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(bos)) {
			// use serialization to create a deep copy

			oos.writeObject(value);
			oos.flush();

			ByteArrayInputStream bais = new ByteArrayInputStream(bos.toByteArray());
			return new ObjectInputStream(bais).readObject();
		} catch (ClassNotFoundException | IOException ex) {
			throw new HibernateException(ex);
		}
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		return null;
	}

	@Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return null;
	}

	@Override
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return null;
	}
}
