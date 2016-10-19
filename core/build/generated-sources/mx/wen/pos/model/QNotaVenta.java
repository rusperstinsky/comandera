package mx.wen.pos.model;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.*;
import com.mysema.query.types.path.*;

import javax.annotation.Generated;


/**
 * QNotaVenta is a Querydsl query type for NotaVenta
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QNotaVenta extends EntityPathBase<NotaVenta> {

    private static final long serialVersionUID = -1859301394;

    private static final PathInits INITS = PathInits.DIRECT;

    public static final QNotaVenta notaVenta = new QNotaVenta("notaVenta");

    public final QCliente cliente;

    public final SetPath<DetalleNotaVenta, QDetalleNotaVenta> detalles = this.<DetalleNotaVenta, QDetalleNotaVenta>createSet("detalles", DetalleNotaVenta.class, QDetalleNotaVenta.class);

    public final QEmpleado empleado;

    public final StringPath factura = createString("factura");

    public final DateTimePath<java.util.Date> fechaHoraFactura = createDateTime("fechaHoraFactura", java.util.Date.class);

    public final DateTimePath<java.util.Date> fechaMod = createDateTime("fechaMod", java.util.Date.class);

    public final StringPath id = createString("id");

    public final NumberPath<Integer> idCliente = createNumber("idCliente", Integer.class);

    public final NumberPath<Integer> idEmpleado = createNumber("idEmpleado", Integer.class);

    public final NumberPath<Integer> idSucursal = createNumber("idSucursal", Integer.class);

    public final NumberPath<java.math.BigDecimal> montoDescuento = createNumber("montoDescuento", java.math.BigDecimal.class);

    public final StringPath observacionesNv = createString("observacionesNv");

    public final SetPath<Pago, QPago> pagos = this.<Pago, QPago>createSet("pagos", Pago.class, QPago.class);

    public final NumberPath<Integer> por100Descuento = createNumber("por100Descuento", Integer.class);

    public final StringPath sFactura = createString("sFactura");

    public final QSucursal sucursal;

    public final NumberPath<java.math.BigDecimal> sumaPagos = createNumber("sumaPagos", java.math.BigDecimal.class);

    public final StringPath tipoDescuento = createString("tipoDescuento");

    public final StringPath udf2 = createString("udf2");

    public final StringPath udf3 = createString("udf3");

    public final StringPath udf4 = createString("udf4");

    public final StringPath udf5 = createString("udf5");

    public final NumberPath<java.math.BigDecimal> ventaNeta = createNumber("ventaNeta", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> ventaTotal = createNumber("ventaTotal", java.math.BigDecimal.class);

    public QNotaVenta(String variable) {
        this(NotaVenta.class, forVariable(variable), INITS);
    }

    public QNotaVenta(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QNotaVenta(PathMetadata<?> metadata, PathInits inits) {
        this(NotaVenta.class, metadata, inits);
    }

    public QNotaVenta(Class<? extends NotaVenta> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this.cliente = inits.isInitialized("cliente") ? new QCliente(forProperty("cliente")) : null;
        this.empleado = inits.isInitialized("empleado") ? new QEmpleado(forProperty("empleado"), inits.get("empleado")) : null;
        this.sucursal = inits.isInitialized("sucursal") ? new QSucursal(forProperty("sucursal"), inits.get("sucursal")) : null;
    }

}

