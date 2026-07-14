package com.pwc.routing.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Swagger is the start page: {@code /} redirects to the Swagger UI, and the
 * OpenAPI document behind it describes both REST endpoints. Offline via
 * {@link OfflineFetcherConfig} like the other integration tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(OfflineFetcherConfig.class)
class SwaggerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rootRedirectsToSwaggerUi() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
    }

    @Test
    void openApiDocumentDescribesBothEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/routing/{origin}/{destination}']").exists())
                .andExpect(jsonPath("$.paths['/countries']").exists());
    }
}
