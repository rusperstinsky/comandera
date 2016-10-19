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
@Table( name = "detalle_nota_ven", schema = "public" )
public class DetalleNotaVenta implements Serializable {

    private static final long serialVersionUID = -748894414171291739L;

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO, generator = "detalle_nota_ven_id_seq" )
    @SequenceGenerator( name = "detalle_nota_ven_id_seq", sequenceName = "detalle_nota_ven_id_seq" )
    @Column( name = "id" )
    private Integer id;

    @Column( name = "id_factura", nullable = false )
    private String idFactura;

    @Column( name = "id_articulo", nullable = false )
    private Integer idArticulo;

    @Column( name = "cantidad_fac" )
    private Double cantidadFac;

    @Type( type = "mx.wen.pos.model.MoneyAdapter" )
    @Column( name = "precio_unit_lista" )
    private BigDecimal precioUnitLista;

    @Type( type = "mx.wen.pos.model.MoneyAdapter" )
    @Column( name = "precio_unit_final" )
    private BigDecimal precioUnitFinal;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "fecha_mod", nullable = false )
    private Date fechaMod;

    @Column( name = "id_sucursal", nullable = false )
    private Integer idSucursal;

    @ManyToOne
    @NotFound( action = NotFoundAction.IGNORE )
    @JoinColumn( name = "id_articulo", insertable = false, updatable = false )
    private Articulo articulo;
    
    @ManyToOne
    @NotFound( action = NotFoundAction.IGNORE )
    @JoinColumn( name = "id_factura", insertable = false, updatable = false )
    private NotaVenta notaVenta;

    @ManyToOne
    @NotFound( action = NotFoundAction.IGNORE )
    @JoinColumn( name = "id_sucursal", insertable = false, updatable = false )
    private Sucursal sucursal;

    @PrePersist
    protected void onPrePersist() {
        fechaMod = new Date();
        articulo = null;
        sucursal = null;
    }

    @PreUpdate
    protected void onPreUpdate() {
        fechaMod = new Date();
    }

    @PostLoad
    protected void onPostLoad() {
        idFactura = StringUtils.trimToNull( idFactura );
    }

    public Integer getId() {
        return id;
    }

    public void setId( Integer id ) {
        this.id = id;
    }

    public String getIdFactura() {
        return idFactura;
    }

    public void setIdFactura( String idFactura ) {
        this.idFactura = idFactura;
    }

    public Integer getIdArticulo() {
        return idArticulo;
    }

    public void setIdArticulo( Integer idArticulo ) {
        this.idArticulo = idArticulo;
    }

    public Double getCantidadFac() {
        return cantidadFac;
    }

    public void setCantidadFac( Double cantidadFac ) {
        this.cantidadFac = cantidadFac;
    }

    public BigDecimal getPrecioUnitLista() {
        return precioUnitLista;
    }

    public void setPrecioUnitLista( BigDecimal precioUnitLista ) {
        this.precioUnitLista = precioUnitLista;
    }

    public BigDecimal getPrecioUnitFinal() {
        return precioUnitFinal;
    }

    public void setPrecioUnitFinal( BigDecimal precioUnitFinal ) {
        this.precioUnitFinal = precioUnitFinal;
    }

    public Date getFechaMod() {
        return fechaMod;
    }

    public void setFechaMod( Date fechaMod ) {
        this.fechaMod = fechaMod;
    }

    public Integer getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal( Integer idSucursal ) {
        this.idSucursal = idSucursal;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo( Articulo articulo ) {
        this.articulo = articulo;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal( Sucursal sucursal ) {
        this.sucursal = sucursal;
    }

	public NotaVenta getNotaVenta() {
		return notaVenta;
	}

	public void setNotaVenta(NotaVenta notaVenta) {
		this.notaVenta = notaVenta;
	}
    
}
