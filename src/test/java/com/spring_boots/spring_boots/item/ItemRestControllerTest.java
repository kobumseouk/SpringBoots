package com.spring_boots.spring_boots.item;

import com.spring_boots.spring_boots.item.controller.ItemRestController;
import com.spring_boots.spring_boots.item.service.ItemRestService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
}
