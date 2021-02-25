package com.jschool.test

import com.jschool.MedEventsEJB
import com.jschool.dto.MedEventViewDto
import com.jschool.enums.MedEventStatus
import com.jschool.exception.NoEventFoundException
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.MockitoAnnotations
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.assertEquals

class MedEventEJBTest {
    @InjectMocks
    var medEventsEJB = MedEventsEJB()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        val map = HashMap<String, MedEventViewDto>()

        val dto1 = MedEventViewDto(1, "Ivan", "Ivanov",
                "Elena", "Lenina",
                "SCHEDULED", LocalDate.now(), LocalTime.of(0, 0).plusHours(2))
        val dto2 = MedEventViewDto(2, "Ivan", "Ivanov",
                "Elena", "Lenina",
                "SCHEDULED", LocalDate.now(), LocalTime.of(0, 0).plusHours(1))
        val dto3 = MedEventViewDto(3, "Ivan", "Ivanov",
                "Elena", "Lenina",
                "SCHEDULED", LocalDate.now(), LocalTime.of(0, 0).plusHours(3))
        val dto4 = MedEventViewDto(4, "Ivan", "Ivanov",
                "Elena", "Lenina",
                "SCHEDULED", LocalDate.now(), LocalTime.of(0, 0).plusHours(4))
        val dto5 = MedEventViewDto(5, "Ivan", "Ivanov",
                "Elena", "Lenina",
                "SCHEDULED", LocalDate.now(), LocalTime.of(0, 0).plusHours(6))

        map.put(dto1.idMedEvent.toString(), dto1)
        map.put(dto2.idMedEvent.toString(), dto2)
        map.put(dto3.idMedEvent.toString(), dto3)
        map.put(dto4.idMedEvent.toString(), dto4)
        map.put(dto5.idMedEvent.toString(), dto5)

        ReflectionTestUtils.setField(medEventsEJB, "medEvents", map)
        ReflectionTestUtils.setField(medEventsEJB, "closedEvents", ArrayList<MedEventViewDto>())
        medEventsEJB.setPushContext(null)
    }

    @Test
    fun sortingTest() {
        val list = medEventsEJB.medEvents
        val listIds = ArrayList<Int>()
        for (event in list) {
            listIds.add(event.startsTime.hour)
        }
        assertEquals(mutableListOf(1, 2, 3, 4, 6), listIds)
    }

    @Test
    fun closeEventTest1() {
        medEventsEJB.closeEvent(2, MedEventStatus.DONE)
        assertEquals(4, medEventsEJB.medEvents.size)
        assertEquals(1, medEventsEJB.closedEvents.size)
    }

    @Test(expected = NoEventFoundException::class)
    fun closeEventTest2() {
        medEventsEJB.closeEvent(-1, MedEventStatus.DONE)
    }

    @Test(expected = IllegalArgumentException::class)
    fun closeEventTest3() {
        medEventsEJB.closeEvent(1, null)
    }

    @Test(expected = NoEventFoundException::class)
    fun closeEventTest4() {
        medEventsEJB.closeEvent(Int.MAX_VALUE, MedEventStatus.DONE)
    }

    @Test(expected = NoEventFoundException::class)
    fun closeEventTest5() {
        medEventsEJB.closeEvent(Int.MIN_VALUE, MedEventStatus.DONE)
    }

    @Test
    fun newEventTest1() {
        val size1 = medEventsEJB.medEvents.size
        val dto = MedEventViewDto(6, "Ivan", "Ivanov",
                "Elena", "Lenina",
                "SCHEDULED", LocalDate.now(), LocalTime.of(0, 0).plusHours(6))
        medEventsEJB.newEvent(dto)
        assert(size1 + 1 == medEventsEJB.medEvents.size)
    }

    @Test(expected = NullPointerException::class)
    fun newEventTest2() {
        medEventsEJB.newEvent(null)
    }

    @Test
    fun newEventTest3() {
        val dto = MedEventViewDto(Int.MAX_VALUE, "Ivan", "Ivanov",
                "Elena", "Lenina",
                "SCHEDULED", LocalDate.now(), LocalTime.of(0, 0).plusHours(6))
        medEventsEJB.newEvent(dto)
        val list = medEventsEJB.medEvents
        for (dto1 in list) {
            if (dto1 == dto) {
                assert(true)
            }
        }
    }
}