package mx.wen.pos.ui.model

import groovy.beans.Bindable
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import mx.wen.pos.model.Articulo
import mx.wen.pos.model.DetalleNotaVenta


@Slf4j
@Bindable
@ToString
@EqualsAndHashCode
class Item {
  Integer id
  String name
  String desc
  BigDecimal price
  BigDecimal priceDiscount
  Date dateMod
  Integer idStore
  Integer stock
  String type
  String subtype
  String brand
  String provider


  static Item toItem( Articulo articulo ) {
    if ( articulo?.id ) {
      Item item = new Item(
        id: articulo.id,
        name: articulo.articulo,
        desc: articulo.descripcion,
        price: articulo.precio,
        priceDiscount: articulo.precio,
        dateMod: articulo.fechaMod,
        idStore: articulo.idSucursal,
        stock: articulo.cantExistencia,
        type: articulo.tipo,
        subtype: articulo.subtipo,
        brand: articulo.marca,
        provider: articulo.proveedor,
      )
      return item
    }
    return null
  }


  static Item toItem( DetalleNotaVenta detalleNotaVenta ) {
    try {
      if ( detalleNotaVenta?.id && detalleNotaVenta?.articulo ) {
        Articulo articulo = detalleNotaVenta.articulo
        Item item = new Item(
          id: articulo.id,
          name: articulo.articulo,
          desc: articulo.descripcion,
          price: detalleNotaVenta.precioUnitLista,
          priceDiscount: detalleNotaVenta.precioUnitFinal,
          dateMod: articulo.fechaMod,
          idStore: articulo.idSucursal,
          stock: articulo.cantExistencia,
          type: articulo.tipo,
          subtype: articulo.subtipo,
          brand: articulo.marca,
          provider: articulo.proveedor,
        )
        return item
      }
    } catch ( Exception e ) {
      log.error( e.message, e )
    }
    return null
  }
}