package mx.wen.pos.model;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Cacheable
@Table( name = "articulos", schema = "public" )
public class Articulo implements Serializable {

    private static final long serialVersionUID = -2921740576181746900L;

    @Id
    @Column( name = "id_articulo" )
    private Integer id;

    @Column( name = "articulo", length = 20, nullable = false )
    private String articulo;

    @Column( name = "desc_articulo" )
    private String descripcion;

    @Column( name = "tipo" )
    private String tipo;

    @Column( name = "subtipo" )
    private String subtipo;

    @Column( name = "marca" )
    private String marca;

    @Type( type = "mx.wen.pos.model.MoneyAdapter" )
    @Column( name = "precio" )
    private BigDecimal precio;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "fecha_mod", nullable = false )
    private Date fechaMod;

    @Column( name = "id_sucursal", nullable = false )
    private Integer idSucursal;

    @Column( name = "proveedor", length = 1 )
    private String proveedor;

    @Column( name = "existencia" )
    private Integer cantExistencia;

    @PostLoad
    private void onPostLoad() {
        articulo = StringUtils.trimToEmpty( articulo );
        descripcion = StringUtils.trimToEmpty( descripcion );
        tipo = StringUtils.trimToEmpty( tipo );
        subtipo = StringUtils.trimToEmpty( subtipo );
        marca = StringUtils.trimToEmpty( marca );
        cantExistencia = cantExistencia != null ? cantExistencia : 0;
    }



    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getArticulo() {
        return articulo;
    }

    public void setArticulo(String articulo) {
        this.articulo = articulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getSubtipo() {
        return subtipo;
    }

    public void setSubtipo(String subtipo) {
        this.subtipo = subtipo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Date getFechaMod() {
        return fechaMod;
    }

    public void setFechaMod(Date fechaMod) {
        this.fechaMod = fechaMod;
    }

    public Integer getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(Integer idSucursal) {
        this.idSucursal = idSucursal;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public Integer getCantExistencia() {
        return cantExistencia;
    }

    public void setCantExistencia(Integer cantExistencia) {
        this.cantExistencia = cantExistencia;
    }
}
