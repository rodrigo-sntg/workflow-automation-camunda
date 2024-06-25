package com.ntconsult.workflow.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@FieldNameConstants
public class TravelRequest {

    public static final String NAME = "travelRequest";

    private Long id;
    private String departamento;
    private String origem;
    private String destino;
    private Date dataInicio;
    private Date dataFim;
    private Double valorAdiantamento;
    private Double valorTotal;
    private String emailSolicitante;


    public TravelRequest(String departamento, String origem, String destino, Date dataInicio, Date dataFim, Double valorAdiantamento, Double valorTotal, String emailSolicitante) {
        this.departamento = departamento;
        this.origem = origem;
        this.destino = destino;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.valorAdiantamento = valorAdiantamento;
        this.valorTotal = valorTotal;
        this.emailSolicitante = emailSolicitante;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(Fields.departamento, departamento);
        map.put(Fields.valorAdiantamento, valorAdiantamento);
        map.put(Fields.valorTotal, valorTotal);
        map.put(Fields.dataInicio, dataInicio);
        map.put(Fields.dataFim, dataFim);
        map.put(Fields.destino, destino);
        map.put(Fields.origem, origem);
        map.put(Fields.emailSolicitante, emailSolicitante);
        return map;
    }
}