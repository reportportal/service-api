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
package com.epam.ta.reportportal.auth.acl;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.MutableAcl;

/**
 * @author Ivan Nikitsenka
 */
public class ReportPortalAclService extends JdbcMutableAclService {

    private static final String IDENTITY_QUERY = "select currval(pg_get_serial_sequence('acl_class', 'id'))";
    private static final String SID_IDENTITY_QUERY = "select currval(pg_get_serial_sequence('acl_sid', 'id'))";
    private static final String OBJECT_IDENTITY_PRIMARY_KEY_QUERY = "select acl_object_identity.id\n"
        + "from acl_object_identity,\n"
        + "     acl_class\n"
        + "where acl_object_identity.object_id_class = acl_class.id\n"
        + "  and acl_class.class= ? \n"
        + "  and acl_object_identity.object_id_identity = ? ::varchar";
    private static final String FIND_CHILDREN_QUERY =
        "select obj.object_id_identity as obj_id, class.class as class"
            + " from acl_object_identity obj, acl_object_identity parent, acl_class class "
            + "where obj.parent_object = parent.id and obj.object_id_class = class.id "
            + "and parent.object_id_identity = ?::varchar and parent.object_id_class = ("
            + "select id FROM acl_class where acl_class.class = ?)";

    public ReportPortalAclService(DataSource dataSource, LookupStrategy lookupStrategy,
        AclCache aclCache) {
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
    public MutableAcl createAcl(Object object) {
        MutableAcl acl = getAcl(object);
        if (acl != null) {
            return acl;
        }
        return createAcl(new ObjectIdentityImpl(object));
    }

    /**
     * Add read permissions to the object for the user.
     *
     * @param object to add permission settings.
     * @param userName this user will be allowed to read the object.
     * @return {@link MutableAcl}
     */
    public MutableAcl addReadPermissions(Object object, String userName) {
        MutableAcl acl = getAcl(object);
        if (isAceExistForUser(object, userName)) {
            return acl;
        }
        PrincipalSid sid = new PrincipalSid(userName);
        acl.insertAce(0, BasePermission.READ, sid, true);
        return updateAcl(acl);
    }


    /**
     * Returns true if the user has any permission for the object.
     *
     * @param object
     * @param userName
     * @return boolean
     */
    public boolean isAceExistForUser(Object object, String userName) {
        MutableAcl acl = getAcl(object);
        if (acl == null){
            return false;
        }
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
     *  Returns existed ACL for object.
     *
     * @param object
     * @return {@link MutableAcl}
     */
    public MutableAcl getAcl(Object object) {
        ObjectIdentityImpl objectIdentity = new ObjectIdentityImpl(object);
        if (retrieveObjectIdentityPrimaryKey(objectIdentity) != null) {
            return (MutableAcl) readAclById(objectIdentity);
        }
        return null;
    }
}
