package ua.kernel.dabbd.commons.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ua.kernel.dabbd.commons.entity.DmTrackerEntity;

@Repository
public interface DmTrackerRepository extends PagingAndSortingRepository<DmTrackerEntity, String> {

}
