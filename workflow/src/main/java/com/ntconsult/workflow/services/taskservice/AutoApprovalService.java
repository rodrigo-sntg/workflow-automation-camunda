package com.ntconsult.workflow.services.taskservice;

import com.ntconsult.workflow.entities.TravelRequest;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;

@Service
public class AutoApprovalService implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {

        String departamento = (String) execution.getVariable(TravelRequest.Fields.departamento);
        Double valorAdiantamento = (Double) execution.getVariable(TravelRequest.Fields.valorAdiantamento);

        if ((departamento.equals("Vendas") && valorAdiantamento <= 1000) ||
                (departamento.equals("Marketing") && valorAdiantamento <= 500)) {
            execution.setVariable("approvalStatus", "approved");
        } else {
            execution.setVariable("approvalStatus", "manual");
        }
    }
}