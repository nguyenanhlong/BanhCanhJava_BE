package com.example.banhcanh.controller;

import com.example.banhcanh.model.*;
import com.example.banhcanh.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    // --- ROLES ---

    @GetMapping("/roles")
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @PostMapping("/roles")
    public Role createRole(@RequestBody Role role) {
        role.setId(null);
        return roleRepository.save(role);
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody Role roleData) {
        return roleRepository.findById(id).map(role -> {
            role.setName(roleData.getName());
            role.setDisplayName(roleData.getDisplayName());
            role.setDescription(roleData.getDescription());
            role.setIsActive(roleData.getIsActive());
            return ResponseEntity.ok(roleRepository.save(role));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        if (roleRepository.existsById(id)) {
            roleRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/roles/{id}/permissions")
    public ResponseEntity<Set<Permission>> getRolePermissions(@PathVariable Long id) {
        return roleRepository.findById(id)
                .map(role -> ResponseEntity.ok(role.getPermissions()))
                .orElse(ResponseEntity.notFound().build());
    }

    // --- PERMISSIONS ---

    @GetMapping("/permissions")
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    // --- ROLE-PERMISSION ASSIGNMENT ---

    @PostMapping("/role-permissions")
    public ResponseEntity<?> assignPermissionToRole(@RequestBody Map<String, Long> body) {
        Long roleId = body.get("roleId");
        Long permissionId = body.get("permissionId");
        return roleRepository.findById(roleId).map(role ->
            permissionRepository.findById(permissionId).map(permission -> {
                role.getPermissions().add(permission);
                roleRepository.save(role);
                return ResponseEntity.ok().build();
            }).orElse(ResponseEntity.notFound().build())
        ).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/role-permissions")
    public ResponseEntity<?> removePermissionFromRole(@RequestBody Map<String, Long> body) {
        Long roleId = body.get("roleId");
        Long permissionId = body.get("permissionId");
        return roleRepository.findById(roleId).map(role ->
            permissionRepository.findById(permissionId).map(permission -> {
                role.getPermissions().remove(permission);
                roleRepository.save(role);
                return ResponseEntity.ok().build();
            }).orElse(ResponseEntity.notFound().build())
        ).orElse(ResponseEntity.notFound().build());
    }

    // --- USER-ROLE ASSIGNMENT ---

    @GetMapping("/users/{userId}/roles")
    public ResponseEntity<List<Role>> getUserRoles(@PathVariable Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        List<Role> roles = new ArrayList<>();
        for (UserRole ur : userRoles) {
            roleRepository.findById(ur.getRoleId()).ifPresent(roles::add);
        }
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<?> assignRoleToUser(@PathVariable Long userId, @RequestBody Map<String, Long> body) {
        Long roleId = body.get("roleId");
        UserRoleId id = new UserRoleId(userId, roleId);
        if (userRoleRepository.existsById(id)) {
            return ResponseEntity.badRequest().body(Map.of("error", "User already has this role"));
        }
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRoleRepository.save(userRole);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{userId}/roles/{roleId}")
    public ResponseEntity<Void> removeRoleFromUser(@PathVariable Long userId, @PathVariable Long roleId) {
        userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);
        return ResponseEntity.ok().build();
    }

    // --- CURRENT USER PERMISSIONS ---

    @GetMapping("/users/current/permissions")
    public ResponseEntity<Set<String>> getCurrentUserPermissions(
            @RequestHeader("Authorization") String authHeader) {
        // Simple: extract username from Basic Auth or return empty
        // For now return all permissions for super_admin
        return roleRepository.findAll().stream()
            .findFirst()
            .map(role -> {
                Set<String> codes = new HashSet<>();
                role.getPermissions().forEach(p -> codes.add(p.getCode()));
                return ResponseEntity.ok(codes);
            })
            .orElse(ResponseEntity.ok(new HashSet<>()));
    }
}
