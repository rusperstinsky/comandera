package mx.wen.pos.model;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.*;
import com.mysema.query.types.path.*;

import javax.annotation.Generated;


/**
 * QCliente is a Querydsl query type for Cliente
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QCliente extends EntityPathBase<Cliente> {

    private static final long serialVersionUID = 2001060586;

    public static final QCliente cliente = new QCliente("cliente");

    public final StringPath apellidoMaterno = createString("apellidoMaterno");

    public final StringPath apellidoPaterno = createString("apellidoPaterno");

    public final StringPath codigo = createString("codigo");

    public final StringPath colonia = createString("colonia");

    public final StringPath direccion = createString("direccion");

    public final StringPath email = createString("email");

    public final StringPath extAdicional = createString("extAdicional");

    public final StringPath extTrabajo = createString("extTrabajo");

    public final DateTimePath<java.util.Date> fechaAlta = createDateTime("fechaAlta", java.util.Date.class);

    public final DateTimePath<java.util.Date> fechaModificado = createDateTime("fechaModificado", java.util.Date.class);

    public final DateTimePath<java.util.Date> fechaNacimiento = createDateTime("fechaNacimiento", java.util.Date.class);

    public final DateTimePath<java.util.Date> horaAlta = createDateTime("horaAlta", java.util.Date.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath idEstado = createString("idEstado");

    public final StringPath idLocalidad = createString("idLocalidad");

    public final NumberPath<Integer> idSucursal = createNumber("idSucursal", Integer.class);

    public final StringPath nombre = createString("nombre");

    public final StringPath obs = createString("obs");

    public final StringPath rfc = createString("rfc");

    public final StringPath telefonoAdicional = createString("telefonoAdicional");

    public final StringPath telefonoCasa = createString("telefonoCasa");

    public final StringPath telefonoTrabajo = createString("telefonoTrabajo");

    public final StringPath udf1 = createString("udf1");

    public final StringPath udf2 = createString("udf2");

    public final StringPath udf3 = createString("udf3");

    public final StringPath udf4 = createString("udf4");

    public final StringPath udf5 = createString("udf5");

    public final StringPath udf6 = createString("udf6");

    public QCliente(String variable) {
        super(Cliente.class, forVariable(variable));
    }

    public QCliente(Path<? extends Cliente> entity) {
        super(entity.getType(), entity.getMetadata());
    }

    public QCliente(PathMetadata<?> metadata) {
        super(Cliente.class, metadata);
    }

}

