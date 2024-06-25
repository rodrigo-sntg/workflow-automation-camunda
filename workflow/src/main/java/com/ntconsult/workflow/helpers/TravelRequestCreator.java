package com.ntconsult.workflow.helpers;

import com.ntconsult.workflow.entities.TravelRequest;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import java.util.Date;

public class TravelRequestCreator {
    public static TravelRequest fromExecution(DelegateExecution execution) {
        return new TravelRequest(
            (String) execution.getVariable("departamento"),
            (String) execution.getVariable("origem"),
            (String) execution.getVariable("destino"),
            (Date) execution.getVariable("dataInicio"),
            (Date) execution.getVariable("dataFim"),
            (Double) execution.getVariable("valorAdiantamento"),
            (Double) execution.getVariable("valorTotal"),
            (String) execution.getVariable("emailSolicitante")
        );
    }
}