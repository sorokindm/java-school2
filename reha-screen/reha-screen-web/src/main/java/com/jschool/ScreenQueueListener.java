package com.jschool;

import com.jschool.dto.MedEventViewDto;
import com.jschool.enums.MedEventStatus;
import com.jschool.enums.MessageType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.time.LocalDateTime;

/**
 * Async message listener of ScreenQueue
 */
@MessageDriven(name="ScreenListener",activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup",propertyValue = "java:jboss/exported/jms/queue/ScreenQueue"),
        @ActivationConfigProperty(propertyName = "destinationType",propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode",propertyValue = "Auto-acknowledge")
})
public class ScreenQueueListener implements MessageListener {

    private final Logger logger = LogManager.getLogger();

    @Inject
    EventData data;

    @Override
    public void onMessage(Message message) {
        int type;
        try {
            type = message.getIntProperty("type");
        } catch (JMSException e) {
            logger.error("Failed to read JMS message 'type' property:",e);
            return;
        }

        if (type == MessageType.CLOSED_MED_EVENT.ordinal()) {
            try {
                int id = message.getIntProperty("medEventId");
                MedEventStatus status=MedEventStatus.valueOf(message.getStringProperty("status"));
                data.closeEvent(id,status);
            } catch (JMSException e) {
                logger.error("Failed to read JMS message 'id' property:",e);
            }
        }
        if (type == MessageType.NEW_MED_EVENT.ordinal()) {
            try {
                ObjectMessage objectMessage=(ObjectMessage) message;
                MedEventViewDto dto=new MedEventViewDto();
                dto.setIdMedEvent(message.getIntProperty("medEventId"));
                dto.setPatientName(message.getStringProperty("patientName"));
                dto.setPatientLastName(message.getStringProperty("patientLastName"));
                dto.setNurseName(message.getStringProperty("nurseName"));
                dto.setNurseLastName(message.getStringProperty("nurseLastName"));
                dto.setStatus(message.getStringProperty("status"));
                LocalDateTime starts=(LocalDateTime) objectMessage.getObject();
                dto.setStartsTime(starts.toLocalTime());
                dto.setStartsDate(starts.toLocalDate());
                data.newEvent(dto);
            } catch (JMSException e) {
                logger.error("Failed to get RestMedEventDto object from message",e);
            }
        }
    }
}
