package org.galatea.starter.domain;

import org.springframework.data.repository.CrudRepository;

public interface SearchRepository extends CrudRepository<Search, Long> {
  Search findBySymbolAndRangeAndDateAllIgnoreCase(String symbol, String range, String date);
}
