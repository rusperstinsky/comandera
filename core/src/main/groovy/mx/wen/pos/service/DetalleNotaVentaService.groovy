package mx.wen.pos.service

import mx.wen.pos.model.DetalleNotaVenta

interface DetalleNotaVentaService {

  DetalleNotaVenta obtenerDetalleNotaVenta( String idFactura, Integer idArticulo )

  List<DetalleNotaVenta> listarDetallesNotaVentaPorIdFactura( String idFactura )

}
