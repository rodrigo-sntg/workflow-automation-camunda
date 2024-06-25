package com.ntconsult.workflow.services.notificacoes;

import com.amazonaws.services.sns.AmazonSNS;
import com.ntconsult.workflow.dto.NotificationContext;
import com.ntconsult.workflow.entities.TravelRequest;
import com.ntconsult.workflow.services.notifications.TimeoutNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeoutNotificationServiceTest {

    @Mock
    private AmazonSNS snsClient;

    @InjectMocks
    private TimeoutNotificationService notificationService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void createNotificationContext_ShouldCreateContext() {
        TravelRequest travelRequest = new TravelRequest("Marketing", "São Paulo", "Rio de Janeiro", new Date(), new Date(), 1000.0, 2000.0, "test@example.com");

        NotificationContext context = notificationService.createNotificationContext(travelRequest);

        assertEquals("marketing@email.com", context.getRecipient());
        assertEquals("Solicitação de viagem - Solicitação vencida", context.getSubject());
        assertEquals("Uma solicitação de viagem sob sua responsabilidade acabou de vencer.", context.getBody());
    }
}
