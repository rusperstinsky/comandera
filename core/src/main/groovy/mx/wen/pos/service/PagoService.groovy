package mx.wen.pos.service

import mx.wen.pos.model.Pago

interface PagoService {

  Pago obtenerPago( Integer id )

  List<Pago> listarPagosPorIdFactura( String idFactura )

  Pago actualizarPago( Pago pago )

  Boolean obtenerTipoPagosDolares( String formaPago )

  String obtenerPlanNormalTarjetaCredito( )
}
