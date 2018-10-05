package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.auth.acl.ReportPortalAclService;
import com.github.benmanes.caffeine.cache.Caffeine;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.ConsoleAuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
import org.springframework.security.acls.domain.SpringCacheBasedAclCache;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Configuration
@EnableAutoConfiguration
public class ACLContext {

    @Autowired
    DataSource dataSource;

    @Bean
    public SpringCacheBasedAclCache aclCache() {
        return new SpringCacheBasedAclCache(coffeinCache(), permissionGrantingStrategy(), aclAuthorizationStrategy());
    }

    @Bean
    public CaffeineCache coffeinCache(){
        return new CaffeineCache("aclCache", Caffeine.newBuilder().build());
    }

    @Bean
    public PermissionGrantingStrategy permissionGrantingStrategy() {
        return new DefaultPermissionGrantingStrategy(new ConsoleAuditLogger());
    }

    @Bean
    public AclAuthorizationStrategy aclAuthorizationStrategy() {
        return new AclAuthorizationStrategyImpl(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Bean
    public LookupStrategy lookupStrategy() {
        return new BasicLookupStrategy(dataSource, aclCache(), aclAuthorizationStrategy(), new ConsoleAuditLogger());
    }

    @Bean
    public ReportPortalAclService aclService() {
        return new ReportPortalAclService(dataSource,lookupStrategy(), aclCache());
    }

    @Bean
    public AclPermissionEvaluator aclPermissionEvaluator(){
        return new AclPermissionEvaluator(aclService());
    }

}