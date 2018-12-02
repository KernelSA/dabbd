package ua.kernel.dabbd.commons.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ua.kernel.dabbd.commons.entity.TriggerLogEnitity;

@Repository
public interface TriggerLogRepository extends PagingAndSortingRepository<TriggerLogEnitity, Long> {

}
