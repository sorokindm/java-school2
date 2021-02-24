package com.jschool.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class MedEventViewDto implements Serializable, Comparable<MedEventViewDto> {
    private int idMedEvent;

    private String patientName;

    private String patientLastName;

    private String nurseName;

    private String nurseLastName;

    private String status;

    private LocalDate startsDate;

    private LocalTime startsTime;

    @Override
    public int compareTo(MedEventViewDto o) {
        return startsTime.compareTo(o.getStartsTime());
    }
}
