package com.jschool;


import com.jschool.dto.MedEventViewDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Web named bean/model Handles data to jsf and estabilishes a websocket
 */
@ApplicationScoped
@Startup
@Named("medEvents")
public class EventData {

    private final Logger logger = LogManager.getLogger();

    @Inject
    @Push(channel = "update")
    private PushContext push;

    @Inject
    private MedEventsEJB medEventsEJB;

    @PostConstruct
    public void init() {
        medEventsEJB.setPushContext(push);
        logger.info("Set push context in ejb module");
    }

    /**
     * Gets current time
     * @return Current time in HH:mm format
     */
    public String getTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        return LocalTime.now().format(dtf);
    }

    /**
     * Returns list of upcoming medEvents
     * @return List of current MedEvents except first one
     */
    public List<MedEventViewDto> getMedEventsList() {
        List<MedEventViewDto> list=medEventsEJB.getMedEvents();
        if (list.size()<=1) return new ArrayList<>();
        return new ArrayList<>(medEventsEJB.getMedEvents().subList(1, medEventsEJB.getMedEvents().size()));
    }

    /**
     * Gets first (by start time) event
     * @return first event
     */
    public MedEventViewDto getCurrentEvent() {
        List<MedEventViewDto> list=medEventsEJB.getMedEvents();
        if (!list.isEmpty()) return list.get(0);
        return null;
    }

    /**
     * Gets list of TimedOut events
     * @return list of timed out events
     */
    public List<MedEventViewDto> getClosedEvents() {
        return medEventsEJB.getClosedEvents();
    }

}

