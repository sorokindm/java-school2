package com.jschool.beans;


import com.jschool.dto.RestMedEventDto;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.faces.bean.ApplicationScoped;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@ApplicationScoped
@Startup
@Singleton
public class EventData {

    private Client client;

    private List<RestMedEventDto> medEvents;

    @PostConstruct
    public void init() {
        medEvents = readRestMedEvents();
    }

    private List<RestMedEventDto> readRestMedEvents() {
        List<RestMedEventDto> events;
        try {
            client = ClientBuilder.newClient();
            events = client.target("http://localhost:8081/java_school/screen")
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<RestMedEventDto>>() {
                    });
        } catch (WebApplicationException ex) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return events;
    }

    public List<RestMedEventDto> getMedEvents() {
        return medEvents;
    }
}

