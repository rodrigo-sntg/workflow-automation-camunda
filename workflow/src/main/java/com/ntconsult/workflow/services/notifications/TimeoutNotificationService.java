package com.ntconsult.workflow.services.notifications;

import com.amazonaws.services.sns.AmazonSNS;
import com.ntconsult.workflow.dto.NotificationContext;
import com.ntconsult.workflow.entities.TravelRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TimeoutNotificationService extends NotificationService {



    private static final Map<String, String> departmentEmails = Map.of(
            "Marketing", "marketing@email.com",
            "Vendas", "vendas@email.com"
    );

    @Autowired
    protected TimeoutNotificationService(AmazonSNS snsClient) {
        super(snsClient);
    }

    @Override
    public NotificationContext createNotificationContext(TravelRequest travelRequest) {
        String recipient = departmentEmails.getOrDefault(travelRequest.getDepartamento(), "default@email.com");

        return new NotificationContext(
                recipient,
                "Solicitação de viagem - Solicitação vencida",
                "Uma solicitação de viagem sob sua responsabilidade acabou de vencer."
        );

    }
}