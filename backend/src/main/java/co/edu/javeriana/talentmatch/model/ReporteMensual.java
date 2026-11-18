package co.edu.javeriana.talentmatch.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Constancia de un reporte mensual de rechazos generado automaticamente (HU-002). */
@Entity
@Table(name = "reporte_mensual")
public class ReporteMensual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /** AAAA-MM */
    private String periodo;
    private LocalDateTime fechaGeneracion;
    private int managers;
    private int anomalias;

    protected ReporteMensual() {
    }

    public ReporteMensual(String periodo, LocalDateTime fechaGeneracion, int managers, int anomalias) {
        this.periodo = periodo;
        this.fechaGeneracion = fechaGeneracion;
        this.managers = managers;
        this.anomalias = anomalias;
    }

    public Long getId() {
        return id;
    }

    public String getPeriodo() {
        return periodo;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public int getManagers() {
        return managers;
    }

    public int getAnomalias() {
        return anomalias;
    }
}
