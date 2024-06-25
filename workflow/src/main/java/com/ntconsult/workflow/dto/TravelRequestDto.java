package com.ntconsult.workflow.dto;

import lombok.Data;

@Data
public class TravelRequestDto {
    private String departamento;
    private String origem;
    private String destino;
    private String dataIda;
    private String dataVolta;
}