package com.jschool;

import com.jschool.dto.MedEventViewDto;
import com.jschool.dto.RestMedEventDto;
import com.jschool.enums.MedEventStatus;
import com.jschool.exception.NoEventFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.push.PushContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.time.LocalTime;
import java.util.*;

/**
 * Application scoped bean that encapsulates app logic. Manages data cache,data updates, scheduled tasks
 */
@ApplicationScoped
@Startup
@Singleton
public class MedEventsEJB {
    private final Logger logger = LogManager.getLogger();

    private Map<String, MedEventViewDto> medEvents;

    private List<MedEventViewDto> closedEvents;

    private PushContext pushContext;

    @PostConstruct
    public void init() {
        medEvents = readRestMedEvents();
        closedEvents = new ArrayList<>();
    }

    @Schedule(hour = "0")
    public void dailyRefresh() {
        init();
        logger.info("Finished daily bean refresh.");
    }

    @Schedule(hour="*",minute = "*")
    public void minuteUpdate() {
        for (Map.Entry<String,MedEventViewDto> entry: medEvents.entrySet()) {
            if (entry.getValue().getStartsTime().isBefore(LocalTime.now())) {
                closedEvents.add(entry.getValue());
                medEvents.remove(entry.getKey());
                logger.info("Timed out event due to time with id"+entry.getValue().getIdMedEvent());
            }
        }
        if (pushContext!=null) pushContext.send("update");
    }

    /**
     * Removes event from current events map and adds it to closedEvents list
     * Sends websocket message to update views
     * @param id - id of event to close
     * @param status - status of event after closing
     */
    public void closeEvent(int id, MedEventStatus status) {
        if(status==null) throw new IllegalArgumentException("MedEventStatus cannot be null");
        MedEventViewDto dto = medEvents.get(Integer.toString(id));
        medEvents.remove(Integer.toString(id));
        if (dto != null) {
            dto.setStatus(status.toString());
            closedEvents.add(dto);
        }
        else {
            logger.error("No such event found");
            throw new NoEventFoundException();
        }

        if (pushContext!= null) pushContext.send("update");
        logger.info("Removed Med Event with id:"+ id);
    }

    /**
     * Puts new event to current events map
     * Sends websocket message to update views
     * @param dto - dto to put to the map
     */
    public void newEvent(MedEventViewDto dto) {
        medEvents.put(Integer.toString(dto.getIdMedEvent()), dto);
        if (pushContext!=null) pushContext.send("update");
        logger.info("Added new Med Event from message with id:"+ dto.getIdMedEvent());
    }

    public void setPushContext(PushContext pushContext) {
        this.pushContext = pushContext;
    }

    public List<MedEventViewDto> getMedEvents() {
        ArrayList<MedEventViewDto> list = new ArrayList<>(medEvents.values());
        Collections.sort(list);
        return list;
    }

    public List<MedEventViewDto> getClosedEvents() {
        return closedEvents;
    }

    /**
     * Loads today's medEvents from remote REST service
     * @return Map of medEvents dtos
     */
    private Map<String, MedEventViewDto> readRestMedEvents() {
        List<RestMedEventDto> events;
        Client client;
        try {
            client = ClientBuilder.newClient();
            events = client.target("http://localhost:8081/java_school/screen")
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<RestMedEventDto>>() {
                    });
        } catch (WebApplicationException ex) {
            logger.error("Could not get Med Events data via REST on startup", ex);
            return new HashMap<>();
        }
        Map<String, MedEventViewDto> eventsMap = new HashMap<>();
        for (RestMedEventDto dto : events) {
            MedEventViewDto viewDto = new MedEventViewDto();
            viewDto.setIdMedEvent(dto.getIdMedEvent());
            viewDto.setNurseName(dto.getNurseName());
            viewDto.setNurseLastName(dto.getNurseLastName());
            viewDto.setPatientName(dto.getPatientName());
            viewDto.setPatientLastName(dto.getPatientLastName());
            viewDto.setStatus(dto.getStatus());
            viewDto.setStartsDate(dto.getStarts().toLocalDate());
            viewDto.setStartsTime(dto.getStarts().toLocalTime());
            eventsMap.put(Integer.toString(dto.getIdMedEvent()), viewDto);
        }
        logger.info("Loaded events on application startup. Events quantity:"+eventsMap.size());
        return eventsMap;
    }
}
