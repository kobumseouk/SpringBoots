package com.spring_boots.spring_boots.item.controller;

import com.spring_boots.spring_boots.item.dto.CreateItemDto;
import com.spring_boots.spring_boots.item.dto.ResponseItemDto;
import com.spring_boots.spring_boots.item.dto.UpdateItemDto;
import com.spring_boots.spring_boots.item.service.ItemRestService;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.data.domain.Pageable;

@ActiveProfiles("test")
public class ItemRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private ItemRestService itemRestService;

    @InjectMocks
    private ItemRestController itemRestController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(itemRestController).build();
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

        when(itemRestService.getItem(1L)).thenReturn(responseItemDto);

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testDeleteItem() throws Exception {
        doNothing().when(itemRestService).deleteItem(1L);

        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testUpdateItem() throws Exception {
        UpdateItemDto updateItemDto = new UpdateItemDto();

        ResponseItemDto responseItemDto = new ResponseItemDto();

        when(itemRestService.updateItem(any(Long.class), any(UpdateItemDto.class))).thenReturn(responseItemDto);

        mockMvc.perform(put("/api/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"field\":\"value\"}"))
                .andExpect(status().isOk());
    }

    // 검색한 아이템들 목록 조회 + 페이지네이션 확인
    @Test
    public void testSearchItems() throws Exception {
        // item1과 item2에 필요한 데이터 설정
        List<ResponseItemDto> searchResults = Arrays.asList(new ResponseItemDto(), new ResponseItemDto(), new ResponseItemDto());
        Page<ResponseItemDto> page = new PageImpl<>(searchResults, PageRequest.of(0, 8), 3);

        // 서비스 메소드 모킹
        when(itemRestService.searchAndSortItems(anyString(), anyString(), anyInt(), anyInt())).thenReturn(page);

        // 테스트 실행
        mockMvc.perform(get("/api/items/search")
                .param("keyword", "test")
                .param("sort", "price-asc")
                .param("page", "0")
                .param("limit", "8"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content", hasSize(3)))
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.size").value(8))
            .andExpect(jsonPath("$.number").value(0));
    }
}
