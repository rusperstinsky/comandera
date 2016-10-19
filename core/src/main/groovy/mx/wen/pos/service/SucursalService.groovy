package mx.wen.pos.service

import mx.wen.pos.model.Sucursal

interface SucursalService {

  Sucursal obtenSucursalActual( Integer idSuc )

  Sucursal obtenSucursalActual( )

  Sucursal obtenerSucursal( Integer pSucursal )

  List<Sucursal> listarSucursales( )

  List<Sucursal> listarAlmacenes( )

  List<Sucursal> listarSoloSucursales( )

  List<Sucursal> listarSoloAlmacenPorAclarar( Integer id )

  Boolean validarSucursal( Integer pSucursal )

}
