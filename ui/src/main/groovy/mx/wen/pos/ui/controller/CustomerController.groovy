package mx.wen.pos.ui.controller

import groovy.util.logging.Slf4j
import mx.wen.pos.model.Cliente
import mx.wen.pos.service.ClienteService
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import mx.wen.pos.ui.model.*

@Slf4j
@Component
class CustomerController {

    private static ClienteService clienteService

    @Autowired
    public CustomerController(
            ClienteService clienteService
    ) {
        this.clienteService = clienteService
    }

    static private final String TAG_OTROS_PAISES = 'OTROS'
    static private final String TAG_MEXICO = 'MEXICO'


    static Customer getCustomer(Integer id) {
        log.debug("obteniendo cliente id: ${id}")
        def result = clienteService.obtenerCliente(id)
        Customer.toCustomer(result)
    }

    static List<Customer> findCustomers(Customer sample) {
        log.debug("obteniendo lista de customer similar a: ${sample}")
        def results = clienteService.buscarCliente(sample?.name, sample?.fathersName, sample?.mothersName)
        log.debug("se obtiene lista con: ${results?.size()} customers")
        results.collect {
            Customer.toCustomer(it)
        }
    }

    static Customer addCustomer(Customer customer, boolean editar) {
        log.debug("registrando cliente: ${customer?.dump()}")
        if (StringUtils.isNotBlank(customer?.name)) {
            //log.debug("resultados de localidades: ${results*.usuario}")
            Cliente cliente = new Cliente()
            if(customer?.id){
                cliente.id = customer.id
            }   
            cliente.nombre = customer.name
            cliente.apellidoPaterno = customer.fathersName
            cliente.apellidoMaterno = customer.mothersName
            cliente.fechaNacimiento = customer.dob
            /*cliente.direccion = customer.address?.primary
            cliente.colonia = customer.address?.location
            cliente.codigo = customer.address?.zipcode
            cliente.rfc = customer.rfc ?: customer.type?.rfc
            cliente.idEstado = localidad?.idEstado ?: estado?.id
            cliente.idLocalidad = localidad?.idLocalidad
            cliente.telefonoCasa = homeContact?.primary
            cliente.email = emailContact?.primary
            cliente.udf1 = customer.age*/
            cliente = clienteService.agregarCliente(cliente, editar)
            customer.id = cliente?.id
            return customer
        }
        return null
    }


    static Boolean validRfc( String rfc ){
      return clienteService.esRfcValido( rfc )
    }


    static Customer findDefaultCustomer() {
        log.debug("obteniendo customer por default")
        Customer.toCustomer(clienteService.obtenerClientePorDefecto())
    }

}
