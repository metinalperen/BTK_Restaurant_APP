package com.example.demo.service;

import com.example.demo.model.Role;
import com.example.demo.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    // CREATE: Yeni rol ekleme
    public Role createRole(Role role) {
        return roleRepository.save(role);
    }

    // READ: Tüm rolleri listeleme
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    // READ: ID ile rol bulma
    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }

    // UPDATE: Rol güncelleme
    public Role updateRole(Long id, Role updatedRole) {
        return roleRepository.findById(id)
            .map(role -> {
                role.setName(updatedRole.getName());
                return roleRepository.save(role);
            })
            .orElseThrow(() -> new RuntimeException("Rol bulunamadı"));
    }

    // DELETE: Rol silme
    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }
}