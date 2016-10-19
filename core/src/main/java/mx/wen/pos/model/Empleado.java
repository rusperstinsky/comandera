package mx.wen.pos.model;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Cacheable
@Table( name = "empleado", schema = "public" )
public class Empleado implements Serializable {



    @Id
    @Column( name = "id_empleado" )
    private Integer id;

    @Column( name = "nombre_empleado" )
    private String nombre;

    @Column( name = "ap_paterno" )
    private String apellidoPaterno;

    @Column( name = "ap_materno" )
    private String apellidoMaterno;

    @Column( name = "id_puesto" )
    private Integer idPuesto;

    @Column( name = "passwd", length = 10 )
    private String passwd;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "fecha_alta" )
    private Date fechaAlta;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "fecha_mod" )
    private Date fechaModificado = new Date();

    @Column( name = "id_sucursal", nullable = false )
    private Integer idSucursal;

    @ManyToOne
    @NotFound( action = NotFoundAction.IGNORE )
    @JoinColumn( name = "id_sucursal", insertable = false, updatable = false )
    private Sucursal sucursal;

    @PostLoad
    private void trim() {
        nombre = StringUtils.trimToEmpty( nombre );
        apellidoPaterno = StringUtils.trimToEmpty( apellidoPaterno );
        apellidoMaterno = StringUtils.trimToEmpty( apellidoMaterno );
        passwd = StringUtils.trimToEmpty( passwd );
    }

    @PrePersist
    protected void prePersist() {
        this.preUpdate();
    }

    @PreUpdate
    protected void preUpdate() {
      this.setFechaModificado( new Date() );
    }


    public String getNombreCompleto() {
        StringBuilder sb = new StringBuilder();
        sb.append( StringUtils.trimToEmpty( nombre ) );
        sb.append( StringUtils.isNotBlank( nombre ) ? " " : "" );
        sb.append( StringUtils.trimToEmpty( apellidoPaterno ) );
        sb.append( StringUtils.isNotBlank( apellidoPaterno ) ? " " : "" );
        sb.append( StringUtils.trimToEmpty( apellidoMaterno ) );
        return sb.toString().trim();
    }

    public String nombreCompleto() {
        StringBuilder sb = new StringBuilder();
        sb.append( StringUtils.trimToEmpty( nombre ) );
        sb.append( StringUtils.isNotBlank( nombre ) ? " " : "" );
        sb.append( StringUtils.trimToEmpty( apellidoPaterno ) );
        sb.append( StringUtils.isNotBlank( apellidoPaterno ) ? " " : "" );
        sb.append( StringUtils.trimToEmpty( apellidoMaterno ) );
        return sb.toString().trim();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public Integer getIdPuesto() {
        return idPuesto;
    }

    public void setIdPuesto(Integer idPuesto) {
        this.idPuesto = idPuesto;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public Date getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(Date fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public Date getFechaModificado() {
        return fechaModificado;
    }

    public void setFechaModificado(Date fechaModificado) {
        this.fechaModificado = fechaModificado;
    }

    public Integer getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(Integer idSucursal) {
        this.idSucursal = idSucursal;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }
}
