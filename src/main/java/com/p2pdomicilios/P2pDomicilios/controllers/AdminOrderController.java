package com.p2pdomicilios.P2pDomicilios.controllers;

import com.p2pdomicilios.P2pDomicilios.dto.AdminOrderDetailDTO;
import com.p2pdomicilios.P2pDomicilios.dto.AdminOrderListItemDTO;
import com.p2pdomicilios.P2pDomicilios.services.AdminOrderService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public ResponseEntity<List<AdminOrderListItemDTO>> listOrders(
        @RequestParam(required = false) String estado,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta
    ) {
        return ResponseEntity.ok(adminOrderService.listOrders(estado, fechaDesde, fechaHasta));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminOrderDetailDTO> getOrderDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminOrderService.getOrderDetail(id));
    }
}
