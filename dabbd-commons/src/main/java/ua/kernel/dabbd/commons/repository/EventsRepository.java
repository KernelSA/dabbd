package ua.kernel.dabbd.commons.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.kernel.dabbd.commons.entity.EventsEntity;

@Repository
public interface EventsRepository extends JpaRepository<EventsEntity, Long> {

}
