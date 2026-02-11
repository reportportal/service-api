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

package com.epam.reportportal.base.infrastructure.persistence.entity;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

public class LTreeType implements UserType<String> {

  @Override
  public int getSqlType() {
    return Types.OTHER;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Class<String> returnedClass() {
    return String.class;
  }

  @Override
  public boolean equals(String s, String j1) {
    return StringUtils.equals(s, j1);
  }

  @Override
  public int hashCode(String s) {
    return s.hashCode();
  }

  @Override
  public String nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session,
      Object owner) throws SQLException {
    return rs.getString(position);
  }

  @Override
  public String deepCopy(String s) {
    return s;
  }

  @Override
  public void nullSafeSet(PreparedStatement st, String value, int index,
      SharedSessionContractImplementor session) throws SQLException {
    st.setObject(index, value, Types.OTHER);
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public Serializable disassemble(String s) {
    return s;
  }

  @Override
  public String assemble(Serializable cached, Object owner) {
    return deepCopy((String) cached);
  }

}
