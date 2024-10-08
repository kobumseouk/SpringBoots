package com.spring_boots.spring_boots.item;

import com.amazonaws.Response;
import com.spring_boots.spring_boots.item.controller.ItemRestController;
import com.spring_boots.spring_boots.item.dto.CreateItemDto;
import com.spring_boots.spring_boots.item.dto.ResponseItemDto;
import com.spring_boots.spring_boots.item.service.ItemRestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @Test
    public void testCreateItem() throws Exception {
        // 필드 설정
        CreateItemDto createItemDto = new CreateItemDto();
        // 필드 설정
        ResponseItemDto responseItemDto = new ResponseItemDto();

        when(itemRestService.createItem(any(CreateItemDto.class))).thenReturn(responseItemDto);

        mockMvc.perform(post("/api/admin/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"field\":\"value\"}"))
                .andExpect(status().isCreated());
    }
    
}
