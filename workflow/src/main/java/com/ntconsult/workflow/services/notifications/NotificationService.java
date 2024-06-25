package com.ntconsult.workflow.services.notifications;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntconsult.workflow.dto.NotificationContext;
import com.ntconsult.workflow.entities.TravelRequest;
import com.ntconsult.workflow.helpers.TravelRequestCreator;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public abstract class NotificationService implements JavaDelegate {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    @Autowired
    private AmazonSNS snsClient;

    @Value("${aws.sns.topic-arn}")
    private String topicArn;

    protected NotificationService(AmazonSNS snsClient) {
        this.snsClient = snsClient;
    }

    public void execute(DelegateExecution execution) {
        TravelRequest travelRequest = TravelRequestCreator.fromExecution(execution);
        NotificationContext context = createNotificationContext(travelRequest);

        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonMessage = mapper.writeValueAsString(context);
            logger.info("Publicando mensagem no tópico SNS: {}", jsonMessage);
            snsClient.publish(new PublishRequest(topicArn, jsonMessage));
            logger.info("Mensagem publicada com sucesso no tópico: {}", topicArn);
        } catch (JsonProcessingException e) {
            logger.error("Erro ao serializar o contexto de notificação para JSON", e);
            throw new RuntimeException("Erro ao processar JSON", e);
        } catch (Exception e) {
            logger.error("Erro ao publicar mensagem no SNS", e);
            throw new RuntimeException("Erro ao publicar no SNS", e);
        }
    }


    protected abstract NotificationContext createNotificationContext(TravelRequest travelRequest);
}