package com.spring_boots.spring_boots.category.dto;

import com.spring_boots.spring_boots.category.entity.Category;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-04T14:56:55+0900",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17.0.9 (GraalVM Community)"
)
@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Override
    public CategoryResponseDto categoryToCategoryResponseDto(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryResponseDto categoryResponseDto = new CategoryResponseDto();

        categoryResponseDto.setId( category.getId() );
        categoryResponseDto.setCategoryName( category.getCategoryName() );
        categoryResponseDto.setCategoryThema( category.getCategoryThema() );
        categoryResponseDto.setCategoryContent( category.getCategoryContent() );
        categoryResponseDto.setImageUrl( category.getImageUrl() );
        categoryResponseDto.setDisplayOrder( category.getDisplayOrder() );
        categoryResponseDto.setCreatedAt( category.getCreatedAt() );
        categoryResponseDto.setUpdatedAt( category.getUpdatedAt() );

        return categoryResponseDto;
    }

    @Override
    public CategoryDto categoryToCategoryDto(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryDto categoryDto = new CategoryDto();

        categoryDto.setId( category.getId() );
        categoryDto.setCategoryName( category.getCategoryName() );
        categoryDto.setCategoryContent( category.getCategoryContent() );
        categoryDto.setImageUrl( category.getImageUrl() );
        categoryDto.setDisplayOrder( category.getDisplayOrder() );

        return categoryDto;
    }

    @Override
    public CategoryAdminDto categoryToCategoryAdminDto(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryAdminDto categoryAdminDto = new CategoryAdminDto();

        categoryAdminDto.setId( category.getId() );
        categoryAdminDto.setCategoryName( category.getCategoryName() );
        categoryAdminDto.setCategoryThema( category.getCategoryThema() );
        categoryAdminDto.setDisplayOrder( category.getDisplayOrder() );
        categoryAdminDto.setCreatedAt( category.getCreatedAt() );
        categoryAdminDto.setUpdatedAt( category.getUpdatedAt() );

        return categoryAdminDto;
    }

    @Override
    public Category categoryRequestDtoToCategory(CategoryRequestDto requestDto) {
        if ( requestDto == null ) {
            return null;
        }

        Category.CategoryBuilder category = Category.builder();

        category.categoryThema( requestDto.getCategoryThema() );
        category.categoryName( requestDto.getCategoryName() );
        category.categoryContent( requestDto.getCategoryContent() );
        category.displayOrder( requestDto.getDisplayOrder() );
        category.imageUrl( requestDto.getImageUrl() );

        return category.build();
    }

    @Override
    public void updateCategoryFromDto(CategoryRequestDto dto, Category category) {
        if ( dto == null ) {
            return;
        }

        category.setCategoryThema( dto.getCategoryThema() );
        category.setCategoryName( dto.getCategoryName() );
        category.setCategoryContent( dto.getCategoryContent() );
        category.setDisplayOrder( dto.getDisplayOrder() );
        category.setImageUrl( dto.getImageUrl() );
    }
}
