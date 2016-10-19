package mx.wen.pos.service.impl

import mx.wen.pos.model.Parametro
import mx.wen.pos.model.TipoPago
import mx.wen.pos.model.TipoParametro
import mx.wen.pos.repository.ParametroRepository
import mx.wen.pos.repository.TipoPagoRepository
import mx.wen.pos.service.TipoPagoService
import mx.wen.pos.service.business.Registry
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.annotation.Resource

@Service( 'tipoPagoService' )
@Transactional( readOnly = true )
class TipoPagoServiceImpl implements TipoPagoService {

  private static final Logger log = LoggerFactory.getLogger( TipoPagoServiceImpl.class )

  @Resource
  private TipoPagoRepository tipoPagoRepository

  @Resource
  private ParametroRepository parametroRepository

  private List<TipoPago> listarTiposPagoRegistrados( ) {
    List<TipoPago> resultados = new ArrayList<>()
    List<TipoPago> resultadosTmp = tipoPagoRepository.findAll( ) ?: [ ]
    /*Collections.sort(resultadosTmp, new Comparator<TipoPago>() {
        @Override
        int compare(TipoPago o1, TipoPago o2) {
            return o1.tipoCon.compareTo(o2.tipoCon)
        }
    })*/
    resultados.retainAll { TipoPago tipoPago ->
      StringUtils.isNotBlank( tipoPago?.id )
    }
    for(TipoPago tipoPago : resultadosTmp){
      if(tipoPago.id.contains("EFM")){
        resultados.add( tipoPago )
      }
    }
    for(TipoPago tipoPago : resultadosTmp){
      if(tipoPago.descripcion.contains("[")){
        resultados.add( tipoPago )
      }
    }
    for(TipoPago tipoPago : resultadosTmp){
      if(!tipoPago.id.contains("EFM") && !tipoPago.descripcion.contains("[")){
        resultados.add( tipoPago )
      }
    }
    return resultados
  }

  @Override
  TipoPago obtenerTipoPagoPorDefecto( ) {
    log.info( "obteniendo tipo pago por defecto" )
    return tipoPagoRepository.findOne( 'EFM' )
  }

  @Override
  List<TipoPago> listarTiposPago( ) {
    log.info( "listando tipos de pago" )
    return listarTiposPagoRegistrados()
  }

  @Override
  List<TipoPago> listarTiposPagoActivos( ) {
    log.info( "listando tipos de pago activos" )
    List<TipoPago> tiposPago = [ ]
    List<TipoPago> tiposPagoTmp = [ ]
    Parametro parametro = parametroRepository.findOne( TipoParametro.TIPO_PAGO.value )
    String[] valores = parametro?.valores
    log.debug( "obteniendo parametro de formas de pago activas id: ${parametro?.id} valores: ${valores}" )
    if ( valores.any() ) {
      List<TipoPago> resultados = listarTiposPagoRegistrados()
      log.debug( "tipos de pago existentes: ${resultados*.id}" )
      tiposPagoTmp = resultados.findAll { TipoPago tipoPago ->
        valores.contains( tipoPago?.id?.trim() )
      }
      tiposPago.addAll(tiposPagoTmp)
      log.debug( "tipos de pago obtenidos: ${tiposPago*.id}" )
    }
    return tiposPago
  }
}
