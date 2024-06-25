package com.ntconsult.quotation.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;


import java.util.Date;

@Data
@Entity
@ToString
public class TravelRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String departamento;
    private String origem;
    private String destino;
    private Date dataInicio;
    private Date dataFim;
    private Double valorAdiantamento;
    private Double valorTotal;
    private String emailSolicitante;
    private String status;
}