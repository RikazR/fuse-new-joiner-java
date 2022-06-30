package org.galatea.starter.domain;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ResultRepository extends CrudRepository<HistoricalPriceResult, Long> {
  List<HistoricalPriceResult> findBySearch(Search search);
}
