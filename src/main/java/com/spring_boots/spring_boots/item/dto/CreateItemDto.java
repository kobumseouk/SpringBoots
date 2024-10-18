package com.spring_boots.spring_boots.item.dto;

import com.spring_boots.spring_boots.category.entity.Category;
import com.spring_boots.spring_boots.item.entity.Item;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class CreateItemDto {
    private Long id;

    @NotBlank(message = "상품명은 필수입니다.")
    @Length(max = 200)
    private String itemName;

    @NotNull(message = "카테고리 ID는 필수입니다.")
    private Long categoryId;

    @NotBlank(message = "가격은 필수입니다.")
    @Positive(message = "가격은 0보다 커야 합니다.")
    private Integer itemPrice;

    @Length(max = 10000, message = "설명란의 최대 글자수는 10000입니다.")
    private String itemDescription;

    private String itemMaker;

    private String itemColor;

    private String imageUrl;

    private List<String> keywords = new ArrayList<>();



    public Item toEntity() {
        Item item = new Item();
        item.setItemName(itemName);
        item.setItemPrice(itemPrice);
        item.setItemDescription(itemDescription);
        item.setItemMaker(itemMaker);
        item.setItemColor(itemColor);
        item.setImageUrl(imageUrl);
        item.setKeywords(keywords);
        return item;
    }
}
