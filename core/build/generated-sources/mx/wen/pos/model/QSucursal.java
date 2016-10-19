package mx.wen.pos.model;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.*;
import com.mysema.query.types.path.*;

import javax.annotation.Generated;


/**
 * QSucursal is a Querydsl query type for Sucursal
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QSucursal extends EntityPathBase<Sucursal> {

    private static final long serialVersionUID = -1035751536;

    private static final PathInits INITS = PathInits.DIRECT;

    public static final QSucursal sucursal = new QSucursal("sucursal");

    public final StringPath calle = createString("calle");

    public final StringPath centroCostos = createString("centroCostos");

    public final StringPath colonia = createString("colonia");

    public final StringPath cp = createString("cp");

    public final DateTimePath<java.util.Date> fechaModificado = createDateTime("fechaModificado", java.util.Date.class);

    public final QEmpleado gerente;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath idEstado = createString("idEstado");

    public final NumberPath<Integer> idGerente = createNumber("idGerente", Integer.class);

    public final StringPath idLocalidad = createString("idLocalidad");

    public final StringPath nombre = createString("nombre");

    public final StringPath numero = createString("numero");

    public final StringPath telefonos = createString("telefonos");

    public final StringPath udf1 = createString("udf1");

    public final StringPath udf2 = createString("udf2");

    public final StringPath udf3 = createString("udf3");

    public QSucursal(String variable) {
        this(Sucursal.class, forVariable(variable), INITS);
    }

    public QSucursal(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QSucursal(PathMetadata<?> metadata, PathInits inits) {
        this(Sucursal.class, metadata, inits);
    }

    public QSucursal(Class<? extends Sucursal> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this.gerente = inits.isInitialized("gerente") ? new QEmpleado(forProperty("gerente"), inits.get("gerente")) : null;
    }

}

