package mx.wen.pos.ui.model

import groovy.beans.Bindable
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import mx.wen.pos.model.NotaVenta
import mx.wen.pos.ui.controller.OrderController
import org.apache.commons.lang3.StringUtils

import java.text.NumberFormat

@Bindable
@ToString
@EqualsAndHashCode
class Order {

  private static final String OPHTALMIC_TYPE = 'B'

  String id
  Integer employee
  Customer customer
  BigDecimal totalSell
  BigDecimal netSell
  BigDecimal paid
  List<Payment> payments = [ ]
  Date date
  List<OrderItem> items
  Integer percentaje
  BigDecimal discountAmount
  String typeDiscount
  String status
  String comments
  Date dateMod
  Integer idStore
  String bill
  BigDecimal due = BigDecimal.ZERO
  String udf2
  String udf3
  String udf4
  String udf5


  private Double usdRate

  public Order( ) {
    //this.usdRate = OrderController.requestUsdRate()
  }

  String getTicket( ) {
    "${bill ?: ''}"
  }

  private Double getUsdDue( ) {
    Double due = due.doubleValue() / this.usdRate
    return due
  }

  String getDueString( ) {
    String str = String.format( "\$%,.2f", this.due )
    /*if ( OrderController.requestUsdDisplayed() ) {
      str = String.format( "\$%,.2f/US\$%,.0f", this.due, this.usdDue )
    }*/
    return str
  }

  protected void round( ) {
    this.totalSell = NumberFormat.getInstance().parse(String.format( "%.2f", this.totalSell ))
    this.netSell = NumberFormat.getInstance().parse(String.format( "%.2f", this.netSell ))
    this.paid = NumberFormat.getInstance().parse(String.format( "%.2f", this.paid ))
    //this.total = NumberUtils.createBigDecimal( String.format( "%.2f", this.total ) )
    //this.paid = NumberUtils.createBigDecimal( String.format( "%.2f", this.paid ) )
  }

  static Order toOrder( NotaVenta notaVenta ) {
    if ( notaVenta?.id ) {
      Order order = new Order(
          id: notaVenta.id,
          employee:notaVenta.idEmpleado,
          customer: Customer.toCustomer( notaVenta.cliente ),
          totalSell: notaVenta.ventaTotal ?: 0,
          netSell: notaVenta.ventaNeta ?: 0,
          paid: notaVenta.sumaPagos ?: 0,
          payments: notaVenta.pagos?.collect {Payment.toPaymment( it )},
          date: notaVenta.fechaHoraFactura,
          items: notaVenta.detalles?.collect {OrderItem.toOrderItem( it )},
          percentaje: notaVenta.por100Descuento,
          discountAmount: notaVenta.montoDescuento,
          typeDiscount: notaVenta.tipoDescuento,
          status: notaVenta.sFactura,
          comments: notaVenta.observacionesNv,
          dateMod: notaVenta.fechaMod,
          idStore: notaVenta.idSucursal,
          bill: notaVenta.factura,
          udf2: notaVenta.udf2,
          udf3: notaVenta.udf3,
          udf4: notaVenta.udf4,
          udf5: notaVenta.udf5
      )
      order.round()
      order.due = ((order.netSell.subtract( order.paid )).compareTo(new BigDecimal(0.05)) > 0 || (order.netSell.subtract( order.paid )).compareTo(new BigDecimal(-0.05)) < 0) ? order.netSell.subtract( order.paid ) : BigDecimal.ZERO
      return order
    }
    return null
  }

}
