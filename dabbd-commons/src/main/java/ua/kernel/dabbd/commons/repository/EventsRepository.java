package ua.kernel.dabbd.commons.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ua.kernel.dabbd.commons.entity.EventsEntity;

@Repository
public interface EventsRepository extends PagingAndSortingRepository<EventsEntity, Long> {

}
