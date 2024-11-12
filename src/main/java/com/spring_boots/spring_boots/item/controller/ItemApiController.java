package com.spring_boots.spring_boots.item.controller;


import com.spring_boots.spring_boots.common.config.error.BadRequestException;
import com.spring_boots.spring_boots.item.dto.CreateItemDto;
import com.spring_boots.spring_boots.item.dto.ResponseItemDto;
import com.spring_boots.spring_boots.item.dto.SearchItemDto;
import com.spring_boots.spring_boots.item.dto.UpdateItemDto;
import com.spring_boots.spring_boots.item.service.ItemService;
import com.spring_boots.spring_boots.item.service.SearchHistoryService;
import com.spring_boots.spring_boots.s3Bucket.service.S3BucketService;
import com.spring_boots.spring_boots.user.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ItemApiController {

    private final ItemService itemService;
    private final SearchHistoryService searchHistoryService;
    private final S3BucketService s3BucketService;

    // Item 만들기
    @PostMapping("/admin/items")
    public ResponseEntity<ResponseItemDto> createItem(@Valid @ModelAttribute CreateItemDto requestItemDto,
                                                      @RequestParam("file")MultipartFile file) throws IOException {
        if (file != null && Objects.requireNonNull(file.getContentType()).startsWith("image")) {
            String imageUrl = s3BucketService.uploadFile(file);
            requestItemDto.setImageUrl(imageUrl);
        }
        ResponseItemDto responseDto = itemService.createItem(requestItemDto, file);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);


    }

    // Items 전체보기
    @GetMapping("/items")
    public ResponseEntity<Page<ResponseItemDto>> getItems(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(itemService.getAllItems(page, size));
    }

    // Item 상세보기
    @GetMapping("/items/{itemId}")
    public ResponseEntity<ResponseItemDto> getItem(@PathVariable("itemId") Long id) {
        return ResponseEntity.ok(itemService.getItem(id));
    }

    // Item 삭제하기
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable("itemId") Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    // Item 수정하기
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ResponseItemDto> updateItem (@Valid @PathVariable("itemId") Long id,
                                                       @ModelAttribute UpdateItemDto updateItemDto) throws IOException{
        ResponseItemDto responseDto = itemService.updateItem(id, updateItemDto);
        return ResponseEntity.ok(responseDto);
    }

    // CategoryId로 Items 조회하기 (페이지 네이션, 정렬 추가)
    @GetMapping("/items/categories/{category_id}")
    public ResponseEntity<Page<ResponseItemDto>> getItemsByCategory(
        @PathVariable("category_id") Long categoryId,
        @RequestParam(required = false, defaultValue = "default") String sort,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "8") int limit) {

        Page<ResponseItemDto> result = itemService.getItemsByCategoryWithSorting(categoryId, sort, page, limit);
        return new ResponseEntity<>(result, HttpStatus.OK);
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

    // 키워드 검색과 페이징 + 정렬 조회 - Items 조회
    @GetMapping("/items/search")
    public ResponseEntity<Page<SearchItemDto>> searchItems(
        @RequestParam String keyword,
        @RequestParam(required = false) String sort,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "8") int limit,
        UserDto currentUser) {

        // 로그인한 사용자인 경우에만 검색어 기록
        if (currentUser != null) {
            searchHistoryService.saveSearchKeyword(currentUser.getUserId(), keyword);
        }

        Page<SearchItemDto> result = itemService.searchAndSortItems(keyword, sort, page, limit);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 검색 기록 조회
    @GetMapping("/users/search-history")
    public ResponseEntity<List<String>> getSearchHistory(UserDto currentUser) {
        // 로그인하지 않은 사용자 처리
        if (currentUser == null)
            return ResponseEntity.ok(new ArrayList<>());

        List<String> searchHistory = searchHistoryService.getRecentSearches(currentUser.getUserId());
        return ResponseEntity.ok(searchHistory);
    }

    // 검색 기록 개별 삭제
    @DeleteMapping("/users/search-history/{keyword}")
    public ResponseEntity<Void> deleteSearchHistory(
        UserDto currentUser,
        @PathVariable String keyword) {

        // 로그인하지 않은 사용자 처리
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        searchHistoryService.deleteSearchHistory(currentUser.getUserId(), keyword);
        return ResponseEntity.noContent().build();
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
