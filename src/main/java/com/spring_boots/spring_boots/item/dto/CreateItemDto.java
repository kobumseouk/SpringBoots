package com.spring_boots.spring_boots.item.dto;

import com.spring_boots.spring_boots.item.entity.Item;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
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
    @Max(value = 10000000, message = "가격은 최대 10,000,000원 이하여야 합니다.")
    private Long itemPrice;

    @Length(max = 1000, message = "설명란의 최대 글자수는 1000입니다.")
    private String itemDescription;


    private String itemMaker;

    private List<String> itemColor;

    private String imageUrl;

    private List<String> keywords;

    // 사용하지 않음
    private int itemSize;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public Item toEntity() {
        Item item = new Item();
        item.setItemName(itemName);
        item.setItemPrice(itemPrice);
        item.setItemDescription(itemDescription);
        item.setItemMaker(itemMaker);
        item.setItemColor(itemColor);
        item.setImageUrl(imageUrl);
        item.setKeywords(keywords);
        item.setItemSize(itemSize);

        LocalDateTime now = LocalDateTime.now();
        item.setCreatedAt(now); // 엔티티에 createdAt 설정
        item.setUpdatedAt(now); // 엔티티에 updatedAt 설정

        return item;
    }
}
