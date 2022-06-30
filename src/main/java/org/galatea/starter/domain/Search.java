package org.galatea.starter.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Search {
  @Id
  @GeneratedValue
  private long id;

  @Column(nullable = false)
  private String symbol;
  private String range;
  private String date;

  protected Search() {}

  public Search(String symbol, String range, String date) {
    this.symbol = symbol;
    this.range = range;
    this.date = date;
  }

  public Long getId() {
    return id;
  }

  public String getSymbol() {
    return symbol;
  }

  public String getRange() {
    return range;
  }

  public String getDate() {
    return date;
  }
}
