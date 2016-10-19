package mx.wen.pos.ui.model

import groovy.beans.Bindable
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import mx.wen.pos.model.Empleado
import org.apache.commons.lang.StringUtils

@Bindable
@ToString( excludes = 'password' )
@EqualsAndHashCode
class User {
  String name
  String fathersName
  String mothersName
  Integer username
  String password
  Integer suc
  Integer rating

  String getFullName( ) {
    "${name ?: ''} ${fathersName ?: ''} ${mothersName ?: ''}"
  }

  static toUser( Empleado empleado ) {
    if ( empleado?.id ) {
      def user = new User()
      user.name = empleado.nombre
      user.fathersName = empleado.apellidoPaterno
      user.mothersName = empleado.apellidoMaterno
      user.username = empleado.id
      user.password = empleado.passwd
      user.suc = empleado.idSucursal
      user.rating = empleado.idPuesto
      return user
    }
    return null
  }

  boolean equals(Object pObj) {
    boolean result = false
    if (pObj instanceof User) {
      result = this.getUsername().trim().equalsIgnoreCase(pObj.getUsername().trim())
    } else if ( pObj instanceof Empleado ) {
      result = this.getUsername().trim().equalsIgnoreCase(StringUtils.trimToEmpty(pObj.getId().toString()))
    }
    return result
  }

  String toString() {
    return String.format('(%s) %s', this.getUsername(), this.getFullName())
  }
}
