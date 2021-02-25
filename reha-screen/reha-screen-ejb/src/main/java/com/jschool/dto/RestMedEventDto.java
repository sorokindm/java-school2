package com.jschool.dto;

import lombok.Data;

import javax.json.bind.annotation.JsonbDateFormat;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Dto for screen rest service. Used to load data on application startup
 */
@Data
public class RestMedEventDto implements Serializable {
    private int idMedEvent;

    private String patientName;

    private String patientLastName;

    private String nurseName;

    private String nurseLastName;

    private String status;

    @JsonbDateFormat("yyyy-MM-dd HH:mm")
    private LocalDateTime starts;

}
