package com.santosh.orderservice.Service;

import com.santosh.orderservice.Repository.OrderLineItemsRepository;
import com.santosh.orderservice.Repository.OrderRepository;
import com.santosh.orderservice.dto.request.InventoryResponse;
import com.santosh.orderservice.dto.request.OrderLineItemsDto;
import com.santosh.orderservice.dto.request.OrderRequest;
import com.santosh.orderservice.model.Order;
import com.santosh.orderservice.model.OrderLineItems;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderLineItemsRepository orderLineItemsRepository;

    private final WebClient webClient;


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

            List<String> skuCodes = orderRequest.getOrderLineItemsList().stream()
                    .map(item -> item.getSkuCode())
                    .toList();

            //call inventoryService , and place order if the product is in stock
            InventoryResponse[] inventoryResponseArray = webClient.get()
                    .uri("http://localhost:8086/api/inventory/"
                            , uriBuilder -> uriBuilder.queryParam("sku-code", skuCodes).build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block();

            boolean allMatch = Arrays.stream(inventoryResponseArray).allMatch(InventoryResponse::isInStock);

            if (!allMatch) {
                throw new IllegalStateException("Product is not in stock");
            }
            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID().toString());

            Order newOrder = orderRepository.save(order);
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

            order.setOrderLineItemsList(collectOrderLineItems);

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
