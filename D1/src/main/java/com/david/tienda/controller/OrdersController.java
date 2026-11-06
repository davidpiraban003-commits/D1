package com.david.tienda.controller;

import com.david.tienda.dto.HttpGlobalResponseDTO;
import com.david.tienda.dto.OrderRequestDTO;
import com.david.tienda.dto.OrderResponseDTO;
import com.david.tienda.dto.OrderStatusRequestDTO;
import com.david.tienda.enums.RoleEnum;
import com.david.tienda.security.RequiresRole;
import com.david.tienda.service.OrdersService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrdersController extends BaseController {

    private final OrdersService ordersService;

    public OrdersController(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @PostMapping
    @RequiresRole(RoleEnum.CLIENTE)
    public ResponseEntity<HttpGlobalResponseDTO<OrderResponseDTO>> create(@RequestBody OrderRequestDTO body,
                                                                         HttpServletRequest request) {
        return respond(HttpStatus.CREATED, "Pedido creado correctamente",
                () -> ordersService.create(userId(request), body));
    }

    @GetMapping("/mine")
    @RequiresRole(RoleEnum.CLIENTE)
    public ResponseEntity<HttpGlobalResponseDTO<List<OrderResponseDTO>>> mine(HttpServletRequest request) {
        return respond(HttpStatus.OK, "Mis pedidos", () -> ordersService.findMine(userId(request)));
    }

    @GetMapping("/sales")
    @RequiresRole(RoleEnum.VENDEDOR)
    public ResponseEntity<HttpGlobalResponseDTO<List<OrderResponseDTO>>> sales(HttpServletRequest request) {
        return respond(HttpStatus.OK, "Pedidos de mis productos", () -> ordersService.findSales(userId(request)));
    }

    @GetMapping
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<List<OrderResponseDTO>>> list() {
        return respond(HttpStatus.OK, "Todos los pedidos", ordersService::findAll);
    }

    @PatchMapping("/{id}/status")
    @RequiresRole({RoleEnum.VENDEDOR, RoleEnum.ADMIN})
    public ResponseEntity<HttpGlobalResponseDTO<OrderResponseDTO>> changeStatus(@PathVariable Integer id,
                                                                               @RequestBody OrderStatusRequestDTO body,
                                                                               HttpServletRequest request) {
        return respond(HttpStatus.OK, "Estado del pedido actualizado",
                () -> ordersService.changeStatus(id, body, userId(request), rolId(request)));
    }

    @PatchMapping("/{id}/cancel")
    @RequiresRole(RoleEnum.CLIENTE)
    public ResponseEntity<HttpGlobalResponseDTO<OrderResponseDTO>> cancel(@PathVariable Integer id,
                                                                         HttpServletRequest request) {
        return respond(HttpStatus.OK, "Pedido cancelado",
                () -> ordersService.cancel(id, userId(request)));
    }
}
