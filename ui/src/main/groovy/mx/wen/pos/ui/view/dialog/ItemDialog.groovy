package mx.wen.pos.ui.view.dialog

import groovy.swing.SwingBuilder
import mx.wen.pos.ui.controller.ItemController
import mx.wen.pos.ui.controller.OrderController
import mx.wen.pos.ui.model.Item
import mx.wen.pos.ui.model.Order
import mx.wen.pos.ui.model.OrderItem
import net.miginfocom.swing.MigLayout

import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.ItemEvent
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JDialog
import javax.swing.JSpinner

class ItemDialog extends JDialog {

  private SwingBuilder sb
  private OrderItem tmpOrderItem
  private OrderItem orderItem
  private Order order
  private List<Item> items
  private List<String> colors
  private List<String> deliversList
  private JSpinner quantity

  ItemDialog( Component parent, Order order, final OrderItem orderItem ) {
    this.orderItem = orderItem
    this.order = order
    sb = new SwingBuilder()
    tmpOrderItem = new OrderItem(
        item: orderItem?.item,
        quantity: orderItem?.quantity,
        //delivers: orderItem?.delivers
    )
    items = [ ]
    //colors = [ ]
    deliversList = [ 'Sucursal', 'Pino' ] as ObservableList
    items.addAll( ItemController.findItems( tmpOrderItem.item?.name ) )
    //colors.addAll( items*.color )
    buildUI( parent )
    doBindings()
  }

  private void buildUI( Component parent ) {
    sb.dialog( this,
        title: "Artículo ${tmpOrderItem.item?.name ?: ''}",
        location: parent.locationOnScreen,
        resizable: false,
        modal: true,
        pack: true,
        layout: new MigLayout( 'wrap 4', '[fill,grow]20[fill]20[fill]20[fill,grow]' )
    ) {
      label(  )
      label( 'Artículo' )
      label( 'Cantidad' )
      label(  )

      label( )
      label( tmpOrderItem.item?.name )
      quantity = spinner( model: spinnerNumberModel( minimum: 1, stepSize: 1, value: tmpOrderItem.quantity ) )
      label( )

      panel( layout: new MigLayout( 'right', '[fill,100!]' ), constraints: 'span' ) {
        button( 'Borrar', actionPerformed: doDelete )
        button( 'Aplicar', actionPerformed: doSubmit )
        button( 'Cancelar', defaultButton: true, actionPerformed: {dispose()} )
      }
    }
  }

  private void doBindings( ) {
    sb.build {
      bean( quantity, value: bind( source: tmpOrderItem, sourceProperty: 'quantity', mutual: true ) )
    }
  }

  private def doDelete = { ActionEvent ev ->
    JButton source = ev.source as JButton
    source.enabled = false
    OrderController.removeOrderItemFromOrder( order.id, orderItem )
    source.enabled = true
    dispose()
  }

  private def doSubmit = { ActionEvent ev ->
    JButton source = ev.source as JButton
    source.enabled = false
    OrderController.removeOrderItemFromOrder( order.id, orderItem )
    OrderController.addOrderItemToOrder( order.id, tmpOrderItem )
    source.enabled = true
    dispose()
  }
}
