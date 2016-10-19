package mx.wen.pos.service.impl

import mx.wen.pos.util.CustomDateUtils as MyDateUtils

import com.ibm.icu.text.RuleBasedNumberFormat
import com.mysema.query.BooleanBuilder
import groovy.util.logging.Slf4j
import mx.wen.pos.repository.impl.RepositoryFactory
import mx.wen.pos.service.business.InventorySearch
import mx.wen.pos.service.business.Registry
import mx.wen.pos.util.CustomDateUtils
import mx.wen.pos.util.MoneyUtils
import org.apache.commons.lang.WordUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateFormatUtils
import org.apache.commons.lang3.time.DateUtils
import org.apache.velocity.app.VelocityEngine
import org.springframework.format.number.CurrencyFormatter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.ui.velocity.VelocityEngineUtils

import javax.xml.bind.DatatypeConverter
import java.awt.image.BufferedImage
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import javax.annotation.Resource

import mx.wen.pos.model.*
import mx.wen.pos.repository.*
import mx.wen.pos.service.*
import java.text.DecimalFormat
import org.apache.commons.lang.math.NumberUtils
import java.math.RoundingMode

@Slf4j
@Service( 'ticketService' )
@Transactional( readOnly = true )
class TicketServiceImpl implements TicketService {

  private static final String DATE_FORMAT = 'dd-MM-yyyy'
  private static final String TIME_FORMAT = 'HH:mm:ss'
  private static final String DATE_TIME_FORMAT = 'dd-MM-yyyy HH:mm:ss'
  private static final String TAG_CANCELADO = 'C'
  private static final String TAG_DEVUELTO = 'D'
  private static final Integer LONGITUD_MAXIMA = 70
  private static final String TAG_EFD = 'EFD'
  private static final String TAG_TRANSFER = 'TR'
  private static final String TAG_DEPOSITO_MN = 'EFECTIVO'
  private static final String TAG_DEPOSITO_US = 'DOLARES'
  private static final String TAG_TRANSACCION_VENTA = 'VENTA'
  private static final String TAG_TRANSACCION_ENTRADA = 'E'
  private static final String TAG_TRANSACCION_SALIDA = 'S'
  private static final String TAG_FORMA_PAGO_USD = "EFD";
  private static final String TAG_FORMA_PAGO_TC = "TV";
  private static final String TAG_FORMA_PAGO_TD = "TD";
  private static final String TAG_FORMA_PAGO_TCM = "TCM";
  private static final String TAG_FORMA_PAGO_TDM = "TDM";
  private static final String TAG_FORMA_PAGO_TCD = "UV";
  //private static final String TAG_TRANSACCION_ENTRADA_TIENDA = 'ENTRADA_TIENDA'
  //private static final String TAG_TRANSACCION_SALIDA_TIENDA = 'SALIDA_TIENDA'

  private static final BigDecimal CERO_BIGDECIMAL = 0.005

  @Resource
  private ArticuloRepository articuloRepository

  /*@Resource
  private MonedaExtranjeraService monedaExtranjeraService*/

  @Resource
  private ParametroRepository parametroRepository

  @Resource
  private NotaVentaRepository notaVentaRepository

  /*@Resource
  private DiferenciaRepository diferenciaRepository

  @Resource
  private OrdenPromDetRepository ordenPromDetRepository

  @Resource
  private MensajeTicketRepository mensajeTicketRepository*/

  @Resource
  private NotaVentaService notaVentaService

  @Resource
  private DetalleNotaVentaRepository detalleNotaVentaRepository

  @Resource
  private TransInvRepository transInvRepository

  @Resource
  private TransInvDetalleRepository transInvDetalleRepository

  /*@Resource
  private DevolucionRepository devolucionRepository

  @Resource
  private CierreDiarioRepository cierreDiarioRepository

  @Resource
  private GenericoRepository genericoRepository

  @Resource
  private ResumenDiarioRepository resumenDiarioRepository

  @Resource
  private CotizaDetRepository cotizaDetRepository

  @Resource
  private AperturaRepository aperturaRepository

  @Resource
  private MonedaDetalleRepository monedaDetalleRepository*/

  @Resource
  private PagoRepository pagoRepository

  /*@Resource
  private PromocionRepository promocionRepository

  @Resource
  private PagoExternoRepository pagoExternoRepository

  @Resource
  private DepositoRepository depositoRepository

  @Resource
  private ResumenTerminalRepository resumenTerminalRepository*/

  @Resource
  private EmpleadoRepository empleadoRepository

  /*@Resource
  private EntregadoExternoRepository entregadoExternoRepository

  @Resource
  private ModificacionRepository modificacionRepository*/

  @Resource
  private SucursalRepository sucursalRepository

  /*@Resource
  private DescuentoRepository descuentoRepository

  @Resource
  private ClienteRepository clienteRepository

  @Resource
  private ComprobanteService comprobanteService

  @Resource
  private ContribuyenteService contribuyenteService

  @Resource
  private EstadoService estadoService */

  @Resource
  private VelocityEngine velocityEngine


  private File generaTicket( String template, Map<String, Object> items ) {
    log.info( "generando archivo de ticket con plantilla: ${template}" )
    if ( StringUtils.isNotBlank( template ) && items?.any() ) {
      try {
        String fileName = items.nombre_ticket ?: 'ticket'
        def file = File.createTempFile( fileName, null )
        file.withWriter { BufferedWriter writer ->
          items.writer = writer
          VelocityEngineUtils.mergeTemplate( velocityEngine, template, "ASCII", items, writer )
          true
        }
        log.debug( "archivo generado en: ${file.path}" )
        return file
      } catch ( ex ) {
        log.error( "error al generar archivo de ticket: ${ex.message}", ex )
      }
    } else {
      log.warn( "parametros no validos ${template}/${items}" )
    }
    return null
  }

  private void imprimeTicket( String template, Map<String, Object> items ) {
    File ticket = generaTicket( template, items )
    if ( ticket?.exists() ) {
      try {
        def parametro = parametroRepository.findOne( TipoParametro.IMPRESORA_TICKET.value )
        def cmd = "${parametro?.valor} ${ticket.path}"
        log.info( "ejecuta: ${cmd}" )
        def proc = cmd.execute()
        proc.waitFor()
      } catch ( ex ) {
        log.error( "error durante la ejecucion del comando de impresion: ${ex.message}", ex )
      }
    } else {
      log.warn( "archivo de ticket no generado, no se puede imprimir" )
    }
  }

  @Override
  void imprimeVenta( String idNotaVenta ) {
    imprimeVenta( idNotaVenta, false )
  }

  void imprimeVenta( String idNotaVenta, Boolean pNewOrder ) {
    log.info( "imprimiendo ticket venta de notaVenta id: ${idNotaVenta}" )
    NotaVenta notaVenta = notaVentaService.obtenerNotaVenta( idNotaVenta )
    if ( StringUtils.isNotBlank( notaVenta?.id ) ) {
      NumberFormat formatter = NumberFormat.getCurrencyInstance( Locale.US )
      List<String> lstComentario = new ArrayList<String>()
      String marcasFactura = ''
      String articulosFactura = ''
      String dateTextFormat = "dd 'de' MMMM 'de' yyyy"
      Locale locale = new Locale( 'es' )
      def detalles = [ ]
      List<DetalleNotaVenta> detallesLst = detalleNotaVentaRepository.findByIdFacturaOrderByIdArticuloAsc( idNotaVenta )
      BigDecimal subtotal = BigDecimal.ZERO
      BigDecimal totalArticulos = BigDecimal.ZERO
      detallesLst?.each { DetalleNotaVenta tmp ->
        // TODO: rld review for SOI lux
        // BigDecimal precio = tmp?.precioUnitFinal?.multiply( tmp?.cantidadFac ) ?: 0
        BigDecimal precio = tmp?.precioUnitLista?.multiply( tmp?.cantidadFac ) ?: 0
        subtotal = subtotal.add( precio )
        String descripcion = "[${tmp?.idArticulo}] ${tmp?.articulo?.descripcion}"
        String descripcion1
        String descripcion2 = ""
        if ( descripcion.length() > 36 ) {
          descripcion1 = descripcion.substring( 0, 36 )
          if ( descripcion.length() > 72 ) {
            descripcion2 = descripcion.substring( 37, 72 )
          } else {
            descripcion2 = descripcion.substring( 37 )
          }
        } else {
          descripcion1 = descripcion
        }

        totalArticulos = totalArticulos.add( tmp.cantidadFac )
        marcasFactura = marcasFactura+","+StringUtils.trimToEmpty(tmp.articulo.marca)
        articulosFactura = articulosFactura+","+StringUtils.trimToEmpty(tmp.articulo.id.toString())
        def detalle = [
            cantidad: tmp?.cantidadFac?.toInteger() ?: '',
            codigo: "${tmp?.articulo?.articulo ?: ''}",
            descripcion1: descripcion1,
            descripcion2: descripcion2,
            precio: formatter.format( precio )
        ]
        detalles.add( detalle )
      }
      Boolean pagoUsd = false;
      Boolean tipoPagoUsd = false;
      def pagos = [ ]
      List<Pago> pagosLst = pagoRepository.findByIdFacturaOrderByFechaAsc( idNotaVenta )
      pagosLst?.each { Pago pmt ->
        BigDecimal monto = pmt?.monto ?: 0
        String ref = pmt?.referenciaPago ?: ''
        Integer pos = ( ref.size() >= 4 ) ? ( ref.size() - 4 ) : 0
        String tipoPago
          /*boolean creditoEmp = pmt?.eTipoPago?.equals( Registry.getTipoPagoCreditoEmpleado() )
          if(Registry.tipoPagoDolares.contains(StringUtils.trimToEmpty(pmt.idFPago))){
            tipoPagoUsd = true
          }*/
        /*if ( creditoEmp ) {
          tipoPago = pmt?.eTipoPago?.descripcion
        } else {*/
          tipoPago = "${pmt?.eTipoPago?.descripcion} ${ref.substring( pos )}"
        //}
        if(pmt.idFPago.equalsIgnoreCase(TAG_FORMA_PAGO_USD)){
            pagoUsd = true
        }
        BigDecimal usdAmount = BigDecimal.ZERO
        try{
          if( StringUtils.trimToEmpty(pmt.idPlan) && tipoPagoUsd && pmt.idPlan.isNumber()){
            usdAmount = NumberFormat.getInstance().parse( StringUtils.trimToEmpty(pmt.idPlan) )
          }
        } catch ( NumberFormatException e ) { println e }
        def pago = [
            tipo_pago: tipoPago,
            monto: formatter.format( monto ),
            dolares: tipoPagoUsd ? formatter.format( usdAmount ) : BigDecimal.ZERO
        ]
        pagos.add( pago )

        /*if ( creditoEmp ) {
          lstComentario.add( String.format( "Empleado: %s", pmt?.clave ) )
          lstComentario.add( String.format( "   # Emp: %s", pmt?.idBancoEmisor ) )
        }*/
      }
      BigDecimal ventaNeta = notaVenta.ventaNeta ?: 0
      String empleado = String.format( "%s [%d]", notaVenta.empleado.nombreCompleto, notaVenta?.empleado?.id )
      RuleBasedNumberFormat textFormatter = new RuleBasedNumberFormat( locale, RuleBasedNumberFormat.SPELLOUT )
      String textoVentaNeta = ( "${textFormatter.format( ventaNeta.intValue() )} PESOS "
          + "${ventaNeta.remainder( 1 ).unscaledValue()}/100 M.N." )
      //AddressAdapter companyAddress = Registry.companyAddress

      List<String> promociones = new ArrayList<>()
      //List<OrdenPromDet> lstPromociones = ordenPromDetRepository.findByIdFactura( notaVenta.id )
      List<String> msjPromo = new ArrayList<>()
      /*QMensajeTicket mensaje = QMensajeTicket.mensajeTicket
      List<MensajeTicket> lstMensajesTickets = (List<MensajeTicket>)mensajeTicketRepository.findAll( mensaje.fechaFinal.after( new Date() ) )
      for(MensajeTicket msj : lstMensajesTickets){
          Boolean alreadyAdd = false
          Boolean hasBrand = false
          Boolean hasArticle = false
          if( (!StringUtils.trimToEmpty(msj.idLinea).equalsIgnoreCase('') && !StringUtils.trimToEmpty(msj.idLinea).equalsIgnoreCase('*'))||
                  (!StringUtils.trimToEmpty(msj.listaArticulo).equalsIgnoreCase('') && !StringUtils.trimToEmpty(msj.listaArticulo).equalsIgnoreCase('*')) ){
              if( StringUtils.trimToEmpty(msj.idLinea) != '' ){
                hasBrand = true
              }
              if( StringUtils.trimToEmpty(msj.listaArticulo) != '' ){
                hasArticle = true
              }
              if( hasBrand ){
                Boolean validBrand = false
                  String[] marcas = marcasFactura.split(',')
                  for(String marca : marcas){
                      if(StringUtils.trimToEmpty(marca) != '' && msj.idLinea.contains(marca)){
                        validBrand = true
                      }
                  }
                if( validBrand ){
                  if( hasArticle ){
                    String[] articulos = articulosFactura.split(',')
                    for(String art : articulos){
                      if(!alreadyAdd && StringUtils.trimToEmpty(art) != '' && msj.listaArticulo.contains(art)){
                        alreadyAdd = true
                        msjPromo.add(msj.mensaje)
                      }
                    }
                  } else {
                    if(!alreadyAdd){
                      alreadyAdd = true
                      msjPromo.add(msj.mensaje)
                    }
                  }
                }
              } else if( hasArticle ){
                String[] articulos = articulosFactura.split(',')
                for(String art : articulos){
                  if(!alreadyAdd && StringUtils.trimToEmpty(art) != '' && msj.listaArticulo.contains(art)){
                    alreadyAdd = true
                    msjPromo.add(msj.mensaje)
                  }
                }
              }
          } else if( (StringUtils.trimToEmpty(msj.idLinea).equalsIgnoreCase('') || StringUtils.trimToEmpty(msj.idLinea).equalsIgnoreCase('*'))&&
                  (StringUtils.trimToEmpty(msj.listaArticulo).equalsIgnoreCase('') || StringUtils.trimToEmpty(msj.listaArticulo).equalsIgnoreCase('*')) ){
            msjPromo.add(msj.mensaje)
          }
      }

      for(OrdenPromDet promo : lstPromociones){
          Promocion promocion = promocionRepository.findOne( promo.idPromocion )
          if(promocion != null){
            String data = '['+promocion.idPromocion.toString()+']'+', '+promocion.descripcion
            promociones.add( data )
          }
      }

      MonedaDetalle tipoCambio = monedaExtranjeraService.findActiveRate( "USD", notaVenta.fechaHoraFactura )*/
      DateFormat dfTime = new SimpleDateFormat( "HH:mm" )
      DateFormat df = new SimpleDateFormat( "dd/MM/yyyy" )

      def items = [
          nombre_ticket: 'ticket-venta',
          nota_venta: notaVenta,
          //compania: companyAddress,
          //despliega_atencion_a_clientes: companyAddress.hasCustomerService(),
          venta_neta: formatter.format( ventaNeta ),
          subtotal: formatter.format( subtotal ),
          descuento: formatter.format( subtotal.subtract( ventaNeta ) ),
          detalles: detalles,
          pagos: pagos,
          pagoUsd: pagoUsd,
          tipoPagoUsd: tipoPagoUsd,
          articulos: totalArticulos,
          cliente: notaVenta.cliente,
          empleado: empleado,
          sucursal: notaVenta.sucursal,
          fecha: DateFormatUtils.format( notaVenta.fechaHoraFactura, dateTextFormat, locale ),
          hora: dfTime.format(notaVenta.getFechaHoraFactura()),
          texto_venta_neta: textoVentaNeta.toUpperCase(),
          fecha_entrega: df.format(notaVenta.getFechaHoraFactura()),
          comentarios: lstComentario,
          promociones: promociones,
          existPromo: promociones.size() > 0 ? 'existe' : '',
          mensajesPromo: msjPromo,
          formaPago: pagoUsd,
          //tipoCambio: String.format( "%,.2f", tipoCambio.tipoCambio )
      ] as Map<String, Object>
      imprimeTicket( 'template/ticket-venta-si.vm', items )
      /*if ( Registry.isReceiptDuplicate() && pNewOrder ) {
        imprimeTicket( 'template/ticket-venta-si.vm', items )
      }*/
    } else {
      log.warn( 'no se imprime ticket venta, parametros invalidos' )
    }
  }

  /*@Override
  boolean imprimeCierreTerminales( Date fechaCierre, List<ResumenDiario> resumenesDiario, Empleado empleado, String terminal ) {
    boolean terminalEmpty = false
    final String CREDIT = 'N'
    final List<String> FX_CARD = ['TCD', 'TDD']
    final List<String> RETURN_LIST = [ 'C', 'D' ]
    final String CREDIT_TAG = 'Cred'
    final String DEBIT_TAG = 'Debito'
    final String FX_TAG = 'USD'
    final String TITLE_USD_TAG = 'Cantidad'
    final String TITLE_MN_TAG = 'Plan'
    NumberFormat formatterMoney = new DecimalFormat( '#,##0.00' )
    List<CierreTerminales> resumenTerminales = new ArrayList<CierreTerminales>()
    if ( terminal.equalsIgnoreCase( 'TODAS' ) ) {
      String tituloPlan
      if ( resumenesDiario.size() > 0 ) {
        for ( ResumenDiario resumen : resumenesDiario ) {
          CierreTerminales terminales = findorCreate( resumenTerminales, resumen.idTerminal )
          terminales.AcumulaTerminales( resumen )
        }
        for ( CierreTerminales cierre : resumenTerminales ) {
          BigDecimal total = 0
          BigDecimal totalDolares = BigDecimal.ZERO
          cierre.detTerminales.each { resumenDiario ->
            if ( resumenDiario.plan?.equals( 'C' ) || resumenDiario.plan?.equals( 'D' ) ) {
              total = total - resumenDiario.importe
            } else {
              total = total + resumenDiario.importe
            }
          }
          def subtotales = [ ]
          for ( ResumenDiario rd : cierre.detTerminales ) {
            String tipo = StringUtils.trimToEmpty(rd.tipo).toUpperCase()
            String rdPlan = StringUtils.trimToEmpty(rd.plan).toUpperCase()
            String plan
            if( tipo.length() > 0 && Registry.isCardPaymentInDollars(tipo)){
              Date fechaStart = DateUtils.truncate( rd.fechaCierre, Calendar.DAY_OF_MONTH )
              Date fechaEnd = new Date( DateUtils.ceiling( rd.fechaCierre, Calendar.DAY_OF_MONTH ).getTime() - 1 )
              BigDecimal dolaresCan = BigDecimal.ZERO
              QPago pay = QPago.pago
              List<Pago> termCancel = pagoRepository.findAll( pay.fecha.between(fechaStart,fechaEnd).and(pay.idFPago.equalsIgnoreCase(rd.tipo)).
                      and(pay.idTerminal.equalsIgnoreCase(rd.terminal.id.trim())).and(pay.notaVenta.sFactura.equalsIgnoreCase('T')) )
              for(Pago pago : termCancel){
                try{
                  BigDecimal monto = new BigDecimal(NumberFormat.getInstance().parse( pago.idPlan ).doubleValue())
                  dolaresCan = dolaresCan.add( monto )
                } catch( NumberFormatException e ){}
              }
              plan = String.format( '%s %s', rdPlan, FX_TAG )
              tituloPlan = TITLE_USD_TAG
              totalDolares = totalDolares.add( NumberFormat.getInstance().parse(rd.plan) )
              totalDolares = totalDolares.subtract( dolaresCan )
            } else {
              plan = rdPlan
              tituloPlan = TITLE_MN_TAG
            }
            if( plan.equalsIgnoreCase('')){
              plan = DEBIT_TAG
            }
            String monto = String.format( '%,.2f', rd.importe )
            def sub = [
                term: cierre.idTerminal,
                plan: plan,
                tknum: String.format( '%3s', String.format( '%d', rd.facturas) ),
                rctnum: String.format( '%5s', String.format( '%d', rd.vouchers) ),
                monto: String.format( '%10s', monto )
            ]
            subtotales.add( sub )
          }
          Boolean dolaresValidos = false
          if(totalDolares.compareTo(BigDecimal.ZERO) < 0 || totalDolares.compareTo(BigDecimal.ZERO) > 0 ){
            dolaresValidos = true
          }
          CurrencyFormatter formatter = new CurrencyFormatter()
          def datos = [ nombre_ticket: 'ticket-cierre-terminal',
              fechaCierre: CustomDateUtils.format( fechaCierre, 'dd-MM-yyyy' ),
              terminal: cierre.idTerminal,
              detalle: subtotales,
              titulo: tituloPlan,
              totalDolares: dolaresValidos ? formatterMoney.format( totalDolares ) : '',
              total: formatter.print( total, Locale.getDefault() ),
              thisSite: String.format( '%s [%d]', empleado.sucursal.nombre, empleado.sucursal.id ),
              empleado: empleado.nombreCompleto() ]
          imprimeTicket( 'template/ticket-cierre-terminal.vm', datos )
        }
      } else {
        terminalEmpty = true
      }
    } else {
      String tituloPlan
      BigDecimal totalDolares = BigDecimal.ZERO
      BigDecimal total = 0
      resumenesDiario.each { resumenDiario ->
        if ( resumenDiario.plan?.equals( 'C' ) || resumenDiario.plan?.equals( 'D' ) ) {
          total = total - resumenDiario.importe
        } else {
          total = total + resumenDiario.importe
        }
      }
      def subtotales = [ ]
      for ( ResumenDiario rd : resumenesDiario ) {
        String tipo = StringUtils.trimToEmpty(rd.tipo).toUpperCase()
        String rdPlan = StringUtils.trimToEmpty(rd.plan).toUpperCase()
        String plan
        if( tipo.length() > 0 && Registry.isCardPaymentInDollars(tipo)){
          plan = String.format( '%s %s', rdPlan, FX_TAG )
          tituloPlan = TITLE_USD_TAG
          totalDolares = totalDolares.add( NumberFormat.getInstance().parse(rd.plan) )
        } else {
          plan = rdPlan
          tituloPlan = TITLE_MN_TAG
        }
        if( plan.equalsIgnoreCase('')){
          plan = DEBIT_TAG
        }
        String monto = String.format( '%,.2f', rd.importe )
        def sub = [
            term: terminal,
            plan: plan,
            tknum: String.format( '%3s', String.format( '%d', rd.facturas) ),
            rctnum: String.format( '%5s', String.format( '%d', rd.vouchers) ),
            monto: String.format( '%10s', monto )
        ]
        subtotales.add( sub )
      }
      Boolean dolaresValidos = false
      if(totalDolares.compareTo(BigDecimal.ZERO) < 0 || totalDolares.compareTo(BigDecimal.ZERO) > 0 ){
        dolaresValidos = true
      }

      Collections.sort( subtotales, new Comparator() {
          @Override
          int compare(Object o1, Object o2) {
              return 0
          }
      } )
      CurrencyFormatter formatter = new CurrencyFormatter()
      def datos = [ nombre_ticket: 'ticket-cierre-terminal',
          fechaCierre: CustomDateUtils.format( fechaCierre, 'dd-MM-yyyy' ),
          terminal: terminal,
          detalle: subtotales,
          titulo: tituloPlan,
          totalDolares: dolaresValidos ? formatterMoney.format( totalDolares ) : '',
          total: formatter.print( total, Locale.getDefault() ),
          thisSite: String.format( '%s [%d]', empleado.sucursal.nombre, empleado.sucursal.id ),
          empleado: empleado.nombreCompleto() ]
      imprimeTicket( 'template/ticket-cierre-terminal.vm', datos )
    }
    return terminalEmpty
  }


  private CierreTerminales findorCreate( List<CierreTerminales> lstTerminales, String idTerminales ) {
    CierreTerminales found = null
    for ( CierreTerminales res : lstTerminales ) {
      if ( res.idTerminal.equals( idTerminales ) ) {
        found = res
        break
      }
    }
    if ( found == null ) {
      found = new CierreTerminales( idTerminales )
      lstTerminales.add( found )
    }
    return found
  }

  @Override
  void imprimeResumenDiario( Date fechaCierre, Empleado empleado ) {

    NumberFormat formatter = new DecimalFormat( '#,##0.00' )
    Date fechaInicio = DateUtils.addDays( fechaCierre, -1 )
    Date fechaFin = DateUtils.addDays( fechaCierre, 1 )
    Date fechaStart = DateUtils.truncate( fechaCierre, Calendar.DAY_OF_MONTH )
    Date fechaEnd = new Date( DateUtils.ceiling( fechaCierre, Calendar.DAY_OF_MONTH ).getTime() - 1 )
    CierreDiario cierreDiario = cierreDiarioRepository.findOne( fechaCierre )

    if ( cierreDiario != null ) {
      QNotaVenta nv = QNotaVenta.notaVenta
      List<NotaVenta> notasVenta = notaVentaRepository.findAll(nv.fechaHoraFactura.between(fechaStart, fechaEnd).
          and(nv.factura.isNotEmpty()).and(nv.factura.isNotNull())) as List<NotaVenta>
      Parametro parametro = parametroRepository.findOne( TipoParametro.CONV_NOMINA.value )
      String[] valores = parametro?.valor?.split( ',' )
      notasVenta = notasVenta.findAll { notaVenta -> !valores.contains( notaVenta.idConvenio ) }

      QPago payment = QPago.pago
      List<Pago> pagos = pagoRepository.findAll(payment.fecha.between(fechaStart,fechaEnd).
          and(payment.notaVenta.factura.isNotEmpty()).and(payment.notaVenta.factura.isNotNull()))
      List<Pago> pagosDolares = new ArrayList<Pago>()
      for (Pago p : pagos) {
        if ( TAG_EFD.equalsIgnoreCase(p.idFormaPago) && !TAG_TRANSFER.equalsIgnoreCase(p.idFPago)) {
          pagosDolares.add( p )
        }
      }

      BigDecimal dolaresPesos = BigDecimal.ZERO
      pagosDolares.each { pago -> dolaresPesos = dolaresPesos + MoneyUtils.parseNumber( pago.idPlan ) }

      List<Deposito> depositosTmp = depositoRepository.findBy_Fecha( fechaCierre )
      def depositos = []
      BigDecimal totalDepositosMN = BigDecimal.ZERO
      BigDecimal totalDepositosUS = BigDecimal.ZERO
      for(Deposito deposito : depositosTmp){
        if( TAG_DEPOSITO_MN.equalsIgnoreCase(deposito.tipoDeposito) ){
          totalDepositosMN = totalDepositosMN + deposito.monto
        } else if( TAG_DEPOSITO_US.equalsIgnoreCase(deposito.tipoDeposito) ){
          totalDepositosUS = totalDepositosUS + deposito.monto
        }
          //if( deposito.empleado == null ){
        def dep = [
                fechaCierre: deposito.fechaCierre,
                monto: String.format('%10s', formatter.format( deposito.monto ) ),
                tipoDeposito: deposito.tipoDeposito,
        ]
        //deposito.empleado = new Empleado()
          //}
        //deposito.empleado.nombre = String.format('%10s', formatter.format( deposito.monto ) )
        depositos.add(deposito)
      }
      depositosTmp.each { deposito ->
        if( TAG_DEPOSITO_MN.equalsIgnoreCase(deposito.tipoDeposito) ){
          totalDepositosMN = totalDepositosMN + deposito.monto
        } else if( TAG_DEPOSITO_US.equalsIgnoreCase(deposito.tipoDeposito) ){
          totalDepositosUS = totalDepositosUS + deposito.monto
        }
        //if( deposito.empleado == null ){
          deposito.empleado = new Empleado()
        //}
        deposito.empleado.nombre = String.format('%10s', formatter.format( deposito.monto ) )
      }
      BigDecimal efectivoNetoMN = cierreDiario.efectivoRecibido + cierreDiario.efectivoExternos - cierreDiario.efectivoDevoluciones
      BigDecimal efectivoNetoUS = dolaresPesos
      BigDecimal diferenciaEfectivoMN = totalDepositosMN - efectivoNetoMN
      BigDecimal diferenciaEfectivoUS = totalDepositosUS - efectivoNetoUS

      List<ResumenDiario> resumenesDiario = resumenDiarioRepository.findByFechaCierre( fechaCierre )
      List<ResumenDiario> resumenesDiario = new ArrayList<>()

      for(ResumenDiario res : resumenesDiarioTmp){
        if(StringUtils.trimToEmpty(res.tipo).length() > 0 && StringUtils.trimToEmpty(res.idTerminal).length() > 0){
          resumenesDiario.add( res )
        }
      }

      List<ResumenDiario> resumenTerminales = new ArrayList<ResumenDiario>()
      List<ResumenDiario> resumenTerminalesTpv = new ArrayList<ResumenDiario>()
      ResumenDiario resumenTerminaleTcmTpv = new ResumenDiario()
      resumenTerminaleTcmTpv.importe = BigDecimal.ZERO
      resumenTerminaleTcmTpv.formaPago = new FormaPago()
      ResumenDiario resumenTerminaleTdmTpv = new ResumenDiario()
      resumenTerminaleTdmTpv.importe = BigDecimal.ZERO
      resumenTerminaleTdmTpv.formaPago = new FormaPago()
      ResumenDiario resumenTerminaleTcdTpv = new ResumenDiario()
      resumenTerminaleTcdTpv.importe = BigDecimal.ZERO
      resumenTerminaleTcdTpv.formaPago = new FormaPago()
      Double montoTcdTpv = 0.00

        ResumenDiario resumenTerminaleTcmTpvCan = new ResumenDiario()
        resumenTerminaleTcmTpvCan.importe = BigDecimal.ZERO
        resumenTerminaleTcmTpvCan.formaPago = new FormaPago()
        ResumenDiario resumenTerminaleTcmTpvDev = new ResumenDiario()
        resumenTerminaleTcmTpvDev.importe = BigDecimal.ZERO
        resumenTerminaleTcmTpvDev.formaPago = new FormaPago()
        ResumenDiario resumenTerminaleTdmTpvCan = new ResumenDiario()
        resumenTerminaleTdmTpvCan.importe = BigDecimal.ZERO
        resumenTerminaleTdmTpvCan.formaPago = new FormaPago()
        ResumenDiario resumenTerminaleTcdTpvCan = new ResumenDiario()
        resumenTerminaleTcdTpvCan.importe = BigDecimal.ZERO
        resumenTerminaleTcdTpvCan.formaPago = new FormaPago()
        Double montoTcdTpvCan = 0.00
      List<ResumenDiario> resumenTerminalesTpvCan = new ArrayList<ResumenDiario>()
      String terminal

      if ( resumenesDiario.size() == 1 ) {
        ResumenDiario resumen = new ResumenDiario()
        resumen = resumenesDiario.first()
        resumen.plan = '0'
        BigDecimal montoDolares = BigDecimal.ZERO
        if( Registry.isCardPaymentInDollars(resumen.tipo) && resumen.plan.isNumber() ){
          montoDolares = montoDolares.add( NumberFormat.getInstance().parse( resumen.plan ) )
          if( montoDolares.compareTo(BigDecimal.ZERO) == 1 || montoDolares.compareTo(BigDecimal.ZERO) == -1 ){
            resumen.plan = formatter.format( montoDolares.doubleValue() )
          } else {
            resumen.plan = '0'
          }
        }
        resumen.formaPago = new FormaPago()
        //resumen.formaPago.descripcion = String.format('%10s', formatter.format( resumen.importe ) )
        if( resumen.importe.compareTo(BigDecimal.ZERO) > 0 ){
          if( StringUtils.trimToEmpty(resumen.idTerminal).length() <= 0 ){
            resumenTerminalesTpv.add( resumen )
          } else {
            resumenTerminales.add( resumen )
          }
        }
      } else {
        Collections.sort( resumenesDiario )
        ResumenDiario current = null
        BigDecimal montoDolares
        Collections.sort(resumenesDiario, new Comparator<ResumenDiario>() {
            @Override
            int compare(ResumenDiario o1, ResumenDiario o2) {
                String thisIdTerm = o1?.idTerminal != null ? StringUtils.trimToEmpty(o1?.idTerminal) : "";
                return thisIdTerm.compareTo(o2?.idTerminal != null ? o2?.idTerminal : "")
            }
        })
        for ( ResumenDiario resumen : resumenesDiario ) {
          if ( ( current == null ) || ( !current.idTerminal.equalsIgnoreCase( resumen.idTerminal ) ) ) {
            current = new ResumenDiario()
            current.idTerminal = resumen.idTerminal.toUpperCase()
            current.importe = BigDecimal.ZERO
            //current.plan = resumen.plan
            montoDolares = BigDecimal.ZERO
          }
          if ( resumen.plan?.equals( TAG_CANCELADO ) || resumen.plan?.equals( TAG_DEVUELTO ) ) {
            current.importe = current.importe.subtract( resumen.importe )
            current.plan = '0'
            if( Registry.isCardPaymentInDollars(resumen.tipo) && resumen.plan.isNumber() ){
              montoDolares = montoDolares.subtract( NumberFormat.getInstance().parse( resumen.plan ) )
              if( montoDolares.compareTo(BigDecimal.ZERO) == 1 || montoDolares.compareTo(BigDecimal.ZERO) == -1 ){
                current.plan = formatter.format( montoDolares.doubleValue() )
              } else {
                current.plan = '0'
              }
            }
            if( StringUtils.trimToEmpty(current.idTerminal).length() <= 0 ){

            }
          } else {
              current.importe = current.importe.add( resumen.importe )
              current.plan = '0'
            if( Registry.isCardPaymentInDollars(resumen.tipo) && resumen.plan.isNumber() ){
                try{
                montoDolares = montoDolares.add( NumberFormat.getInstance().parse( resumen.plan ) )
                } catch (Exception e){}
                if( montoDolares.compareTo(BigDecimal.ZERO) == 1 || montoDolares.compareTo(BigDecimal.ZERO) == -1 ){
                  current.plan = formatter.format( montoDolares.doubleValue() )
                } else {
                  current.plan = '0'
                }
              }
          }
          if( StringUtils.trimToEmpty(current.idTerminal).length() <= 0 ){

          } else {
            //resumenTerminales.add( current )
            findOrCreateRD( resumenTerminales, current )
          }
          current.formaPago = new FormaPago()
          //current.formaPago.descripcion = String.format('%10s', formatter.format( current.importe ) )
          if( current.importe.compareTo(BigDecimal.ZERO) == 0 ){
            if( StringUtils.trimToEmpty(current.idTerminal).length() <= 0 ){

            } else {
              resumenTerminales.remove( current )
            }
          }
          if(StringUtils.trimToEmpty(resumen.idTerminal).length() <= 0){
            if(StringUtils.trimToEmpty(resumen.tipo).equalsIgnoreCase("TV") ){
              resumenTerminaleTcmTpv.idTerminal = resumen.tipo
              resumenTerminaleTcmTpv.plan = '0'
              resumenTerminaleTcmTpv.importe = resumenTerminaleTcmTpv.importe.add(resumen.importe)
              resumenTerminaleTcmTpv.formaPago.descripcion = String.format('%10s', formatter.format( resumenTerminaleTcmTpv.importe ))
            } else if(StringUtils.trimToEmpty(resumen.tipo).equalsIgnoreCase("DV")){
              resumenTerminaleTdmTpv.idTerminal = resumen.tipo
              resumenTerminaleTdmTpv.plan = '0'
              resumenTerminaleTdmTpv.importe = resumenTerminaleTdmTpv.importe.add(resumen.importe)
              resumenTerminaleTdmTpv.formaPago.descripcion = String.format('%10s', formatter.format( resumenTerminaleTdmTpv.importe ))
            } else if(StringUtils.trimToEmpty(resumen.tipo).equalsIgnoreCase("UV")){
              resumenTerminaleTcdTpv.idTerminal = resumen.tipo
                if( StringUtils.trimToEmpty(resumen.plan).length() > 0 &&
                        resumen.plan.isNumber() ){
                  try{
                    montoTcdTpv = montoTcdTpv+NumberFormat.getInstance().parse( resumen.plan ).doubleValue()
                  } catch ( NumberFormatException e ){ println e }
                }
                resumenTerminaleTcdTpv.plan = String.format('%10s', formatter.format( montoTcdTpv ))
                resumenTerminaleTcdTpv.importe = resumenTerminaleTcdTpv.importe.add(resumen.importe)
                resumenTerminaleTcdTpv.formaPago.descripcion = String.format('%10s', formatter.format( resumenTerminaleTcdTpv.importe ))

            } else if(StringUtils.trimToEmpty(resumen.tipo).equalsIgnoreCase("") &&
                    (StringUtils.trimToEmpty(resumen.plan).equalsIgnoreCase("C"))){
              resumenTerminaleTcmTpvCan.idTerminal = resumen.plan
              resumenTerminaleTcmTpvCan.plan = '0'
              resumenTerminaleTcmTpvCan.importe = resumenTerminaleTcmTpvCan.importe.subtract(resumen.importe)
              resumenTerminaleTcmTpvCan.formaPago.descripcion = String.format('%10s', formatter.format( resumenTerminaleTcmTpvCan.importe ))
            } else if(StringUtils.trimToEmpty(resumen.tipo).equalsIgnoreCase("") &&
                    StringUtils.trimToEmpty(resumen.plan).equalsIgnoreCase("D")){
                resumenTerminaleTcmTpvDev.idTerminal = resumen.plan
                resumenTerminaleTcmTpvDev.plan = '0'
                resumenTerminaleTcmTpvDev.importe = resumenTerminaleTcmTpvDev.importe.subtract(resumen.importe)
                resumenTerminaleTcmTpvDev.formaPago.descripcion = String.format('%10s', formatter.format( resumenTerminaleTcmTpvDev.importe ))
            }
          }
        }
      }
      for(ResumenDiario res : resumenTerminales){
        res.formaPago.descripcion = String.format('%10s', formatter.format( res.importe ) )
      }
      if( StringUtils.trimToEmpty(resumenTerminaleTcmTpv.idTerminal).length() > 0 ){
        resumenTerminalesTpv.add(resumenTerminaleTcmTpv)
      }
      if( StringUtils.trimToEmpty(resumenTerminaleTdmTpv.idTerminal).length() > 0 ){
        resumenTerminalesTpv.add(resumenTerminaleTdmTpv)
      }
      if( StringUtils.trimToEmpty(resumenTerminaleTcdTpv.idTerminal).length() > 0 ){
        resumenTerminalesTpv.add(resumenTerminaleTcdTpv)
      }
      if( resumenTerminaleTcmTpvCan.importe.compareTo(BigDecimal.ZERO) > 0 ||
              resumenTerminaleTcmTpvCan.importe.compareTo(BigDecimal.ZERO) < 0){
        resumenTerminalesTpvCan.add(resumenTerminaleTcmTpvCan)
      }
      if( resumenTerminaleTcmTpvDev.importe.compareTo(BigDecimal.ZERO) > 0 ){
        resumenTerminalesTpvCan.add(resumenTerminaleTcmTpvDev)
      }
      List<Pago> vales = pagoRepository.findBy_Fecha( fechaCierre )
      for( Pago pago : vales ){
        pago.referenciaPago = formatter.format( pago.monto )
      }
      vales = vales.findAll {
        NotaVenta tmp = notasVenta.find { notaVenta ->
          it.idFactura = notaVenta.id
        }
        tmp != null && it.idSync != '2' && it.idFormaPago == 'VA'
      }
      BigDecimal montoVales = BigDecimal.ZERO
      vales.each { vale -> montoVales = montoVales + vale.monto }

      QNotaVenta notaVenta = QNotaVenta.notaVenta
      List<NotaVenta> comprobantes = notaVentaRepository.findAll( notaVenta.fechaHoraFactura.between(fechaCierre,fechaFin).and(notaVenta.factura.isNotEmpty()).
          and(notaVenta.factura.isNotNull()) )

      List<PagoExterno> pagosExternos = pagoExternoRepository.findByFechaGreaterThanAndFechaLessThanAndFormaPago_aceptaEnPagos( fechaCierre, fechaFin, true )
      for( PagoExterno pagoExterno : pagosExternos ){
        pagoExterno.referencia = formatter.format( pagoExterno.monto )
      }
      BigDecimal totalPagosExternos = BigDecimal.ZERO
      pagosExternos.each { pagoExterno -> totalPagosExternos = totalPagosExternos + pagoExterno.monto }

      Parametro parametroGerente = parametroRepository.findOne( TipoParametro.ID_GERENTE.value )
      Empleado gerente = new Empleado()
      if( parametroGerente.valor.contains(',') ){
        String[] ids = parametroGerente.valor.split(',')
        if( ids.length > 0 ){
          gerente = empleadoRepository.findById( StringUtils.trimToEmpty(ids[0]) )
        }
      } else {
        gerente = empleadoRepository.findById( parametroGerente.valor )
      }

      List<EntregadoExterno> entregadosExternos = entregadoExternoRepository.findByFechaGreaterThanAndFechaLessThan( fechaInicio, fechaFin )
      log.debug( "Entregados externos ${ entregadosExternos.size() }" )
      Map<EntregadoExterno> entregadosExternosTmp = new HashMap<EntregadoExterno>()
      entregadosExternos.each { entregadoExterno ->
        if ( entregadosExternosTmp.containsKey( entregadoExterno.idFactura ) ) {
          EntregadoExterno tmp = entregadosExternosTmp.get( entregadoExterno.idFactura ) as EntregadoExterno
          tmp.idFactura = formatter.format( tmp.pago + entregadoExterno.pago )
          entregadosExternosTmp.put( entregadoExterno.idFactura, tmp )
        } else {
          entregadosExternosTmp.put( entregadoExterno.idFactura, entregadoExterno )
        }
      }
      Parametro parametroIva = parametroRepository.findOne( TipoParametro.IVA_VIGENTE.value )
      Integer ivaPorcentaje = NumberFormat.getInstance().parse( parametroIva.valor ).intValue()
      Double ivaVigente = 1+(ivaPorcentaje/100)

      Double ventaBruta = cierreDiario.ventaBruta.doubleValue()/ivaVigente
      Double cancelaciones = cierreDiario.cancelaciones.doubleValue()/ivaVigente
      Double ventaNeta = cierreDiario.ventaNeta
      Double ventaNetaSinIva = cierreDiario.ventaNeta.doubleValue()/ivaVigente
      Double montoTotalIva = cierreDiario.ventaNeta.subtract( ventaNetaSinIva )

      List<Articulo> lstArticulos = articuloRepository.findByCantExistenciaLessThan( 0 )
      for( Articulo articulo : lstArticulos ) {
        articulo.descripcion = String.format('%10s', formatter.format( articulo.precio ) )
      }


      BigDecimal descuento = cierreDiario.modificaciones.doubleValue()/ivaVigente

      MonedaDetalle tipoCambioUsd = monedaExtranjeraService.findActiveRate( 'USD' )
      MonedaDetalle tipoCambioEur = monedaExtranjeraService.findActiveRate( 'EUR' )
      Integer cantidadVentaNeta = cierreDiario.cantidadVentas - cierreDiario.cantidadCancelaciones

      BigDecimal desc = BigDecimal.ZERO
      Integer cantDesc = 0
      QNotaVenta notaV = QNotaVenta.notaVenta
      List<NotaVenta> lstNotasActivas = notaVentaRepository.findAll(notaV.fechaHoraFactura.between(fechaStart,fechaEnd).
          and(notaV.factura.isNotEmpty()).and(notaV.factura.isNotNull()).and(notaV.sFactura.ne('T')))

      for( NotaVenta notas : lstNotasActivas ){
        List<OrdenPromDet> lstOrdenPromDet = ordenPromDetRepository.findByIdFactura( notas.id )
        for( OrdenPromDet promo : lstOrdenPromDet ){
          desc = desc.add( promo.descuentoMonto )
          cantDesc = cantDesc+1
        }
        if( notas.montoDescuento.abs().compareTo(CERO_BIGDECIMAL) > 0 ){
          desc = desc.add( notas.montoDescuento )
          cantDesc = cantDesc+1
        }
      }
      desc = desc.div(ivaVigente)

      def retornos = []
      QTransInv trans = QTransInv.transInv
      List<TransInv> lstRetornos = transInvRepository.findAll(trans.fecha.between(fechaStart,fechaEnd).and(trans.idTipoTrans.eq('RETORNO')))
      for(TransInv transaccion : lstRetornos){
        QTransInvDetalle transDet = QTransInvDetalle.transInvDetalle
        List<TransInvDetalle> lstTransDet = transInvDetalleRepository.findAll( transDet.idTipoTrans.eq(transaccion.idTipoTrans).
            and(transDet.folio.eq(transaccion.folio) ))
        String [] referencia = transaccion.referencia.split(/\|/)
        def transTmp = [
            folio: transaccion.folio,
            importe: referencia[1].trim(),
            ticket: referencia[0],
            detalles: lstTransDet
        ]
        retornos.add( transTmp );
      }

      QPago pay = QPago.pago
      def notasCredito = []
      List<Pago> lstNotasCredito = pagoRepository.findAll( pay.fecha.between(fechaStart,fechaEnd).and(pay.idFPago.eq('NOT')).
          and(pay.notaVenta.factura.isNotEmpty()).and(pay.notaVenta.factura.isNotNull()))
      for(Pago pago: lstNotasCredito){
        String monto = formatter.format( pago.monto )
        def notaCreditoTmp = [
            factura: pago.notaVenta.factura,
            clave: pago.clave,
            monto: monto
        ]
        notasCredito.add( notaCreditoTmp )
      }

      def ventasEmpleado = []
      List<Pago> lstVentasEmpleado = pagoRepository.findAll( pay.fecha.between(fechaStart,fechaEnd).and(pay.idFPago.eq('CRE')).
          and(pay.notaVenta.factura.isNotEmpty()).and(pay.notaVenta.factura.isNotNull())) as List<Pago>
      for(Pago pago: lstVentasEmpleado){
        String monto = formatter.format( pago.monto )
        def ventaEmpleadoTmp = [
            factura: pago.notaVenta.factura,
            idBancoEmisor: pago.idBancoEmisor,
            monto: monto
        ]
        ventasEmpleado.add( ventaEmpleadoTmp )
      }

      def ventasVales = []
      def ventasValesUSD = []
      BigDecimal montoTotalVales = BigDecimal.ZERO
      BigDecimal montoUsdTotalVales = BigDecimal.ZERO
      BigDecimal montoPesosUsdTotalVales = BigDecimal.ZERO
      List<Pago> lstPagosVales = pagoRepository.findAll( pay.fecha.between(fechaStart,fechaEnd).and((pay.idFPago.eq('VMN')).
            or(pay.idFPago.eq('VUS'))).and(pay.notaVenta.factura.isNotEmpty()).and(pay.notaVenta.factura.isNotNull()) ) as List<Pago>
      for(Pago pago : lstPagosVales){
          BigDecimal montoDolares = BigDecimal.ZERO
          if( pago?.idPlan != null && StringUtils.trimToEmpty(pago?.idPlan).isNumber() ){
              Double dolares = 0.00
              try{
                montoDolares = new BigDecimal( NumberFormat.getInstance().parse(pago?.idPlan).doubleValue() )
                dolares = NumberFormat.getInstance().parse(pago?.idPlan).doubleValue()
              } catch (Exception e) {}
              montoUsdTotalVales = montoUsdTotalVales.add(new BigDecimal(dolares))
              montoPesosUsdTotalVales = montoPesosUsdTotalVales.add(pago?.monto)
          }
          def ventasValesTmp = [
            factura: pago?.notaVenta?.factura,
            referencia: pago?.clave,
            importe: String.format('%10s', formatter.format(pago?.monto)),
            dolares: montoDolares != '' ? String.format('%10s', formatter.format(montoDolares)) : ''
          ]
          if( 'VMN'.equalsIgnoreCase(pago.idFPago.trim())){
            montoTotalVales = montoTotalVales.add(pago?.monto)
            ventasVales.add( ventasValesTmp )
          } else if( 'VUS'.equalsIgnoreCase(pago.idFPago.trim()) ){
            ventasValesUSD.add( ventasValesTmp )
          }

      }

      def cupones = []
      QDescuento qDescuento = QDescuento.descuento
      List<Descuento> lstCupones = descuentoRepository.findAll(qDescuento.fecha.between(fechaStart,fechaEnd),
              qDescuento.idFactura.asc()) as List<Descuento>
      for(Descuento desCupon : lstCupones){
        if( StringUtils.trimToEmpty(desCupon.tipoClave).equalsIgnoreCase("CUPON") ){
          String monto = ""
          String tipo = ""
          String key = StringUtils.trimToEmpty(desCupon.clave).substring(5,6)
          if( key == "1" ){
            monto = String.format('%10s', formatter.format(desCupon?.notaVenta?.montoDescuento))
            tipo = "M"
          } else if( key == "0" ){
            monto = String.format('%10s', StringUtils.trimToEmpty(desCupon?.porcentaje))
            tipo = "D"
          }
          def cupon = [
                  factura: StringUtils.trimToEmpty(desCupon?.notaVenta?.factura),
                  monto: monto,
                  tipo: tipo
          ]
          cupones.add(cupon)
        }
      }
      Sucursal sucursal = sucursalRepository.findOne(Registry.currentSite)
      def datos = [ nombre_ticket: 'ticket-resumen-diario',
          fecha_cierre: MyDateUtils.format( fechaCierre, 'dd-MM-yyyy' ),
          hora_cierre: cierreDiario.horaCierre != null ? String.format('%s %s', MyDateUtils.format( cierreDiario.fechaCierre, 'dd-MM-yyyy' ), MyDateUtils.format( cierreDiario.horaCierre, 'HH:mm:ss' ) ): '',
          empleado: empleado != null ? empleado.nombreCompleto() : "",
          id_sucursal: empleado != null ? empleado.sucursal.id : sucursal.id,
          nombre_sucursal: empleado != null ? empleado.sucursal.nombre : sucursal.nombre,
          estado_cierre_diario: cierreDiario.estado,
          cantidad_ventas_brutas: cierreDiario.cantidadVentas == 0 ? '-' : cierreDiario.cantidadVentas,
          importe_ventas_brutas: cierreDiario.ventaBruta.compareTo(BigDecimal.ZERO) == 0 ? String.format('%10s', '-') : String.format('%10s', formatter.format( ventaBruta ) ),
          cantidad_modificaciones: cierreDiario.cantidadModificaciones == 0 ? '-' : cierreDiario.cantidadModificaciones,
          importe_modificaciones: cierreDiario.modificaciones.compareTo(BigDecimal.ZERO) == 0 ? String.format('%10s', '-') : String.format('%10s', formatter.format( descuento ) ),
          cantidad_modificaciones_netas: cantDesc == 0 ? '-' : cantDesc,
          importe_modificaciones_netas: desc.compareTo(BigDecimal.ZERO) == 0 ? String.format('%10s', '-') : String.format('%10s', formatter.format( desc ) ),
          cantidad_cancelaciones: cierreDiario.cantidadCancelaciones == 0 ? '-' : cierreDiario.cantidadCancelaciones,
          cantidad_venta_neta: cantidadVentaNeta == 0 ? '-' : cantidadVentaNeta,
          importe_cancelaciones: cierreDiario.cancelaciones.compareTo(BigDecimal.ZERO) == 0 ? String.format('%10s', '-') : String.format('%10s', formatter.format( cancelaciones ) ),
          importe_venta_neta: cierreDiario.ventaNeta.compareTo(BigDecimal.ZERO) == 0 ? String.format('%10s', '-') : String.format('%10s', formatter.format( ventaNeta ) ),
          importe_ingresos_brutos: cierreDiario.ingresoBruto.compareTo(BigDecimal.ZERO) == 0 ? String.format('%10s', '-') : String.format('%10s', formatter.format( cierreDiario.ingresoBruto) ),
          importe_devoluciones: cierreDiario.devoluciones.compareTo(BigDecimal.ZERO) == 0 ? String.format('%10s', '-') : String.format('%10s', formatter.format( cierreDiario.devoluciones ) ),
          importe_ingresos_netos: cierreDiario.ingresoNeto.compareTo(BigDecimal.ZERO) == 0 ? String.format('%10s', '-') : String.format('%10s', formatter.format( cierreDiario.ingresoNeto ) ),
          importe_efectivo_recibido: cierreDiario.efectivoRecibido.compareTo(BigDecimal.ZERO) == 0 ? String.format('%10s', '-') : String.format('%10s', formatter.format( cierreDiario.efectivoRecibido ) ),
          importe_efectivo_externos: cierreDiario.efectivoExternos.compareTo(BigDecimal.ZERO) == 0 ? String.format('%10s', '-') : String.format('%10s', formatter.format( cierreDiario.efectivoExternos ) ),
          importe_efectivo_devoluciones: cierreDiario.efectivoDevoluciones.compareTo(BigDecimal.ZERO) == 0 ? String.format('%10s', '-') : String.format('%10s', formatter.format( cierreDiario.efectivoDevoluciones ) ),
          importe_efectivo_neto: cierreDiario.efectivoNeto.compareTo(BigDecimal.ZERO) == 0 ? String.format('%10s', '-') : String.format('%10s', formatter.format( cierreDiario.efectivoNeto ) ),
          importe_dolares_recibido: dolaresPesos.compareTo(BigDecimal.ZERO) == 0 ? String.format('%10s', '-') : String.format('%10s', formatter.format( dolaresPesos.multiply( new BigDecimal( 1.00 ) ) ) ),
          importe_dolares_devoluciones: cierreDiario.dolaresDevoluciones.compareTo(BigDecimal.ZERO) == 0 ? String.format('%10s', '-') : formatter.format( cierreDiario.dolaresDevoluciones ),
          importe_dolares_pesos: cierreDiario.dolaresRecibidos.compareTo(BigDecimal.ZERO) == 0 ? String.format('%10s', '-') : String.format('%10s', formatter.format( cierreDiario.dolaresRecibidos ) ),
          ventas_vales: ventasVales,
          ventas_vales_usd: ventasValesUSD,
          monto_vales_pesos: montoTotalVales.compareTo(BigDecimal.ZERO) == 0 ? String.format('%10s', '-') : String.format('%10s', formatter.format( montoTotalVales ) ),
          monto_vales_usd: montoUsdTotalVales.compareTo(BigDecimal.ZERO) == 0 ? String.format('%10s', '-') : String.format('%10s', formatter.format( montoUsdTotalVales ) ),
          monto_en_pesos_usd: montoPesosUsdTotalVales.compareTo(BigDecimal.ZERO) == 0 ? String.format('%10s', '-') : String.format('%10s', formatter.format( montoPesosUsdTotalVales ) ),
          exist_vales: lstPagosVales.size() > 0 ? 'exist' : '',
          iva_vigente: montoTotalIva.compareTo(BigDecimal.ZERO) == 0 ? String.format('%10s', '-') : String.format('%10s', formatter.format( montoTotalIva ) ),
          artSinExis: lstArticulos.size() > 0 ? lstArticulos : null,
          today: MyDateUtils.format( new Date(), 'dd-MM-yyyy' ),
          tipo_cambio_USD: String.format( '%.2f', tipoCambioUsd.tipoCambio ),
          tipo_cambio_EUR: String.format( '%.2f', tipoCambioEur.tipoCambio ),
          depositos: depositos.size() > 0 ? depositos : null,
          faltanteMN: diferenciaEfectivoMN < 0 ? String.format('%10s', formatter.format( diferenciaEfectivoMN ) ) : null,
          sobranteMN: diferenciaEfectivoMN > 0 ? String.format('%10s', formatter.format( diferenciaEfectivoMN ) ) : null,
          faltanteUS: diferenciaEfectivoUS < 0 ? String.format('%10s', formatter.format( diferenciaEfectivoUS ) ) : null,
          sobranteUS: diferenciaEfectivoUS > 0 ? String.format('%10s', formatter.format( diferenciaEfectivoUS ) ) : null,
          resumen_terminales: resumenTerminales.size() > 0 ? resumenTerminales : null,
          cupones: cupones,
          resumen_terminales_tpv: resumenTerminalesTpv.size() > 0 ? resumenTerminalesTpv : null,
          resumen_terminales_tpv_can: resumenTerminalesTpvCan.size() > 0 ? resumenTerminalesTpvCan : null,
          numero_vales: vales.size(),
          monto_vales: montoVales.compareTo(BigDecimal.ZERO) == 0 ? '-' : String.format('%10s', formatter.format( montoVales ) ),
          vales: vales.size() > 0 ? vales : null,
          totalComprobantes: comprobantes.size(),
          comprobantesInicial: cierreDiario.facturaInicial != null ? cierreDiario.facturaInicial : '',
          comprobantesFinal: cierreDiario.facturaFinal != null ? cierreDiario.facturaFinal : '',
          pagosExternos: pagosExternos.isEmpty() ? null : pagosExternos,
          totalPagosExternos: totalPagosExternos.compareTo(BigDecimal.ZERO) == 0 ? '-' : String.format('%10s', formatter.format( totalPagosExternos ) ),
          gerente: gerente?.nombreCompleto(),
          retornos: retornos.size() > 0 ? retornos : null,
          notas_credito: notasCredito.size() > 0 ? notasCredito : null,
          ventas_empleado: ventasEmpleado.size() > 0 ? ventasEmpleado : null,
          observaciones: StringUtils.isNotBlank( cierreDiario.observaciones ) ? StringUtils.replace( cierreDiario.observaciones, '~', '\n' ) : '',
          entregadosExternos: entregadosExternosTmp.isEmpty() ? null : new ArrayList<EntregadoExterno>( entregadosExternosTmp.values() ), ]
      imprimeTicket( 'template/ticket-resumen-diario.vm', datos )
    } else {
      log.error( "Se ha producido un error al imprimir el Resumen Diario. No hay datos sobre el día ${ MyDateUtils.format( fechaCierre, 'dd/MM/yyyy' ) }" )
      imprimeTicket( 'template/error.vm', [ nombre_ticket: 'ticket-cierre-terminal', mensaje: "Se ha producido un error al imprimir el Resumen Diario. No hay datos sobre el día ${ MyDateUtils.format( fechaCierre, 'dd/MM/yyyy' ) }" ] )
    }
  }

  @Override
  void imprimeUbicacionListaPrecios( ListaPrecios listaPrecios, List<Articulo> articulos ) {
    def idSucursal = parametroRepository.findOne( TipoParametro.ID_SUCURSAL.value )?.valor
    def sucursal = sucursalRepository.findOne( idSucursal?.toInteger() )
    for(Articulo articulo : articulos){
      Articulo tmp = articuloRepository.findOne( articulo.id )
      if( tmp != null ){
        articulo.cantExistencia = tmp.cantExistencia
      }
    }
    def lstArticulos = [ ]
    String descripcion1 = ''
    String descripcion2 = ''
    String descripcion3 = ''
    String descripcion4 = ''
    String descripcion5 = ''
    for(Articulo art : articulos){
      if( art.descripcion.length() > 22 ){
        descripcion1 = art.descripcion.substring(0,22)
        if( art.descripcion.length() > 44 ){
          descripcion2 = art.descripcion.substring(22,44)
        }
        if( art.descripcion.length() > 66 ){
          descripcion3 = art.descripcion.substring(44,66)
        }
        if( art.descripcion.length() > 88 && art.descripcion.length() > 100 ){
          descripcion4 = art.descripcion.substring(88,100)
        } else if( art.descripcion.length() > 88 ) {
          descripcion4 = art.descripcion.substring(88)
        }
        if( art.descripcion.length() > 100 && art.descripcion.length() > 122 ){
          descripcion5 = art.descripcion.substring(100,122)
        } else if( art.descripcion.length() > 100 ) {
          descripcion5 = art.descripcion.substring(100)
        }
      } else {
        descripcion1 = art.descripcion
      }
      def tmpArticulo = [
        id: art.id,
        articulo: art.articulo,
        descripcion1: descripcion1,
        descripcion2: descripcion2,
        descripcion3: descripcion3,
        descripcion4: descripcion4,
        descripcion5: descripcion5,
        cantidad: art.cantExistencia
      ]
      if( art.cantExistencia > 0 ){
          lstArticulos.add( tmpArticulo )
      }
    }
    def items = [
        nombre_ticket: 'ticket-ubicacion-lista-precios',
        sucursal: sucursal,
        id_lista: listaPrecios?.id,
        fecha: new Date().format( 'dd-MM-yyyy' ),
        articulos: articulos,
        lstArticulos: lstArticulos
    ]
    if ( Registry.isSunglass() ) {
      imprimeTicket( 'template/ticket-ubicacion-lista-precios-si.vm', items )
    } else {
      imprimeTicket( 'template/ticket-ubicacion-lista-precios.vm', items )
    }
  }

  @Override
  void imprimeCargaListaPrecios( ListaPrecios listaPrecios ) {
    def idSucursal = parametroRepository.findOne( TipoParametro.ID_SUCURSAL.value )?.valor
    def sucursal = sucursalRepository.findOne( idSucursal?.toInteger() )
    def items = [
        nombre_ticket: 'ticket-carga-lista-precios',
        sucursal: sucursal,
        id_lista: listaPrecios?.id,
        tipo_carga: listaPrecios?.tipoCarga,
        fecha: new Date().format( 'dd-MM-yyyy' )
    ]
    imprimeTicket( 'template/ticket-carga-lista-precios.vm', items )
  }

  void imprimeTransInv( TransInv pTrans ) {
    this.imprimeTransInv( pTrans, true )
  }

    protected static String replaceCharAt(String s, int pos, char c) {
        StringBuffer buf = new StringBuffer( s );
        buf.setCharAt( pos, c );
        return buf.toString( );
    }

    protected static  String claveAleatoria(Integer fecha, Integer monto) {
      String folioAux = "" + monto.intValue();
      String sucursalAux = "" + fecha.intValue()
      String abc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
      if (folioAux.size() < 5) {
        folioAux = folioAux?.padLeft( 5, '0' )
      } else {
        folioAux = folioAux.substring(0,5);
      }
      String resultado = sucursalAux?.padLeft( 6, '0' ) + folioAux
      for (int i = 0; i < resultado.size(); i++) {
            int numAleatorio = (int) (Math.random() * abc.size());
            if (resultado.charAt(i) == '0') {
                resultado = replaceCharAt(resultado, i, abc.charAt(numAleatorio))
            }
            else {
                int numero = Integer.parseInt ("" + resultado.charAt(i));
                numero = 10 - numero
                char diff = Character.forDigit(numero, 10);
                resultado = replaceCharAt(resultado, i, diff)
            }
      }
      return resultado;
    }

  void imprimeTransInv( TransInv pTrans, Boolean pNewTransaction ) {
    TransInvAdapter adapter = TransInvAdapter.instance
    def parts = [ ]
    Integer cantidad = 0
    String referencia = ''
    for ( TransInvDetalle trDet in pTrans.trDet ) {
      Articulo part = ServiceFactory.partMaster.obtenerArticulo( trDet.sku, true)
      def tkPart = [
          sku: adapter.getText( trDet, adapter.FLD_TRD_SKU ),
          partNbr: adapter.getText( part, adapter.FLD_PART_CODE ),
          color: adapter.getText( part, adapter.FLD_PART_COLOR_CODE ),
          partColor: adapter.getText( part, adapter.FLD_PART_CODE_PLUS_COLOR ) ,
          desc: adapter.getText( part, adapter.FLD_PART_DESC ),
          price: String.format( '%12s', adapter.getText( part, adapter.FLD_PART_PRICE ) ),
          qty: String.format( '%5s', adapter.getText( trDet, adapter.FLD_TRD_QTY ) )
      ]
        cantidad = cantidad+trDet.cantidad
      parts.add( tkPart )
    }
    String barcode = ""
    AddressAdapter companyAddress = Registry.companyAddress
    Sucursal site = sucursalRepository.findOne( pTrans.sucursal )
    Sucursal siteTo = null
    if ( pTrans.sucursalDestino != null ) {
      siteTo = sucursalRepository.findOne( pTrans.sucursalDestino )
    }
    Empleado emp = empleadoRepository.findOne( pTrans.idEmpleado )
    List<String> remarks = adapter.split( StringUtils.trimToEmpty( pTrans.observaciones ), 36 )
    Empleado mgr = null
    if ( site.idGerente != null ) {
      mgr = empleadoRepository.findById( site.idGerente )
    }
    if ( InventorySearch.esTipoTransaccionSalida( pTrans.idTipoTrans ) ) {
      barcode = StringUtils.trimToEmpty(site.centroCostos)+StringUtils.trimToEmpty(String.format("%06d",pTrans.folio))
      def tkInvTr = [
          nombre_ticket: "ticket-salida-inventario",
          effDate: adapter.getText( pTrans, adapter.FLD_TR_EFF_DATE ),
          thisSite: adapter.getText( site ),
          user: adapter.getText( emp ),
          mgr: adapter.getText( mgr ),
          trNbr: adapter.getText( pTrans, adapter.FLD_TR_NBR ),
          siteTo: adapter.getText( siteTo ),
          remarks_1: ( remarks.size() > 0 ? remarks.get( 0 ) : "" ),
          remarks_2: ( remarks.size() > 1 ? remarks.get( 1 ) : "" ),
          quantity: cantidad,
          parts: parts,
          barcode: barcode,
          mostrarCodigo: true,
      ]
      imprimeTicket( "template/ticket-salida-inventario.vm", tkInvTr )
    } else if ( InventorySearch.esTipoTransaccionAjuste( pTrans.idTipoTrans ) ) {
      def tkInvTr = [
          nombre_ticket: "ticket-ajuste-inventario",
          effDate: adapter.getText( pTrans, adapter.FLD_TR_EFF_DATE ),
          thisSite: adapter.getText( site ),
          user: adapter.getText( emp ),
          mgr: adapter.getText( mgr ),
          trNbr: adapter.getText( pTrans, adapter.FLD_TR_NBR ),
          siteTo: adapter.getText( siteTo ),
          remarks_1: ( remarks.size() > 0 ? remarks.get( 0 ) : "" ),
          remarks_2: ( remarks.size() > 1 ? remarks.get( 1 ) : "" ),
          parts: parts
      ]
      imprimeTicket( "template/ticket-ajuste-inventario.vm", tkInvTr )
    } else if ( InventorySearch.esTipoTransaccionDevolucion( pTrans.idTipoTrans ) ) {
      def tkInvTr = [
          nombre_ticket: "ticket-devolucion",
          effDate: adapter.getText( pTrans, adapter.FLD_TR_EFF_DATE ),
          compania: companyAddress,
          sucursal: site,
          user: adapter.getText( emp ),
          mgr: adapter.getText( mgr ),
          ticket: adapter.getText( pTrans, adapter.FLD_SRC_TICKET ),
          empName: adapter.getText( pTrans, adapter.FLD_SALES_PERSON ),
          returnAmount: adapter.getText( pTrans, adapter.FLD_RETURN_AMOUNT ),
          trNbr: adapter.getText( pTrans, adapter.FLD_TR_NBR ),
          remarks_1: ( remarks.size() > 0 ? remarks.get( 0 ) : "" ),
          remarks_2: ( remarks.size() > 1 ? remarks.get( 1 ) : "" ),
          quantity: cantidad,
          parts: parts
      ]
      imprimeTicket( "template/ticket-devolucion.vm", tkInvTr )
      if ( Registry.isReceiptDuplicate() && pNewTransaction ) {
        imprimeTicket( 'template/ticket-devolucion.vm', tkInvTr )
      }

    } else if ( InventorySearch.esTipoTransaccionSalidaSucursal( pTrans.idTipoTrans ) ) {
        def tkInvTr = [
                nombre_ticket: "ticket-salida-sucursal",
                effDate: adapter.getText( pTrans, adapter.FLD_TR_EFF_DATE ),
                thisSite: adapter.getText( site ),
                user: adapter.getText( emp ),
                codaleatorio: pTrans.referencia,
                mgr: adapter.getText( mgr ),
                trNbr: adapter.getText( pTrans, adapter.FLD_TR_NBR ),
                siteTo: adapter.getText( siteTo ),
                remarks_1: ( remarks.size() > 0 ? remarks.get( 0 ) : "" ),
                remarks_2: ( remarks.size() > 1 ? remarks.get( 1 ) : "" ),
                quantity: cantidad,
                parts: parts
        ]
        imprimeTicket( "template/ticket-salida-sucursal.vm", tkInvTr )
    } else if ( InventorySearch.esTipoTransaccionEntrada( pTrans.idTipoTrans ) ) {
        def tkInvTr = [
                nombre_ticket: "ticket-entrada-sucursal",
                effDate: adapter.getText( pTrans, adapter.FLD_TR_EFF_DATE ),
                thisSite: adapter.getText( site ),
                user: adapter.getText( emp ),
                codaleatorio: pTrans.referencia,
                mgr: adapter.getText( mgr ),
                trNbr: adapter.getText( pTrans, adapter.FLD_TR_NBR ),
                siteTo: adapter.getText( siteTo ),
                remarks_1: ( remarks.size() > 0 ? remarks.get( 0 ) : "" ),
                remarks_2: ( remarks.size() > 1 ? remarks.get( 1 ) : "" ),
                quantity: cantidad,
                parts: parts
        ]
        imprimeTicket( "template/ticket-entrada-inventario.vm", tkInvTr )
    }
  }

  @Override
  void imprimeCancelacion( String idNotaVenta ) {
    log.info( "imprimiendo ticket cancelacion de notaVenta id: ${idNotaVenta}" )
    NotaVenta notaVenta = notaVentaService.obtenerNotaVenta( idNotaVenta )
    List<Modificacion> mods = modificacionRepository.findByIdFacturaAndTipo( idNotaVenta ?: '', 'can' )
    log.debug( "modificaciones: ${mods*.id}" )
    Modificacion modificacion = mods?.any() ? mods.first() : null
    log.debug( "obtiene modificacion: ${modificacion?.id}" )
    if ( StringUtils.isNotBlank( notaVenta?.id ) && modificacion?.id ) {
      List<Pago> pagosLst = pagoRepository.findByIdFacturaOrderByFechaAsc( idNotaVenta )
      BigDecimal totalPorDevolver = 0
      pagosLst.each { Pago pmt ->
        totalPorDevolver += pmt.porDevolver ?: 0
      }
      if ( totalPorDevolver == 0 ) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance( Locale.US )
        Integer sucursalId = sucursalRepository.getCurrentSucursalId()
        Sucursal sucursal = sucursalRepository.findOne( sucursalId )
        Empleado empleado = empleadoRepository.findOne( modificacion.idEmpleado )
        def detalles = [ ]
        List<DetalleNotaVenta> detallesLst = detalleNotaVentaRepository.findByIdFacturaOrderByIdArticuloAsc( idNotaVenta )
        detallesLst.each { DetalleNotaVenta det ->
          BigDecimal precio = det?.precioUnitFinal?.multiply( det?.cantidadFac )
          String articuloDesc = det?.articulo?.articulo.length() <= 13 ?det?.articulo?.articulo: det?.articulo?.articulo.substring(0,13)
          String articuloDesc1 = det?.articulo?.articulo.length() > 13 ? det?.articulo?.articulo.substring(13) : ''
          def item = [
              cantidad: "${det?.cantidadFac?.toInteger() ?: 0}",
              codigo: "[${det?.idArticulo}]${articuloDesc} ${det?.articulo?.codigoColor ?: ''}",
              codigo1: articuloDesc1,
              surte: "${det?.surte ?: ''}",
              precio: formatter.format( precio ?: 0 )
          ]
          log.debug( "genera detalle: ${item}" )
          detalles.add( item )
        }
        def pagos = [ ]
        BigDecimal totalPagos = 0
        pagosLst.each { Pago pmt ->
          BigDecimal monto = pmt?.monto ?: 0
          String referenciaPago
          NotaVenta nota = notaVentaRepository.findOne(pmt?.referenciaPago)
          if(nota != null){
            referenciaPago = nota.factura
          } else {
            referenciaPago = pmt?.referenciaPago
          }
          totalPagos += monto
          def item = [
              descripcion: "${pmt?.eTipoPago?.descripcion ?: ''}",
              referencia: "${referenciaPago ?: ''}",
              monto: formatter.format( monto )
          ]
          log.debug( "genera pago: ${item}" )
          pagos.add( item )
        }
        def transferencias = [ ]
        def devoluciones = [ ]
        BigDecimal totalTransferencias = 0
        BigDecimal totalDevoluciones = 0
        List<Devolucion> devolucionesLst = devolucionRepository.findByIdModOrderByFechaAsc( modificacion.id )
        devolucionesLst.each { Devolucion dev ->
          BigDecimal monto = dev?.monto ?: 0
          if ( 'd'.equalsIgnoreCase( dev?.tipo ) ) {
            totalDevoluciones += monto
            String desc = dev?.pago?.eTipoPago?.descripcion
            String desc1 = ''
            if( dev?.pago?.eTipoPago?.descripcion.length() > 15 ){
                desc = dev?.pago?.eTipoPago?.descripcion.substring(0,15)
                desc1 = dev?.pago?.eTipoPago?.descripcion.substring(15)
            }
            def item = [
                original: "${desc ?: ''}",
                original1: "${desc1 ?: ''}",
                devolucion: "${dev?.formaPago?.descripcion ?: ''}",
                importe: formatter.format( monto )
            ]
            log.debug( "genera devolucion: ${item}" )
            devoluciones.add( item )
          } else {
            totalTransferencias += monto
            String referenciaPago
            NotaVenta nv = notaVentaRepository.findOne( dev?.transf )
            if(nv != null){
              referenciaPago = nv.factura
            } else {
              referenciaPago = dev?.transf
            }
            def item = [
                descripcion: "Factura ${referenciaPago} (${dev?.formaPago?.descripcion ?: ''})",
                monto: formatter.format( monto )
            ]
            log.debug( "genera transferencia: ${item}" )
            transferencias.add( item )
          }
        }
        Map<String, Object> items = [
            nombre_ticket: 'ticket-cancelacion',
            sucursal: sucursal,
            fecha: modificacion.fecha != null ? modificacion.fecha.format( DATE_FORMAT ) : new Date().format( DATE_FORMAT ),
            fecha_venta: notaVenta.fechaHoraFactura?.format( DATE_TIME_FORMAT ) ?: '',
            empleado: empleado,
            nota_venta: notaVenta,
            cliente: notaVenta.cliente?.nombreCompleto( false ),
            gerente: sucursal.gerente?.nombreCompleto(),
            modificacion: modificacion,
            detalles: detalles,
            venta_neta: formatter.format( notaVenta.ventaNeta ),
            total_saldo: formatter.format( notaVenta.ventaNeta.subtract( notaVenta.sumaPagos ) ),
            pagos: pagos,
            total_pagos: formatter.format( totalPagos ),
            devoluciones: devoluciones,
            total_devoluciones: formatter.format( totalDevoluciones ),
            transferencias: transferencias,
            total_transferencias: formatter.format( totalTransferencias ),
            total_movimientos: formatter.format( totalDevoluciones.add( totalTransferencias ) )
        ]
        imprimeTicket( 'template/ticket-cancelacion.vm', items )
      } else {
        log.warn( 'no se imprime ticket cancelacion, aun tiene monto por devolver' )
      }
    } else {
      log.warn( 'no se imprime ticket cancelacion, parametros invalidos' )
    }
  }

  @Override
  void imprimePlanCancelacion( String idNotaVenta ) {
    log.info( "imprimiendo ticket plan cancelacion de notaVenta id: ${idNotaVenta}" )
    NotaVenta notaVenta = notaVentaService.obtenerNotaVenta( idNotaVenta )
    List<Modificacion> mods = modificacionRepository.findByIdFacturaAndTipo( idNotaVenta ?: '', 'can' )
    log.debug( "modificaciones: ${mods*.id}" )
    Modificacion modificacion = mods?.any() ? mods.first() : null
    log.debug( "obtiene modificacion: ${modificacion?.id}" )
    if ( StringUtils.isNotBlank( notaVenta?.id ) && modificacion?.id ) {
      NumberFormat formatter = NumberFormat.getCurrencyInstance( Locale.US )
      Integer sucursalId = sucursalRepository.getCurrentSucursalId()
      Sucursal sucursal = sucursalRepository.findOne( sucursalId )
      List<DetalleNotaVenta> detallesLst = detalleNotaVentaRepository.findByIdFacturaOrderByIdArticuloAsc( idNotaVenta )
      List<Pago> pagosLst = pagoRepository.findByIdFacturaOrderByFechaAsc( idNotaVenta )
      def pagos = [ ]
      pagosLst.each { Pago pmt ->
        BigDecimal monto = pmt?.monto ?: 0
        String referenciaPago
        NotaVenta nota = notaVentaRepository.findOne( pmt?.referenciaPago )
        if(nota != null){
          referenciaPago = nota?.factura
        } else {
          referenciaPago = pmt?.referenciaPago
        }
        def item = [
            descripcion: "${pmt?.eTipoPago?.descripcion}",
            descripcion1: "${referenciaPago}",
            monto: formatter.format( monto )
        ]
        log.debug( "genera pago: ${item}" )
        pagos.add( item )
      }
      Map<String, Object> items = [
          nombre_ticket: 'ticket-plan-cancelacion',
          ticket_id: "${sucursal.centroCostos}-${notaVenta.factura}",
          nota_venta: notaVenta,
          poliza: "",
          poliza_vigente: "",
          factura: "",
          detalles: detallesLst*.articulo*.articulo,
          pagos: pagos
      ]
      imprimeTicket( 'template/ticket-plan-cancelacion.vm', items )
    } else {
      log.warn( 'no se imprime ticket cancelacion, parametros invalidos' )
    }
  }

  void imprimeResumenExistencias( InvOhSummary pSummary ) {
    log.debug( String.format( "Imprime Resume de Existencias\n%s", pSummary.toString() ) )
    DateFormat df = new SimpleDateFormat( "dd/MM/yyyy HH:mm" )
    Sucursal site = ServiceFactory.sites.obtenSucursalActual()
      Collections.sort( pSummary.lines, new Comparator<InvOhDet>() {
          @Override
          int compare(InvOhDet o1, InvOhDet o2) {
              return o1.id.compareTo(o2.id)
          }
      })
    def tkQtyOH = [
        nombre_ticket: "ticket-resumen-inventario",
        effDate: df.format( new Date() ),
        thisSite: TransInvAdapter.instance.getText( site ),
        genre: StringUtils.trimToNull( pSummary.genre ),
        brand: StringUtils.trimToNull( pSummary.brand ),
        lineas: pSummary.lines,
        qtyTotal: pSummary.qtyTotal
    ]
    imprimeTicket( "template/ticket-resumen-inventario.vm", tkQtyOH )
  }

  @Override
  void imprimeReferenciaFiscal( String idFiscal ) {
    log.info( "imprimiendo ticket referencia fiscal con idFiscal: ${idFiscal}" )
    Comprobante comprobante = comprobanteService.obtenerComprobante( idFiscal )
    if ( comprobante?.id ) {
      Integer idCliente = comprobante.idCliente?.isInteger() ? comprobante.idCliente.toInteger() : 0
      Cliente cliente = clienteRepository.findOne( idCliente )
      AddressAdapter companyAddress = Registry.companyAddress
      Map<String, Object> items = [
          nombre_ticket: 'ticket-referencia-fiscal',
          comprobante: comprobante,
          email: StringUtils.trimToEmpty( comprobante.email ),
          cliente: cliente,
          empresa: companyAddress.shortName,
          email_contacto: 'clienteconfianza@lux.mx',
          telefono_contacto: '01 800 9000 LUX(589)'
      ]
      if ( Registry.isSunglass() ) {
        imprimeTicket( 'template/ticket-referencia-fiscal-si.vm', items )
      } else {
        imprimeTicket( 'template/ticket-referencia-fiscal.vm', items )
      }
    } else {
      log.warn( 'no se imprime ticket referencia fiscal, no existe comprobante' )
    }
  }

  @Override
  void imprimeComprobanteFiscal( String idFiscal ) {
    log.info( "imprimiendo ticket comprobante fiscal con idFiscal: ${idFiscal}" )
    Comprobante comprobante = comprobanteService.obtenerComprobante( idFiscal )
    if ( comprobante?.id ) {
      List<File> archivos = comprobanteService.descargarArchivosComprobante( idFiscal )
      if ( archivos?.any() ) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance( Locale.US )
        Integer idCliente = comprobante.idCliente?.isInteger() ? comprobante.idCliente.toInteger() : 0
        Cliente cliente = clienteRepository.findOne( idCliente )
        Integer idSucursal = sucursalRepository.getCurrentSucursalId()
        Sucursal sucursal = sucursalRepository.findOne( idSucursal )
        File xml = archivos.first()
        def cfd = new XmlSlurper().parseText( xml?.text )
        String rfcEmisor = cfd.Emisor.@rfc
        Contribuyente contribuyenteEmisor = contribuyenteService.obtenerContribuyentePorRfc( rfcEmisor )
        AddressAdapter companyAddress = Registry.companyAddress
        Estado estadoEmisor = estadoService.obtenerEstado( contribuyenteEmisor?.idEstado )
        def emisor
        if ( contribuyenteEmisor != null ) {
          emisor = [
              rfc: contribuyenteEmisor?.rfc,
              nombre: contribuyenteEmisor?.nombre,
              calle: contribuyenteEmisor?.domicilio,
              colonia: contribuyenteEmisor?.colonia,
              municipio: contribuyenteEmisor?.ciudad,
              estado: estadoEmisor?.nombre,
              codigo_postal: contribuyenteEmisor?.codigoPostal,
              regimen_fiscal: cfd.Emisor.RegimenFiscal.@Regimen
          ]
        } else {
          emisor = [
              rfc: companyAddress?.taxId,
              nombre: companyAddress?.name,
              calle: companyAddress?.address_1,
              colonia: companyAddress?.address_2,
              municipio: companyAddress?.city,
              estado: estadoEmisor?.nombre,
              codigo_postal: companyAddress?.CP,
              regimen_fiscal: cfd.Emisor.RegimenFiscal.@Regimen
          ]
        }
        def receptor = [
            rfc: cfd.Receptor.@rfc,
            nombre: cfd.Receptor.@nombre,
            calle: cfd.Receptor.Domicilio.@calle,
            colonia: cfd.Receptor.Domicilio.@colonia,
            municipio: cfd.Receptor.Domicilio.@municipio,
            estado: cfd.Receptor.Domicilio.@estado,
            codigo_postal: cfd.Receptor.Domicilio.@codigoPostal
        ]
        def conceptos = [ ]
        Integer totalArticulos = 0
        cfd.Conceptos.Concepto.each {
          String precioTxt = it.@valorUnitario ?: ''
          Double precio = precioTxt.isNumber() ? precioTxt.toDouble() : 0
          String importeTxt = it.@importe ?: ''
          String cantidadTxt = it.@cantidad ?: ''
          Double importe = importeTxt.isNumber() ? importeTxt.toDouble() : 0
          Integer cant = cantidadTxt.isNumber() ? cantidadTxt.toInteger() : 0
          String descripcionTmp = it.@descripcion
            String descripcion1 = ''
            String descripcion2 = ''
            String descripcion3 = ''
            if ( descripcionTmp.length() > 24 ) {
                descripcion1 = descripcionTmp.substring( 0, 24 )
                if ( descripcionTmp.length() > 48 ) {
                    descripcion2 = descripcionTmp.substring( 24, 48 )
                    if( descripcionTmp.length() > 72 ) {
                        descripcion3 = descripcionTmp.substring( 48, 72 )
                    } else {
                        descripcion3 = descripcionTmp.substring( 48 )
                    }
                } else {
                    descripcion2 = descripcionTmp.substring( 24 )
                }
            } else {
                descripcion1 = descripcionTmp
            }
          String descripcion = WordUtils.wrap( descripcionTmp, 24 )
          List<String> descripciones = descripcion.tokenize( '\n' ) ?: [ ]
          totalArticulos = totalArticulos+cant
          def concepto = [
              cantidad: it.@cantidad,
              unidad: it.@unidad,
              descripcion1: descripcion1,
              descripcion2: descripcion2,
              descripcion3: descripcion3,
              valor_unitario: formatter.format( precio ),
              importe: formatter.format( importe )
          ]
          conceptos.add( concepto )
          descripciones.eachWithIndex { String val, Integer idx ->
            if ( idx ) {
              def tmp = [
                  cantidad: '',
                  unidad: '',
                  descripcion: val,
                  valor_unitario: '',
                  importe: ''
              ]
              //conceptos.add( tmp )
            }
          }
        }
        def impuestos = [ ]
        cfd.Impuestos.Traslados.Traslado.each {
          String importeTxt = it.@importe ?: ''
          Double importe = importeTxt.isNumber() ? importeTxt.toDouble() : 0
          def impuesto = [
              impuesto: it.@impuesto,
              tasa: it.@tasa,
              importe: formatter.format( importe )
          ]
          impuestos.add( impuesto )
        }
        String subtotalTxt = cfd.@subTotal ?: ''
        Double subtotal = subtotalTxt.isNumber() ? subtotalTxt.toDouble() : 0
        String totalImpuestosTxt = cfd.Impuestos.@totalImpuestosTrasladados ?: ''
        Double totalImpuestos = totalImpuestosTxt.isNumber() ? totalImpuestosTxt.toDouble() : 0
        String totalTxt = cfd.@total ?: ''
        BigDecimal total = totalTxt.isNumber() ? totalTxt.toBigDecimal() : 0
        RuleBasedNumberFormat textFormatter = new RuleBasedNumberFormat( new Locale( 'es' ), RuleBasedNumberFormat.SPELLOUT )
        String textoTotal = "${textFormatter.format( total.intValue() )} ${total.remainder( 1 ).unscaledValue()}/100 M.N."
        String cadenaOriginal = comprobante.cadenaOriginal

        String pipe = '\u01C0'

        Map<String, Object> items = [
            nombre_ticket: 'ticket-comprobante-fiscal',
            folio: "${cfd.@serie}-${cfd.@folio}",
            fecha: cfd.@fecha,
            sello: StringUtils.trimToEmpty(comprobante.sello) != '' ? comprobante.sello : cfd.@sello,
            totalArt: totalArticulos,
            cadena_original: StringUtils.trimToEmpty(cadenaOriginal) ? cadenaOriginal : '',
            sello_cfdi: null,
            cadena_original_cfdi: null,
            num_aprobacion: cfd.@noAprobacion,
            anio_aprobacion: cfd.@anoAprobacion,
            lugar_expedicion: cfd.@LugarExpedicion,
            forma_pago: cfd.@formaDePago,
            num_certificado: cfd.@noCertificado,
            subtotal: formatter.format( subtotal ),
            total_impuestos: formatter.format( totalImpuestos ),
            total: formatter.format( total ),
            texto_total: textoTotal.toUpperCase(),
            metodo_pago: cfd.@metodoDePago,
            comprobante: comprobante,
            emisor: emisor,
            receptor: receptor,
            cliente: cliente,
            sucursal: sucursal,
            conceptos: conceptos,
            impuestos: impuestos,
            leyenda: "Este documento es una representacion impresa de un CFD",
            empresa: companyAddress.shortName
        ]
        imprimeTicket( 'template/ticket-comprobante-fiscal.vm', items )
      } else {
        log.warn( 'no se imprime ticket comprobante fiscal, no se obtienen archivos' )
      }
    } else {
      log.warn( 'no se imprime ticket comprobante fiscal, no existe comprobante' )
    }
  }

  void imprimeCotizacion( Cotizacion cotizacion, CotizaDet cotizaDet, boolean totalizar, boolean convenio, String convenioDesc ) {
    log.debug( "imprimeCotizacion" )
    DateFormat df = new SimpleDateFormat( "dd-MM-yyyy" )
    SimpleDateFormat nextDate = new SimpleDateFormat( "dd MMMM yyyy" )
    BigDecimal totalMonto = BigDecimal.ZERO
    String total = " "
    String convenioNota
    String convenioAst
    String tipoArticulo = ' '
    Sucursal site = ServiceFactory.sites.obtenSucursalActual()

    if ( convenio && convenioDesc != null ) {
      String nombre = convenioDesc.trim()
      convenioNota = "* Precios especiales para convenio $nombre, para hacerlos validos es necesario presentar la documentacion pactada con la empresa"
      convenioAst = "*"
    } else {
      convenioNota = ''
      convenioAst = ''
    }

    def articulos = [ ]
    QCotizaDet cotizadet = QCotizaDet.cotizaDet
    Iterable<CotizaDet> lstArticulos = cotizaDetRepository.findAll( cotizadet.id_cotiza.eq( cotizacion.id ) )

    Calendar fechaExp = Calendar.getInstance();
    fechaExp.add( Calendar.MONTH, 1 );

    NumberFormat formatter = NumberFormat.getCurrencyInstance( Locale.US )
    String letrero = " "
    log.debug( "Totalizar::", totalizar )
    BooleanBuilder qColor = new BooleanBuilder()

    Iterable<Articulo> lstArt = null
    if ( totalizar ) {
      for ( CotizaDet cotizaciones : lstArticulos ) {
        QArticulo art = QArticulo.articulo1
        if ( cotizaciones.color != null && cotizaciones.color.length() > 0 ) {
          qColor.and( art.codigoColor.eq( cotizaciones.color ) )
        } else {
          qColor.and( art.codigoColor.isNull() )
        }

        lstArt = articuloRepository.findAll( art.articulo.eq( cotizaciones.articulo ).
            and( qColor ) )
        totalMonto = totalMonto.add( cotizaciones.precioUnit )
        total = formatter.format( totalMonto ).toString()
        if ( lstArt.iterator().hasNext() ) {
          Articulo articulo = lstArt.iterator().next()
          tipoArticulo = genericoRepository.findOne( articulo.generico.id ).descripcion
        }
      }
      log.debug( "Total::", total )
      letrero = "TOTAL:"
    } else {
      for ( CotizaDet cotizaciones : lstArticulos ) {
        if ( lstArt.iterator().hasNext() ) {
          Articulo articulo = lstArt.iterator().next()
          tipoArticulo = genericoRepository.findOne( articulo.generico.id ).descripcion
        }
      }
    }

    lstArticulos?.each { CotizaDet tmp ->
      String articuloDesc = StringUtils.trimToEmpty( tmp?.articulos?.descripcion )
      String color = tmp?.color
      BigInteger precio = tmp?.precioUnit
      tipoArticulo = genericoRepository.findOne( tmp?.articulos?.idGenerico ).descripcion
      String descripcion
      if ( articuloDesc.length() > LONGITUD_MAXIMA ) {
        descripcion = articuloDesc.substring( 0, LONGITUD_MAXIMA )
      } else {
        descripcion = articuloDesc
      }

      def articulo = [
          articulo: "[${tmp?.articulos?.id}] ${tipoArticulo} ${tmp?.articulos?.marca}",
          precio: formatter.format( precio ),
      ]
      articulos.add( articulo )
    }

    AddressAdapter companyAddress = Registry.companyAddress
    def data = [
        date: df.format( new Date() ),
        thisSite: site,
        compania: companyAddress,
        nombre: cotizacion.nombre,
        observaciones: cotizacion.observaciones,
        cotizacionId: cotizacion.id,
        empleado: cotizacion.emp,
        articulos: articulos,
        total: total,
        letrero: letrero,
        nextDate: nextDate.format( fechaExp.getTime() ).toString().toUpperCase(),
        notaConvenio: convenioNota,
        asteriscoConvenio: convenioAst
    ]
    imprimeTicket( "template/ticket-cotizacion.vm", data )
  }

  void imprimeAperturaCaja( Date fechaApertura ) {
    log.debug( "Imprime Apertura Caja" )
    DateFormat df = new SimpleDateFormat( "dd-MM-yyyy" )
    DateFormat dft = new SimpleDateFormat( "dd-MM-yyyy HH:mm a" )
    NumberFormat formatter = NumberFormat.getCurrencyInstance( Locale.US )

    Parametro parametroGerente = parametroRepository.findOne( TipoParametro.ID_GERENTE.value )
    QEmpleado empleado = QEmpleado.empleado
    Empleado gerente = empleadoRepository.findOne( empleado.id.eq( parametroGerente.valor ) )
    log.debug( "Gerente", gerente.nombreCompleto )
    Sucursal site = ServiceFactory.sites.obtenSucursalActual()
    Apertura apertura = aperturaRepository.findOne( fechaApertura )
    MonedaDetalle monedaDetEur = monedaExtranjeraService.findActiveRate( "EUR", apertura.fechaApertura )
    MonedaDetalle monedaDetUsd = monedaExtranjeraService.findActiveRate( "USD", apertura.fechaApertura )

    def data = [
        sucursal: site.id,
        fecha: df.format( apertura.fechaApertura ),
        mnx: formatter.format( apertura.efvoPesos ),
        usd: formatter.format( apertura.efvoDolares ),
        observaciones: apertura.observaciones,
        monedasDetUsd: formatter.format( monedaDetUsd.tipoCambio ),
        monedasDetEur: formatter.format( monedaDetEur.tipoCambio ),
        gerente: gerente.nombreCompleto,
        fechaImpresion: dft.format( new Date() )
    ]
    imprimeTicket( "template/ticket-aperturaCaja.vm", data )
  }

  void imprimeDevolucion( TransInv pTrans ) {
    TransInvAdapter adapter = TransInvAdapter.instance
    def parts = [ ]
    for ( TransInvDetalle trDet in pTrans.trDet ) {
      Articulo part = articuloRepository.findOne( trDet.sku )
      def tkPart = [
          sku: adapter.getText( trDet, adapter.FLD_TRD_SKU ),
          partNbr: adapter.getText( part, adapter.FLD_PART_CODE ),
          desc: adapter.getText( part, adapter.FLD_PART_DESC ),
          qty: adapter.getText( trDet, adapter.FLD_TRD_QTY )
      ]
      parts.add( tkPart )
    }
    AddressAdapter companyAddress = Registry.companyAddress
    Sucursal site = sucursalRepository.findOne( pTrans.sucursal )
    Empleado emp = empleadoRepository.findOne( pTrans.idEmpleado )
    List<String> remarks = adapter.split( StringUtils.trimToEmpty( pTrans.observaciones ), 40 )
    Empleado mgr = null
    if ( site.idGerente != null ) {
      mgr = empleadoRepository.findById( site.idGerente )
    }
    if ( InventorySearch.esTipoTransaccionSalida( pTrans.idTipoTrans ) ) {
      def tkInvTr = [
          nombre_ticket: "ticket-devolucion",
          effDate: adapter.getText( pTrans, adapter.FLD_TR_EFF_DATE ),
          compania: companyAddress,
          sucursal: site,
          user: adapter.getText( emp ),
          mgr: adapter.getText( mgr ),
          ticket: adapter.getText( pTrans, adapter.FLD_SRC_TICKET ),
          empName: adapter.getText( pTrans, adapter.FLD_SALES_PERSON ),
          returnAmount: adapter.getText( pTrans, adapter.FLD_RETURN_AMOUNT ),
          trNbr: adapter.getText( pTrans, adapter.FLD_TR_NBR ),
          remarks_1: ( remarks.size() > 0 ? remarks.get( 0 ) : "" ),
          remarks_2: ( remarks.size() > 1 ? remarks.get( 1 ) : "" ),
          parts: parts
      ]
      imprimeTicket( "template/ticket-salida-inventario.vm", tkInvTr )
    } else if ( InventorySearch.esTipoTransaccionAjuste( pTrans.idTipoTrans ) ) {
      def tkInvTr = [
          nombre_ticket: "ticket-ajuste-inventario",
          effDate: adapter.getText( pTrans, adapter.FLD_TR_EFF_DATE ),
          thisSite: adapter.getText( site ),
          user: adapter.getText( emp ),
          mgr: adapter.getText( mgr ),
          trNbr: adapter.getText( pTrans, adapter.FLD_TR_NBR ),
          siteTo: adapter.getText( siteTo ),
          remarks_1: ( remarks.size() > 0 ? remarks.get( 0 ) : "" ),
          remarks_2: ( remarks.size() > 1 ? remarks.get( 1 ) : "" ),
          parts: parts
      ]
      imprimeTicket( "template/ticket-ajuste-inventario.vm", tkInvTr )
    }
  }

  void imprimeCotizacion( Integer pQuoteId ) {
    Cotizacion quote = RepositoryFactory.quotes.findOne( pQuoteId )
    if ( quote != null ) {
      log.debug( String.format( 'Print Cotizacion:%d', pQuoteId ) )
      Sucursal site = sucursalRepository.findOne( Registry.currentSite )
      Empleado salesmen = empleadoRepository.findOne( quote.idEmpleado )
      Cliente customer = clienteRepository.findOne( quote.idCliente )
      double totalAmt = 0
      def tkParts = [ ]
      for ( CotizaDet det : RepositoryFactory.quoteDetail.findByIdCotiza( quote.idCotiza ) ) {
        Articulo part = articuloRepository.findOne( det.sku )
        String cantidad = ( det.cantidad != 1 ? String.format( '(%d@%,.2f)', det.cantidad, part.precio ) : '' )
        String price = String.format( '$%,.2f', det.cantidad * part.precio )
        totalAmt += ( det.cantidad * part.precio )
        def tkPart = [
            desc: String.format( '[%d] %s %s  %s', part.id, part.generico?.descripcion, part.marca, cantidad ),
            price: price
        ]
        tkParts.add( tkPart )
      }
      def tkCotiza = [
          company: Registry.companyShortName,
          quoteNbr: StringUtils.right( String.format( "      %d", quote.idCotiza ), 6 ),
          quoteDate: CustomDateUtils.format( quote.fechaMod, 'dd-MM-yyyy' ),
          site: ( site != null ? site.nombre : '' ),
          phone: ( site != null ? site.telefonos : '' ),
          empName: String.format( '(%s) %s', quote.idEmpleado, ( salesmen != null ? salesmen.nombreCompleto : '' ) ),
          custName: customer.nombreCompleto,
          remarks: quote.observaciones,
          parts: tkParts,
          totalPrice: String.format( '$%,.2f', totalAmt ),
          quoteExpires: CustomDateUtils.format( DateUtils.addDays( quote.fechaMod, 30 ), 'dd MMMM yyyy' )
      ]
      this.imprimeTicket( 'template/ticket-cotizacion-simple.vm', tkCotiza )
    } else {
      log.debug( String.format( 'Cotizacion (%d) not found.', pQuoteId ) )
    }
  }

  void imprimeTransaccionesInventario( Date fechaTransacciones ){
    log.debug( "imprimeTransaccionesInventario" )

    if( fechaTransacciones != null ){
    DateFormat df = new SimpleDateFormat( "dd/MM/yyyy" )
    Sucursal site = ServiceFactory.sites.obtenSucursalActual()
    Date fechaStart = DateUtils.truncate( fechaTransacciones, Calendar.DAY_OF_MONTH )
    Date fechaEnd = new Date( DateUtils.ceiling( fechaTransacciones, Calendar.DAY_OF_MONTH ).getTime() - 1 )
    List <TransaccionesDiarias> lstEntradas = new ArrayList<>()
    List <TransaccionesDiarias> lstSalidas = new ArrayList<>()
    List <TransaccionesDiarias> lstVentas = new ArrayList<>()
    Integer entradas = 0
    Integer salidas = 0
    Integer ventas = 0
    QTransInvDetalle detalle = QTransInvDetalle.transInvDetalle
    List <TransInvDetalle> lstDetalles = transInvDetalleRepository.findAll( detalle.transInv.fecha.eq(fechaTransacciones),
                                                                            detalle.sku.asc() )
    for(TransInvDetalle transaccion : lstDetalles ){
      Articulo articulo = articuloRepository.findOne( transaccion.sku )
      if( articulo.idGenerico.trim().equalsIgnoreCase('A') ){
        if(transaccion.tipoMov.trim().equalsIgnoreCase(TAG_TRANSACCION_ENTRADA) ){
            TransaccionesDiarias transacciones = findOrCreate( lstEntradas, articulo.marca )
            transacciones.AcumulaTransacciones( transaccion )
        }
        if(transaccion.tipoMov.trim().equalsIgnoreCase(TAG_TRANSACCION_SALIDA) &&
                !transaccion.idTipoTrans.trim().equalsIgnoreCase(TAG_TRANSACCION_VENTA) ){
            TransaccionesDiarias transacciones = findOrCreate( lstSalidas, articulo.marca )
            transacciones.AcumulaTransacciones( transaccion )
        }
        if(transaccion.tipoMov.trim().equalsIgnoreCase(TAG_TRANSACCION_SALIDA) &&
                transaccion.idTipoTrans.trim().equalsIgnoreCase(TAG_TRANSACCION_VENTA) ){
            TransaccionesDiarias transacciones = findOrCreate( lstVentas, articulo.marca )
            transacciones.AcumulaTransacciones( transaccion )
        }
      }
    }
    for(TransaccionesDiarias entrada : lstEntradas){
      entradas = entradas+entrada.cantidad
    }
    for(TransaccionesDiarias salida : lstSalidas){
        salidas = salidas+salida.cantidad
    }
    for(TransaccionesDiarias venta : lstVentas){
       ventas = ventas+venta.cantidad
    }

    def tkTransacciones = [
        effDate: df.format( fechaTransacciones ),
        thisSite: TransInvAdapter.instance.getText( site ),
        entradas: lstEntradas,
        cantEntradas: entradas,
        existEntradas: lstEntradas.size() > 0 ? true : false,
        salidas: lstSalidas,
        cantSalidas: salidas,
        existSalidas: lstSalidas.size() > 0 ? true : false,
        ventas: lstVentas,
        cantVentas: ventas,
        existVentas: lstVentas.size() > 0 ? true : false,
    ]
      this.imprimeTicket( 'template/ticket-transacciones.vm', tkTransacciones )
    } else {
      log.debug( String.format( 'Fecha (%d) not found.', fechaTransacciones ) )
    }
  }

  private TransaccionesDiarias findOrCreate( List<TransaccionesDiarias> lstTransacciones, String marca ) {
      TransaccionesDiarias found = null
      for ( TransaccionesDiarias transaccion : lstTransacciones ) {
          if ( transaccion.marca.equals( marca ) ) {
              found = transaccion
              break
          }
      }
      if ( found == null ) {
          found = new TransaccionesDiarias( marca )
          lstTransacciones.add( found )
      }
      return found
  }



  void imprimeDiferencias(  ){
    log.debug('imprimeDiferencias(  )')
    DateFormat df = new SimpleDateFormat( "dd-MM-yyyy" )
    Sucursal site = sucursalRepository.findOne( Registry.currentSite )
    String sucursal = site.nombre+' ['+site.id+']'
    QDiferencia qDiferencia = QDiferencia.diferencia
    List<Diferencia> lstDiferencias = diferenciaRepository.findAll( qDiferencia.diferencias.isNotNull().
            and(qDiferencia.diferencias.goe(1).or(qDiferencia.diferencias.loe(-1))), qDiferencia.id.asc() )
    def diferencias = []
    for( Diferencia dif : lstDiferencias ){
      String articulo = ""
      String color = ""
      if( dif.articulo != null ){
        if( StringUtils.trimToEmpty(dif.articulo.articulo).contains("-") ){
          String[] data = StringUtils.trimToEmpty(dif.articulo.articulo).split("-")
          if( data.size() > 1 ){
            articulo = StringUtils.trimToEmpty(data[0])
            color = StringUtils.trimToEmpty(data[1])
          } else {
            articulo = StringUtils.trimToEmpty(data[0])
          }
        } else {
          articulo = StringUtils.trimToEmpty(dif.articulo.articulo)
          color = StringUtils.trimToEmpty(dif.articulo.codigoColor)
        }
      }
      def diferencia = [
        id: dif.id,
        articulo: articulo,
        color: color,
        cantidadFisico: dif.cantidadFisico,
        cantidadSoi: dif.cantidadSoi,
        diferencias: dif.diferencias < 0 ? StringUtils.trimToEmpty(dif.diferencias.toString()) : " "+StringUtils.trimToEmpty(dif.diferencias.toString())
      ]
        diferencias.add(diferencia)
    }
    if(lstDiferencias.size() > 0){
        def datos = [
            nombre_ticket: "ticket-diferencias",
            thisSite: sucursal,
            date: df.format( new Date() ),
            diferencias: diferencias
        ]
        imprimeTicket( 'template/ticket-diferencias.vm', datos )
    } else {
        log.debug( String.format( 'No existen registros de diferencias' ) )
    }
  }



  void imprimeIncidencias( Incidencia incidencia ){
    log.debug( "imprimeDiferencias( )" )
      DateFormat df = new SimpleDateFormat( "dd-MM-yyyy" )
      Sucursal site = sucursalRepository.findOne( Registry.currentSite )
      String sucursal = site.nombre+' ['+site.id+']'
      String empleadoCapturo = '['+incidencia.idEmpleadoCap+']' +incidencia.nombreCap
      String empleado = '['+incidencia.idEmpleado+']' +incidencia.nombre
      if(incidencia != null){
          def datos = [
                  nombre_ticket: "ticket-incidencias",
                  thisSite: sucursal,
                  empleado_capturo: empleadoCapturo,
                  empleado: empleado,
                  date: df.format( incidencia.fecha ),
                  incidencia: incidencia
          ]
          imprimeTicket( 'template/ticket-incidencias.vm', datos )
      } else {
          log.debug( String.format( 'No existe registro de incidencias' ) )
      }
  }



  void imprimeGarantia( BigDecimal montoGarantia, Integer idArticulo, String idNota ){
    log.debug( "imprimeGarantia( )" )
    NotaVenta notaVenta = notaVentaRepository.findOne( StringUtils.trimToEmpty(idNota) )
    if( notaVenta != null && StringUtils.trimToEmpty(notaVenta.udf4).length() > 2 ){
      DateFormat df = new SimpleDateFormat( "dd-MM-yy" )
      DateFormat df2 = new SimpleDateFormat( "ddMMyy" )
      Sucursal site = ServiceFactory.sites.obtenSucursalActual()
      AddressAdapter companyAddress = Registry.companyAddress
      String[] dataSeg = StringUtils.trimToEmpty(notaVenta.udf4).split(/\|/)
      Integer contador = 0
      for(String d : dataSeg){
        if( d.contains(StringUtils.trimToEmpty(idArticulo.toString())) ){
          break
        } else {
          contador = contador+1
        }
      }
      String[] key = StringUtils.trimToEmpty(dataSeg[contador]).split(",")
      if( key.length > 1 ){
        String clave = key[0]
        Integer idArt = idArticulo
        String txtDate = clave.substring(0,6)
        String txtDateFormat = ""
        for(int i=0;i<txtDate.length();i++){
          if(txtDate.charAt(i).isDigit()){
            Integer number = 0
            try{
              number = NumberFormat.getInstance().parse(StringUtils.trimToEmpty(txtDate.charAt(i).toString())).intValue()
            } catch (NumberFormatException e){
              println e.message
            }
            txtDateFormat = txtDateFormat+StringUtils.trimToEmpty((10-number).toString())
          } else {
            txtDateFormat = txtDateFormat+"0"
          }
        }
        Date fecha = df2.parse(txtDateFormat)
        String date = df.format(fecha)
        try{
          idArt = NumberFormat.getInstance().parse(StringUtils.trimToEmpty(key[1]))
        } catch ( NumberFormatException e ){ print e.message }
        Articulo articulo = articuloRepository.findOne( idArt )
        def data = [
                date: date,
                thisSite: site,
                compania: companyAddress,
                codaleatorio: StringUtils.trimToEmpty(clave),
                articulo: articulo != null ? StringUtils.trimToEmpty(articulo.articulo) : ""
        ]
        imprimeTicket( "template/ticket-garantia.vm", data )
      }
    }
  }


  void imprimeVoucherTpv( Pago pago, String copia, Boolean reimpresion ){
    log.debug( "imprimeVoucherTpv( )" )
    NumberFormat formatter = NumberFormat.getCurrencyInstance( Locale.US )
    AddressAdapter companyAddress = Registry.companyAddress
    Boolean meses = false
    Integer months = 0
    if(StringUtils.trimToEmpty(pago.idPlan).length() > 0 && StringUtils.trimToEmpty(pago.idPlan).isNumber() &&
            !StringUtils.trimToEmpty(pago.idFPago).equalsIgnoreCase(TAG_FORMA_PAGO_TCD)){
      try{
        months = NumberFormat.getInstance().parse(StringUtils.trimToEmpty(pago.idPlan)).intValue()
      } catch ( NumberFormatException e ){ println e }
      if( months > 1 ){
        meses = true
      }
    }
    String fecha = pago.fecha.format("dd-MM-yyyy")
    String hora= pago.fecha.format("HH:mm")
    String idSucursal = parametroRepository.findOne( TipoParametro.ID_SUCURSAL.value )?.valor
    Sucursal sucursal = sucursalRepository.findOne( idSucursal?.toInteger() )
    String dataTmp = pago.idTerminal.replace("||","| |")
    String[] data = dataTmp.split(/\|/)
    String producto = ""
    String operacion = ""
    String aid = ""
    String arqc = ""
    String cliente = ""
    String lecturaTar = ""
    if( data.length >= 6 ){
      producto = data[0]
      operacion = data[1]
      aid = data[2]
      arqc = data[3]
      cliente = data[4]
      lecturaTar = data[5]
    }
    Double importe = pago.monto.doubleValue()
    if( StringUtils.trimToEmpty(pago.idFPago).equalsIgnoreCase(TAG_FORMA_PAGO_TCD) ){
      try{
        importe = NumberFormat.getInstance().parse(StringUtils.trimToEmpty(pago.idPlan)).doubleValue()
      } catch ( NumberFormatException e ) { println e }
    }
    String ticket = StringUtils.trimToEmpty((Registry.currentSite+2000).toString())+"-"+StringUtils.trimToEmpty(pago.notaVenta.factura)
    if(pago != null){
      def datos = [
          fecha: fecha,
          hora: hora,
          copia: copia,
          nombreEmpresa: StringUtils.trimToEmpty(sucursal?.nombre),
          direccionEmpresa1: StringUtils.trimToEmpty(sucursal?.direccion),
          direccionEmpresa2: StringUtils.trimToEmpty(sucursal?.colonia),
          direccionEmpresa3: StringUtils.trimToEmpty(sucursal?.idLocalidad),
          tarjeta: pago.referenciaPago,
          producto: producto,
          meses: meses,
          tipo: StringUtils.trimToEmpty(pago.idFPago).startsWith(TAG_FORMA_PAGO_TC) ? "CREDITO" : "DEBITO",
          ticket: ticket,
          plan: months,
          estatus: "APROBADA",
          numAutorizacion: pago.referenciaClave,
          operacion: operacion,
          aid: aid,
          arqc: arqc,
          cliente: cliente,
          importe: formatter.format(importe),
          reimpresion: reimpresion,
          moneda: StringUtils.trimToEmpty(pago.idFPago).equalsIgnoreCase(TAG_FORMA_PAGO_TCD) ? "USD" : "MXN",
          afiliacion: StringUtils.trimToEmpty(pago.idFPago).equalsIgnoreCase(TAG_FORMA_PAGO_TCD) ? StringUtils.trimToEmpty(Registry.noAfiliacionUsd) : StringUtils.trimToEmpty(Registry.noAfiliacion),
          lecturaTar: lecturaTar
      ]
      imprimeTicket( 'template/ticket-tpv.vm', datos )
    } else {
      log.debug( String.format( 'No existe registro de tarjeta' ) )
    }
  }



  void imprimeResumenTarjetas( Date fechaCierre ){
    log.debug( "imprimeVoucherTpv( )" )
    NumberFormat formatter = NumberFormat.getCurrencyInstance( Locale.US )
    Date fechaInicio = DateUtils.truncate( fechaCierre, Calendar.DAY_OF_MONTH );
    Date fechaFin = new Date( DateUtils.ceiling( fechaCierre, Calendar.DAY_OF_MONTH ).getTime() - 1 );
    QPago qPago = QPago.pago
    List<Pago> lstPagos = pagoRepository.findAll( qPago.fecha.between(fechaInicio,fechaFin) ) as List<Pago>
    List<Modificacion> lstModificacion = modificacionRepository.findByFechaBetween(fechaInicio,fechaFin) as List<Modificacion>
    Collections.sort(lstModificacion, new Comparator<Modificacion>() {
        @Override
        int compare(Modificacion o1, Modificacion o2) {
            return o1.idFactura.compareTo(o2.idFactura)
        }
    })
    List<Pago> selected = new ArrayList<Pago>()
    for ( Pago p : lstPagos ) {
      if ( p.idTerminal.contains("|") ) {
        if( !StringUtils.trimToEmpty(p.notaVenta.factura).isEmpty() ){
          selected.add( p )
        }
      }
    }
    List<Pago> pagos = new ArrayList<>()
    BigDecimal pagosMonto = BigDecimal.ZERO
    def pagosCan = [ ]
    for(Pago pago : selected){
      //pago.idRecibo = String.format('%1$#15s', formatter.format(pago.monto) );
      String data = StringUtils.trimToEmpty(formatter.format(pago.monto))
      Integer sizeData = data.length()
      for(int i=0;i<15-sizeData;i++){
        data = " "+data
      }
      pago.idRecibo = String.format('%s', data );
      if(StringUtils.trimToEmpty(pago.idFPago).equalsIgnoreCase("UV") || StringUtils.trimToEmpty(pago.idPlan).equalsIgnoreCase("1")){
        pago.idPlan = ""
      }
      pagos.add(pago)
      pagosMonto = pagosMonto.add(pago.monto)
    }
    for(Modificacion modificacion : lstModificacion){
      for(Pago pago : modificacion.notaVenta.pagos){
        String status = ""
        if( StringUtils.trimToEmpty(modificacion.fecha.format("dd/MM/yyyy")).equalsIgnoreCase(StringUtils.trimToEmpty(modificacion.notaVenta.fechaHoraFactura.format("dd/MM/yyyy"))) ){
          status = "C"
        } else {
          status = "D"
        }
        if( pago.idTerminal.contains("|") ){
          if(StringUtils.trimToEmpty(pago.idFPago).equalsIgnoreCase("UV") || StringUtils.trimToEmpty(pago.idPlan).equalsIgnoreCase("1")){
            pago.idPlan = ""
          }
            String data1 = StringUtils.trimToEmpty(formatter.format(pago.monto))
            Integer sizeData = data1.length()
            for(int i=0;i<15-sizeData;i++){
                data1 = " "+data1
            }
            pago.idRecibo = String.format('%s', data1 );
          def data = [
            factura: StringUtils.trimToEmpty(modificacion.notaVenta.factura),
            plan: StringUtils.trimToEmpty(pago.idPlan),
            importe: String.format('%s', "(${StringUtils.trimToEmpty(data1)})${status}")
          ]
          pagosCan.add(data)
          pagosMonto = pagosMonto.subtract(pago.monto)
        }
      }
    }
    Collections.sort(pagos, new Comparator<Pago>() {
        @Override
        int compare(Pago o1, Pago o2) {
            return o1.notaVenta.factura.compareTo(o2.notaVenta.factura)
        }
    })
    if(selected.size() > 0 || lstModificacion.size() > 0){
      def datos = [
            fecha: fechaCierre.format("dd/MM/yyyy"),
            pagos: pagos,
            pagosCan: pagosCan,
            total: pagos.size()+pagosCan.size(),
            totalMonto: formatter.format(pagosMonto),
      ]
      imprimeTicket( 'template/ticket-resumen-tarjetas.vm', datos )
    } else {
      log.debug( String.format( 'No existen pagos con tarjeta por tpv' ) )
    }
  }



  void imprimeVoucherCancelacionTpv( Integer idPago, String copia, String transaccion ){
      log.debug( "imprimeVoucherCancelacionTpv( )" )
      NumberFormat formatter = NumberFormat.getCurrencyInstance( Locale.US )
      AddressAdapter companyAddress = Registry.companyAddress
      Boolean meses = false
      Integer months = 0
      Pago pago = pagoRepository.findOne(idPago)
      if(StringUtils.trimToEmpty(pago.idPlan).length() > 0 && StringUtils.trimToEmpty(pago.idPlan).isNumber() &&
              !StringUtils.trimToEmpty(pago.idFPago).equalsIgnoreCase(TAG_FORMA_PAGO_TCD)){
        try{
          months = NumberFormat.getInstance().parse(StringUtils.trimToEmpty(pago.idPlan)).intValue()
        } catch ( NumberFormatException e ){ println e }
      }
      if( months > 1 ){
          meses = true
      }
      String fecha = pago.fecha.format("dd-MM-yyyy")
      String hora= pago.fecha.format("HH:mm")
      String idSucursal = parametroRepository.findOne( TipoParametro.ID_SUCURSAL.value )?.valor
      Sucursal sucursal = sucursalRepository.findOne( idSucursal?.toInteger() )
      String[] data = pago.idTerminal.split(/\|/)
      String producto = ""
      String operacion = ""
      String aid = ""
      String arqc = ""
      String cliente = ""
      String lecturaTar = ""
      if( data.length >= 6 ){
          producto = data[0]
          operacion = data[1]
          aid = data[2]
          arqc = data[3]
          cliente = data[4]
          lecturaTar = data[5]
      }
      Double importe = pago.monto.doubleValue()
      if( StringUtils.trimToEmpty(pago.idFPago).equalsIgnoreCase(TAG_FORMA_PAGO_TCD) ){
          try{
              importe = NumberFormat.getInstance().parse(StringUtils.trimToEmpty(pago.idPlan)).doubleValue()
          } catch ( NumberFormatException e ) { println e }
      }
      if(pago != null){
          def datos = [
                  fecha: fecha,
                  hora: hora,
                  copia: copia,
                  transaccion: transaccion,
                  nombreEmpresa: StringUtils.trimToEmpty(sucursal?.nombre),
                  direccionEmpresa1: StringUtils.trimToEmpty(sucursal?.direccion),
                  direccionEmpresa2: StringUtils.trimToEmpty(sucursal?.colonia),
                  direccionEmpresa3: StringUtils.trimToEmpty(sucursal?.idLocalidad),
                  tarjeta: pago.referenciaPago,
                  producto: producto,
                  meses: meses,
                  tipo: StringUtils.trimToEmpty(pago.idFPago).startsWith(TAG_FORMA_PAGO_TC) ? "CREDITO" : "DEBITO",
                  plan: months,
                  estatus: "APROBADA",
                  numAutorizacion: pago.referenciaClave,
                  operacion: operacion,
                  aid: aid,
                  arqc: arqc,
                  cliente: cliente,
                  importe: String.format("-%s",formatter.format(importe)),
                  moneda: StringUtils.trimToEmpty(pago.idFPago).equalsIgnoreCase(TAG_FORMA_PAGO_TCD) ? "USD" : "MXN",
                  afiliacion: StringUtils.trimToEmpty(pago.idFPago).equalsIgnoreCase(TAG_FORMA_PAGO_TCD) ? StringUtils.trimToEmpty(Registry.noAfiliacionUsd) : StringUtils.trimToEmpty(Registry.noAfiliacion),
                  lecturaTar: lecturaTar
          ]
          imprimeTicket( 'template/ticket-tpv-can.vm', datos )
      } else {
          log.debug( String.format( 'No existe registro de tarjeta' ) )
      }
  }



  private static ResumenDiario findOrCreateRD( List<ResumenDiario> lstResumenesDiarios, ResumenDiario pResumenDiario ) {
    ResumenDiario found = null
    for ( ResumenDiario resumenDiario : lstResumenesDiarios ) {
      if ( resumenDiario.idTerminal.equals( pResumenDiario.idTerminal ) ) {
        found = resumenDiario
        //resumenDiario.importe = resumenDiario.importe.add(pResumenDiario.importe)
        break
      }
    }
    if ( found == null ) {
      found = pResumenDiario
      lstResumenesDiarios.add( found )
    }
    return found
  }*/
}

