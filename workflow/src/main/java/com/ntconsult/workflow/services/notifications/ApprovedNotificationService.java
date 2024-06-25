package com.ntconsult.workflow.services.notifications;

import com.amazonaws.services.sns.AmazonSNS;
import com.ntconsult.workflow.dto.NotificationContext;
import com.ntconsult.workflow.entities.TravelRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApprovedNotificationService extends NotificationService {

    @Autowired
    protected ApprovedNotificationService(AmazonSNS snsClient) {
        super(snsClient);
    }

    @Override
    protected NotificationContext createNotificationContext(TravelRequest travelRequest) {
        return new NotificationContext(
                travelRequest.getEmailSolicitante(),
                "Solicitação aprovada!",
                "Sua solicitação de viagem foi aprovada."
        );
    }
}