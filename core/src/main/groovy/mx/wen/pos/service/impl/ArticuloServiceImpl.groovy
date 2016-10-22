package mx.wen.pos.service.impl

import com.mysema.query.BooleanBuilder
import com.mysema.query.types.Predicate
import groovy.util.logging.Slf4j
import mx.wen.pos.model.Articulo
import mx.wen.pos.model.QArticulo
import mx.wen.pos.repository.ArticuloRepository
import mx.wen.pos.service.ArticuloService
import mx.wen.pos.service.business.Registry
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.annotation.Resource


import java.sql.SQLException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.regex.Pattern
import org.springframework.ui.velocity.VelocityEngineUtils
import org.apache.velocity.app.VelocityEngine
import java.text.NumberFormat

@Slf4j
@Service( 'articuloService' )
@Transactional( readOnly = true )
class ArticuloServiceImpl implements ArticuloService {

  @Resource
  private ArticuloRepository articuloRepository

  @Resource
  private VelocityEngine velocityEngine

  private static final Integer CANT_CARACTEREZ_SKU = 6
  private static final Integer CANT_CARACTEREZ_COD_BAR = 15
  private static final Integer CANT_ARTICULOS_ENVIAR = 500
  private static final String TAG_GENERICO_NO_INVENTARIABLE = "NO INVENTARIABLE"
  private static final String TAG_GENERICO_ARMAZON = "A"


  @Override
  Articulo obtenerArticulo( Integer id ) {
    log.info( "obteniendo articulo con id: ${id}" )
    Articulo articulo = articuloRepository.findOne( id ?: 0 )
    if ( articulo?.id ) {
      return articulo
    }
    return null
  }

  @Override
  List<Articulo> obtenerArticuloPorTipo( String tipo ){
    log.info( "obteniendo articulos con tipo: ${tipo}" )
    List<Articulo> articulos = articuloRepository.findItemsByType( StringUtils.trimToEmpty(tipo) )
    return articulos
  }
  /*@Override
  List<Articulo> listarArticulosPorCodigo( String articulo ) {
    return listarArticulosPorCodigo( articulo, true )
  }*/

  @Override
  List<Articulo> listarArticulosPorCodigo( String articulo ) {
    log.info( "listando articulos con articulo: ${articulo}" )
    Predicate predicate = QArticulo.articulo1.articulo.equalsIgnoreCase( articulo )
    List<Articulo> resultados = articuloRepository.findAll( predicate, QArticulo.articulo1.id.asc() ) as List<Articulo>
    return resultados
  }

  @Override
  List<Articulo> listarArticulosPorSubtipo( String subtipo ) {
    log.info( "listando articulos con articulo: ${subtipo}" )
    Predicate predicate = QArticulo.articulo1.subtipo.equalsIgnoreCase( StringUtils.trimToEmpty(subtipo) )
    List<Articulo> resultados = articuloRepository.findAll( predicate, QArticulo.articulo1.id.asc() ) as List<Articulo>
    return resultados
  }

  /*@Override
  List<Articulo> listarArticulosPorCodigoSimilar( String articulo ) {
    return listarArticulosPorCodigoSimilar( articulo, true )
  }*/

  @Override
  List<Articulo> listarArticulosPorCodigoSimilar( String articulo ) {
    log.info( "listando articulos con articulo similar: ${articulo}" )
    Predicate predicate = QArticulo.articulo1.articulo.startsWithIgnoreCase( articulo )
    List<Articulo> resultados = articuloRepository.findAll( predicate, QArticulo.articulo1.articulo.asc() ) as List<Articulo>
    return resultados
  }

  @Override
  Integer obtenerExistencia( Integer id ) {
    Articulo articulo = obtenerArticulo( id, false )
    return articulo?.cantExistencia ?: 0
  }

  @Override
  Boolean validarArticulo( Integer id ) {
    return articuloRepository.exists( id )
  }

  @Override
  String validarGenericoArticulo( Integer id ) {
    String genericoInvalido = ""
    Articulo articulo = articuloRepository.findOne( id )
    if( articulo != null ){
      if( articulo.generico == null ){
        if(StringUtils.trimToEmpty(articulo.idGenerico).length() > 0){
          genericoInvalido =  StringUtils.trimToEmpty(articulo.idGenerico)
        } else if( articulo.idGenerico == null || StringUtils.trimToEmpty(articulo.idGenerico).length() <= 0 ){
          genericoInvalido =  "vacio"
        }
      } else if( !articulo.generico.inventariable ){
        genericoInvalido = TAG_GENERICO_NO_INVENTARIABLE
      }
    }
    return genericoInvalido
  }

  @Override
  @Transactional
  Boolean registrarArticulo( Articulo pArticulo ) {
    if ( pArticulo != null ) {
      pArticulo = articuloRepository.save( pArticulo )
      return pArticulo?.id > 0
    }
    return false
  }

  @Override
  @Transactional
  Boolean registrarListaArticulos( List<Articulo> pListaArticulo ) {
    boolean registrado = false
    if ( ( pListaArticulo != null ) && ( pListaArticulo.size() > 0 ) ) {
      articuloRepository.save( pListaArticulo )
      registrado = true
    }
    articuloRepository.flush()
    return registrado
  }

  @Override
  List<Articulo> obtenerListaArticulosPorId( List<Integer> pListaId ) {
    return articuloRepository.findByIdIn( pListaId )
  }

  List<Articulo> findArticuloyColor( String articulo, String color ) {
    log.debug( "findArticuloyColor()" )

    List<Articulo> lstArticulos = new ArrayList<Articulo>()
    List<Articulo> lstArticulos2 = new ArrayList<Articulo>()
    Integer idArticulo = 0

     if ( !articulo.contains( "-" ) && !articulo.contains( "/" ) && !articulo.contains( "+" ) && !articulo.contains( "." ) && articulo.isNumber() ) {
      try{
        if( articulo.length() > CANT_CARACTEREZ_COD_BAR ){
          articulo = articulo.substring( 1 )
        }
          if( articulo.length() > CANT_CARACTEREZ_SKU ){
            idArticulo = Integer.parseInt( articulo.substring( 0, CANT_CARACTEREZ_SKU ) )
          } else {
            idArticulo = Integer.parseInt( articulo )
          }
      }catch ( Exception e ){
        log.error( "No se introdujo el SKU del articulo", e  )
      }
    }

    QArticulo art = QArticulo.articulo1
    lstArticulos2 = articuloRepository.findAll( art.id.eq( idArticulo ).or( art.articulo.eq( articulo ) ) ) as List
    if ( lstArticulos2.size() == 0 || lstArticulos2.size() > 1 ) {
      log.debug( "if de Articulos" )
      BooleanBuilder colour = new BooleanBuilder()
      if ( color.length() == 0 ) {
        colour.and( art.codigoColor.isNull() )
      } else {
        colour.and( art.codigoColor.eq( color ) )
      }
      lstArticulos2 = articuloRepository.findAll( art.id.eq( idArticulo ).or( art.articulo.eq( articulo ) ).and( colour ) ) as List
    }
    if ( lstArticulos2.size() > 0 ) {
      lstArticulos = lstArticulos2
    }

    return lstArticulos
  }


  Articulo buscaArticulo( Integer id ){
    return articuloRepository.findOne( id )
  }

}
