package com.spring_boots.spring_boots.item.controller;

import com.spring_boots.spring_boots.common.config.error.ResourceNotFoundException;
import com.spring_boots.spring_boots.item.dto.CreateItemDto;
import com.spring_boots.spring_boots.item.dto.ResponseItemDto;
import com.spring_boots.spring_boots.item.dto.SearchItemDto;
import com.spring_boots.spring_boots.item.dto.UpdateItemDto;
import com.spring_boots.spring_boots.item.entity.Item;
import com.spring_boots.spring_boots.item.service.ItemService;
import com.spring_boots.spring_boots.s3Bucket.service.S3BucketService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ItemApiController {

    private final ItemService itemService;

    private final S3BucketService s3BucketService;

    // Item 만들기
    @PostMapping("/admin/items")
    public ResponseEntity<ResponseItemDto> createItem(@Valid @ModelAttribute CreateItemDto requestItemDto,
                                                      @RequestParam("file")MultipartFile file) {
        try {
            if (file != null && Objects.requireNonNull(file.getContentType()).startsWith("image")) {
                String imageUrl = s3BucketService.uploadFile(file);
                requestItemDto.setImageUrl(imageUrl);

            }
            ResponseItemDto responseDto = itemService.createItem(requestItemDto, file);
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    // Items 전체보기
    @GetMapping("/items")
    public ResponseEntity<Page<ResponseItemDto>> getItems(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        Page<ResponseItemDto> result = itemService.getAllItems(page, size);
        return ResponseEntity.ok(result);
    }


    // Item 상세보기
    @GetMapping("/items/{itemId}")
    public ResponseEntity<ResponseItemDto> getItem(@PathVariable("itemId") Long id) {
        try {
            ResponseItemDto responseDto = itemService.getItem(id);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Item 삭제하기
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable("itemId") Long id) {
        try {
            itemService.deleteItem(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 오류
        }catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 400 오류
        }
    }

    // Item 수정하기
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ResponseItemDto> updateItem (@Valid @PathVariable("itemId") Long id, @ModelAttribute UpdateItemDto updateItemDto) {
        try {
            ResponseItemDto responseDto = itemService.updateItem(id, updateItemDto);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // CategoryId로 Items 조회하기 (페이지 네이션, 정렬 추가)
    @GetMapping("/items/categories/{category_id}")
    public ResponseEntity<Page<ResponseItemDto>> getItemsByCategory(
        @PathVariable("category_id") Long categoryId,
        @RequestParam(required = false, defaultValue = "default") String sort,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "8") int limit) {
        try {
            Page<ResponseItemDto> result = itemService.getItemsByCategoryWithSorting(categoryId, sort, page, limit);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // 테마에 해당하는 모든 아이템 - 전체보기 고정 구현
    @GetMapping("/items/thema/{thema}")
    public ResponseEntity<Page<ResponseItemDto>> getItemsByTheme(
        @PathVariable("thema") String thema,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "8") int limit,
        @RequestParam(defaultValue = "default") String sort) {

        Page<ResponseItemDto> result = itemService.getItemsByCategoryThemaWithSorting(thema, sort, page, limit);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 키워드와 정렬 - Items 조회
    @GetMapping("/items/search")
    public ResponseEntity<Page<SearchItemDto>> searchItems(
        @RequestParam String keyword,
        @RequestParam(required = false) String sort,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "8") int limit) {
        Page<SearchItemDto> result = itemService.searchAndSortItems(keyword, sort, page, limit);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 상품 이름으로 조회
    @GetMapping("/items/list/search/name")
    public ResponseEntity<Page<ResponseItemDto>> searchItemsByName (@RequestParam String itemName,
                                                                    @RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "10") int size) {
        Page<ResponseItemDto> result = itemService.searchItemsByName(itemName, page, size);
        return ResponseEntity.ok(result);
    }


}
