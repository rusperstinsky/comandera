package mx.wen.pos.model;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

@Entity
@Table( name = "nota_venta", schema = "public" )
public class NotaVenta implements Serializable {

    private static final long serialVersionUID = -2336697153151190921L;

    @Id
    @Column( name = "id_factura" )
    private String id;

    @Column( name = "id_empleado" )
    private Integer idEmpleado;

    @Column( name = "id_cliente" )
    private Integer idCliente;

    @Type( type = "mx.wen.pos.model.MoneyAdapter" )
    @Column( name = "venta_total" )
    private BigDecimal ventaTotal;

    @Type( type = "mx.wen.pos.model.MoneyAdapter" )
    @Column( name = "venta_neta" )
    private BigDecimal ventaNeta;

    @Type( type = "mx.wen.pos.model.MoneyAdapter" )
    @Column( name = "suma_pagos" )
    private BigDecimal sumaPagos;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "fecha_hora_factura", nullable = false )
    private Date fechaHoraFactura;

    @Column( name = "por100_descuento" )
    private Integer por100Descuento = 0;

    @Type( type = "mx.wen.pos.model.MoneyAdapter" )
    @Column( name = "monto_descuento" )
    private BigDecimal montoDescuento;

    @Column( name = "tipo_descuento", length = 1 )
    private String tipoDescuento;

    @Column( name = "s_factura", length = 1, nullable = false )
    private String sFactura = "N";

    @Column( name = "observaciones_nv" )
    private String observacionesNv;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "fecha_mod", nullable = false )
    private Date fechaMod;

    @Column( name = "id_sucursal", nullable = false )
    private Integer idSucursal;

    @Column( name = "factura" )
    private String factura;

    @Column( name = "udf2" )
    private String udf2;

    @Column( name = "udf3" )
    private String udf3;

    @Column( name = "udf4" )
    private String udf4;

    @Column( name = "udf5" )
    private String udf5;

    @OneToMany( fetch = FetchType.EAGER )
    @NotFound( action = NotFoundAction.IGNORE )
    @JoinColumn( name = "id_factura", referencedColumnName = "id_factura", insertable = false, updatable = false )
    private Set<DetalleNotaVenta> detalles = new HashSet<DetalleNotaVenta>();

    @OneToMany( fetch = FetchType.EAGER )
    @NotFound( action = NotFoundAction.IGNORE )
    @JoinColumn( name = "id_factura", referencedColumnName = "id_factura", insertable = false, updatable = false )
    private Set<Pago> pagos = new HashSet<Pago>();

    @ManyToOne
    @NotFound( action = NotFoundAction.IGNORE )
    @JoinColumn( name = "id_cliente", insertable = false, updatable = false )
    private Cliente cliente;

    @ManyToOne
    @NotFound( action = NotFoundAction.IGNORE )
    @JoinColumn( name = "id_empleado", insertable = false, updatable = false )
    private Empleado empleado;

    @ManyToOne
    @NotFound( action = NotFoundAction.IGNORE )
    @JoinColumn( name = "id_sucursal", insertable = false, updatable = false )
    private Sucursal sucursal;

    @PrePersist
    protected void onPrePersist() {
        fechaHoraFactura = new Date();
        fechaMod = new Date();
        detalles = null;
        pagos = null;
        cliente = null;
        empleado = null;
        sucursal = null;
    }

    @PreUpdate
    protected void onPreUpdate() {
        fechaMod = new Date();
    }

    @PostLoad
    void onPostLoad() {
        tipoDescuento = StringUtils.trimToEmpty( tipoDescuento );
        sFactura = StringUtils.trimToEmpty( sFactura );
        observacionesNv = StringUtils.trimToEmpty( observacionesNv );
        factura = StringUtils.trimToEmpty( factura );
        udf2 = StringUtils.trimToEmpty( udf2 );
        udf3 = StringUtils.trimToEmpty( udf3 );
        udf4 = StringUtils.trimToEmpty( udf4 );
        udf5 = StringUtils.trimToEmpty( udf5 );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Integer idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public BigDecimal getVentaTotal() {
        return ventaTotal;
    }

    public void setVentaTotal(BigDecimal ventaTotal) {
        this.ventaTotal = ventaTotal;
    }

    public BigDecimal getVentaNeta() {
        return ventaNeta;
    }

    public void setVentaNeta(BigDecimal ventaNeta) {
        this.ventaNeta = ventaNeta;
    }

    public BigDecimal getSumaPagos() {
        return sumaPagos;
    }

    public void setSumaPagos(BigDecimal sumaPagos) {
        this.sumaPagos = sumaPagos;
    }

    public Date getFechaHoraFactura() {
        return fechaHoraFactura;
    }

    public void setFechaHoraFactura(Date fechaHoraFactura) {
        this.fechaHoraFactura = fechaHoraFactura;
    }

    public Integer getPor100Descuento() {
        return por100Descuento;
    }

    public void setPor100Descuento(Integer por100Descuento) {
        this.por100Descuento = por100Descuento;
    }

    public BigDecimal getMontoDescuento() {
        return montoDescuento;
    }

    public void setMontoDescuento(BigDecimal montoDescuento) {
        this.montoDescuento = montoDescuento;
    }

    public String getTipoDescuento() {
        return tipoDescuento;
    }

    public void setTipoDescuento(String tipoDescuento) {
        this.tipoDescuento = tipoDescuento;
    }

    public String getsFactura() {
        return sFactura;
    }

    public void setsFactura(String sFactura) {
        this.sFactura = sFactura;
    }

    public String getObservacionesNv() {
        return observacionesNv;
    }

    public void setObservacionesNv(String observacionesNv) {
        this.observacionesNv = observacionesNv;
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

    public String getFactura() {
        return factura;
    }

    public void setFactura(String factura) {
        this.factura = factura;
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

    public String getUdf4() {
        return udf4;
    }

    public void setUdf4(String udf4) {
        this.udf4 = udf4;
    }

    public String getUdf5() {
        return udf5;
    }

    public void setUdf5(String udf5) {
        this.udf5 = udf5;
    }

    public Set<DetalleNotaVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(Set<DetalleNotaVenta> detalles) {
        this.detalles = detalles;
    }

    public Set<Pago> getPagos() {
        return pagos;
    }

    public void setPagos(Set<Pago> pagos) {
        this.pagos = pagos;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }
}
