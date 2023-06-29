package my.interview;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@AutoConfigureMockMvc
@Slf4j
public class RestControllerTests {
  @Autowired private MockMvc mockMvc;

  @Test
  public void testExchangeMachine_Scenario_1() throws Exception {
    int coinsCount = 100;

    // Machine Init - testApiMachine_Init(int size)
    testApiMachine_Init(coinsCount)
        .andExpect(jsonPath("$.availableCentBalance.1").value("100"))
        .andExpect(jsonPath("$.availableCentBalance.5").value("100"))
        .andExpect(jsonPath("$.availableCentBalance.10").value("100"))
        .andExpect(jsonPath("$.availableCentBalance.25").value("100"));
    // Machine State - testApiMachine_State()
    testApiMachine_State();

    // TopupCoins - testApiMachine_Topup(int coinCents, int coinCount)
    testApiMachine_Topup(35, 10)
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.code").value("Illegal cent amount: 35"));
    testApiMachine_Topup(10, 10)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.availableCentBalance.10").value("110"));
    testApiMachine_Topup(25, 10)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.availableCentBalance.25").value("110"));
    testApiMachine_State();

    // exchange bill - testApi_exchangeBill()
    testApi_exchangeBill(7).andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.code").value("Illegal bill amount: 7"));
    testApi_exchangeBill(1).andExpect(status().isOk())
            .andExpect(jsonPath("$.changeResult.coinsNumber").value("4"));

    //testApiMachine_State();
  }

  public ResultActions testApiMachine_Init(int coinCount) throws Exception {
    return mockMvc
        .perform(post("/api/machine/init?coinCount=" + coinCount))
        .andDo(
            mvcResult ->
                log.info(
                    "TEST - url: {}, response: {}",
                    "/api/machine/init?coinCount=" + coinCount,
                    mvcResult.getResponse().getContentAsString()))
        .andExpect(status().isOk());
  }

  public ResultActions testApiMachine_State() throws Exception {
    return mockMvc
        .perform(get("/api/machine/state"))
        .andDo(
            mvcResult ->
                log.info(
                    "TEST - url: {}, response: {}",
                    "/api/machine/state",
                    mvcResult.getResponse().getContentAsString()))
        .andExpect(status().isOk());
  }

  public ResultActions testApiMachine_Topup(int coinCents, int coinCount) throws Exception {
    return mockMvc
        .perform(
            get(
                String.format(
                    "/api/machine/topup_coin?coinCents=%s&coinCount=%s", coinCents, coinCount)))
        .andDo(
            mvcResult ->
                log.info(
                    "TEST - url: {}, response: {}",
                    String.format(
                        "/api/machine/topup_coin?coinCents=%s&coinCount=%s", coinCents, coinCount),
                    mvcResult.getResponse().getContentAsString()));
  }

  public ResultActions testApi_exchangeBill(int change_bill) throws Exception {
    return mockMvc
        .perform(get(String.format("/api/machine/bill_to_coints?change_bill=%s", change_bill)))
        .andDo(
            mvcResult ->
                log.info(
                    "TEST - url: {}, response: {}",
                    String.format("api/machine/bill_to_coints?change_bill=%s", change_bill),
                    mvcResult.getResponse().getContentAsString()));
    // .andExpect(status().isOk());
  }
}
