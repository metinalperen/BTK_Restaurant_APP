package com.example.demo.service;

import com.example.demo.enums.UserRolesEnum;
import com.example.demo.model.Role;
import com.example.demo.repository.RoleRepository;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Map;

@Service
public class RoleMappingService {

    private final RoleRepository roleRepository;

    // Map role names to their enum ordinal (ID)
    private static final Map<String, Long> ROLE_NAME_TO_ID = new HashMap<>();

    static {
        ROLE_NAME_TO_ID.put("admin", 0L);
        ROLE_NAME_TO_ID.put("waiter", 1L);
        ROLE_NAME_TO_ID.put("cashier", 2L);
    }

    public RoleMappingService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role getRoleByName(String roleName) {
        Long roleId = ROLE_NAME_TO_ID.get(roleName);
        if (roleId == null) {
            throw new IllegalArgumentException("Invalid role name: " + roleName);
        }

        return roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role with ID " + roleId + " not found in database"));
    }

    public Long getRoleIdByName(String roleName) {
        Long roleId = ROLE_NAME_TO_ID.get(roleName);
        if (roleId == null) {
            throw new IllegalArgumentException("Invalid role name: " + roleName);
        }
        return roleId;
    }
}