package com.david.tienda.service;

import com.david.tienda.dto.OrderRequestDTO;
import com.david.tienda.dto.OrderResponseDTO;
import com.david.tienda.dto.OrderStatusRequestDTO;
import com.david.tienda.entity.Orders;
import com.david.tienda.entity.Products;
import com.david.tienda.entity.Users;
import com.david.tienda.enums.OrderStatusEnum;
import com.david.tienda.enums.RoleEnum;
import com.david.tienda.repository.OrdersRepository;
import com.david.tienda.repository.ProductsRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final ProductsRepository productsRepository;
    private final UsersService usersService;

    public OrdersService(OrdersRepository ordersRepository, ProductsRepository productsRepository,
                         UsersService usersService) {
        this.ordersRepository = ordersRepository;
        this.productsRepository = productsRepository;
        this.usersService = usersService;
    }

    @Transactional
    public OrderResponseDTO create(Integer clientId, OrderRequestDTO dto) {
        if (dto.productId() == null) {
            throw badRequest("El productId es obligatorio");
        }
        if (dto.quantity() == null || dto.quantity() <= 0) {
            throw badRequest("La cantidad (quantity) debe ser mayor que cero");
        }

        // Bloquea la fila del producto mientras dure la transacción (evita vender el mismo stock dos veces)
        Products product = productsRepository.findByIdForUpdate(dto.productId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        if (product.getStock() < dto.quantity()) {
            throw badRequest("Stock insuficiente: quedan " + product.getStock() + " unidades");
        }
        Users client = usersService.getUser(clientId);

        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(dto.quantity().longValue()));
        product.setStock(product.getStock() - dto.quantity());
        productsRepository.save(product);

        Orders order = new Orders();
        order.setClient(client);
        order.setProduct(product);
        order.setQuantity(dto.quantity());
        order.setTotal(total);
        order.setStatus(OrderStatusEnum.PENDIENTE);
        return toDto(ordersRepository.save(order));
    }

    public List<OrderResponseDTO> findMine(Integer clientId) {
        return ordersRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream().map(this::toDto).toList();
    }

    public List<OrderResponseDTO> findSales(Integer sellerId) {
        return ordersRepository.findByProductSellerIdOrderByCreatedAtDesc(sellerId).stream()
                .map(this::toDto).toList();
    }

    public List<OrderResponseDTO> findAll() {
        return ordersRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toDto).toList();
    }

    
    @Transactional
    public OrderResponseDTO changeStatus(Integer orderId, OrderStatusRequestDTO dto, Integer requesterId,
                                         Integer requesterRolId) {
        OrderStatusEnum newStatus = OrderStatusEnum.fromText(dto.status())
                .orElseThrow(() -> badRequest("Estado inválido: usa PENDIENTE, ENVIADO, ENTREGADO o CANCELADO"));
        Orders order = getOrder(orderId);

        boolean isAdmin = requesterRolId != null && requesterRolId == RoleEnum.ADMIN.getId();
        if (!isAdmin && !order.getProduct().getSeller().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este pedido no corresponde a tus productos");
        }
        if (order.getStatus() == OrderStatusEnum.CANCELADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El pedido está cancelado y ya no puede cambiar de estado");
        }

        if (newStatus == OrderStatusEnum.CANCELADO) {
            returnStock(order);
        }
        order.setStatus(newStatus);
        return toDto(ordersRepository.save(order));
    }

    
    @Transactional
    public OrderResponseDTO cancel(Integer orderId, Integer clientId) {
        Orders order = getOrder(orderId);
        if (!order.getClient().getId().equals(clientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes cancelar pedidos de otro cliente");
        }
        if (order.getStatus() != OrderStatusEnum.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden cancelar pedidos PENDIENTES");
        }
        returnStock(order);
        order.setStatus(OrderStatusEnum.CANCELADO);
        return toDto(ordersRepository.save(order));
    }

    private void returnStock(Orders order) {
        Products product = productsRepository.findByIdForUpdate(order.getProduct().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        product.setStock(product.getStock() + order.getQuantity());
        productsRepository.save(product);
    }

    private Orders getOrder(Integer id) {
        return ordersRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
    }

    private OrderResponseDTO toDto(Orders order) {
        return new OrderResponseDTO(order.getId(), order.getClient().getId(), order.getClient().getUsername(),
                order.getProduct().getId(), order.getProduct().getName(), order.getQuantity(), order.getTotal(),
                order.getStatus().name(), order.getCreatedAt());
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
