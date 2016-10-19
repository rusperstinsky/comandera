package mx.wen.pos.ui.controller

import groovy.util.logging.Slf4j
import mx.wen.pos.ui.model.Branch
import mx.wen.pos.ui.model.Session
import mx.wen.pos.ui.model.SessionItem
import mx.wen.pos.ui.model.User
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import mx.wen.pos.service.EmpleadoService
import mx.wen.pos.service.SucursalService

import mx.wen.pos.model.Empleado

import java.text.NumberFormat
import java.text.ParseException

@Slf4j
@Component
class AccessController {

  private static EmpleadoService empleadoService
  private static SucursalService sucursalService

  @Autowired
  AccessController( EmpleadoService empleadoService, SucursalService sucursalService ) {
    this.empleadoService = empleadoService
    this.sucursalService = sucursalService
  }

  static User getUser( String username ) {
    log.info( "solicitando usuario: ${username}" )
    Integer value = 0
    try{
      value = NumberFormat.getInstance().parse(StringUtils.trimToEmpty(username))
    } catch ( ParseException e ){
      println e.message
    }
    return User.toUser( empleadoService.obtenerEmpleado( value ) )
  }

  static boolean checkCredentials( String username, String password ) {
    log.info( "comprobando credenciales para el usuario: ${username}" )
    if ( StringUtils.isNotBlank( username ) && StringUtils.isNotBlank( password ) ) {
      User user = getUser( username )
      if ( user?.username != null ) {
        if ( password.equalsIgnoreCase( user?.password ) ) {
          log.info( "credenciales correctas" )
          return true
        } else {
          log.warn( "acceso denegado, credenciales incorrectas" )
        }
      } else {
        log.warn( "usuario no existente" )
      }
    } else {
      log.warn( "no se comprueban credenciales, parametros invalidos" )
    }
    return false
  }

  static Boolean isAdmin( User user ) {
    log.info( "verificando permiso para usuario: ${user.dump()}" )
    Boolean valid = false
    if( user != null && user.rating == 1 ){
      valid = true
    }
    return valid
  }

  static User logIn( String username, String password ) {
    log.info( "solicitando autorizacion de acceso para el usuario: $username" )
    if ( checkCredentials( username, password ) ) {
      User user = getUser( username )
      Session.put( SessionItem.USER, user )
      Branch branch = Branch.toBranch( sucursalService.obtenSucursalActual( user.suc ) )
      Session.put( SessionItem.BRANCH, branch )
      log.info( "acceso autorizado: $username" )
      return user
    } else {
      log.warn( "acceso denegado, credenciales incorrectas" )
    }
    return null
  }

  static void logOut( ) {
    Session.clear()
    log.info( "log out" )
  }

  private static boolean isAuthorizer( Empleado empleado ) {
    log.info( "verificando si empleado es autorizador: ${empleado?.id}" )
    if ( empleado?.id ) {
      if ( ( 1..2 ).contains( empleado.idPuesto ) ) {
        log.info( "usuario es autorizador" )
        return true
      } else {
        log.info( "no es usuario autorizador" )
      }
    } else {
      log.warn( "no se verifica usuario, parametros invalidos" )
    }
    return false
  }

  static boolean isAuthorizerInSession( ) {
    log.info( "comprobando si usuario en sesion requiere autorizacion" )
    User user = Session.get( SessionItem.USER ) as User
    log.debug( "usuario en sesion: ${StringUtils.trimToEmpty(user?.username.toString())}" )
    if ( user?.username != null ) {
      Empleado empleado = empleadoService.obtenerEmpleado( user.username )
      if ( isAuthorizer( empleado ) ) {
        log.info( "usuario autorizador, no requiere autorizacion" )
        return true
      } else {
        log.info( "usuario requiere autorizacion" )
      }
    } else {
      log.warn( "no se realiza comprobacion, no existe usuario en sesion" )
    }
    return false
  }

  static boolean canAuthorize( String username, String password ) {
    log.info( "solicitando autorizacion por usuario: $username" )
    if ( checkCredentials( username, password ) ) {
      Empleado empleado = empleadoService.obtenerEmpleado( username )
      if ( isAuthorizer( empleado ) ) {
        log.info( "autorizacion realizada: $username" )
        return true
      } else {
        log.info( "autorizacion rechazada, no es usuario autorizador" )
      }
    } else {
      log.warn( "credenciales erroneas" )
    }
    return false
  }


  static Boolean iniciaSesionPrimeraVez(){
    return empleadoService.sesionPrimeraVez()
  }
}
