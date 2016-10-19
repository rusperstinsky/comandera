package mx.wen.pos.ui.model

import mx.wen.pos.model.Cliente
import mx.wen.pos.model.DetalleNotaVenta
import mx.wen.pos.model.NotaVenta
import org.apache.commons.lang3.StringUtils

class OrderActive implements Comparable<OrderActive> {

  private Order order
  private Customer customer
  OrderActive(Order pOrder, Customer pCustomer) {
    this.order = pOrder
    this.customer = pCustomer
  }

  Order getOrder() {
    return this.order
  }

  Customer getCustomer() {
    return this.customer
  }

  String getCustomerName() {
    return this.customer.fullName
  }

  String getPartList() {
    StringBuffer sb = new StringBuffer()
    for (OrderItem orderLine : this.order.items) {
      if (sb.length() > 0) {
        sb.append( ', ')
      }
      sb.append( StringUtils.trimToEmpty( orderLine.item.name ) )
    }
    return sb.toString()
  }

  BigDecimal getAmount() {
    return this.order.netSell
  }

  // Comparable
  int compareTo( OrderActive order ) {
    return this.getCustomerName().compareToIgnoreCase( order.getCustomerName() )
  }

  String toString( ) {
    return String.format( 'Order:%s  Customer:%s  Amount:%,.2f', this.getOrder().id, this.getCustomerName(),
        this.amount)
  }
}
