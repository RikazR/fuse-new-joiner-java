package org.galatea.starter.entrypoint;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import junitparams.JUnitParamsRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.ASpringTest;
import org.galatea.starter.domain.HistoricalPriceResult;
import org.galatea.starter.domain.ResultRepository;
import org.galatea.starter.domain.Search;
import org.galatea.starter.domain.SearchRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.NestedServletException;


@RequiredArgsConstructor
@Slf4j
// We need to do a full application start up for this one, since we want the feign clients to be instantiated.
// It's possible we could do a narrower slice of beans, but it wouldn't save that much test run time.
@SpringBootTest
// this gives us the MockMvc variable
@AutoConfigureMockMvc
// we previously used WireMockClassRule for consistency with ASpringTest, but when moving to a dynamic port
// to prevent test failures in concurrent builds, the wiremock server was created too late and feign was
// already expecting it to be running somewhere else, resulting in a connection refused
@AutoConfigureWireMock(port = 0, files = "classpath:/wiremock")
// Use this runner since we want to parameterize certain tests.
// See runner's javadoc for more usage.
@RunWith(JUnitParamsRunner.class)
public class IexRestControllerTest extends ASpringTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private SearchRepository searchRepository;

  @Autowired
  private ResultRepository resultRepository;

  @Test
  public void testGetSymbolsEndpoint() throws Exception {
    MvcResult result = this.mvc.perform(
        // note that we were are testing the fuse REST end point here, not the IEX end point.
        // the fuse end point in turn calls the IEX end point, which is WireMocked for this test.
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/iex/symbols?token=abc")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        // some simple validations, in practice I would expect these to be much more comprehensive.
        .andExpect(jsonPath("$[0].symbol", is("A")))
        .andExpect(jsonPath("$[1].symbol", is("AA")))
        .andExpect(jsonPath("$[2].symbol", is("AAAU")))
        .andReturn();
  }

  @Test
  public void testGetLastTradedPrice() throws Exception {

    MvcResult result = this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .get("/iex/lastTradedPrice?symbols=AAPL&token=abc")
            // This URL will be hit by the MockMvc client. The result is configured in the file
            // src/test/resources/wiremock/mappings/mapping-lastTradedPrice.json
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].symbol", is("FB")))
        .andExpect(jsonPath("$[0].price").value(new BigDecimal("186.34")))
        .andReturn();
  }

  @Test
  public void testGetLastTradedPriceEmpty() throws Exception {

    MvcResult result = this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .get("/iex/lastTradedPrice?token=abc&symbols=")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", is(Collections.emptyList())))
        .andReturn();
  }

  @Test
  public void testGetHistoricalPriceWithRange() throws Exception {

    histPriceRangeRequest();
  }

  @Test
  public void testGetHistoricalPriceWithDate() throws Exception {

    MvcResult result = this.mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/iex/historicalPrice?symbol=AAPL&date=20220104&token=abc")
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].symbol", is("AAPL")))
        .andExpect(jsonPath("$[0].close").value(new BigDecimal("182.53")))
        .andExpect(jsonPath("$[0].date", is("2022-01-04")))
        .andExpect(jsonPath("$[1].symbol", is("AAPL")))
        .andExpect(jsonPath("$[1].high").value(new BigDecimal("182.66")))
        .andExpect(jsonPath("$[2].date", is("2022-01-04")))
        .andReturn();
  }

  @Test
  public void testGetHistoricalPriceBase() throws Exception {

    MvcResult result = this.mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/iex/historicalPrice?symbol=AAPL&token=abc")
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].symbol", is("AAPL")))
        .andExpect(jsonPath("$[0].close").value(new BigDecimal("140.36")))
        .andExpect(jsonPath("$[1].symbol", is("AAPL")))
        .andExpect(jsonPath("$[1].high").value(new BigDecimal("141.785")))
        .andReturn();
  }

  @Test
  public void testGetHistoricalPriceNoSymbol() throws Exception {

    MvcResult result = this.mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/iex/historicalPrice?token=abc")
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest())
        .andReturn();
  }

  @Test(expected = NestedServletException.class)
  public void testGetHistoricalPriceInvalidSymbol() throws Exception {

    MvcResult result = this.mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/iex/historicalPrice?symbol=invalid&token=abc")
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isInternalServerError())
        .andReturn();
  }

  private void histPriceRangeRequest() throws Exception {
    MvcResult result = this.mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/iex/historicalPrice?symbol=AAPL&range=6m&token=abc")
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].symbol", is("AAPL")))
        .andExpect(jsonPath("$[0].close").value(new BigDecimal("180.33")))
        .andExpect(jsonPath("$[1].symbol", is("AAPL")))
        .andExpect(jsonPath("$[1].high").value(new BigDecimal("181.33")))
        .andExpect(jsonPath("$[2].date", is("2021-12-29")))
        .andReturn();
  }
  @Test
  public void testSearchStorage() throws Exception {

    histPriceRangeRequest();

    Search search = searchRepository.findBySymbolAndRangeAndDateAllIgnoreCase("AAPL", "6m", "");
    assertEquals(search.getSymbol(), "AAPL");
    assertEquals(search.getRange(), "6m");
    assertEquals(search.getDate(), "");

    List<HistoricalPriceResult> historicalPriceResults = resultRepository.findBySearch(search);
    assertEquals(4, historicalPriceResults.size());
    HistoricalPriceResult firstResult = historicalPriceResults.get(0);
    HistoricalPriceResult secondResult = historicalPriceResults.get(1);

    assertEquals("AAPL", firstResult.getSymbol());
    assertEquals("2021-12-27", firstResult.getDate());
    assertEquals(new BigDecimal("180.33"), firstResult.getClose());
    assertEquals("AAPL", secondResult.getSymbol());
    assertEquals(Integer.valueOf(79144339), secondResult.getVolume());
    assertEquals(new BigDecimal("178.53"), secondResult.getLow());

    histPriceRangeRequest();
  }
}
