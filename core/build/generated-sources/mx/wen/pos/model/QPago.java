package mx.wen.pos.model;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.*;
import com.mysema.query.types.path.*;

import javax.annotation.Generated;


/**
 * QPago is a Querydsl query type for Pago
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QPago extends EntityPathBase<Pago> {

    private static final long serialVersionUID = 385665961;

    private static final PathInits INITS = PathInits.DIRECT;

    public static final QPago pago = new QPago("pago");

    public final QEmpleado empleado;

    public final QTipoPago eTipoPago;

    public final DateTimePath<java.util.Date> fecha = createDateTime("fecha", java.util.Date.class);

    public final DateTimePath<java.util.Date> fechaModificacion = createDateTime("fechaModificacion", java.util.Date.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath idBanco = createString("idBanco");

    public final NumberPath<Integer> idEmpleado = createNumber("idEmpleado", Integer.class);

    public final StringPath idFactura = createString("idFactura");

    public final StringPath idFPago = createString("idFPago");

    public final StringPath idPlan = createString("idPlan");

    public final NumberPath<Integer> idSucursal = createNumber("idSucursal", Integer.class);

    public final NumberPath<java.math.BigDecimal> monto = createNumber("monto", java.math.BigDecimal.class);

    public final QNotaVenta notaVenta;

    public final QPlan plan;

    public final StringPath referenciaPago = createString("referenciaPago");

    public final QSucursal sucursal;

    public QPago(String variable) {
        this(Pago.class, forVariable(variable), INITS);
    }

    public QPago(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QPago(PathMetadata<?> metadata, PathInits inits) {
        this(Pago.class, metadata, inits);
    }

    public QPago(Class<? extends Pago> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this.empleado = inits.isInitialized("empleado") ? new QEmpleado(forProperty("empleado"), inits.get("empleado")) : null;
        this.eTipoPago = inits.isInitialized("eTipoPago") ? new QTipoPago(forProperty("eTipoPago")) : null;
        this.notaVenta = inits.isInitialized("notaVenta") ? new QNotaVenta(forProperty("notaVenta"), inits.get("notaVenta")) : null;
        this.plan = inits.isInitialized("plan") ? new QPlan(forProperty("plan")) : null;
        this.sucursal = inits.isInitialized("sucursal") ? new QSucursal(forProperty("sucursal"), inits.get("sucursal")) : null;
    }

}

