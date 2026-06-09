package com.p2pdomicilios.P2pDomicilios.controllers;

import com.p2pdomicilios.P2pDomicilios.dto.AdminUserListItemDTO;
import com.p2pdomicilios.P2pDomicilios.dto.AdminUserStatusUpdateRequest;
import com.p2pdomicilios.P2pDomicilios.enums.Role;
import com.p2pdomicilios.P2pDomicilios.services.AdminUserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<List<AdminUserListItemDTO>> listUsers(
        @RequestParam(required = false) Role role,
        @RequestParam(required = false) String estado,
        @RequestParam(required = false) Boolean enabled,
        @RequestParam(required = false) String q
    ) {
        return ResponseEntity.ok(adminUserService.listUsers(role, estado, enabled, q));
    }

    @PatchMapping("/{id}/account-status")
    public ResponseEntity<AdminUserListItemDTO> updateAccountStatus(
        @PathVariable Integer id,
        @Valid @RequestBody AdminUserStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(adminUserService.updateAccountStatus(id, request.getAction()));
    }
}
