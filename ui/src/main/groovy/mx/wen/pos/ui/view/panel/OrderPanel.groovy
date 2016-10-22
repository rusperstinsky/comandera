package mx.wen.pos.ui.view.panel

import groovy.model.DefaultTableModel
import groovy.swing.SwingBuilder
import mx.wen.pos.service.business.Registry
import mx.wen.pos.ui.controller.CustomerController
import mx.wen.pos.ui.controller.ItemController
import mx.wen.pos.ui.controller.OrderController
import mx.wen.pos.ui.model.Branch
import mx.wen.pos.ui.model.Customer
import mx.wen.pos.ui.model.Item
import mx.wen.pos.ui.model.Order
import mx.wen.pos.ui.model.OrderItem
import mx.wen.pos.ui.model.Payment
import mx.wen.pos.ui.model.Session
import mx.wen.pos.ui.model.SessionItem
import mx.wen.pos.ui.model.UpperCaseDocument
import mx.wen.pos.ui.model.User
import mx.wen.pos.ui.resources.UI_Standards
import mx.wen.pos.ui.view.dialog.ExtraItemsDialog
import mx.wen.pos.ui.view.dialog.HelpItemSearchDialog
import mx.wen.pos.ui.view.dialog.ItemDialog
import mx.wen.pos.ui.view.dialog.OrderActiveSelectionDialog
import mx.wen.pos.ui.view.dialog.PaymentDialog
import mx.wen.pos.ui.view.dialog.SuggestedItemsDialog
import mx.wen.pos.ui.view.renderer.MoneyCellRenderer
import net.miginfocom.swing.MigLayout
import org.apache.commons.lang3.StringEscapeUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import mx.wen.pos.ui.MainWindow
import org.springframework.core.io.ClassPathResource

import javax.swing.*
import java.awt.*
import java.awt.event.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.List

class OrderPanel extends JPanel implements FocusListener {

    private static final String TXT_BTN_CLOSE = 'Vendedor'
    private static final String TXT_BTN_QUOTE = 'Cotizar'
    private static final String TXT_BTN_PRINT = 'Imprimir'
    private static final String TXT_BTN_NEW_ORDER = 'Otra venta'
    private static final String TXT_BTN_CANCEL_ORDER = '<html><p align="center">Anular venta</p></html>'
    private static final String TXT_BTN_CONTINUE = 'Continuar'
    private static final String TXT_NO_ORDER_PRESENT = 'Se debe agregar al menos un artículo.'
    private static final String TXT_PAYMENTS_PRESENT = 'Elimine los pagos registrados y reintente.'
    private static final String MSJ_VENTA_NEGATIVA = 'No se pueden agregar artículos sin existencia.'
    private static final String MSJ_PAQUETE_INVALIDO = 'No se pueden agregar el paquete, ya existe uno.'
    private static final String MSJ_LENTE_INVALIDO = 'No se pueden agregar el lente, ya existe uno.'
    private static final String MSJ_SEGURO_APLICADO = 'No se pueden agregar el seguro, existe uno aplicado.'
    private static final String TXT_VENTA_NEGATIVA_TITULO = 'Error al agregar artículo'
    private static final String TXT_PAQUETE_INVALIDO = 'Error al agregar paquete'
    private static final String TXT_LENTE_INVALIDO = 'Error al agregar lente'
    private static final String TXT_REQUEST_NEW_ORDER = 'Solicita nueva orden a mismo cliente.'
    private static final String TXT_REQUEST_CONTINUE = 'Solicita nueva orden a otro cliente.'
    private static final String TXT_REQUEST_QUOTE = 'Cotizar orden actual.'
    private static final String MSJ_QUITAR_PAGOS = 'Elimine los pagos antes de cerrar la sesion.'
    private static final String TXT_QUITAR_PAGOS = 'Error al cerrar sesion.'
    private static final String MSJ_CAMBIAR_VENDEDOR = 'Esta seguro que desea salir de esta sesion.'
    private static final String TXT_CAMBIAR_VENDEDOR = 'Cerrar Sesion'
    private static final String TAG_ARTICULO_B = 'B'
    private static final String TAG_ARTICULO_L = 'L'
    private static final String TAG_ARTICULO_P = 'P'
    private static final String TAG_PAQUETE = 'Q'
    private static final String TAG_FORMA_PAGO_C1 = 'C1'
    private static final String TAG_FORMA_CARGO_EMP = 'FE'
    private static final String TAG_FORMA_CARGO_MVIS = 'FM'
    private static final String TAG_ARTICULO_NO_VIGENTE = 'C'
    private static final String TAG_PAYMENT_TYPE_TRANSF = 'TR'
    private static final String MSG_NO_STOCK = 'Articulo sin existencia ¿Desea continuar?'
    private static final String TXT_NO_STOCK = 'Articulo sin existencia'
    private static final String TAG_GENERICO_SEGUROS = 'J'
    private static final String TAG_GENERICO_ARMAZON = 'A'
    private static final String TAG_GENERICO_LENTE = 'B'
    private static final String TAG_GENERICO_LENTE_CONTACTO = 'H'
    private static final String TAG_SEGUROS_ARMAZON = 'SS'
    private static final String TAG_SEGUROS_OFTALMICO = 'SEG'
    private static final String TAG_SUBTIPO_NINO = 'N'
    private static final String TAG_RECETA_LC = 'LC'

    private Logger logger = LoggerFactory.getLogger(this.getClass())
    private SwingBuilder sb
    private Order order
    private Customer customer
    private JLabel operationType
    private JLabel customerName
    private JButton closeButton
    private JButton searchButton
    private JButton printButton
    private JButton continueButton
    private JButton newOrderButton
    private JButton cancelOrderButton
    private JTextArea comments
    public JTextField itemSearch = new JTextField()
    private JTextField itemQty
    private DefaultTableModel itemsModel
    private DefaultTableModel paymentsModel
    private DefaultTableModel promotionModel
    private JLabel folio
    private JLabel bill
    private JLabel date
    private JLabel total
    private JLabel paid
    private JLabel due
    private JLabel change
    private JLabel lblPromo
    private JLabel lblAmountPromo

    private Integer flag = 0
    private Boolean focusItem = false
    private BigDecimal promoAmount = BigDecimal.ZERO
    private JTabbedPane pestanias
    private PlatesPanel platesPanel
    private DrinksPanel drinksPanel

    private Boolean uiEnabled
    private static boolean ticketRx
    private String armazonString = null
    private Boolean activeDialogProccesCustomer = true
    private Boolean activeDialogBusquedaCliente = true
    private Boolean advanceOnlyInventariable
    private Boolean canceledWarranty
    private Boolean discountAgeApplied
    private String sComments = ''
    private HelpItemSearchDialog helpItemSearchDialog

    private MainWindow mainWindow
    private SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy")
    private def displayFontTit = new Font('', Font.BOLD, 16)
    private def displayFontGen = new Font('', Font.BOLD, 18)

    public Integer numQuote = 0

    OrderPanel( MainWindow mainWindow ) {
        sb = new SwingBuilder()
        order = new Order()
        this.mainWindow = mainWindow
        advanceOnlyInventariable = false
        customer = CustomerController.findDefaultCustomer()
        platesPanel = new PlatesPanel( this )
        drinksPanel = new DrinksPanel( this )
        buildUI()
        doBindings()
        //itemsModel.addTableModelListener(this.promotionDriver)
        uiEnabled = true
        //OperationType
    }

    private void buildUI() {
        sb.panel(this, layout: new MigLayout('fill,wrap 2', '[fill][fill,grow]', '[fill,grow]')) {
          panel(layout: new MigLayout('insets 5,fill,wrap 2', '[fill][fill]', '[fill,grow]')){
            panel(layout: new MigLayout('insets 0,fill', '[fill,280][fill,300!]', '[fill]'), constraints: 'span') {
                panel(border: loweredEtchedBorder(), layout: new MigLayout('wrap 2', '[][fill,right]', '[top]')) {
                    label(constraints: 'span')
                    label(constraints: 'span')
                    label('Cliente: ', font: displayFontTit)
                    customerName = label(font: displayFontGen)
                    label('Tipo: ', font: displayFontTit)
                    operationType = label("Publico General", font: displayFontGen)
                    label('Folio: ', font: displayFontTit)
                    folio = label(font: displayFontGen)
                    label('Fecha: ', font: displayFontTit)
                    label(df.format(new Date()), font: displayFontGen)
                }

                /*panel(border: loweredEtchedBorder(), layout: new MigLayout('wrap 2', '[][grow,right]', '[top]')) {
                    label( constraints: 'span' )
                    label( constraints: 'span' )
                    //date = label(font: displayFont)
                    label('Folio: ', font: displayFontTit)
                    folio = label(font: displayFontGen)
                    label('Fecha: ', font: displayFontTit)
                    label( df.format(new Date()), font: displayFontGen )
                }*/

                panel(border: loweredEtchedBorder(), layout: new MigLayout('wrap 2', '[][grow,right]', '[top]')) {
                    def displayFont = new Font('', Font.BOLD, 22)
                    label('Venta', font: displayFontTit)
                    total = label(font: displayFont)
                    label('Pagado', font: displayFontTit)
                    paid = label(font: displayFont)
                    label('Saldo', font: displayFontTit)
                    due = label(font: displayFont)
                }
            }

            scrollPane(border: titledBorder(title: 'Art\u00edculos'), preferredSize: [120, 550], constraints: 'span') {
                table(selectionMode: ListSelectionModel.SINGLE_SELECTION, mouseClicked: doShowItemClick) {
                    itemsModel = tableModel(list: order.items) {
                        closureColumn(
                                header: 'Pedido',
                                read: { OrderItem tmp -> "${tmp?.item?.name}" },
                                minWidth: 180,
                                maxWidth: 200
                        )
                        closureColumn(
                                header: 'Descripci\u00f3n',
                                read: { OrderItem tmp -> tmp?.description }
                        )
                        closureColumn(
                                header: 'Cantidad',
                                read: { OrderItem tmp -> tmp?.quantity },
                                minWidth: 70,
                                maxWidth: 70
                        )
                        closureColumn(
                                header: 'Precio',
                                read: { OrderItem tmp -> tmp?.item?.price },
                                minWidth: 80,
                                maxWidth: 100,
                                cellRenderer: new MoneyCellRenderer()
                        )
                        closureColumn(
                                header: 'Total',
                                read: { OrderItem tmp -> tmp?.item?.price * tmp?.quantity },
                                minWidth: 80,
                                maxWidth: 100,
                                cellRenderer: new MoneyCellRenderer()
                        )
                    } as DefaultTableModel
                }
            }

            panel(layout: new MigLayout('insets 0,fill', '[fill][fill,300!]', '[]'), constraints: 'span') {
                /*panel(layout: new MigLayout('wrap 3', '[fill][fill][fill,grow]', '[]'), border: titledBorder(title: "") ) {
                button( icon: imageIcon( url: new ClassPathResource( 'img/search_16.png' )?.URL ), actionPerformed: doHelp )
                label( "Articulo" )
                itemSearch = textField(font: new Font('', Font.BOLD, 16), document: new UpperCaseDocument(),
                        actionPerformed: { doItemSearch( "" ) })
                itemSearch.addFocusListener(this)
              }*//* {
                    table(selectionMode: ListSelectionModel.SINGLE_SELECTION,
                            mouseClicked: { MouseEvent ev -> onMouseClickedAtPromotions(ev) },
                            mouseReleased: { MouseEvent ev -> onMouseClickedAtPromotions(ev) }
                    ) {
                        promotionModel = tableModel(list: promotionList) {
                            closureColumn(header: "", type: Boolean, maxWidth: 25,
                                    read: { row -> row.applied },
                                    write: { row, newValue ->
                                        onTogglePromotion(row, newValue)
                                    }
                            )
                            propertyColumn(header: "Descripci\u00f3n", propertyName: "description", editable: false)
                            propertyColumn(header: "Art\u00edculo", propertyName: "partNbrList", maxWidth: 100, editable: false)
                            closureColumn(header: "Precio Base",
                                    read: { IPromotionAvailable promotion -> promotion.baseAmount },
                                    maxWidth: 80,
                                    cellRenderer: new MoneyCellRenderer()
                            )
                            closureColumn(header: "Descto",
                                    read: { IPromotionAvailable promotion -> promotion.discountAmount },
                                    maxWidth: 80,
                                    cellRenderer: new MoneyCellRenderer()
                            )
                            closureColumn(header: "Promoci\u00f3n",
                                    read: { IPromotionAvailable promotion -> promotion.promotionAmount },
                                    maxWidth: 80,
                                    cellRenderer: new MoneyCellRenderer()
                            )
                        } as DefaultTableModel
                    }
                }*/


                scrollPane(border: titledBorder(title: 'Pagos'), mouseClicked: doNewPaymentClick) {
                    table(selectionMode: ListSelectionModel.SINGLE_SELECTION, mouseClicked: doShowPaymentClick) {
                        paymentsModel = tableModel(list: new ArrayList<Payment>()) {
                            closureColumn(header: 'Descripci\u00f3n', read: { Payment tmp -> tmp?.description })
                            closureColumn(header: 'Monto', read: { Payment tmp -> tmp?.amount }, maxWidth: 100, cellRenderer: new MoneyCellRenderer())
                        } as DefaultTableModel
                    }
                }
            }

            scrollPane(border: titledBorder(title: 'Observaciones'), constraints: 'span') {
                comments = textArea(document: new UpperCaseDocument(), lineWrap: true)
            }

            panel(minimumSize: [600, 45], border: BorderFactory.createEmptyBorder(0, 0, 0, 0), constraints: 'span') {
                borderLayout()
                panel(constraints: BorderLayout.LINE_START, border: BorderFactory.createEmptyBorder(0, 0, 0, 0)) {
                    closeButton = button(TXT_BTN_CLOSE,
                            preferredSize: UI_Standards.BUTTON_SIZE,
                            actionPerformed: doClose
                    )
                }
                change = label(foreground: UI_Standards.WARNING_FOREGROUND, constraints: BorderLayout.CENTER)
                panel(constraints: BorderLayout.LINE_END, border: BorderFactory.createEmptyBorder(0, 0, 0, 0)) {
                    button("<html><p align=\"center\">Recuperar Nota</p></html>",
                            preferredSize: UI_Standards.BUTTON_SIZE,
                            actionPerformed: { fireRequestOldOrders() },
                            constraints: 'hidemode 3'
                    )
                    cancelOrderButton = button(TXT_BTN_CANCEL_ORDER,
                            preferredSize: UI_Standards.BUTTON_SIZE,
                            actionPerformed: { fireRequestCancelOrder() },
                            constraints: 'hidemode 3'
                    )
                    printButton = button(TXT_BTN_PRINT,
                            preferredSize: UI_Standards.BUTTON_SIZE,
                            actionPerformed: doPrint
                    )
                }
            }
          }
          panel(layout: new MigLayout('insets 0,fill', '[fill,grow]', '[fill]') ) {
            borderLayout()
            pestanias = tabbedPane( constraints: BorderLayout.CENTER  ) {
              panel( platesPanel, title: "Platos" )
              panel( drinksPanel, title: "Bebidas" )
            }
          }
        }
    }


    //Mapea los elementos de la pantalla con los datos de la BD
    private void doBindings() {
      sb.build {
            bean(customerName, text: bind { customer?.fullName })
            bean(folio, text: bind { order.id })
            if( promoAmount.compareTo(BigDecimal.ZERO) > 0 && !promoApplied() ){
                BigDecimal amountParcial = BigDecimal.ZERO
                BigDecimal amountEnsure = BigDecimal.ZERO
                for(OrderItem orderItem : order.items){
                    if( StringUtils.trimToEmpty(orderItem.item.type).equalsIgnoreCase(TAG_GENERICO_SEGUROS) ){
                        amountEnsure = amountEnsure.add(orderItem.item.price)
                    } else {
                        amountParcial = amountParcial.add(orderItem.item.price.multiply(orderItem.quantity))
                    }
                }
                if( amountParcial.compareTo(BigDecimal.ZERO) < 0 ){
                  amountParcial = BigDecimal.ZERO
                }
                total.text = NumberFormat.getCurrencyInstance(Locale.US).format((amountParcial.subtract(promoAmount)).add(amountEnsure))
                due.text = NumberFormat.getCurrencyInstance(Locale.US).format(((amountParcial.subtract(promoAmount)).add(amountEnsure)).subtract(order.paid))
            } else {
              bean(total, text: bind(source: order, sourceProperty: 'totalSell', converter: currencyConverter))
              bean(due, text: bind(source: order, sourceProperty: 'dueString'))
            }
            bean(paid, text: bind(source: order, sourceProperty: 'paid', converter: currencyConverter))
            bean(itemsModel.rowsModel, value: bind(source: order, sourceProperty: 'items', mutual: true))
            bean(paymentsModel.rowsModel, value: bind(source: order, sourceProperty: 'payments', mutual: true))
            bean(comments, text: bind(source: order, sourceProperty: 'comments', mutual: true))
            //bean(order, customer: bind { new Customer() })
        }
        itemsModel.fireTableDataChanged()
        paymentsModel.fireTableDataChanged()
    }

    //Busca y actualiza la nota actual para mostrar los datos correspondientes
    void updateOrder(String pOrderId) {
      String comments = ''
      if( order.comments != null && order.comments != '' ){
        comments = order.comments
      }
      Order tmp = OrderController.getOrder(pOrderId)
      if (tmp?.id) {
        if( comments.length() > 0 ){
          tmp?.comments = comments
        }
        order = tmp
        doBindings()
      }
    }

    private def dateConverter = { Date val ->
        val?.format('dd-MM-yyyy')
    }

    private def currencyConverter = {
        NumberFormat.getCurrencyInstance(Locale.US).format(it ?: 0)
    }


    //Busca y despliega el dialogo con los datos dorrespondientes al cliente seleccionado
    /*private def doCustomerSearch = { ActionEvent ev ->
        JButton source = ev.source as JButton
        source.enabled = false
        if (order.customer.id == null) {
            sb.doLater {
                if (this.customer == null) {
                    this.operationType.setSelectedItem(OperationType.DEFAULT)
                }
            }
        } else {
          if ( CustomerType.FOREIGN.equals( customer.type ) ) {
              ForeignCustomerDialog dialog = new ForeignCustomerDialog( this, customer, true )
              dialog.show()
              this.customer = dialog.customer
          } else {
              NewCustomerAndRxDialog dialog = new NewCustomerAndRxDialog( this, customer, true )
              dialog.show()
              this.customer = dialog.customer
          }
          sb.doLater {
              this.doBindings()
          }
        }

        doBindings()
        source.enabled = true
    }*/

    //Muestra el dialogo correspondiente a los diferentes tipos de clientes GENERAL, NUEVO, PROCESO, CAJA, COTIZA y EDITA CLIENTE CAJA.


   //Busca y valida el articulo que se inserta en la caja de texto.
    public def doItemSearch(  String tipo ) {
      Registry.getSolicitaGarbageColector()
      String input = StringUtils.trimToEmpty(itemSearch.text)
      if ( StringUtils.isNotBlank( input ) || StringUtils.isNotBlank( tipo ) ) {
        sb.doOutside {
          //DailyCloseController.openDay()
          List<Item> results = ItemController.findItemsByQuery( input, tipo )
          if ( results?.any() ) {
            if ( results.size() == 1 ) {
              if( results?.first()?.stock <= 0 ){
                Integer question =JOptionPane.showConfirmDialog( new JDialog(), MSG_NO_STOCK, TXT_NO_STOCK,
                      JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE )
                if( question == 0){
                  Item item = results.first()
                  if( StringUtils.trimToEmpty(item.subtype) ){
                    ExtraItemsDialog extraItemsDialog = new ExtraItemsDialog( itemSearch, item )

                  }
                  creaVenta( item )
                }
              } else {
                Item item = results.first()
                creaVenta( item )
              }
            } else {
              SuggestedItemsDialog dialog = new SuggestedItemsDialog( itemSearch, input, results )
              dialog.show()
              Item item = dialog.item
              if ( item?.id ) {
                if( item?.stock <= 0 ){
                  Integer question =JOptionPane.showConfirmDialog( new JDialog(), MSG_NO_STOCK, TXT_NO_STOCK,
                        JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE )
                  if( question == 0){
                    creaVenta( item )
                  }
                } else {
                  creaVenta( item )
                }
              }
            }
            doBindings()
          } else {
            optionPane( message: "No se encontraron resultados para: ${input}", optionType: JOptionPane.DEFAULT_OPTION )
                  .createDialog( new JTextField(), "B\u00fasqueda: ${input}" ).show()
          }
        }
        sb.doLater {
          itemSearch.text = null
        }
      }/* else {
        sb.optionPane( message: 'Es necesario ingresar una b\u00fasqeda v\u00e1lida', optionType: JOptionPane.DEFAULT_OPTION )
              .createDialog( new JTextField(), "B\u00fasqueda inv\u00e1lida" ).show()
      }*/
      itemSearch.requestFocus()
    }

    private def doShowItemClick = { MouseEvent ev ->
      if (SwingUtilities.isLeftMouseButton(ev)) {
        if (ev.clickCount == 2) {
          new ItemDialog(ev.component, order, ev.source.selectedElement as OrderItem).show()
          updateOrder(order?.id)
        }
      }
    }

    private def doNewPaymentClick = { MouseEvent ev ->
      if (SwingUtilities.isLeftMouseButton(ev)) {
        if ( ev.clickCount == 1 ) {
          if (order.due) {
            new PaymentDialog( ev.component, order, null, order.due, mainWindow ).show()
            updateOrder(order?.id)
            doBindings()
          } else {
            sb.optionPane( message: 'No hay saldo para aplicar pago', messageType: JOptionPane.ERROR_MESSAGE)
                  .createDialog(this, 'Pago sin saldo').show()
          }
        }
      }
    }

    private def doShowPaymentClick = { MouseEvent ev ->
      if (SwingUtilities.isLeftMouseButton(ev)) {
        Boolean valid = true
        if ( ev.clickCount == 2 ) {
          new PaymentDialog( ev.component, order, ev.source.selectedElement, BigDecimal.ZERO, mainWindow ).show()
          updateOrder(order?.id)
        }
      }
    }

    private void creaVenta(Item item) {
      User u = Session.get(SessionItem.USER) as User
      order.setEmployee(u.username)
      Branch branch = Session.get(SessionItem.BRANCH) as Branch
      order = OrderController.addItemToOrder(order.id, item)
      updateOrder( order.id )
      /*
        if( isOnePackage ){
          if( isOneLens ){
            if (surteSwitch?.agregaArticulo && surteSwitch?.surteSucursal) {
              String surte = surteSwitch?.surte
              if (item.stock > 0) {
                order = OrderController.addItemToOrder(order, item, surte)
                updateOrder( order.id )
                validaLC(item, false)
                controlItem(item, false, log)
                List<IPromotionAvailable> promotionsListTmp = new ArrayList<>()
                promotionsListTmp.addAll(promotionList)
                if( !holdPromo ){
                  for(IPromotionAvailable promo : promotionsListTmp){
                    this.promotionDriver.requestCancelPromotion(promo)
                    OrderController.deleteCuponMv( order.id )
                  }
                }
                if (customer != null) {
                  order.customer = customer
                }
              } else {
                if( esInventariable ){
                  SalesWithNoInventory onSalesWithNoInventory = OrderController.requestConfigSalesWithNoInventory()
                  order.customer = customer
                  if (SalesWithNoInventory.ALLOWED.equals(onSalesWithNoInventory)) {
                    order = OrderController.addItemToOrder(order, item, surte)
                    updateOrder( order.id )
                    validaLC(item, false)
                    controlItem(item, false, log)
                    List<IPromotionAvailable> promotionsListTmp = new ArrayList<>()
                    promotionsListTmp.addAll(promotionList)
                    if( !holdPromo ){
                      for(IPromotionAvailable promo : promotionsListTmp){
                        this.promotionDriver.requestCancelPromotion(promo)
                        OrderController.deleteCuponMv( order.id )
                      }
                    }
                  } else if (SalesWithNoInventory.REQUIRE_AUTHORIZATION.equals(onSalesWithNoInventory)) {
                    boolean authorized
                    if (AccessController.authorizerInSession) {
                      authorized = true
                    } else {
                      AuthorizationDialog authDialog = new AuthorizationDialog(this, " ")
                      authDialog.show()
                      authorized = authDialog.authorized
                    }
                    if (authorized) {
                      order = OrderController.addItemToOrder(order, item, surte)
                      updateOrder( order.id )
                      validaLC(item, false)
                      controlItem(item, false, log)
                      List<IPromotionAvailable> promotionsListTmp = new ArrayList<>()
                      promotionsListTmp.addAll(promotionList)
                      if( !holdPromo ){
                        for(IPromotionAvailable promo : promotionsListTmp){
                          this.promotionDriver.requestCancelPromotion(promo)
                          OrderController.deleteCuponMv( order.id )
                        }
                      }
                    }
                  } else {
                    if(log.equalsIgnoreCase("actionPerformed")){
                      focusItem = true
                    }
                    sb.optionPane(message: MSJ_VENTA_NEGATIVA, messageType: JOptionPane.ERROR_MESSAGE,)
                            .createDialog(this, TXT_VENTA_NEGATIVA_TITULO).show()
                  }
                } else {
                  order = OrderController.addItemToOrder(order, item, surte)
                  updateOrder( order.id )
                  validaLC(item, false)
                  controlItem(item, false, log)
                  List<IPromotionAvailable> promotionsListTmp = new ArrayList<>()
                  promotionsListTmp.addAll(promotionList)
                  if( !holdPromo ){
                    for(IPromotionAvailable promo : promotionsListTmp){
                      this.promotionDriver.requestCancelPromotion(promo)
                      OrderController.deleteCuponMv( order.id )
                    }
                  }
                }
              }
            }
          } else {
            if(log.equalsIgnoreCase("actionPerformed")){
              focusItem = true
            }
            sb.optionPane(message: MSJ_LENTE_INVALIDO, messageType: JOptionPane.ERROR_MESSAGE,)
                    .createDialog(this, TXT_LENTE_INVALIDO).show()
          }
        } else {
          if(log.equalsIgnoreCase("actionPerformed")){
            focusItem = true
          }
          sb.optionPane(message: MSJ_PAQUETE_INVALIDO, messageType: JOptionPane.ERROR_MESSAGE,)
                  .createDialog(this, TXT_PAQUETE_INVALIDO).show()
        }*/
    }

    private def doClose = {
        sb.doLater {
            doBindings()
            if (order.payments.size() == 0) {
                Integer question = JOptionPane.showConfirmDialog(new JDialog(), MSJ_CAMBIAR_VENDEDOR, TXT_CAMBIAR_VENDEDOR,
                        JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE)
                if (question == 0) {
                    if(order != null && order?.id != null){
                      OrderController.deleteCuponMv( StringUtils.trimToEmpty(order.id) )
                      this.promotionDriver.requestPromotionSave(order?.id, false)
                    }
                    MainWindow.instance.requestLogout()
                }
            } else {
                sb.optionPane(message: MSJ_QUITAR_PAGOS, messageType: JOptionPane.INFORMATION_MESSAGE,)
                        .createDialog(this, TXT_QUITAR_PAGOS)
                        .show()
            }
        }
    }

    private def doHelp = { ActionEvent ev ->
      if( helpItemSearchDialog == null ){
        helpItemSearchDialog = new HelpItemSearchDialog()
        helpItemSearchDialog.show()
        doItemSearch(StringUtils.trimToEmpty(helpItemSearchDialog.getTipo()))
        itemSearch.text = null
        helpItemSearchDialog = null
      }
    }

    private def doPrint = { ActionEvent ev ->
        if ( isValidOrder() ) {
          sb.doOutside {
                Registry.getSolicitaGarbageColector()
          }
          saveOrder( )
        } else {
            printButton.enabled = true
            closeButton.enabled = true
            operationType.enabled = true
            /*mainWindow.ordersMenu.enabled = true
            mainWindow.inventoryMenu.enabled = true
            mainWindow.sendReceivedInventoryMenu.enabled = true
            mainWindow.reportsMenu.enabled = true
            mainWindow.toolsMenu.enabled = true*/
        }
    }

    private void saveOrder() {
      User user = Session.get(SessionItem.USER) as User
      String vendedor = user.username
        Order newOrder = OrderController.placeOrder(order)
        if ( StringUtils.isNotBlank( newOrder?.id ) ) {
          OrderController.printOrder( newOrder.id )
          cleanOrder()
        } else {
            sb.optionPane(
                    message: 'Ocurrio un error al registrar la venta, intentar nuevamente',
                    messageType: JOptionPane.ERROR_MESSAGE
            ).createDialog( this, 'No se puede registrar la venta' )
                    .show()
        }
      /*

      OrderController.genreatedEntranceSP( StringUtils.trimToEmpty(newOrder.id) )
      if( newOrder.rx != null ){
        OrderController.sendFax( newOrder )
        OrderController.updateExam( newOrder )
        OrderController.updateRx( newOrder )
      }

        OrderController.saveAcuseCrmClave( order.id )
        ItemController.updateLenteContacto( newOrder.id )
      //if(numQuote > 0){
        OrderController.updateQuote( newOrder, null )
        //numQuote = 0
      //}
      this.promotionDriver.requestPromotionJavaSave(newOrder?.id, true)
      Boolean cSaldo = false
      OrderController.validaEntrega(StringUtils.trimToEmpty(newOrder?.bill),newOrder?.branch?.id?.toString(), true)
      Boolean needJb = OrderController.creaJb(StringUtils.trimToEmpty(newOrder?.bill), cSaldo)
      ItemController.updateLenteContacto( newOrder.id )
        if(isLc(newOrder)){
          OrderController.creaJbLc( newOrder.id )
        }
        if( OrderController.hasOrderLc(newOrder.bill) ){
          OrderController.printTicketEnvioLc( StringUtils.trimToEmpty(newOrder.bill) )
        }
        String idFacturaTransLc = StringUtils.trimToEmpty(OrderController.isReuseOrderLc( StringUtils.trimToEmpty(newOrder.id) ))
        if( idFacturaTransLc.length() > 0 ){
            CancellationController.sendTransferOrderLc( idFacturaTransLc, newOrder.id )
        }

        OrderController.validaSurtePorGenerico( order )
        if( advanceOnlyInventariable ){
          OrderController.creaJbAnticipoInventariables( newOrder?.id )
          advanceOnlyInventariable = false
        }
        if (StringUtils.isNotBlank(newOrder?.id)) {
          Branch branch = Session.get(SessionItem.BRANCH) as Branch
          if( Registry?.genericCustomer?.id != newOrder.customer.id ){
            OrderController.insertaAcuseAPAR(newOrder, branch)
          }
          Boolean montaje = false
          List<OrderItem> items = newOrder?.items
          Iterator iterator = items.iterator()
          while (iterator.hasNext()) {
            Item item = iterator.next().item
            if(StringUtils.trimToEmpty(item?.name).equals('MONTAJE')) {
              montaje = true
            }
          }
          if (montaje == true) {
            Boolean registroTmp = OrderController.revisaTmpservicios(newOrder?.id)
            User u = Session.get(SessionItem.USER) as User
            if (registroTmp == false) {
              CapturaSuyoDialog capturaSuyoDialog = new CapturaSuyoDialog(order, u,false)
              capturaSuyoDialog.show()
            }
            OrderController.printSuyo(newOrder,u)
          }
          CuponMvJava cuponMv = null
          Boolean validClave = true
          Boolean ensureApply = false
          Boolean ffApply = false
          Boolean hasC1 = false
          for(int i=0;i<promotionList.size();i++){
          if(promotionList.get(i) instanceof PromotionDiscount){
            cuponMv = OrderController.obtenerCuponMvJavaByClave( StringUtils.trimToEmpty(promotionList.get(i).discountType.description) )
            if( cuponMv == null ){
              String clave = OrderController.descuentoClavePoridFactura( order.id )
              cuponMv = OrderController.obtenerCuponMvJavaByClave( StringUtils.trimToEmpty(clave) )
            }
            if( StringUtils.trimToEmpty(promotionList.get(i).discountType.text).equalsIgnoreCase("Redencion de Seguro") ||
                    (StringUtils.trimToEmpty(promotionList.get(i).discountType.text).equalsIgnoreCase("DESCUENTO CUPON") &&
                            StringUtils.trimToEmpty(promotionList.get(i).discountType.description).length() >= 11) ){
              ensureApply = true
            }
            if( cuponMv != null ){
              if( StringUtils.trimToEmpty(cuponMv.claveDescuento).startsWith("F") ){
                ffApply = true
              }
              break
            } else if(!OrderController.generatesCoupon(promotionList.get(i).discountType.description)) {
              validClave = false
            }
          }
          }
          if( promotionList.size() <= 0 ){
            validClave = true
          }
          if(validClave){
              for(Payment payment : newOrder.payments){
                  if( Registry.paymentsTypeNoCupon.contains( payment.paymentTypeId ) ){
                      validClave = false
                  }
                  if( TAG_FORMA_PAGO_C1.equalsIgnoreCase( payment.paymentTypeId ) ||
                          TAG_FORMA_CARGO_EMP.equalsIgnoreCase( payment.paymentTypeId ) ||
                          TAG_FORMA_CARGO_MVIS.equalsIgnoreCase( payment.paymentTypeId )){
                      hasC1 = true
                  }
              }
          }
          if( cuponMv != null ){
            for(IPromotionAvailable promo : promotionList){
              if( promo instanceof PromotionDiscount ){
                OrderController.updateCuponMvJavaByClave(newOrder.id, StringUtils.trimToEmpty(promo.discountType.description))
              }
            }
          }
          if( newOrder.total.compareTo(BigDecimal.ZERO) > 0 ){
            if( cuponMv != null ){
                for(IPromotionAvailable promo : promotionList){
                  if( promo instanceof PromotionDiscount ){
                    OrderController.updateCuponMvJavaByClave(newOrder.id, StringUtils.trimToEmpty(promo.discountType.description))
                  }
                }
                if( Registry.tirdthPairValid() ){
                  Integer numeroCupon = cuponMv.claveDescuento.startsWith("8") ? 2 : 3
                  OrderController.updateCuponMvJava( cuponMv.facturaOrigen, newOrder.id, cuponMv.montoCupon, numeroCupon, false)
                }
            } else if( !ensureApply && !ffApply ){
              generatedCoupons( validClave, newOrder )
            }
          }
          if( !ensureApply && !ffApply ){
              if( OrderController.insertSegKig && !hasC1 ){
                Boolean hasLensKid = false
                Boolean hasEnsureKid = false
                for(OrderItem oi : newOrder.items){
                  Articulo articulo = ItemController.findArticle( oi.item.id )
                  String type = StringUtils.trimToEmpty(articulo.subtipo).length() > 0 ? StringUtils.trimToEmpty(articulo.subtipo) : StringUtils.trimToEmpty(articulo.idGenSubtipo)
                  if( StringUtils.trimToEmpty(type).startsWith(TAG_SUBTIPO_NINO) ){
                    hasLensKid = true
                  }
                  if( StringUtils.trimToEmpty(articulo.articulo).equalsIgnoreCase(TAG_SEGUROS_OFTALMICO) ){
                    hasEnsureKid = true
                  }
                }
                if( hasLensKid && !hasEnsureKid && newOrder.total.compareTo(BigDecimal.ZERO) > 0){
                  itemSearch.text = "SEG"
                  doItemSearch( true, "" )
                  newOrder = OrderController.placeOrder(newOrder, vendedor, false)
                  OrderController.insertSegKig = false
                }
              }
          }
          OrderController.printOrder(newOrder.id)
          OrderController.printReuse( StringUtils.trimToEmpty(newOrder.id) )
          if (ticketRx == true) {
            OrderController.printRx(newOrder.id, false)
            OrderController.fieldRX(newOrder.id)
          }
          reviewForTransfers(newOrder.id)
          promoAmount = BigDecimal.ZERO
          lblAmountPromo.text = promoAmount
          sb.doOutside {
            try{
              OrderController.runScriptBckpOrder( newOrder )
            } catch ( Exception e ){
              println e
            }
          }
          // Flujo despues de imprimir nota de venta}
          Order otherOrder = CustomerController.requestOrderByCustomer(this, customer)
          if( otherOrder != null && otherOrder?.id != null ){
            isPaying = true
          }
        } else {
            sb.optionPane(
                    message: 'Ocurrio un error al registrar la venta, intentar nuevamente',
                    messageType: JOptionPane.ERROR_MESSAGE
            ).createDialog(this, 'No se puede registrar la venta')
                    .show()
        }*/
    }

    private boolean isValidOrder() {
        if (itemsModel.size() == 0) {
            sb.optionPane(
                    message: 'Se debe agregar al menos un art\u00edculo a la venta',
                    messageType: JOptionPane.ERROR_MESSAGE
            ).createDialog(this, 'No se puede registrar la venta')
                    .show()
            return false
        }
        return true
    }





    void refreshData() {
        /*if( promotionListTmp.size() > 0 ){
          promotionList.addAll( promotionListTmp )
        }
        if(order.deals.size() > 0 ){
          if(order.deals.first() instanceof IPromotion){
              OrderLinePromotion orderDiscount = order.deals.first()
              println promotionList .size()
              for(PromotionAvailable promotionAvailable : promotionList){
                  if(orderDiscount.promotion.idPromocion == promotionAvailable.promotion.entity.idPromocion
                          && flag <= 0 ){
                      this.promotionDriver.getApplyPromotion(promotionAvailable)
                      flag = flag+1
                  }
              }
          }
        }
        this.promotionDriver.enableItemsTableEvents(false)
        this.getPromotionModel().fireTableDataChanged()
        updateOrder(order?.id)
        this.promotionDriver.enableItemsTableEvents(true)*/
    }

    public void focusGained(FocusEvent e) {

    }

    public void focusLost(FocusEvent e) {
      if (itemSearch.text.length() > 0 && !focusItem) {
        doItemSearch( "" )
        //itemSearch.requestFocus()
      } else if( focusItem ){
        focusItem = false
        //itemSearch.requestFocus()

      }
    }

    void setCustomer(Customer pCustomer) {
        this.logger.debug(String.format('Assign Customer: %s', pCustomer.toString()))
        customer = pCustomer
        doBindings()
    }

    void setOrder(Order pOrder) {
      this.logger.debug(String.format('Assign Order: %s', pOrder.toString()))
      this.updateOrder(pOrder.id)
    }

    void disableUI() {
        uiEnabled = false
    }

    void enableUI() {
        this.doBindings()
        uiEnabled = true
    }


    private void fireRequestCancelOrder( ) {
      OrderController.deleteOrder( StringUtils.trimToEmpty(order.id) )
      User u = Session.get(SessionItem.USER) as User
      cleanOrder()
    }


    private void fireRequestOldOrders( ) {
      /*OrderActiveSelectionDialog dialog = new OrderActiveSelectionDialog()
      dialog.orderList = OrderController.findPendingOrders( )
      dialog.activate()*/
      Order tmpOrder = OrderController.findLastPendingOrder( )
      if (tmpOrder != null) {
        Order o = tmpOrder
        Customer c = tmpOrder.customer
        cleanOrder()
        disableUI()
        setCustomer(c)
        setOrder(o)
        enableUI()
        doBindings()
      } else {
        sb.optionPane( message: "No tiene notas pendientes", optionType: JOptionPane.DEFAULT_OPTION )
              .createDialog( new JTextField(), "Notas Pendientes" ).show()
      }
      //dialog.dispose()
    }


    private Boolean isPaymentListEmpty() {
        return (order.payments.size() == 0)
    }

    /*public void cleanAll( ){
      sb = null
      order = null
      customer = null
    }*/

    private void cleanOrder(){
      order = new Order()
      advanceOnlyInventariable = false
      customer = CustomerController.findDefaultCustomer()
      doBindings()
    }


}