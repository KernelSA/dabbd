package ua.kernel.dabbd.loadtest.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventsTestRepository extends JpaRepository<EventsTestEntity, Long> {

}
