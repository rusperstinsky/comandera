package mx.wen.pos.service.impl

import groovy.util.logging.Slf4j
import mx.wen.pos.service.ClienteService
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.annotation.Resource

import mx.wen.pos.model.*
import mx.wen.pos.repository.*
import mx.wen.pos.service.business.Registry

import java.util.regex.Pattern

@Slf4j
@Service( 'clienteService' )
@Transactional( readOnly = true )
class ClienteServiceImpl implements ClienteService {

  @Resource
  private ClienteRepository clienteRepository

  @Resource
  private ParametroRepository parametroRepository

  @Resource
  private SucursalRepository sucursalRepository

  /*protected void eliminarCliente( Integer id ) {
    if ( !Registry.genericCustomer.equals( id )) {
      try {
        ClientePais cp = clientePaisRepository.findOne( id )
        if( cp != null ){
          clientePaisRepository.delete( cp.id )
          clientePaisRepository.flush()
        }
        Cliente c = clienteRepository.findOne( id )
        if ( c != null ) {
          clienteRepository.delete( c.id )
          clienteRepository.flush()
        }
        log.debug( String.format( "Se elimino cliente:%d", id ) )
      } catch (Exception e) {
        log.debug( String.format( "Error al eliminar cliente: %d", id) )
      }
    }
  }*/

  @Override
  Cliente obtenerCliente( Integer id ) {
    log.debug( "obteniendo cliente id: ${id}" )
    clienteRepository.findOne( id ?: 0 )
  }

  @Override
  List<Cliente> buscarCliente( String nombre, String apellidoPaterno, String apellidoMaterno ) {
    log.debug( "buscando cliente: $nombre $apellidoPaterno $apellidoMaterno" )
    nombre = StringUtils.trimToNull( nombre )
    apellidoPaterno = StringUtils.trimToNull( apellidoPaterno )
    apellidoMaterno = StringUtils.trimToNull( apellidoMaterno )
    if ( nombre || apellidoPaterno || apellidoMaterno ) {
      def result = clienteRepository.findByNombreApellidos( nombre, apellidoPaterno, apellidoMaterno )
      log.debug( "se obtiene lista con: ${result?.size()} elementos" )
      return result
    }
    log.warn( "parametros insuficientes" )
    return [ ] as List<Cliente>
  }

  @Override
  @Transactional
  Cliente agregarCliente( Cliente cliente, boolean editar ) {
    log.debug( "agregando cliente: ${cliente?.dump()}" )
    if ( StringUtils.isNotBlank( cliente?.nombre ) ) {
      cliente.idSucursal = sucursalRepository.getCurrentSucursalId()
      try {
          /*if( editar ) {
            this.eliminarCliente( cliente.id )
          }*/
        cliente = clienteRepository.saveAndFlush( cliente )
        log.debug( "se registra cliente con id: ${cliente?.id}" )
        return cliente
      } catch ( ex ) {
        log.error( "problema al registrar cliente: ${cliente?.dump()}", ex )
      }
    }
    return null
  }


  @Override
  Cliente obtenerClientePorDefecto( ) {
    log.debug( "obteniendo cliente generico" )
    def idCliente = parametroRepository.findOne( TipoParametro.ID_CLIENTE_GENERICO.value )?.valor
    log.debug( "id cliente generico: ${idCliente}" )
    if ( idCliente?.isInteger() ) {
      return clienteRepository.findOne( idCliente?.toInteger() )
    }
    return null
  }


  @Override
  Boolean esRfcValido( String rfc ) {
    log.info( "validando rfc: ${rfc}" )
    String regex = '[a-zA-Z&/]{3,4}[0-9]{6}[a-zA-Z0-9]{3}'
    if ( Pattern.matches( regex, rfc ?: '' ) ) {
      log.debug( 'rfc valido' )
      return true
    } else {
      log.warn( 'rfc invalido' )
    }
    return false
  }
}
