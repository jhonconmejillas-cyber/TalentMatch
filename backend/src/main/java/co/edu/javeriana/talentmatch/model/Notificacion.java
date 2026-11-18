package co.edu.javeriana.talentmatch.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Mensaje in-app generado por el sistema, sin intervencion humana (RNF-08). */
@Entity
@Table(name = "notificacion")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id")
    private Usuario destinatario;
    @Enumerated(EnumType.STRING)
    private TipoNotificacion tipo;
    private String titulo;
    private String mensaje;
    private LocalDateTime fecha;
    private boolean leida;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postulacion_id")
    private Postulacion postulacion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacante_id")
    private Vacante vacante;

    protected Notificacion() {
    }

    public Notificacion(Usuario destinatario, TipoNotificacion tipo, String titulo, String mensaje,
                        LocalDateTime fecha) {
        this.destinatario = destinatario;
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.fecha = fecha;
    }

    public void marcarLeida() {
        this.leida = true;
    }

    public Long getId() {
        return id;
    }

    public Usuario getDestinatario() {
        return destinatario;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public boolean isLeida() {
        return leida;
    }

    public Postulacion getPostulacion() {
        return postulacion;
    }

    public void setPostulacion(Postulacion postulacion) {
        this.postulacion = postulacion;
    }

    public Vacante getVacante() {
        return vacante;
    }

    public void setVacante(Vacante vacante) {
        this.vacante = vacante;
    }
}
