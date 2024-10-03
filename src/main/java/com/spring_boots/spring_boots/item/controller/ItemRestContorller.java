package com.spring_boots.spring_boots.item.controller;

import com.spring_boots.spring_boots.item.mapper.ItemMapper;
import com.spring_boots.spring_boots.item.service.ItemRestService;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@NoArgsConstructor
@RequestMapping("/api")
public class ItemRestContorller {
    private  ItemRestService itemRestService;
    private ItemMapper mapper;

    @Autowired
    public ItemRestContorller(ItemRestService itemRestService, ItemMapper mapper) {
        this.itemRestService = itemRestService;
        this.mapper = mapper;
    }

}
