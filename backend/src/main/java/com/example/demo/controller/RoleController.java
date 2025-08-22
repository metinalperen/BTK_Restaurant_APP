package com.example.demo.controller;

import com.example.demo.dto.request.RoleRequestDTO;
import com.example.demo.dto.response.RoleResponseDTO;
import com.example.demo.model.Role;
import com.example.demo.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(
        name = "Role Management",
        description = "API for managing user roles (CRUD operations, retrieval by ID, and listing all roles)."
)
@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RoleController {
    private final RoleService roleService;
    private final ModelMapper modelMapper;

    public RoleController(RoleService roleService, ModelMapper modelMapper) {
        this.roleService = roleService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    @Operation(
            summary = "Create a new role",
            description = "Creates a new user role."
    )
    public RoleResponseDTO createRole(
            @RequestBody RoleRequestDTO roleRequestDTO) {
        Role role = modelMapper.map(roleRequestDTO, Role.class);
        Role createdRole = roleService.createRole(role);
        return modelMapper.map(createdRole, RoleResponseDTO.class);
    }


    @GetMapping
    @Operation(
            summary = "Get all roles",
            description = "Retrieves a list of all user roles."
    )
    public List<RoleResponseDTO> getAllRoles() {
        return roleService.getAllRoles().stream()
                .map(role -> modelMapper.map(role, RoleResponseDTO.class))
                .collect(Collectors.toList());
    }


    @GetMapping("/{id}")
    @Operation(
            summary = "Get role by ID",
            description = "Retrieves a specific user role by its ID."
    )
    public RoleResponseDTO getRoleById(
            @Parameter(description = "ID of the role to retrieve", required = true)
            @PathVariable Long id) {
        Role role = roleService.getRoleById(id)
                .orElseThrow(() -> new RuntimeException("Rol bulunamadı"));
        return modelMapper.map(role, RoleResponseDTO.class);
    }


    @PutMapping("/{id}")
    @Operation(
            summary = "Update a role",
            description = "Updates an existing user role."
    )
    public RoleResponseDTO updateRole(
            @Parameter(description = "ID of the role to update", required = true)
            @PathVariable Long id,
            @RequestBody RoleRequestDTO roleRequestDTO) {
        Role updatedRole = modelMapper.map(roleRequestDTO, Role.class);
        Role role = roleService.updateRole(id, updatedRole);
        return modelMapper.map(role, RoleResponseDTO.class);
    }


    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a role",
            description = "Deletes a user role by its ID."
    )
    public void deleteRole(
            @Parameter(description = "ID of the role to delete", required = true)
            @PathVariable Long id) {
        roleService.deleteRole(id);
    }
}