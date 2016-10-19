package mx.wen.pos.service.impl

import groovy.util.logging.Slf4j
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import mx.wen.pos.repository.EmpleadoRepository
import mx.wen.pos.service.EmpleadoService

import javax.annotation.Resource
import mx.wen.pos.model.Empleado

import java.text.NumberFormat
import java.text.ParseException

@Slf4j
@Service( 'empleadoService' )
@Transactional( readOnly = true )
class EmpleadoServiceImpl implements EmpleadoService {

  @Resource
  private EmpleadoRepository empleadoRepository


  static final String TAG_ACUSE_IMPORTA_EMPLEADO = 'importa_emp'
  static final Integer TAG_ID_EMPRESA = 7

  @Override
  Empleado obtenerEmpleado( Integer id ) {
    log.info( "obteniendo empleado id: ${id}" )
    if ( id != null ) {
      Empleado empleado = empleadoRepository.findOne( id )
      if ( empleado?.id ) {
        return empleado
      } else {
        log.warn( "empleado no existe" )
      }
    } else {
      log.warn( "no se obtiene empleado, parametros invalidos" )
    }
    return null
  }

  @Override
  void actualizarPass( Empleado empleado ){
      log.info( "actualizando password de empleado id: ${empleado.id}" )
      if ( empleado.id  != null ) {
          Empleado emp = empleadoRepository.save( empleado )
          empleadoRepository.flush()
      }
  }

}
