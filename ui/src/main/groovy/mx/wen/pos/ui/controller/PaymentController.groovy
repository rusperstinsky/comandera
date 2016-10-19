package mx.wen.pos.ui.controller

import mx.wen.pos.model.Plan as CorePlan

import groovy.util.logging.Slf4j
import mx.wen.pos.model.Pago
import mx.wen.pos.model.TipoPago
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import mx.wen.pos.service.*
import mx.wen.pos.ui.model.*

@Slf4j
@Component
class PaymentController {

  private static PagoService pagoService
  private static TipoPagoService tipoPagoService
  private static PlanService planService

  @Autowired
  PaymentController(
    PagoService pagoService,
    TipoPagoService tipoPagoService,
    PlanService planService
  ) {
    this.pagoService = pagoService
    this.tipoPagoService = tipoPagoService
    /*this.bancoService = bancoService
    this.terminalService = terminalService*/
    this.planService = planService
    //this.mensajeService = mensajeService
  }

  static PaymentType findDefaultPaymentType( ) {
    log.info( "obteniendo tipo de pago por defecto" )
    TipoPago result = tipoPagoService.obtenerTipoPagoPorDefecto()
    PaymentType.toPaymentType( result )
  }

  static List<PaymentType> findActivePaymentTypes( ) {
    log.info( "obteniendo tipos de pago activos" )
    List<TipoPago> results = tipoPagoService.listarTiposPagoActivos()
    results?.collect { TipoPago tipoPago ->
      PaymentType.toPaymentType( tipoPago )
    }
  }

  /*static List<Plan> findPlansByTerminal( String terminalId ) {
    log.info( "obteniendo planes por terminal: ${terminalId}" )
    List<CorePlan> results = planService.listarPlanesPorTerminal( terminalId )
    results?.collect { CorePlan plan ->
      Plan.toPlan( plan )
    }
  }*/

  static List<Payment> findPaymentsByOrderId( String orderId ) {
    log.info( "obteniendo pagos por orden id: ${orderId}" )
    List<Pago> results = pagoService.listarPagosPorIdFactura( orderId )
    if ( results?.any() ) {
      return results.collect { Pago pago ->
        Payment.toPaymment( pago )
      }
    }
    return [ ]
  }

  static Boolean findTypePaymentsDollar( String formaPago ){
    log.debug( 'findTypePaymentsDollar( )' )
    return pagoService.obtenerTipoPagosDolares( formaPago )
  }

  static String findDefaultPlanCreditCard(){
      log.debug( 'findDefaultPlanCreditCard( )' )
      return pagoService.obtenerPlanNormalTarjetaCredito()
  }
}