package com.santosh.inventoryservice.service;


import com.santosh.inventoryservice.Repository.InventoryRepository;
import com.santosh.inventoryservice.dto.InventoryResponse;
import com.santosh.inventoryservice.model.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCode) {
        List<InventoryResponse> allInventory = inventoryRepository.findBySkuCodeIn(skuCode)
                .stream()
                .map(inv -> mapToInventoryResponse(inv))
                .collect(Collectors.toList());
        return allInventory;
    }

    private InventoryResponse mapToInventoryResponse(Inventory inv) {
        InventoryResponse invResponse = new InventoryResponse();
        invResponse.setSkuCode(inv.getSkuCode());
        invResponse.setInStock(inv.getQuantity() > 0);

        return invResponse;
    }
}
