package com.wenyu7980.gateway.login.handler.impl;

import com.wenyu7980.authentication.api.domain.AuthLogin;
import com.wenyu7980.authentication.api.domain.AuthLoginResult;
import com.wenyu7980.authentication.api.domain.RolePermission;
import com.wenyu7980.authentication.api.service.LoginFacade;
import com.wenyu7980.authentication.context.model.AuthenticationRolePermission;
import com.wenyu7980.gateway.common.component.DepartmentComponent;
import com.wenyu7980.gateway.login.domain.Login;
import com.wenyu7980.gateway.login.domain.LoginResult;
import com.wenyu7980.gateway.login.entity.TokenEntity;
import com.wenyu7980.gateway.login.handler.LoginHandler;
import com.wenyu7980.gateway.login.service.RsaKeyService;
import com.wenyu7980.gateway.login.service.TokenService;
import com.wenyu7980.organization.api.domain.Department;
import com.wenyu7980.organization.api.domain.UserDetail;
import com.wenyu7980.organization.api.service.UserFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author wenyu
 */
@Component
public class LoginHandlerImpl implements LoginHandler {
    @Autowired
    private LoginFacade loginFacade;
    @Autowired
    private RsaKeyService rsaKeyService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private DepartmentComponent departmentComponent;
    @Autowired
    private UserFacade userFacade;

    @Override
    public Mono<LoginResult> login(Login login) throws IOException, GeneralSecurityException {
        // 登录Authentication服务
        AuthLoginResult result = loginFacade.login(new AuthLogin(login.getUsername(), null, null,
          rsaKeyService.decode(login.getPublicKeyCode(), login.getPassword())));
        // 获取用户信息
        UserDetail user = userFacade.getDetail(result.getUserId());
        LoginResult loginResult = this.convert(result, user);
        // 使用Set去除重复的
        Set<AuthenticationRolePermission> rolePermissions = new HashSet<>();
        for (RolePermission permission : result.getPermissions()) {
            Set<String> departments = new HashSet<>();
            if (permission.getDepartmentId() == null) {
                // 根据用户组织架构计算
                if (permission.getCurrentFlag()) {
                    departments.add(user.getDepartment().getId());
                }
                if (permission.getChildFlag()) {
                    departments.addAll(departmentComponent.getDepartments(user.getDepartment().getId()));
                }
                // 管理的部门和协管的部门级下级部门
                for (Department department : user.getManageDepartments()) {
                    departments.add(department.getId());
                    departments.addAll(departmentComponent.getDepartments(department.getId()));
                }
                for (Department department : user.getAssistDepartments()) {
                    departments.add(department.getId());
                    departments.addAll(departmentComponent.getDepartments(department.getId()));
                }
            } else {
                // 根据Permission Department计算
                departments.addAll(departmentComponent.getDepartments(permission.getDepartmentId()));
            }
            rolePermissions.addAll(departments.stream().map(
              d -> new AuthenticationRolePermission(permission.getCode(), permission.getServiceName(),
                permission.getMethod(), permission.getPath(), permission.getResource(), permission.getResourceId(), d))
              .collect(Collectors.toList()));
        }
        tokenService.save(
          new TokenEntity(result.getToken(), rsaKeyService.decode(login.getPublicKeyCode(), login.getRandomCode()),
            result.getUserId(), 24 * 60 * 60L, user.getDepartment().getId(),
            user.getManageDepartments().stream().map(Department::getId).collect(Collectors.toSet()),
            user.getAssistDepartments().stream().map(Department::getId).collect(Collectors.toSet()),
            // 使用Array提高性能
            new ArrayList<>(rolePermissions)));
        loginResult.setPermissions(new ArrayList<>(rolePermissions));
        return Mono.just(loginResult);
    }

    private LoginResult convert(AuthLoginResult result, UserDetail userDetail) {
        LoginResult loginResult = new LoginResult();
        loginResult.setToken(result.getToken());
        loginResult.setUserId(result.getUserId());
        loginResult.setUsername(result.getUsername());
        loginResult.setMobile(result.getMobile());
        loginResult.setAssistDepartments(userDetail.getAssistDepartments());
        loginResult.setManageDepartments(userDetail.getManageDepartments());
        loginResult.setEntityDepartment(userDetail.getDepartment());
        loginResult.setDepartment(userDetail.getDepartment());
        loginResult.setName(userDetail.getName());
        return loginResult;
    }
}
