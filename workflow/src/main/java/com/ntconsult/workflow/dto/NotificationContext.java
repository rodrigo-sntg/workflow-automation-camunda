package com.ntconsult.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationContext {
        private String recipient;
        private String subject;
        private String body;
}