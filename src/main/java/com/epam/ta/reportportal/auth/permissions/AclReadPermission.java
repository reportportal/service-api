package com.epam.ta.reportportal.auth.permissions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@LookupPermission({ "aclReadPermission" })
public class AclReadPermission implements Permission {

    @Autowired
    public AclPermissionEvaluator aclPermissionEvaluator;

    @Override
    public boolean isAllowed(Authentication authentication, Object targetDomainObject) {
        return aclPermissionEvaluator.hasPermission(authentication, targetDomainObject, "READ");
    }
}
