package co.edu.javeriana.talentmatch.model;

/**
 * Cuenta de acceso al sistema (DC-01). Es abstracta: todo usuario es un
 * Empleado, Manager, AnalistaRRHH, LiderEjecutivo o Administrador.
 *
 * El inicio y cierre de sesion del diagrama ({@code iniciarSesion}, {@code cerrarSesion})
 * se implementan en el paquete security, porque dependen del hash y del token.
 */
public abstract class Usuario {

    private Long id;
    private String nombre;
    private String correo;
    private String contrasenaHash;
    private boolean activo = true;

    protected Usuario() {
    }

    protected Usuario(String nombre, String correo, String contrasenaHash) {
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenaHash = contrasenaHash;
    }

    /** Rol de la cuenta; lo define cada subclase. */
    public abstract Rol getRol();

    public void desactivar() {
        this.activo = false;
    }

    public void activar() {
        this.activo = true;
    }

    /** Compara por id: dos objetos con el mismo id son el mismo usuario. */
    public boolean mismoUsuarioQue(Usuario otro) {
        return otro != null && id != null && id.equals(otro.getId());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    public boolean isActivo() {
        return activo;
    }
}
