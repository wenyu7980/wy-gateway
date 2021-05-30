package com.wenyu7980.gateway.login.domain;

import com.wenyu7980.authentication.context.model.AuthenticationRolePermission;
import com.wenyu7980.organization.api.domain.Department;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 *
 * @author wenyu
 */
public class LoginResult {
    @ApiModelProperty(value = "token", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    private String token;
    @ApiModelProperty(value = "用户id", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    private String userId;
    @ApiModelProperty(value = "用户名", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    private String username;
    @ApiModelProperty(value = "用户姓名", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    private String name;
    @ApiModelProperty(value = "用户手机号", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    private String mobile;
    @ApiModelProperty(value = "所在部门", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    private Department department;
    @ApiModelProperty(value = "所在业务实体", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    private Department entityDepartment;
    @ApiModelProperty(value = "管理的部门", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    private List<Department> manageDepartments;
    @ApiModelProperty(value = "协管的部门", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    private List<Department> assistDepartments;
    @ApiModelProperty(value = "权限", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    private List<AuthenticationRolePermission> permissions;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Department getEntityDepartment() {
        return entityDepartment;
    }

    public void setEntityDepartment(Department entityDepartment) {
        this.entityDepartment = entityDepartment;
    }

    public List<Department> getManageDepartments() {
        return manageDepartments;
    }

    public void setManageDepartments(List<Department> manageDepartments) {
        this.manageDepartments = manageDepartments;
    }

    public List<Department> getAssistDepartments() {
        return assistDepartments;
    }

    public void setAssistDepartments(List<Department> assistDepartments) {
        this.assistDepartments = assistDepartments;
    }

    public List<AuthenticationRolePermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<AuthenticationRolePermission> permissions) {
        this.permissions = permissions;
    }
}
