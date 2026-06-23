package org.yearup.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.models.*;
import org.yearup.repository.OrderLineItemRepository;
import org.yearup.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderLineItemRepository orderLineItemRepository;
    private final ShoppingCartService shoppingCartService;
    private final ProfileService profileService;

    public OrderService(OrderRepository orderRepository, OrderLineItemRepository orderLineItemRepository, ShoppingCartService shoppingCartService, ProfileService profileService) {
        this.orderRepository = orderRepository;
        this.orderLineItemRepository = orderLineItemRepository;
        this.shoppingCartService = shoppingCartService;
        this.profileService = profileService;
    }

    public Order checkout(int userId)
    {
        ShoppingCart cart = shoppingCartService.getByUserId(userId);

        if(cart.getItems().isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shopping cart is empty");
        }

        Profile profile = profileService.getByUserId(userId);

        Order order = new Order();
        order.setUserId(userId);
        order.setDate(LocalDateTime.now());

        order.setAddress(profile.getAddress());
        order.setCity(profile.getCity());
        order.setState(profile.getState());
        order.setZip(profile.getZip());

        order.setShippingAmount(BigDecimal.ZERO);

        order = orderRepository.save(order);

        for(ShoppingCartItem item : cart.getItems().values())
        {
            OrderLineItem lineItem = new OrderLineItem();

            lineItem.setOrderId(order.getOrderId());
            lineItem.setProductId(item.getProductId());

            lineItem.setSalesPrice(BigDecimal.valueOf(item.getProduct().getPrice()));

            lineItem.setQuantity(item.getQuantity());

            lineItem.setDiscount(BigDecimal.valueOf(item.getDiscountPercent()));

            orderLineItemRepository.save(lineItem);
        }

        shoppingCartService.clearCart(userId);

        return order;
    }
}
