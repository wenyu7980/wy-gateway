package com.wenyu7980.gateway.common.component;

import java.util.Set;

public interface DepartmentComponent {
    /**
     * 查下下级部门id
     * @param parentId
     * @return
     */
    Set<String> getDepartments(String parentId);
}
