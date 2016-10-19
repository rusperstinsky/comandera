package mx.wen.pos.ui.model

import groovy.beans.Bindable
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import mx.wen.pos.model.Pago

/*import mx.lux.pos.model.Pago*/

import java.text.NumberFormat

@Bindable
@ToString
@EqualsAndHashCode
class Payment {
  Integer id
  String order
  String paymentReference
  BigDecimal amount
  Date date
  String username
  Date dateMod
  Integer idStore
  String paymentTypeId
  String paymentType
  String planId
  String issuerBankId

  String getDescription( ) {
    Integer pos = ( paymentReference?.size() >= 4 ) ? ( paymentReference.size() - 4 ) : 0
    "${paymentTypeId ? "${paymentTypeId} " : ''}${paymentReference ? "${paymentReference.substring( pos )} " : ''}"
  }

  void setAmount( BigDecimal pAmount ) {
    this.amount = NumberFormat.getInstance().parse(String.format( "%.2f", pAmount ))
    //this.amount = NumberUtils.createBigDecimal( String.format( "%.2f", pAmount ) )
  }

  void setAmount( Double pAmount ) {
    //this.amount = NumberUtils.createBigDecimal( String.format( "%.2f", pAmount ) )
      this.amount = NumberFormat.getInstance().parse(String.format( "%.2f", pAmount ))
  }

  static toPaymment( Pago pago ) {
    if ( pago?.id ) {
      Payment payment = new Payment(
        id: pago.id,
        order: pago.idFactura,
        paymentReference: pago.referenciaPago,
        amount: pago.monto,
        date: pago.fecha,
        username: pago.idEmpleado,
        dateMod: pago.fechaModificacion,
        idStore: pago.idSucursal,
        paymentTypeId: pago.idFPago,
        paymentType: pago.eTipoPago?.descripcion,
        planId: pago.idPlan,
        issuerBankId: pago.idBanco
      )
      return payment
    }
    return null
  }
}
