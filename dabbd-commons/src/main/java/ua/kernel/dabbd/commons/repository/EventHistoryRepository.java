package ua.kernel.dabbd.commons.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventHistoryRepository  extends PagingAndSortingRepository<Long,Object> {

}
