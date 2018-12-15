package ua.kernel.dabbd.commons.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.kernel.dabbd.commons.entity.EventsEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventsRepository extends JpaRepository<EventsEntity, Long> {

    List<EventsEntity> findAllByTrackerId(String trackerId);
    List<EventsEntity> findByTrackerIdAndEventDtGreaterThan(String trackerId, LocalDateTime date);

}
