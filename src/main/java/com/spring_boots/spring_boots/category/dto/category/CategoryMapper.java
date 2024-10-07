package com.spring_boots.spring_boots.category.dto.category;


import com.spring_boots.spring_boots.category.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface CategoryMapper {

  // Category -> CategoryResponseDto
  CategoryResponseDto categoryToCategoryResponseDto(Category category);

  // Category -> CategoryDto
  CategoryDto categoryToCategoryDto(Category category);

  // Category -> CategoryAdminDto
  CategoryAdminDto categoryToCategoryAdminDto(Category category);

  // CategoryRequestDto -> Category
  @Mapping(target = "id", ignore = true)
  Category categoryRequestDtoToCategory(CategoryRequestDto requestDto);

  // CategoryRequestDto -> Category (업데이트)
  @Mapping(target = "id", ignore = true)
  void updateCategoryFromDto(CategoryRequestDto dto, @MappingTarget Category category);

}


