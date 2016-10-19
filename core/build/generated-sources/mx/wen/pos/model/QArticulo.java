package mx.wen.pos.model;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.*;
import com.mysema.query.types.path.*;

import javax.annotation.Generated;


/**
 * QArticulo is a Querydsl query type for Articulo
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QArticulo extends EntityPathBase<Articulo> {

    private static final long serialVersionUID = -233941525;

    public static final QArticulo articulo1 = new QArticulo("articulo1");

    public final StringPath articulo = createString("articulo");

    public final NumberPath<Integer> cantExistencia = createNumber("cantExistencia", Integer.class);

    public final StringPath descripcion = createString("descripcion");

    public final DateTimePath<java.util.Date> fechaMod = createDateTime("fechaMod", java.util.Date.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> idSucursal = createNumber("idSucursal", Integer.class);

    public final StringPath marca = createString("marca");

    public final NumberPath<java.math.BigDecimal> precio = createNumber("precio", java.math.BigDecimal.class);

    public final StringPath proveedor = createString("proveedor");

    public final StringPath subtipo = createString("subtipo");

    public final StringPath tipo = createString("tipo");

    public QArticulo(String variable) {
        super(Articulo.class, forVariable(variable));
    }

    public QArticulo(Path<? extends Articulo> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QArticulo(PathMetadata<?> metadata) {
        super(Articulo.class, metadata);
    }

}

