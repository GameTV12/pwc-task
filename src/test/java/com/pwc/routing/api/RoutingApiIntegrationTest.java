package com.pwc.routing.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Boots the application exactly as in production — startup data comes from
 * the committed start file — except for the {@link OfflineFetcherConfig}
 * stub, so no test ever touches the network or rewrites the start file.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(OfflineFetcherConfig.class)
class RoutingApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void landRouteFromCzechRepublicToItaly() throws Exception {
        mockMvc.perform(get("/routing/CZE/ITA"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.route").isArray())
                .andExpect(jsonPath("$.route[0]").value("CZE"))
                .andExpect(jsonPath("$.route[1]").value("AUT"))
                .andExpect(jsonPath("$.route[2]").value("ITA"));
    }

    @Test
    void countryCodesAreCaseInsensitiveOnInputAndCanonicalUppercaseOnOutput() throws Exception {
        mockMvc.perform(get("/routing/cze/ita"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route[0]").value("CZE"))
                .andExpect(jsonPath("$.route[2]").value("ITA"));
    }

    @Test
    void originEqualToDestinationIsTheTrivialRoute() throws Exception {
        mockMvc.perform(get("/routing/CZE/CZE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route.length()").value(1))
                .andExpect(jsonPath("$.route[0]").value("CZE"));
    }

    @Test
    void noLandRouteIsBadRequestWithProblemDetails() throws Exception {
        mockMvc.perform(get("/routing/CZE/USA"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("No land route from CZE to USA"));
    }

    @Test
    void unknownCountryCodeIsBadRequestWithProblemDetails() throws Exception {
        mockMvc.perform(get("/routing/XXX/ITA"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Unknown country code: XXX"));
    }
}
