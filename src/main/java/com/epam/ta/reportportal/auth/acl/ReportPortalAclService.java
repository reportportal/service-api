/*
 * Copyright 2018 EPAM Systems
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
package com.epam.ta.reportportal.auth.acl;

import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.Permission;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

/**
 * @author Ivan Nikitsenka
 */
public class ReportPortalAclService extends JdbcMutableAclService {

	private static final String IDENTITY_QUERY = "select currval(pg_get_serial_sequence('acl_class', 'id'))";
	private static final String SID_IDENTITY_QUERY = "select currval(pg_get_serial_sequence('acl_sid', 'id'))";
	private static final String OBJECT_IDENTITY_PRIMARY_KEY_QUERY =
			"select acl_object_identity.id\n" + "from acl_object_identity,\n" + "     acl_class\n"
					+ "where acl_object_identity.object_id_class = acl_class.id\n" + "  and acl_class.class= ? \n"
					+ "  and acl_object_identity.object_id_identity = ? ::varchar";
	private static final String FIND_CHILDREN_QUERY = "select obj.object_id_identity as obj_id, class.class as class"
			+ " from acl_object_identity obj, acl_object_identity parent, acl_class class "
			+ "where obj.parent_object = parent.id and obj.object_id_class = class.id "
			+ "and parent.object_id_identity = ?::varchar and parent.object_id_class = ("
			+ "select id FROM acl_class where acl_class.class = ?)";

	public ReportPortalAclService(DataSource dataSource, LookupStrategy lookupStrategy, AclCache aclCache) {
		super(dataSource, lookupStrategy, aclCache);
		this.setClassIdentityQuery(IDENTITY_QUERY);
		this.setSidIdentityQuery(SID_IDENTITY_QUERY);
		this.setObjectIdentityPrimaryKeyQuery(OBJECT_IDENTITY_PRIMARY_KEY_QUERY);
		this.setFindChildrenQuery(FIND_CHILDREN_QUERY);
	}

	/**
	 * Creates new ACL with current user as owner.
	 *
	 * @param object to add acl.
	 * @return {@link MutableAcl}
	 */
	Optional<MutableAcl> createAcl(Object object) {
		Optional<MutableAcl> acl = getAcl(object);
		if (acl.isPresent()) {
			return acl;
		}
		return Optional.of(createAcl(new ObjectIdentityImpl(object)));
	}

	/**
	 * Deletes ACL of provided object.
	 *
	 * @param object to remove acl.
	 */
	void deleteAcl(Object object) {
		Optional<MutableAcl> acl = getAcl(object);
		if (acl.isPresent()) {
			deleteAcl(new ObjectIdentityImpl(object), true);
		}
	}

	/**
	 * Add read permissions to the object for the user.
	 *
	 * @param object     to add permission settings.
	 * @param userName   this user will be allowed to read the object.
	 * @param permission permission
	 * @return {@link MutableAcl}
	 */
	Optional<MutableAcl> addPermissions(Object object, String userName, Permission permission) {
		Optional<MutableAcl> acl = getAcl(object);
		if (!acl.isPresent() || isAceExistForUser(acl.get(), userName)) {
			return acl;
		}
		PrincipalSid sid = new PrincipalSid(userName);
		acl.get().insertAce(0, permission, sid, true);
		updateAcl(acl.get());
		return acl;
	}

	/**
	 * Remove read permissions to the object for the user.
	 *
	 * @param object   to remove permission settings.
	 * @param userName this user will not be allowed to read the object.
	 * @return {@link MutableAcl}
	 */
	Optional<MutableAcl> removePermissions(Object object, String userName) {
		Optional<MutableAcl> acl = getAcl(object);
		if (acl.isPresent() && isAceExistForUser(acl.get(), userName)) {
			PrincipalSid sid = new PrincipalSid(userName);
			if (!acl.get().getOwner().equals(sid)) {
				for (int i = 0; i < acl.get().getEntries().size(); i++) {
					AccessControlEntry entry = acl.get().getEntries().get(i);
					if (sid.equals(entry.getSid())) {
						acl.get().deleteAce(i);
						break;
					}
				}
				updateAcl(acl.get());
				return acl;
			}
		}
		return acl;
	}

	/**
	 * Returns true if the user has any permission for the object.
	 *
	 * @param acl      Acl
	 * @param userName User
	 * @return boolean
	 */
	private boolean isAceExistForUser(MutableAcl acl, String userName) {
		PrincipalSid sid = new PrincipalSid(userName);
		List<AccessControlEntry> entries = acl.getEntries();
		for (AccessControlEntry ace : entries) {
			if (ace != null && sid.equals(ace.getSid())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns existed ACL for object.
	 *
	 * @param object Object
	 * @return {@link MutableAcl}
	 */
	Optional<MutableAcl> getAcl(Object object) {
		ObjectIdentityImpl objectIdentity = new ObjectIdentityImpl(object);
		if (retrieveObjectIdentityPrimaryKey(objectIdentity) != null) {
			return Optional.of((MutableAcl) readAclById(objectIdentity));
		}
		return Optional.empty();
	}
}
