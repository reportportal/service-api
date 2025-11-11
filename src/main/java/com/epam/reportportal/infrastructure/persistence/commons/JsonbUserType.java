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

package com.epam.reportportal.infrastructure.persistence.commons;

import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.SerializationException;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;
import org.springframework.util.ObjectUtils;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public abstract class JsonbUserType<T> implements UserType<T> {

  private final ObjectMapper mapper;

  protected JsonbUserType() {
    mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Override
  public int getSqlType() {
    return Types.JAVA_OBJECT;
  }

  @Override
  public T nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session,
      Object owner) throws SQLException {
    if (rs.getObject(position) == null) {
      return null;
    }
    PGobject pgObject = (PGobject) rs.getObject(position);
    try {
      return mapper.readValue(pgObject.getValue(), this.returnedClass());
    } catch (Exception e) {
      throw new ReportPortalException(
          String.format("Failed to convert String to '%s' ", this.returnedClass().getName()), e);
    }
  }

  @Override
  public void nullSafeSet(PreparedStatement st, Object value, int index,
      SharedSessionContractImplementor session)
      throws HibernateException, SQLException {
    if (value == null) {
      st.setNull(index, Types.OTHER);
      return;
    }
    try {
      PGobject pGobject = new PGobject();
      pGobject.setType("jsonb");
      pGobject.setValue(mapper.writeValueAsString(value));
      st.setObject(index, pGobject);
    } catch (final Exception ex) {
      throw new ReportPortalException("Failed to convert Invoice to String: " + ex.getMessage(),
          ex);
    }

  }

  @Override
  public T deepCopy(Object value) throws HibernateException {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(
        bos)) {
      // use serialization to create a deep copy

      oos.writeObject(value);
      oos.flush();

      ByteArrayInputStream bais = new ByteArrayInputStream(bos.toByteArray());
      return (T) new ObjectInputStream(bais).readObject();
    } catch (ClassNotFoundException | IOException ex) {
      throw new HibernateException(ex);
    }
  }

  @Override
  public Serializable disassemble(Object value) throws HibernateException {
    Object copy = deepCopy(value);
    if (copy instanceof Serializable serializable) {
      return serializable;
    }
    throw new SerializationException(
        String.format("Cannot serialize '%s', %s is not Serializable.", value, value.getClass()),
        null);
  }

  @Override
  public T assemble(Serializable cached, Object owner) throws HibernateException {
    return deepCopy(cached);
  }

  @Override
  public T replace(Object original, Object target, Object owner) throws HibernateException {
    return deepCopy(original);
  }

  @Override
  @JsonIgnore
  public boolean isMutable() {
    return true;
  }

  @Override
  public int hashCode(Object x) throws HibernateException {
    if (x == null) {
      return 0;
    }
    return x.hashCode();
  }

  @Override
  public boolean equals(Object x, Object y) throws HibernateException {
    return ObjectUtils.nullSafeEquals(x, y);
  }
}
