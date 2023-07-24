package com.santosh.orderservice.Service;

import com.santosh.orderservice.Repository.OrderLineItemsRepository;
import com.santosh.orderservice.Repository.OrderRepository;
import com.santosh.orderservice.dto.request.OrderLineItemsDto;
import com.santosh.orderservice.dto.request.OrderRequest;
import com.santosh.orderservice.model.Order;
import com.santosh.orderservice.model.OrderLineItems;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderLineItemsRepository orderLineItemsRepository;

//    public void placeOrder(OrderRequest orderRequest) {
//        Order order = new Order();
//
//        order.setOrderNumber(UUID.randomUUID().toString());
//        order = orderRepository.save(order);
//
//        Order finalOrder = order;
//        List<OrderLineItems> collectOrderLineItems = orderRequest.getOrderLineItemsList().stream()
//                .map(orderItesDto -> mapToOrderItem(orderItesDto, finalOrder))
//                .collect(Collectors.toList());
//
//        order.setOrderLineItemsList(collectOrderLineItems);
//
//       Order savedOrder = orderRepository.save(order);
//        System.out.println(savedOrder.getId());
//    }

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        Order newOrder  = orderRepository.save(order);
        List<OrderLineItems> collectOrderLineItems = orderRequest.getOrderLineItemsList().stream()
                .map(orderLineItemRequest -> {
                    OrderLineItems orderLineItems = new OrderLineItems();
                    orderLineItems.setSkuCode(orderLineItemRequest.getSkuCode());
                    orderLineItems.setPrice(orderLineItemRequest.getPrice());
                    orderLineItems.setQuantity(orderLineItemRequest.getQuantity());

                    // Set the Order object in OrderLineItems
                    orderLineItems.setOrders(newOrder);
                    orderLineItemsRepository.save(orderLineItems);
                    return orderLineItems;
                })
                .collect(Collectors.toList());

        // Set the OrderLineItems list in the Order entity
        order.setOrderLineItemsList(collectOrderLineItems);

        // Save the order with its associated OrderLineItems
        orderRepository.save(order);
    }


    private OrderLineItems mapToOrderItem(OrderLineItemsDto orderLineItemDto, Order order) {
        OrderLineItems orderLineItems = new OrderLineItems();

        orderLineItems.setPrice(orderLineItemDto.getPrice());
        orderLineItems.setQuantity(orderLineItemDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemDto.getSkuCode());
        orderLineItems.setOrders(order);
        return orderLineItems;
    }

}
