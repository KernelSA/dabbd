package ua.kernel.dabbd.restapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.kernel.dabbd.commons.entity.DmTrackerEntity;
import ua.kernel.dabbd.commons.repository.DmTrackerRepository;
import ua.kernel.dabbd.commons.repository.EventsRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TrackerDataService {

    private final DmTrackerRepository dmTrackerRepository;
    private final EventsRepository eventsRepository;

    public List<DmTrackerEntity> getAllTrackersView() {
        return dmTrackerRepository.findAll();
    }

    public Optional<DmTrackerEntity> getTrackerInfo(String trackerId) {
        return dmTrackerRepository.findById(trackerId);
    }

}
