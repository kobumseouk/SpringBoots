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

    @Test
    public void testGetItems() throws Exception {
        List<ResponseItemDto> itemList = Arrays.asList(new ResponseItemDto(), new ResponseItemDto());

        when(itemRestService.getAllItems()).thenReturn(itemList);

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));
    }

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
}
