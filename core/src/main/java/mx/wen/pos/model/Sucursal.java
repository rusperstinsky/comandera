package mx.wen.pos.model;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Cacheable
@Table( name = "sucursales", schema = "public" )
public class Sucursal implements Serializable {

    private static final long serialVersionUID = 4025356042284041635L;

    @Id
    @Column( name = "id_sucursal" )
    private Integer id;

    @Column( name = "nombre" )
    private String nombre;

    @Column( name = "calle" )
    private String calle;

    @Column( name = "colonia" )
    private String colonia;

    @Column( name = "numero" )
    private String numero;

    @Column( name = "localidad" )
    private String idLocalidad;

    @Column( name = "id_estado" )
    private String idEstado;

    @Column( name = "cp", length = 5 )
    private String cp;

    @Column( name = "telefonos" )
    private String telefonos;

    @Column( name = "id_gerente", length = 13 )
    private String idGerente;

    @Column( name = "udf1", length = 13 )
    private String udf1;

    @Column( name = "udf2", length = 13 )
    private String udf2;

    @Column( name = "udf3", length = 13 )
    private String udf3;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "fecha_mod" )
    private Date fechaModificado = new Date();

    @Column( name = "centro_costos", length = 6 )
    private String centroCostos;

    @ManyToOne
    @NotFound( action = NotFoundAction.IGNORE )
    @JoinColumn( name = "id_gerente", insertable = false, updatable = false )
    private Empleado gerente;

    @PostLoad
    private void trim() {
        nombre = StringUtils.trimToEmpty( nombre );
        calle = StringUtils.trimToEmpty( calle );
        colonia = StringUtils.trimToEmpty( colonia );
        idLocalidad = StringUtils.trimToEmpty( idLocalidad );
        idEstado = StringUtils.trimToEmpty( idEstado );
        cp = StringUtils.trimToEmpty( cp );
        telefonos = StringUtils.trimToEmpty( telefonos );
        centroCostos = StringUtils.trimToEmpty( centroCostos );
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

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getColonia() {
        return colonia;
    }

    public void setColonia(String colonia) {
        this.colonia = colonia;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getIdLocalidad() {
        return idLocalidad;
    }

    public void setIdLocalidad(String idLocalidad) {
        this.idLocalidad = idLocalidad;
    }

    public String getIdEstado() {
        return idEstado;
    }

    public void setIdEstado(String idEstado) {
        this.idEstado = idEstado;
    }

    public String getCp() {
        return cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

    public String getTelefonos() {
        return telefonos;
    }

    public void setTelefonos(String telefonos) {
        this.telefonos = telefonos;
    }

    public String getIdGerente() {
        return idGerente;
    }

    public void setIdGerente(String idGerente) {
        this.idGerente = idGerente;
    }

    public String getUdf1() {
        return udf1;
    }

    public void setUdf1(String udf1) {
        this.udf1 = udf1;
    }

    public String getUdf2() {
        return udf2;
    }

    public void setUdf2(String udf2) {
        this.udf2 = udf2;
    }

    public String getUdf3() {
        return udf3;
    }

    public void setUdf3(String udf3) {
        this.udf3 = udf3;
    }

    public Date getFechaModificado() {
        return fechaModificado;
    }

    public void setFechaModificado(Date fechaModificado) {
        this.fechaModificado = fechaModificado;
    }

    public String getCentroCostos() {
        return centroCostos;
    }

    public void setCentroCostos(String centroCostos) {
        this.centroCostos = centroCostos;
    }

    public Empleado getGerente() {
        return gerente;
    }

    public void setGerente(Empleado gerente) {
        this.gerente = gerente;
    }
}
