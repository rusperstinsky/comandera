package mx.wen.pos.ui.model

import groovy.beans.Bindable
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import mx.wen.pos.model.Cliente
import mx.wen.pos.model.NotaVenta
import mx.wen.pos.ui.controller.CustomerController
import org.apache.commons.lang3.StringUtils

import java.text.SimpleDateFormat

@Slf4j
@Bindable
@ToString
@EqualsAndHashCode
class Customer {
    Integer id
    String location
    String state
    String dateCreate
    String name
    String fathersName
    String mothersName
    String rfc = ""//CustomerType.DOMESTIC.rfc
    String adress
    String block
    String telHome
    String telWork
    String extWork
    String telAdi
    String extAdi
    String email
    String dateMod
    Integer idStore
    String udf1
    String udf2
    String udf3
    String udf4
    String udf5
    String udf6
    String obs
    Date dateBirth
    Date timeCreate

    String getFullName() {
        "${StringUtils.trimToEmpty(name) ?: ''} ${StringUtils.trimToEmpty(fathersName) ?: ''} ${StringUtils.trimToEmpty(mothersName) ?: ''}"
    }

    static Customer toCustomer(Cliente cliente) {
      if (cliente?.id) {
        Customer customer = new Customer(
          id: cliente.id,
          location: cliente.idLocalidad,
          state: cliente.idLocalidad,
          dateCreate: cliente.fechaAlta,
          name: cliente.nombre,
          fathersName: cliente.apellidoPaterno,
          mothersName: cliente.apellidoMaterno,
          rfc: cliente.rfc,
          adress: cliente.direccion,
          block: cliente.colonia,
          telHome: cliente.telefonoCasa,
          telWork: cliente.telefonoTrabajo,
          extWork: cliente.extTrabajo,
          telAdi: cliente.telefonoAdicional,
          extAdi: cliente.extAdicional,
          email: cliente.email,
          dateMod: cliente.fechaModificado,
          idStore: cliente.idSucursal,
          udf1: cliente.udf1,
          udf2: cliente.udf2,
          udf3: cliente.udf3,
          udf4: cliente.udf4,
          udf5: cliente.udf5,
          udf6: cliente.udf6,
          obs: cliente.obs,
          dateBirth: cliente.fechaNacimiento,
          timeCreate: cliente.horaAlta
        )
        return customer
      }
      return null
    }


    boolean equals(Object pObj) {
        boolean result = false;
        if (pObj instanceof Customer) {
            result = this.getId().equals((pObj as Customer).getId())
        }
        return result
    }

    /*static List<Customer> toList(List<Cliente> pClienteList) {
        List<Customer> custList = new ArrayList<Customer>()
        for (Cliente c : pClienteList) {
            custList.add(toCustomer(c))
        }
        return custList
    }

    Boolean isLocal() {
        return CustomerType.DOMESTIC.equals(this.type)
    }

    Titulo getTitle() {
        Titulo t = Titles.instance.find(this.title)
        if (t == null) {
            t = Titles.instance.getDefault(this.gender)
        }
        return t
    }

    Contact getPhone(Integer pContactIndex) {
        Contact telefono = null
        Integer ix = 0
        for (Contact c : this.contacts) {
            if (c.type.isPhone()) {
                if (ix == pContactIndex) {
                    telefono = c
                }
                ix++
            }
        }
        return telefono
    }

    Contact getEmail(Integer pContactIndex) {
        Contact email = null
        Integer ix = 0
        for (Contact c : this.contacts) {
            if (c.type.isMail()) {
                if (ix == pContactIndex) {
                    email = c
                }
                ix++
            }
        }
        return email
    }


    Examen getUltimoExamen(Integer id) {
        Examen examen = null

    }

    Date fechaUltimaNotaVenta() {

        if ( this.fechaUltimaVenta == null ) {
            NotaVenta notaVenta = CustomerController.buscarUltimaNotaVentaPorIdCliente(this.id)

            if (notaVenta != null)
                this.fechaUltimaVenta = notaVenta.fechaHoraFactura

        }

        return this.fechaUltimaVenta
    }

    Date fechaUltimoExamen() {

        if (this.fechaUltimoExamen == null) {
            Examen examen = CustomerController.buscarUltimoExamenPorIdCliente(this.id)

            if (examen != null) {
                this.fechaUltimoExamen = examen.fechaAlta
            }
        }

        this.fechaUltimoExamen;
    }


    String getFormaContactoMovil() {
        Integer tipoContacto = 3; // Para celulares

        FormaContacto formaContacto = CustomerController.buscarFormaContactoPorIdClienteTipoContacto(this.id, tipoContacto)

        if ( formaContacto != null )
            this.celular = formaContacto.contacto

        return this.celular
    }

    String getFechaFormato(String tipo){

        Date fecha = null

        if ( tipo.equals("fechaNacimiento") )
            fecha = this.fechaNacimiento

        if ( tipo.equals("fechaUltimaVenta") )
            fecha = fechaUltimaNotaVenta()

        if ( tipo.equals("fechaUltimoExamen") )
            fecha = fechaUltimoExamen()

        if ( fecha == null )
            return null

        String oldDate
        Date date
        String newDate

        oldDate = fecha.toString()
        date = new SimpleDateFormat("yyyy-MM-dd").parse(oldDate)
        newDate = new SimpleDateFormat("dd-MM-yyyy").format(date)

        return newDate
    }*/

}
