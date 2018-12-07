package ua.kernel.dabbd.commons.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.kernel.dabbd.commons.entity.DmTrackerEntity;

@Repository
public interface DmTrackerRepository extends JpaRepository<DmTrackerEntity, String> {

}
