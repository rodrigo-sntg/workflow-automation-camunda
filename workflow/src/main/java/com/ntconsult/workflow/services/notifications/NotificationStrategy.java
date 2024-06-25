package com.ntconsult.workflow.services.notifications;

import com.ntconsult.workflow.dto.NotificationContext;
import com.ntconsult.workflow.entities.TravelRequest;

public interface NotificationStrategy {
    NotificationContext createNotificationContext(TravelRequest travelRequest);
}