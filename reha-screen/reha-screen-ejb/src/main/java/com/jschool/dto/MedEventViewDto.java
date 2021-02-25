package com.jschool.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Dto to store medEvents data
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
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
