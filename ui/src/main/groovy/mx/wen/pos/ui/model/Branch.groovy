package mx.wen.pos.ui.model

import groovy.beans.Bindable
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import mx.wen.pos.model.Sucursal

@Bindable
@ToString
@EqualsAndHashCode
class Branch {
  Integer id
  String name
  String address
  String colony
  String postalCode
  String city
  String telephoneNumbers
  String costCenter

  static Branch toBranch( Sucursal sucursal ) {
    if ( sucursal?.id ) {
      Branch branch = new Branch(
          id: sucursal.id,
          name: sucursal.nombre,
          address: sucursal.colonia,
          colony: sucursal.colonia,
          postalCode: sucursal.cp,
          city: sucursal.idLocalidad,
          telephoneNumbers: sucursal.telefonos,
          costCenter: sucursal.centroCostos
      )
      return branch
    }
    return null
  }

}
