package mx.wen.pos.service

import mx.wen.pos.model.TipoPago

interface TipoPagoService {

  TipoPago obtenerTipoPagoPorDefecto( )

  List<TipoPago> listarTiposPago( )

  List<TipoPago> listarTiposPagoActivos( )

}
