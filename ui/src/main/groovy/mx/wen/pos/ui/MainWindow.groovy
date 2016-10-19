package mx.wen.pos.ui

import mx.wen.pos.ui.controller.AccessController
import groovy.swing.SwingBuilder
import mx.wen.pos.ui.model.Branch
import mx.wen.pos.ui.model.Session
import mx.wen.pos.ui.model.SessionItem
import mx.wen.pos.ui.model.User
import mx.wen.pos.ui.view.dialog.NewCustomer
import mx.wen.pos.ui.view.panel.OrderPanel
import net.miginfocom.swing.MigLayout
import org.apache.commons.lang.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import mx.wen.pos.ui.view.panel.LogInPanel

import java.awt.event.KeyEvent
import java.awt.event.KeyListener


import java.awt.*
import javax.swing.*

class MainWindow extends JFrame implements KeyListener {

  static final String MSG_LOAD_PARTS = "No se encuentra el archivo: %s"
  static final String TEXT_LOAD_PARTS_TITLE = "Importar Catálogo de Artículos"
  static final String TEXT_LOAD_PART_CLASS_TITLE = "Importar Clasificación de Artículos"

  static String version
  static Integer cantPriceListPending

  static MainWindow instance
  private Logger log = LoggerFactory.getLogger( this.getClass() )
  private SwingBuilder sb

  private JPanel mainPanel
  private JPanel logInPanel
  private JPanel orderPanel
  private JPanel showOrderPanel
  //private InvTrView invTrView
  private JPanel dailyClosePanel
  private JPanel priceListPanel
  private JPanel invoicePanel
  private JToolBar infoBar
  private JLabel userLabel
  private JLabel branchLabel
  private JLabel versionLabel
  private JMenu toolsMenu
  private JMenu ordersMenu
  private JMenu customerMenu
  private JMenu inventoryMenu
  private JMenu reportsMenu
  private JMenuItem orderMenuItem
  private JMenuItem insertCustomerMenuItem
  private JMenuItem searchCustomerMenuItem
  private JMenuItem priceListMenuItem
  private JMenuItem invoiceMenuItem
  private JMenuItem sessionMenuItem
  private JMenuItem cancellationReportMenuItem
  private JMenuItem correctTransactionsMenuItem
  private JMenuItem dailyCloseReportMenuItem
  private JMenuItem skuByBrandReportMenuItem
  private JMenuItem incomePerBranchReportMenuItem
  private JMenuItem sellerRevenueReportMenuItem
  private JMenuItem undeliveredJobsReportMenuItem
  private JMenuItem salesReportMenuItem
  private JMenuItem salesByLineReportMenuItem
  private JMenuItem salesByBrandReportMenuItem
  private JMenuItem salesBySellerReportMenuItem
  private JMenuItem inventoryTransactionMenuItem
  private JMenuItem inventoryOhQueryMenuItem
  private JMenuItem salesBySellerByBrandMenuItem
  private JMenuItem stockbyBrandMenuItem
  private JMenuItem stockbyBrandColorMenuItem
  private JMenuItem taxBillsMenuItem
  private JMenuItem discountsMenuItem
  private JMenuItem promotionsMenuItem
  private JMenuItem promotionsListMenuItem
  private JMenuItem paymentsMenuItem
  private JMenuItem quoteMenuItem
  private JMenuItem loadPartsMenuItem
  private JMenuItem loadPartClassMenuItem
  private JMenuItem generateInventoryFile
  private JMenuItem newSalesDayMenuItem
  private JMenuItem cotizacionMenuItem
  private JMenuItem kardexMenuItem
  private JMenuItem salesTodayMenuItem
  private JMenuItem salesByPeriodMenuItem
  private JMenuItem changePasswordMenuItem
  private JMenuItem changeSellerMenuItem
  private JMenuItem generateIn2MenuItem
  private JMenuItem ticketInventoryTransactionsMenuItem
  private JMenuItem sendInventoryMenuItem
  private JMenuItem receivedDiferencesMenuItem
  private JMenuItem generatedInventoryFileMenuItem
  private JMenuItem diferencesInventoryFileMenuItem
  private JMenuItem captureIncidentsMenuItem
  private JMenu sendReceivedInventoryMenu
  private JMenuItem reprintVoucherMenuItem
  private JMenuItem reprintWarrantyMenuItem
  //private PromotionService promotionService


  MainWindow( ) {
    instance = this
    this.addKeyListener( this )
    sb = new SwingBuilder()
    //buildUI()
  }

  private void buildUI( ) {
    logInPanel = new LogInPanel( doForwardToDefaultPanel, version )
    sb.build {
      lookAndFeel( 'system' )
      frame( this,
          title: 'Punto de Venta',
          focusable: true,
          layout: new MigLayout( 'fill,insets 1,center,wrap', '[fill]', '[top]' ),
          minimumSize: [ 900, 650 ] as Dimension,
          extendedState: JFrame.MAXIMIZED_BOTH,
          location: [ 50, 15 ] as Point,
          pack: true,
          resizable: true,
          defaultCloseOperation: EXIT_ON_CLOSE
      ) {
        menuBar {
          ordersMenu = menu( text: 'Ventas', mnemonic: 'V',
              menuSelected: {
                boolean userLoggedIn = Session.contains( SessionItem.USER )
                orderMenuItem.visible = userLoggedIn
                /*orderSearchMenuItem.visible = userLoggedIn
                dailyCloseMenuItem.visible = userLoggedIn
                priceListMenuItem.visible = userLoggedIn
                invoiceMenuItem.visible = userLoggedIn*/
                // TODO: Benja enable feature cotizacionMenuItem.visible = userLoggedIn
              }
          ){
            orderMenuItem = menuItem( text: 'Venta',
                visible: false,
                actionPerformed: {
                  mainPanel.remove( orderPanel )
                  orderPanel = new OrderPanel( this )
                  mainPanel.add( 'orderPanel', orderPanel )
                  mainPanel.layout.show( mainPanel, 'orderPanel' )
                }
            )
              /*orderSearchMenuItem = menuItem( text: 'Consulta',
                  visible: false,
                  actionPerformed: {
                    mainPanel.remove( showOrderPanel )
                    showOrderPanel = new ShowOrderPanel()
                    mainPanel.add( 'showOrderPanel', showOrderPanel )
                    mainPanel.layout.show( mainPanel, 'showOrderPanel' )
                  }
              )
              cotizacionMenuItem = menuItem( text: 'Cotizaciones',
                  visible: false,
                  actionPerformed: { QuoteController.instance.requestQuote() }
              )
              dailyCloseMenuItem = menuItem( text: 'Cierre diario',
                  visible: false,
                  actionPerformed: {
                    dailyClosePanel = new DailyClosePanel()
                    mainPanel.add( 'dailyClosePanel', dailyClosePanel )
                    mainPanel.layout.show( mainPanel, 'dailyClosePanel' )
                  }
              )
              priceListMenuItem = menuItem( text: 'Lista de Precios',
                  visible: false,
                  actionPerformed: {
                    priceListPanel = new PriceListPanel().panel
                    mainPanel.add( 'priceListPanel', priceListPanel )
                    mainPanel.layout.show( mainPanel, 'priceListPanel' )
                  }
              )
              invoiceMenuItem = menuItem( text: 'Facturaci\u00f3n',
                  visible: false,
                  actionPerformed: {
                    invoicePanel = new InvoicePanel()
                    mainPanel.add( 'invoicePanel', invoicePanel )
                    mainPanel.layout.show( mainPanel, 'invoicePanel' )
                  }
              )*/
            }
          customerMenu = menu( text: 'Clientes', mnemonic: 'C',
                  menuSelected: {
                    boolean isAdmin = AccessController.isAdmin(Session.get(SessionItem.USER))
                    boolean userLoggedIn = Session.contains( SessionItem.USER )
                    insertCustomerMenuItem.visible = userLoggedIn
                    searchCustomerMenuItem.visible = userLoggedIn
                    /*orderSearchMenuItem.visible = userLoggedIn
                    dailyCloseMenuItem.visible = userLoggedIn
                    priceListMenuItem.visible = userLoggedIn
                    invoiceMenuItem.visible = userLoggedIn*/
                    // TODO: Benja enable feature cotizacionMenuItem.visible = userLoggedIn
                  }
          ) {
            insertCustomerMenuItem = menuItem(text: 'Insertar Cliente',
                    visible: false,
                    actionPerformed: {
                      NewCustomer dialog = new NewCustomer( null )
                      dialog.show()
                    }
            )
            searchCustomerMenuItem = menuItem(text: 'Buscar Cliente',
                    visible: false,
                    actionPerformed: {

                    }
            )
          }
          inventoryMenu = menu( text: 'Inventario', mnemonic: 'I',
              visible: AccessController.isAdmin(Session.get(SessionItem.USER)),
              menuSelected: {
                /*
                inventoryTransactionMenuItem.visible = userLoggedIn
                inventoryOhQueryMenuItem.visible = userLoggedIn
                generateInventoryFile.visible = userLoggedIn
                loadPartsMenuItem.visible = userLoggedIn
                loadPartClassMenuItem.visible = userLoggedIn
                ticketInventoryTransactionsMenuItem.visible = userLoggedIn*/
                //sendInventoryMenuItem.visible = userLoggedIn
                //receivedDiferencesMenuItem.visible = userLoggedIn
                //sendReceivedInventoryMenu.visible = userLoggedIn
              }
          ){}/* {
            inventoryTransactionMenuItem = menuItem( text: 'Transacciones',
                visible: false,
                actionPerformed: {
                  if ( invTrView == null ) {
                    invTrView = new InvTrView()
                  }
                  mainPanel.add( 'invTrPanel', invTrView.panel )
                  invTrView.activate()
                  mainPanel.layout.show( mainPanel, 'invTrPanel' )
                }
            )
            inventoryOhQueryMenuItem = menuItem( text: "Ticket Existencias", visible: true,
                actionPerformed: { InvQryController.instance.requestInvOhTicket() }
            )
            ticketInventoryTransactionsMenuItem = menuItem( text: "Ticket Transacciones de Inventario", visible: true,
                actionPerformed: { TransactionsDateSelectionDialog dialog = new TransactionsDateSelectionDialog()
                                    dialog.show()
                }
            )
            loadPartsMenuItem = menuItem( text: TEXT_LOAD_PARTS_TITLE,
                visible: true,
                actionPerformed: {
                  requestImportPartMaster()
                }
            )
            loadPartClassMenuItem = menuItem( text: TEXT_LOAD_PART_CLASS_TITLE,
                visible: true,
                actionPerformed: {
                  requestImportPartClass()
                }
            )
            generateInventoryFile = menuItem( text: 'Archivo Inventario',
                visible: false,
                actionPerformed: {
                  generateInventoryFile()
                }
            )
            sendReceivedInventoryMenu = menu(
                    visible: false,
                    text: 'Inventario Fisico'
            ){
                sendInventoryMenuItem = menuItem( text: 'Inicializar',
                        actionPerformed: {
                          Runtime garbage = Runtime.getRuntime();
                          garbage.gc();
                          initializingInventory()
                        }
                )
                receivedDiferencesMenuItem = menuItem( text: 'Cargar Archivo',
                        actionPerformed: {
                          Runtime garbage = Runtime.getRuntime();
                          garbage.gc();
                          diferencesPhysicalInventory()
                        }
                )
                generatedInventoryFileMenuItem = menuItem( text: 'Imprime Diferencias',
                        actionPerformed: {
                          //diferencesPhysicalInventoryPrint()
                          Runtime garbage = Runtime.getRuntime();
                          garbage.gc();
                          receivedDifferencesFile()
                        }
                )
            }
          }*/
          reportsMenu = menu( text: "Reportes", mnemonic: "R",
              menuSelected: {
                /*boolean userLoggedIn = Session.contains( SessionItem.USER )
                cancellationReportMenuItem.visible = userLoggedIn
                dailyCloseReportMenuItem.visible = userLoggedIn
                incomePerBranchReportMenuItem.visible = userLoggedIn
                sellerRevenueReportMenuItem.visible = userLoggedIn */
                //undeliveredJobsReportMenuItem.visible = userLoggedIn
                //salesReportMenuItem.visible = userLoggedIn
                //salesByLineReportMenuItem.visible = userLoggedIn
                /*salesBySellerReportMenuItem.visible = userLoggedIn
                salesByBrandReportMenuItem.visible = userLoggedIn
                salesBySellerByBrandMenuItem.visible = userLoggedIn
                stockbyBrandMenuItem.visible = userLoggedIn
                stockbyBrandColorMenuItem.visible = userLoggedIn*/
                //jobControlMenuItem.visible = userLoggedIn
                //workSubmittedMenuItem.visible = userLoggedIn
                /*taxBillsMenuItem.visible = userLoggedIn
                discountsMenuItem.visible = userLoggedIn
                promotionsMenuItem.visible = userLoggedIn
                promotionsListMenuItem.visible = userLoggedIn
                paymentsMenuItem.visible = userLoggedIn*/
                //quoteMenuItem.visible = userLoggedIn
                /*kardexMenuItem.visible = userLoggedIn
                salesTodayMenuItem.visible = userLoggedIn
                salesByPeriodMenuItem.visible = userLoggedIn
                skuByBrandReportMenuItem.visible = userLoggedIn*/
                //examsMenuItem.visible = userLoggedIn
                //optometristSalesMenuItem.visible = userLoggedIn
              }
          ) {}/*{
            cancellationReportMenuItem = menuItem( text: "Cancelaciones",
                visible: false,
                actionPerformed: {
                  ReportController.fireReport( ReportController.Report.Cancellations )
                }
            )
            dailyCloseReportMenuItem = menuItem( text: "Cierre Diario",
                visible: false,
                actionPerformed: {
                  ReportController.fireReport( ReportController.Report.DailyClose )
                }
            )
            jobControlMenuItem = menuItem( text: "Control de Trabajos",
                visible: false,
                actionPerformed: {
                  ReportController.fireReport( ReportController.Report.JobControl )
                }
            )
            quoteMenuItem = menuItem( text: "Cotizaciones",
                visible: false,
                actionPerformed: {
                  ReportController.fireReport( ReportController.Report.Quote )
                }
            )
            discountsMenuItem = menuItem( text: "Descuentos",
                visible: false,
                actionPerformed: {
                  ReportController.fireReport( ReportController.Report.Discounts )
                }
            )
            stockbyBrandColorMenuItem = menuItem( text: "Existencias por Art\u00edculo",
                visible: false,
                actionPerformed: {
                  ReportController.fireReport( ReportController.Report.StockbyBrandColor )
                }
            )
            stockbyBrandMenuItem = menuItem( text: "Existencias por Marca",
                visible: false,
                actionPerformed: {
                  ReportController.fireReport( ReportController.Report.StockbyBrand )
                }
            )
            taxBillsMenuItem = menuItem( text: "Facturas Fiscales",
                visible: false,
                actionPerformed: {
                  ReportController.fireReport( ReportController.Report.TaxBills )
                }
            )
            salesByPeriodMenuItem = menuItem( text: "Ingresos por Periodo",
                visible: false,
                actionPerformed: {
                  ReportController.fireReport( ReportController.Report.PaymentsbyPeriod )
                }
            )
            incomePerBranchReportMenuItem = menuItem( text: "Ingresos por Sucursal",
                visible: false,
                actionPerformed: {
                  ReportController.fireReport( ReportController.Report.IncomePerBranch )
                }
            )
            sellerRevenueReportMenuItem = menuItem( text: "Ingresos por Vendedor",
                visible: false,
                actionPerformed: {
                  ReportController.fireReport( ReportController.Report.SellerRevenue )
                }
            )
            kardexMenuItem = menuItem( text: "Kardex por Articulo",
                visible: false,
                actionPerformed: {
                  ReportController.fireReport( ReportController.Report.Kardex )
            }
            )
            paymentsMenuItem = menuItem( text: "Pagos",
                visible: false,
                actionPerformed: {
                  ReportController.fireReport( ReportController.Report.Payments )
                }
            )
            promotionsMenuItem = menuItem( text: "Promociones en Ventas",
                visible: false,
                actionPerformed: {
                  ReportController.fireReport( ReportController.Report.PromotionsinSales )
                }
            )
            promotionsListMenuItem = menuItem( text: "Promociones",
                visible: false,
                actionPerformed: {
                  ReportController.fireReport( ReportController.Report.Promotions )
                }
            )
            skuByBrandReportMenuItem = menuItem( text: "Sku Por Marca",
                visible: false,
                actionPerformed: {
                  ReportController.fireReport( ReportController.Report.SkuByBrand )
                }
            )
            salesReportMenuItem = menuItem( text: "Ventas",
                visible: false,
                actionPerformed: {
                  ReportController.fireReport( ReportController.Report.Sales )
                }
            )
            salesTodayMenuItem = menuItem( text: "Ventas del D\u00eda",
            visible: false,
            actionPerformed: {
              ReportController.fireReport( ReportController.Report.SalesToday )
            }
            )
            salesByLineReportMenuItem = menuItem( text: "Ventas por L\u00ednea",
                visible: false,
                actionPerformed: {
                  ReportController.fireReport( ReportController.Report.SalesbyLine )
                }
            )
            salesByBrandReportMenuItem = menuItem( text: "Ventas por Marca",
                visible: false,
                actionPerformed: {
                  ReportController.fireReport( ReportController.Report.SalesbyBrand )
                }
            )
            salesBySellerReportMenuItem = menuItem( text: "Ventas por Vendedor",
                visible: false,
                actionPerformed: {
                  ReportController.fireReport( ReportController.Report.SalesbySeller )
                }
            )
            salesBySellerByBrandMenuItem = menuItem( text: "Ventas por Vendedor por Marca",
                visible: false,
                actionPerformed: {
                  ReportController.fireReport( ReportController.Report.SalesbySellerbyBrand )
                }
            )
          }*/
          toolsMenu = menu( text: 'Herramientas', mnemonic: 'H',
              menuSelected: {
                boolean userLoggedIn = Session.contains( SessionItem.USER )
                sessionMenuItem.visible = userLoggedIn
                /*newSalesDayMenuItem.visible = userLoggedIn
                changePasswordMenuItem.visible = userLoggedIn
                changeSellerMenuItem.visible = userLoggedIn
                generateIn2MenuItem.visible = userLoggedIn
                captureIncidentsMenuItem.visible = userLoggedIn
                correctTransactionsMenuItem.visible = userLoggedIn
                reprintVoucherMenuItem.visible = userLoggedIn
                reprintWarrantyMenuItem.visible = userLoggedIn*/
              }
          ) {
            sessionMenuItem = menuItem( text: 'Cerrar Sesi\u00f3n',
                  visible: false,
                  actionPerformed: {
                    requestLogout()
                  }
            )
            menuItem( menuItem( text: 'Salir',
                  visible: true,
                  actionPerformed: { System.exit( 0 ) } )
            )
          }/*{
            changePasswordMenuItem = menuItem( text: 'Cambio de Password',
                visible: true,
                actionPerformed: {
                    ChangePasswordDialog dialog = new ChangePasswordDialog()
                    dialog.show()
                }
            )
            changeSellerMenuItem = menuItem( text: 'Cambio de Vendedor',
                    actionPerformed: {
                        ChangeSellerDialog dialog = new ChangeSellerDialog()
                        dialog.show()
                    }
            )
            captureIncidentsMenuItem = menuItem( text: 'Captura de Incidencias',
                  actionPerformed: {
                      boolean authorized = false
                      AuthorizationDialog authDialog = new AuthorizationDialog( this, "Operaci\u00f3n requiere autorizaci\u00f3n", true )
                      authDialog.show()
                      authorized = authDialog.authorized
                      if( authorized ){
                        CaptureIncidentsDialog dialog = new CaptureIncidentsDialog()
                        dialog.show()
                      }
                  }
            )
            correctTransactionsMenuItem = menuItem( text: 'Aplicar Transacciones de Inventario',
                  actionPerformed: {
                    boolean authorized = false
                    SupportDialog authDialog = new SupportDialog( this, "Operaci\u00f3n requiere contrase\u00f1a", true )
                    authDialog.show()
                    authorized = authDialog.authorized
                    if( authorized ){
                      List<NotaVenta> lstOrders = new ArrayList<>()
                      List<NotaVenta> lstCanc = new ArrayList<>()
                      lstOrders.addAll( OrderController.lstOrdersWithoutTrans() )
                      lstCanc.addAll( OrderController.lstOrdersCancWithoutTrans() )
                      OrderController.correctionTransactions( false, null )
                      CorrectedOrdersDialog dialog = new CorrectedOrdersDialog( lstOrders, lstCanc )
                      dialog.show()
                    }
                  }
            )
            generateIn2MenuItem = menuItem( text: 'Genera IN2',
                  actionPerformed: {
                    TwoDatesSelectionDialog dialog = new TwoDatesSelectionDialog()
                    dialog.setTitle( 'Genera IN2' )
                    dialog.activate()
                    Date forDateStart = dialog.getSelectedDateStart()
                    Date forDateEnd = dialog.getSelectedDateEnd()
                    if ( forDateStart != null && forDateEnd != null && dialog.button ) {
                      log.debug( "Genera IN2" )
                      InvTrController controller = InvTrController.instance
                      controller.generateIN2( forDateStart, forDateEnd )
                    }
                    dialog = null
                  }
            )
            newSalesDayMenuItem = menuItem( text: 'Registrar Efectivo Caja',
                visible: true,
                actionPerformed: {
                  requestNewSalesDay()
                }
            )
            sessionMenuItem = menuItem( text: 'Cerrar Sesi\u00f3n',
                visible: false,
                actionPerformed: {
                  requestLogout()
                }
            )
            menuItem( action: System.exit( 0 ) )
          }*/
        }
        infoBar = toolBar(
            visible: false,
            floatable: false,
            background: Color.WHITE,
            constraints: 'hidemode 3'
        ) {
          borderLayout()
          userLabel = label( constraints: BorderLayout.LINE_START )
          branchLabel = label( constraints: BorderLayout.CENTER, horizontalAlignment: JLabel.CENTER_ALIGNMENT )
          versionLabel = label( constraints: BorderLayout.LINE_END )
        }

        mainPanel = panel( layout: new CardLayout(), alignmentX: CENTER_ALIGNMENT, alignmentY: CENTER_ALIGNMENT )
        mainPanel.add( 'logInPanel', logInPanel )
      }
    }
  }

  public void keyReleased( KeyEvent e ) {


  }

  public void keyTyped( KeyEvent e ) {
  }

  public void keyPressed( KeyEvent e ) {
    if ( e.isControlDown() && e.getKeyChar() != 'k' && e.getKeyCode() == 75 ) {
      // TODO: Benjamin enable through feature
      // log.debug( "Abriendo dialogo de cotizaciones" )
      // QuoteController.instance.requestQuote()
    }
  }

  private def doForwardToDefaultPanel = {
    orderPanel = new OrderPanel( this )
    mainPanel.add( 'orderPanel', orderPanel )
    mainPanel.layout.show( mainPanel, 'orderPanel' )
    User user = Session.get( SessionItem.USER ) as User
    Branch branch = Session.get( SessionItem.BRANCH ) as Branch
    userLabel.text = "[${StringUtils.trimToEmpty(user?.username.toString()) ?: ''}] ${user?.fullName ?: ''}"
    branchLabel.text = "[${branch?.id ?: ''}] ${branch?.name ?: ''}"
    versionLabel.text = version
    infoBar.visible = true
    //changeSellerMenuItem.enabled = IOController.getInstance().isManagerLogged( )
    //captureIncidentsMenuItem.enabled = IOController.getInstance().isManagerLogged( )
  }

  private void initialize( ) {
    sb.doOutside {
      PriceListController.loadExpiredPriceList()
      if( AccessController.iniciaSesionPrimeraVez() ){
        DailyCloseController.validPendingClosedDays()
      }
      DailyCloseController.openDay()
      //IOController.getInstance().loadAdjustFile()
      IOController.getInstance().loadMessageTicketFile()
      IOController.getInstance().autoUpdateFxRates()
      DailyCloseController.RegistrarPromociones()
      IOController.getInstance().autoUpdateEmployeeFile()
      IOController.getInstance().startAsyncNotifyDispatcher()
      IOController.getInstance().autoUpdateSubtype()
      AccessController.makeIN2()
      //InvTrController controllerInv = InvTrController.instance
      controllerInv.readAdjutFile()
    }
  }

  private void refresh( ){
      buildUI()
  }

  JPanel getMainPanel( ) {
    return mainPanel
  }

  void requestNewSalesDay( ) {
    OpenSalesController.instance.requestNewDay()
  }

  void requestLogout( ) {
    AccessController.logOut()
    infoBar.visible = false
    mainPanel.remove( orderPanel )
    mainPanel.remove( showOrderPanel )
    mainPanel.remove( dailyClosePanel )
    mainPanel.remove( priceListPanel )
    mainPanel.layout.show( mainPanel, 'logInPanel' )
  }

  static void main( args ) {
    ApplicationContext ctx = new ClassPathXmlApplicationContext( "classpath:spring-config.xml" )
    ctx.registerShutdownHook()
    SwingUtilities.invokeLater(
        new Runnable() {
          void run( ) {
            version = Session.getVersion()
            MainWindow window = new MainWindow()
            window.initialize()
            window.refresh()
            window.show()
          }
        }
    )
  }
}
