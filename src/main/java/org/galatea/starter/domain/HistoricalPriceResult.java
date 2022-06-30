package org.galatea.starter.domain;

import java.math.BigDecimal;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

// https://vladmihalcea.com/manytoone-jpa-hibernate/
@Entity
public class HistoricalPriceResult {
  @Id
  @GeneratedValue
  private long id;

  private String symbol;
  private String date;
  private Integer volume;
  private BigDecimal close;
  private BigDecimal high;
  private BigDecimal low;
  private BigDecimal open;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "search_id")
  private Search search;

  protected HistoricalPriceResult() {}

  public HistoricalPriceResult(String symbol, String date, Integer volume, BigDecimal close,
      BigDecimal high, BigDecimal low, BigDecimal open, Search search) {
    this.symbol = symbol;
    this.date = date;
    this.volume = volume;
    this.close = close;
    this.high = high;
    this.low = low;
    this.open = open;
    this.search = search;
  }

  public long getId() {
    return id;
  }

  public String getSymbol() {
    return symbol;
  }

  public String getDate() {
    return date;
  }

  public Integer getVolume() {
    return volume;
  }

  public BigDecimal getClose() {
    return close;
  }

  public BigDecimal getHigh() {
    return high;
  }

  public BigDecimal getLow() {
    return low;
  }

  public BigDecimal getOpen() {
    return open;
  }

  public Search getSearch() {
    return search;
  }
}
