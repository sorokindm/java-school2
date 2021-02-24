package com.jschool;


import com.jschool.dto.MedEventViewDto;
import com.jschool.dto.RestMedEventDto;
import com.jschool.enums.MedEventStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@ApplicationScoped
@Startup
@Singleton
@Named("medEvents")
public class EventData {

    private final Logger logger = LogManager.getLogger();



    private Map<String, MedEventViewDto> medEvents;

    private List<MedEventViewDto> closedEvents;

    @Inject
    @Push(channel = "update")
    private PushContext pushContext;

    @PostConstruct
    public void init() {
        medEvents = readRestMedEvents();
        closedEvents = new ArrayList<>();
    }

    public Map<String, MedEventViewDto> getMedEvents() {
        return medEvents;
    }

    public String getTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        return LocalTime.now().format(dtf);
    }

    public void closeEvent(int id, MedEventStatus status) {
        MedEventViewDto dto = medEvents.get(Integer.toString(id));
        medEvents.remove(Integer.toString(id));
        if (dto != null) {
            dto.setStatus(status.toString());
            closedEvents.add(dto);
        }

        pushContext.send("update");
        logger.info("Removed Med Event with id:" , id);
    }

    public void newEvent(MedEventViewDto dto) {
        medEvents.put(Integer.toString(dto.getIdMedEvent()), dto);
        pushContext.send("update");
        logger.info("Added new Med Event from message with id:", dto.getIdMedEvent());
    }

    public List<MedEventViewDto> getMedEventsList() {
        ArrayList<MedEventViewDto> dtos = new ArrayList<>(medEvents.values());
        Collections.sort(dtos);
        return new ArrayList<>(medEvents.values());
    }

    public MedEventViewDto getCurrentEvent() {
        ArrayList<MedEventViewDto> dtos = new ArrayList<>(medEvents.values());
        Collections.sort(dtos);
        return dtos.get(0);
    }

    public List<MedEventViewDto> getClosedEvents() {
        return closedEvents;
    }

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
            return null;
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
        return eventsMap;
    }
}

