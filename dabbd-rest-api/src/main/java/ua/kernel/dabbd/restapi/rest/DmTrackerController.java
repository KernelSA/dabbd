package ua.kernel.dabbd.restapi.rest;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ua.kernel.dabbd.commons.entity.DmTrackerEntity;
import ua.kernel.dabbd.restapi.service.TrackerDataService;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DmTrackerController {

    private final TrackerDataService trackerDataService;

    @RequestMapping(value = "/api/tracker/all", method = RequestMethod.GET)
    public List<DmTrackerEntity> allTrackersInfo() {
        log.debug("REST Endpoint /api/tracker/all called");
        return trackerDataService.getAllTrackersView();
    }

    @RequestMapping(value = "/api/tracker/{trackerId}", method = RequestMethod.GET)
    public Optional<DmTrackerEntity> trackerInfo(@PathVariable String trackerId) {
        log.debug("REST Endpoint /api/tracker/{} called", trackerId);
        return trackerDataService.getTrackerInfo(trackerId);
    }

}
