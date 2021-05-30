package com.wenyu7980.gateway.common.component.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wenyu7980.gateway.common.component.DepartmentComponent;
import com.wenyu7980.organization.api.domain.DepartmentListDetail;
import com.wenyu7980.organization.api.service.DepartmentFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DepartmentComponentImpl implements DepartmentComponent {
    private final Cache<String, Map<String, Set<String>>> DEPARTMENTS;
    @Autowired
    private DepartmentFacade departmentFacade;

    public DepartmentComponentImpl() {
        this.DEPARTMENTS = Caffeine.newBuilder().build();
    }

    @Override
    public Set<String> getDepartments(String parentId) {
        return this.DEPARTMENTS.get("DEPARTMENTS", v -> new HashMap<>()).getOrDefault(parentId, new HashSet<>());
    }

    @Scheduled(fixedDelay = 60 * 1000)
    private void refresh() {
        this.DEPARTMENTS.put("DEPARTMENTS", this.getDepartments());
    }

    private Map<String, Set<String>> getDepartments() {
        List<DepartmentListDetail> details = departmentFacade.getList();
        Map<String, DepartmentNode> nodes = new HashMap<>();
        for (DepartmentListDetail detail : details) {
            nodes.put(detail.getId(), new DepartmentNode(detail.getId(), detail.getParentId()));
        }
        for (DepartmentNode node : nodes.values()) {
            if (node.getParentId() != null) {
                nodes.get(node.getParentId()).addNode(node);
            }
        }
        return nodes.values().stream()
          .collect(() -> new HashMap<>(), (h, v) -> h.put(v.getId(), v.getDepartments()), HashMap::putAll);
    }

    private static class DepartmentNode {
        private String id;
        private String parentId;
        private Set<DepartmentNode> nodes;

        public DepartmentNode(String id, String parentId) {
            this.id = id;
            this.parentId = parentId;
            this.nodes = new HashSet<>();
        }

        public void addNode(DepartmentNode node) {
            this.nodes.add(node);
        }

        public Set<String> getDepartments() {
            Set<String> nodes = new HashSet<>();
            for (DepartmentNode node : this.nodes) {
                nodes.add(node.getId());
                nodes.addAll(node.getDepartments());
            }
            return nodes;
        }

        public String getParentId() {
            return parentId;
        }

        public String getId() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            DepartmentNode that = (DepartmentNode) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
