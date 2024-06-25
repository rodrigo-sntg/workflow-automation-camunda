package com.ntconsult.workflow.dto;

import lombok.Data;

@Data
public class QuotationResponseDTO {
        private Long id;
        private String origem;
        private String destino;
        private String dataIda;
        private String dataVolta;
        private Double valor;
        private Double valorAdiantamento;

}