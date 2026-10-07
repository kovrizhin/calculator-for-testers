package io.paradym.testercalculator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OperationFlowSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldEvaluateHighPriorityEquation() throws Exception {
        MvcResult submitted = mockMvc.perform(post("/api/operations")
                        .with(httpBasic("alice", "alice123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"equation\":\"6+7+(3+4*5) * 3\",\"priority\":\"HIGH\"}"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();
        String id = objectMapper.readTree(submitted.getResponse().getContentAsString()).get("id").asString();

        Instant deadline = Instant.now().plus(Duration.ofSeconds(5));
        int lastStatus = 0;
        String lastBody = "";
        while (Instant.now().isBefore(deadline)) {
            MvcResult polled = mockMvc.perform(get("/api/operations/" + id).with(httpBasic("alice", "alice123")))
                    .andReturn();
            lastStatus = polled.getResponse().getStatus();
            lastBody = polled.getResponse().getContentAsString();
            if (lastStatus == 200) {
                break;
            }
            assertThat(lastStatus).isEqualTo(202);
            Thread.sleep(200);
        }

        assertThat(lastStatus).isEqualTo(200);
        var body = objectMapper.readTree(lastBody);
        assertThat(body.get("status").asString()).isEqualTo("DONE");
        assertThat(body.get("result").asDouble()).isEqualTo(82.0);
    }

    @Test
    void shouldExposeHealthWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void shouldRejectMeWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void shouldReturnCurrentUser() throws Exception {
        mockMvc.perform(get("/api/me").with(httpBasic("bob", "bob123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bob"));
    }
}
