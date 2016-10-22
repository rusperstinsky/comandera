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
@Table( name = "pagos", schema = "public" )
public class Pago implements Serializable {

    private static final long serialVersionUID = 3627038677660169174L;

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO, generator = "pagos_id_pago_seq" )
    @SequenceGenerator( name = "pagos_id_pago_seq", sequenceName = "pagos_id_pago_seq" )
    @Column( name = "id_pago" )
    private Integer id;

    @Column( name = "id_factura" )
    private String idFactura;

    @Column( name = "referencia_pago" )
    private String referenciaPago;

    @Type( type = "mx.wen.pos.model.MoneyAdapter" )
    @Column( name = "monto_pago" )
    private BigDecimal monto;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "fecha_pago", nullable = false )
    private Date fecha;

    @Column( name = "id_empleado" )
    private String idEmpleado;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "fecha_mod", nullable = false )
    private Date fechaModificacion;

    @Column( name = "id_sucursal", nullable = false )
    private Integer idSucursal;

    @Column( name = "id_f_pago" )
    private String idFPago;

    @Column( name = "id_banco", length = 20 )
    private String idPlan;

    @Column( name = "id_plan" )
    private String idBanco;

    @ManyToOne
    @NotFound( action = NotFoundAction.IGNORE )
    @JoinColumn( name = "id_factura", referencedColumnName = "id_factura", insertable = false, updatable = false )
    private NotaVenta notaVenta;

    @ManyToOne
    @NotFound( action = NotFoundAction.IGNORE )
    @JoinColumn( name = "id_empleado", insertable = false, updatable = false )
    private Empleado empleado;

    @ManyToOne
    @NotFound( action = NotFoundAction.IGNORE )
    @JoinColumn( name = "id_sucursal", insertable = false, updatable = false )
    private Sucursal sucursal;

    @ManyToOne
    @NotFound( action = NotFoundAction.IGNORE )
    @JoinColumn( name = "id_f_pago", insertable = false, updatable = false )
    private TipoPago eTipoPago;

    @ManyToOne
    @NotFound( action = NotFoundAction.IGNORE )
    @JoinColumn( name = "id_plan", insertable = false, updatable = false )
    private Plan plan;

    @PostLoad
    private void onPostLoad() {
        idFactura = StringUtils.trimToEmpty( idFactura );
        referenciaPago = StringUtils.trimToEmpty( referenciaPago );
        idFPago = StringUtils.trimToEmpty( idFPago );
        idPlan = StringUtils.trimToEmpty( idPlan );
    }

    @PrePersist
    private void onPrePersist() {
        //fecha = new Date();
        fechaModificacion = new Date();
    }

    @PreUpdate
    private void onPreUpdate() {
        fechaModificacion = new Date();
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

    public String getReferenciaPago() {
        return referenciaPago;
    }

    public void setReferenciaPago( String referenciaPago ) {
        this.referenciaPago = referenciaPago;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto( BigDecimal monto ) {
        this.monto = monto;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha( Date fecha ) {
        this.fecha = fecha;
    }

    public String getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado( String idEmpleado ) {
        this.idEmpleado = idEmpleado;
    }

    public Date getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion( Date fechaModificacion ) {
        this.fechaModificacion = fechaModificacion;
    }

    public Integer getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal( Integer idSucursal ) {
        this.idSucursal = idSucursal;
    }

    public String getIdFPago() {
        return idFPago;
    }

    public void setIdFPago( String idFPago ) {
        this.idFPago = idFPago;
    }

    public String getIdPlan() {
        return idPlan;
    }

    public void setIdPlan( String idPlan ) {
        this.idPlan = idPlan;
    }

    public NotaVenta getNotaVenta() {
        return notaVenta;
    }

    public void setNotaVenta( NotaVenta notaVenta ) {
        this.notaVenta = notaVenta;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado( Empleado empleado ) {
        this.empleado = empleado;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal( Sucursal sucursal ) {
        this.sucursal = sucursal;
    }

    public TipoPago geteTipoPago() {
        return eTipoPago;
    }

    public void seteTipoPago( TipoPago eTipoPago ) {
        this.eTipoPago = eTipoPago;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan( Plan plan ) {
        this.plan = plan;
    }

    public String getIdBanco() {
        return idBanco;
    }

    public void setIdBanco(String idBanco) {
        this.idBanco = idBanco;
    }
}
