package com.spring_boots.spring_boots.category.dto.category;

import com.spring_boots.spring_boots.category.entity.Category;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-08T13:31:44+0900",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17.0.9 (GraalVM Community)"
)
@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Override
    public CategoryResponseDto categoryToCategoryResponseDto(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryResponseDto.CategoryResponseDtoBuilder categoryResponseDto = CategoryResponseDto.builder();

        categoryResponseDto.id( category.getId() );
        categoryResponseDto.categoryName( category.getCategoryName() );
        categoryResponseDto.categoryThema( category.getCategoryThema() );
        categoryResponseDto.categoryContent( category.getCategoryContent() );
        categoryResponseDto.imageUrl( category.getImageUrl() );
        categoryResponseDto.displayOrder( category.getDisplayOrder() );
        categoryResponseDto.createdAt( category.getCreatedAt() );
        categoryResponseDto.updatedAt( category.getUpdatedAt() );

        return categoryResponseDto.build();
    }

    @Override
    public CategoryDto categoryToCategoryDto(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryDto.CategoryDtoBuilder categoryDto = CategoryDto.builder();

        categoryDto.id( category.getId() );
        categoryDto.categoryName( category.getCategoryName() );
        categoryDto.categoryContent( category.getCategoryContent() );
        categoryDto.imageUrl( category.getImageUrl() );
        categoryDto.displayOrder( category.getDisplayOrder() );

        return categoryDto.build();
    }

    @Override
    public CategoryAdminDto categoryToCategoryAdminDto(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryAdminDto.CategoryAdminDtoBuilder categoryAdminDto = CategoryAdminDto.builder();

        categoryAdminDto.id( category.getId() );
        categoryAdminDto.categoryName( category.getCategoryName() );
        categoryAdminDto.categoryThema( category.getCategoryThema() );
        categoryAdminDto.displayOrder( category.getDisplayOrder() );
        categoryAdminDto.createdAt( category.getCreatedAt() );
        categoryAdminDto.updatedAt( category.getUpdatedAt() );

        return categoryAdminDto.build();
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
