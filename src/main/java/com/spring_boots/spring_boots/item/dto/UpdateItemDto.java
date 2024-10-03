package com.spring_boots.spring_boots.item.dto;

import com.spring_boots.spring_boots.category.entity.Category;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@ToString
public class UpdateItemDto {
    @NotBlank(message = "상품명은 필수입니다.")
    @Length(max = 200)
    private String itemName;

    @NotBlank(message = "카테고리는 필수입니다.")
    private Category category;

    @NotBlank(message = "가격은 필수입니다.")
    private Integer itemPrice;

    @Length(max = 10000, message = "설명란의 최대 글자수는 10000입니다.")
    private String itemDescription;

    private String itemMaker;

    @NotBlank(message = "상품 색상은 필수입니다.")
    private String itemColor;

    @NotBlank(message = "상품 사이즈는 필수입니다.")
    private Integer itemSize;

    private String imageUrl;

    private String imageDeleted;
}
