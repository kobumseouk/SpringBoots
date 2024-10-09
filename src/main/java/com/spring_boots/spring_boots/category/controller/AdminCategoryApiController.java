package com.spring_boots.spring_boots.category.controller;


import com.spring_boots.spring_boots.category.dto.category.CategoryAdminDto;
import com.spring_boots.spring_boots.category.dto.category.CategoryRequestDto;
import com.spring_boots.spring_boots.category.dto.category.CategoryResponseDto;
import com.spring_boots.spring_boots.category.service.CategoryService;
import com.spring_boots.spring_boots.common.config.error.BadRequestException;
import com.spring_boots.spring_boots.common.config.error.ResourceNotFoundException;
import com.spring_boots.spring_boots.user.domain.UserRole;
import com.spring_boots.spring_boots.user.domain.Users;
import com.spring_boots.spring_boots.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryApiController {

  private final CategoryService categoryService;
  private final UserService userService;

///*  // 관리자 - 새 카테고리 추가
//  @PostMapping
//  public ResponseEntity<CategoryResponseDto> createCategory(@Valid @RequestBody CategoryRequestDto requestDto) {
//    try {
//      CategoryResponseDto responseDto = categoryService.createCategory(requestDto);
//      return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
//    } catch (BadRequestException e) {
//      throw new BadRequestException("필수_파라미터_누락", "카테고리 생성 실패: " + e.getMessage());
//    }
//  }
//
//  // 관리자 - 카테고리 정보 수정
//  @PutMapping("/{category_id}")
//  public ResponseEntity<CategoryResponseDto> updateCategory(
//      @PathVariable("category_id") Long categoryId,
//      @Valid @RequestBody CategoryRequestDto requestDto) {
//    try {
//      CategoryResponseDto responseDto = categoryService.updateCategory(categoryId, requestDto);
//      return ResponseEntity.ok(responseDto);
//    } catch (ResourceNotFoundException e) {
//      throw new ResourceNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId);
//    } catch (BadRequestException e) {
//      throw new BadRequestException("파라미터_길이_초과", "카테고리 수정 실패: " + e.getMessage());
//    }
//  }
//
//  // 관리자 - 카테고리 삭제
//  @DeleteMapping("/{category_id}")
//  public ResponseEntity<Void> deleteCategory(@PathVariable("category_id") Long categoryId) {
//    try {
//      categoryService.deleteCategory(categoryId);
//      return ResponseEntity.noContent().build();
//    } catch (ResourceNotFoundException e) {
//      throw new ResourceNotFoundException("삭제할 카테고리를 찾을 수 없습니다: " + categoryId);
//    }
//  }
//
//  // 관리자 카테고리 전체 목록 조회 (페이지네이션)
//  @GetMapping
//  public ResponseEntity<Page<CategoryAdminDto>> getAdminCategories(
//      @RequestParam(name = "page", defaultValue = "0") int page,
//      @RequestParam(name = "limit", defaultValue = "10") int limit) {
//    try {
//      Page<CategoryAdminDto> pageDto = categoryService.getAdminCategories(page, limit);
//      return ResponseEntity.ok(pageDto);
//    } catch (BadRequestException e) {
//      throw new BadRequestException("잘못된_파라미터_형식", "잘못된 페이지네이션 파라미터: " + e.getMessage());
//    }
//  }
//
//  // 관리자 개별 카테고리 조회 - 카테고리 수정 시 필요
//  @GetMapping("/{category_id}")
//  public ResponseEntity<CategoryAdminDto> getAdminCategory(@PathVariable("category_id") Long categoryId) {
//    try {
//      CategoryAdminDto categoryDto = categoryService.getAdminCategory(categoryId);
//      return ResponseEntity.ok(categoryDto);
//    } catch (ResourceNotFoundException e) {
//      throw new ResourceNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId);
//    }
//  }*/



  // 관리자 - 새 카테고리 추가
  @PostMapping
  public ResponseEntity<CategoryResponseDto> createCategory(
      @AuthenticationPrincipal Users user,
      @Valid @RequestBody CategoryRequestDto requestDto) {
    Users authUser = userService.findById(user.getUserId());    //인증객체 가져올시 영속성컨텍스트에서 가져와야함

    if (authUser == null || !authUser.getRole().equals(UserRole.ADMIN)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    try {
      CategoryResponseDto responseDto = categoryService.createCategory(requestDto);
      return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    } catch (BadRequestException e) {
      throw new BadRequestException("필수_파라미터_누락", "카테고리 생성 실패: " + e.getMessage());
    }
  }

  // 관리자 - 카테고리 정보 수정
  @PutMapping("/{category_id}")
  public ResponseEntity<CategoryResponseDto> updateCategory(
      @AuthenticationPrincipal Users user,
      @PathVariable("category_id") Long categoryId,
      @Valid @RequestBody CategoryRequestDto requestDto) {
    Users authUser = userService.findById(user.getUserId());    //인증객체 가져올시 영속성컨텍스트에서 가져와야함

    if (authUser == null || !authUser.getRole().equals(UserRole.ADMIN)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    try {
      CategoryResponseDto responseDto = categoryService.updateCategory(categoryId, requestDto);
      return ResponseEntity.ok(responseDto);
    } catch (ResourceNotFoundException e) {
      throw new ResourceNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId);
    } catch (BadRequestException e) {
      throw new BadRequestException("파라미터_길이_초과", "카테고리 수정 실패: " + e.getMessage());
    }
  }

  // 관리자 - 카테고리 삭제
  @DeleteMapping("/{category_id}")
  public ResponseEntity<Void> deleteCategory(
      @AuthenticationPrincipal Users user,
      @PathVariable("category_id") Long categoryId) {
    Users authUser = userService.findById(user.getUserId());    //인증객체 가져올시 영속성컨텍스트에서 가져와야함

    if (authUser == null || !authUser.getRole().equals(UserRole.ADMIN)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    try {
      categoryService.deleteCategory(categoryId);
      return ResponseEntity.noContent().build();
    } catch (ResourceNotFoundException e) {
      throw new ResourceNotFoundException("삭제할 카테고리를 찾을 수 없습니다: " + categoryId);
    }
  }

  // 관리자 카테고리 전체 목록 조회 (페이지네이션)
  @GetMapping
  public ResponseEntity<Page<CategoryAdminDto>> getAdminCategories(
      @AuthenticationPrincipal Users user,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "limit", defaultValue = "10") int limit) {
    Users authUser = userService.findById(user.getUserId());    //인증객체 가져올시 영속성컨텍스트에서 가져와야함

    if (authUser == null || !authUser.getRole().equals(UserRole.ADMIN)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    try {
      Page<CategoryAdminDto> pageDto = categoryService.getAdminCategories(page, limit);
      return ResponseEntity.ok(pageDto);
    } catch (BadRequestException e) {
      throw new BadRequestException("잘못된_파라미터_형식", "잘못된 페이지네이션 파라미터: " + e.getMessage());
    }
  }

  // 관리자 개별 카테고리 조회 - 카테고리 수정 시 필요
  @GetMapping("/{category_id}")
  public ResponseEntity<CategoryAdminDto> getAdminCategory(
      @AuthenticationPrincipal Users user,
      @PathVariable("category_id") Long categoryId) {
    Users authUser = userService.findById(user.getUserId());    //인증객체 가져올시 영속성컨텍스트에서 가져와야함

    if (authUser == null || !authUser.getRole().equals(UserRole.ADMIN)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    try {
      CategoryAdminDto categoryDto = categoryService.getAdminCategory(categoryId);
      return ResponseEntity.ok(categoryDto);
    } catch (ResourceNotFoundException e) {
      throw new ResourceNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId);
    }
  }

}
