package mx.wen.pos.service

import mx.wen.pos.model.Cliente

interface ClienteService {

  Cliente obtenerCliente( Integer id )

  List<Cliente> buscarCliente( String nombre, String apellidoPaterno, String apellidoMaterno )

  Cliente agregarCliente( Cliente cliente, boolean editar )

  Cliente obtenerClientePorDefecto( )

  Boolean esRfcValido( String rfc )
}
