package com.spring_boots.spring_boots.item.controller;

import com.spring_boots.spring_boots.item.dto.ResponseItemDto;
import com.spring_boots.spring_boots.item.dto.SearchItemDto;
import com.spring_boots.spring_boots.item.dto.UpdateItemDto;
import com.spring_boots.spring_boots.item.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
public class ItemApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemApiController itemApiController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(itemApiController).build();
    }

    /*
    @Test
    public void testCreateItem() throws Exception {
        // 필드 설정
        CreateItemDto createItemDto = new CreateItemDto();
        // 필드 설정
        ResponseItemDto responseItemDto = new ResponseItemDto();

        when(itemRestService.createItem(any(CreateItemDto.class), any(MultipartFile.class))).thenReturn(responseItemDto);

        mockMvc.perform(post("/api/admin/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"field\":\"value\"}"))
                .andExpect(status().isCreated());
    }*/

//    @Test
//    public void testGetItems() throws Exception {
//        List<ResponseItemDto> itemList = Arrays.asList(new ResponseItemDto(), new ResponseItemDto());
//
//        when(itemRestService.getAllItems()).thenReturn(itemList);
//
//        mockMvc.perform(get("/api/items"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$", hasSize(2)));
//    }

    @Test
    public void testGetItem() throws Exception {
        ResponseItemDto responseItemDto = new ResponseItemDto();

        when(itemService.getItem(1L)).thenReturn(responseItemDto);

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testDeleteItem() throws Exception {
        doNothing().when(itemService).deleteItem(1L);

        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testUpdateItem() throws Exception {
        UpdateItemDto updateItemDto = new UpdateItemDto();

        ResponseItemDto responseItemDto = new ResponseItemDto();

        when(itemService.updateItem(any(Long.class), any(UpdateItemDto.class))).thenReturn(responseItemDto);

        mockMvc.perform(put("/api/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"field\":\"value\"}"))
                .andExpect(status().isOk());
    }

    // 검색한 아이템들 목록 조회 + 페이지네이션 + 정렬 기능 확인
    @Test
    @DisplayName("검색, 정렬, 페이징 통합 테스트")
    public void testSearchItems() throws Exception {
        // given
        List<SearchItemDto> searchResults = Arrays.asList(new SearchItemDto(), new SearchItemDto());
        Page<SearchItemDto> page = new PageImpl<>(searchResults, PageRequest.of(0, 8), 2);

        String[] sortOptions = {"price-asc", "price-desc", "newest", "best", "default"};

        for (String sortOption : sortOptions) {
            // when
            when(itemService.searchAndSortItems(anyString(), eq(sortOption), anyInt(), anyInt()))
                .thenReturn(page);

            // then
            mockMvc.perform(get("/api/items/search")
                    .param("keyword", "test")
                    .param("sort", sortOption)
                    .param("page", "0")
                    .param("limit", "8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(8))
                .andExpect(jsonPath("$.number").value(0));

            verify(itemService).searchAndSortItems("test", sortOption, 0, 8);
        }
    }

    // 빈 문자열의 검색어를 받는 경우
    @Test
    public void testSearchItems_WithEmptyKeyword() throws Exception {
        mockMvc.perform(get("/api/items/search")
                .param("page", "0")
                .param("limit", "8"))
            .andExpect(status().isBadRequest());
    }
}
