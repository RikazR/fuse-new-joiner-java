package org.galatea.starter.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.domain.HistoricalPriceResult;
import org.galatea.starter.domain.IexHistoricalPrice;
import org.galatea.starter.domain.IexLastTradedPrice;
import org.galatea.starter.domain.IexSymbol;
import org.galatea.starter.domain.ResultRepository;
import org.galatea.starter.domain.Search;
import org.galatea.starter.domain.SearchRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * A layer for transformation, aggregation, and business required when retrieving data from IEX.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IexService {

  @NonNull
  private IexClient iexClient;

  @NonNull
  private SearchRepository searchRepository;

  @NonNull
  private ResultRepository resultRepository;


  /**
   * Get all stock symbols from IEX.
   *
   * @return a list of all Stock Symbols from IEX.
   */
  public List<IexSymbol> getAllSymbols(final String token) {
    return iexClient.getAllSymbols(token);
  }

  /**
   * Get the last traded price for each Symbol that is passed in.
   *
   * @param symbols the list of symbols to get a last traded price for.
   * @return a list of last traded price objects for each Symbol that is passed in.
   */
  public List<IexLastTradedPrice> getLastTradedPriceForSymbols(
      final List<String> symbols, final String token
  ) {
    if (CollectionUtils.isEmpty(symbols)) {
      return Collections.emptyList();
    } else {
      return iexClient.getLastTradedPriceForSymbols(symbols.toArray(new String[0]), token);
    }
  }

  public List<IexHistoricalPrice> getHistoricalPriceForSymbol(
      final String symbol, final String range, final String date, final String token
  ) {
    Search search = searchRepository.findBySymbolAndRangeAndDateAllIgnoreCase(symbol, range, date);
    if (search == null) {
      log.info("Search not found, searching on IEX");
      List<IexHistoricalPrice> restResult = iexClient.getHistoricalPriceForSymbol(symbol, range, date, token);
      Search newSearch = new Search(symbol, range, date);
      searchRepository.save(newSearch);
      restResult.forEach(iexHistoricalPrice -> resultRepository.save(new HistoricalPriceResult(
          iexHistoricalPrice.getSymbol(),
          iexHistoricalPrice.getDate(),
          iexHistoricalPrice.getVolume(),
          iexHistoricalPrice.getClose(),
          iexHistoricalPrice.getHigh(),
          iexHistoricalPrice.getLow(),
          iexHistoricalPrice.getOpen(),
          newSearch
      )));
      return restResult;
    }
    List<HistoricalPriceResult> searchResult = resultRepository.findBySearch(search);
    log.info("Length of search result: " + searchResult.size() + " Search obj: " + search);
    return searchResult.stream().map(historicalPriceResult -> IexHistoricalPrice.builder()
        .symbol(historicalPriceResult.getSymbol())
        .date(historicalPriceResult.getDate())
        .volume(historicalPriceResult.getVolume())
        .close(historicalPriceResult.getClose())
        .high(historicalPriceResult.getHigh())
        .low(historicalPriceResult.getLow())
        .open(historicalPriceResult.getOpen())
        .build()).collect(Collectors.toList());
  }

}
