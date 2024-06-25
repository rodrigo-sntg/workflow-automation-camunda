package com.ntconsult.workflow.services.notifications;

import com.amazonaws.services.sns.AmazonSNS;
import com.ntconsult.workflow.dto.NotificationContext;
import com.ntconsult.workflow.entities.TravelRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RejectedNotificationService extends NotificationService {

    @Autowired
    protected RejectedNotificationService(AmazonSNS snsClient) {
        super(snsClient);
    }

    @Override
    protected NotificationContext createNotificationContext(TravelRequest travelRequest) {
        return new NotificationContext(
                travelRequest.getEmailSolicitante(),
                "Solicitação de viagem rejeitada",
                "Sua solicitação de viagem foi rejeitada."
        );
    }
}