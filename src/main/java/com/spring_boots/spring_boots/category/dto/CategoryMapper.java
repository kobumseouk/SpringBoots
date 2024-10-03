package com.spring_boots.spring_boots.category.dto;

// import com.spring_boots.spring_boots.category.dto.*;
import com.spring_boots.spring_boots.category.entity.Category;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public abstract class CategoryMapper {

  // Category -> CategoryResponseDto
  public abstract CategoryResponseDto categoryToCategoryResponseDto(Category category);

  // Category -> CategoryDto
  public abstract CategoryDto categoryToCategoryDto(Category category);

  // Category -> CategoryAdminDto
  public abstract CategoryAdminDto categoryToCategoryAdminDto(Category category);

  // CategoryRequestDto -> Category
  @Mapping(target = "id", ignore = true)
  public abstract Category categoryRequestDtoToCategory(CategoryRequestDto requestDto);

  // CategoryRequestDto -> Category (업데이트)
  @Mapping(target = "id", ignore = true)
  public abstract void updateCategoryFromDto(CategoryRequestDto dto, @MappingTarget Category category);

}


