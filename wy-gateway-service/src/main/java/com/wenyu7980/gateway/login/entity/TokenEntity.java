package com.wenyu7980.gateway.login.entity;

import com.wenyu7980.authentication.context.model.AuthenticationRolePermission;

import java.util.List;
import java.util.Set;

/**
 *
 * @author wenyu
 */
public class TokenEntity {
    /** token */
    private String token;
    private String random;
    /** 用户id */
    private String userId;
    /** 过期时间 */
    private Long timeout;
    /** 所属部门 */
    private String departmentId;
    /** 管理部门 */
    private Set<String> manageDepartments;
    /** 协管部门 */
    private Set<String> assistDepartments;
    /** 权限 */
    private List<AuthenticationRolePermission> rolePermissions;

    protected TokenEntity() {
    }

    public TokenEntity(String token, String random, String userId, Long timeout, String departmentId,
      Set<String> manageDepartments, Set<String> assistDepartments,
      List<AuthenticationRolePermission> rolePermissions) {
        this.token = token;
        this.random = random;
        this.userId = userId;
        this.timeout = timeout;
        this.departmentId = departmentId;
        this.manageDepartments = manageDepartments;
        this.assistDepartments = assistDepartments;
        this.rolePermissions = rolePermissions;
    }

    public String getToken() {
        return token;
    }

    public String getRandom() {
        return random;
    }

    public String getUserId() {
        return userId;
    }

    public Long getTimeout() {
        return timeout;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public Set<String> getManageDepartments() {
        return manageDepartments;
    }

    public Set<String> getAssistDepartments() {
        return assistDepartments;
    }

    public List<AuthenticationRolePermission> getRolePermissions() {
        return rolePermissions;
    }
}
