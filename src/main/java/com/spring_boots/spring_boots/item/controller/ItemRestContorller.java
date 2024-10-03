package com.spring_boots.spring_boots.item.controller;

import com.spring_boots.spring_boots.item.dto.CreateItemDto;
import com.spring_boots.spring_boots.item.dto.ResponseItemDto;
import com.spring_boots.spring_boots.item.mapper.ItemMapper;
import com.spring_boots.spring_boots.item.service.ItemRestService;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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
    @PostMapping("/admin/items")
    public ResponseEntity<ResponseItemDto> createItem(@Valid @RequestBody CreateItemDto requestItemDto) {
        ResponseItemDto responseDto = itemRestService.createItem(requestItemDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/items")
    public ResponseEntity<List<ResponseItemDto>> getItems() {
        List<ResponseItemDto> result = itemRestService.getAllItems();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/items/{itemId}")
    public ResponseEntity<ResponseItemDto> getItem(@PathVariable("id") Long id) {
        try {
            ResponseItemDto responseDto = itemRestService.getItem(id);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ResponseItemDto> deleteItem(@PathVariable("id") Long id) {
        try {
            itemRestService.deleteItem(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
