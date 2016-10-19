package mx.wen.pos.repository.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import mx.wen.pos.repository.*

@Component
class RepositoryFactory {

  /*private static ClienteRepository customerCatalog
  private static DescuentoRepository discounts
  private static GenericoRepository genres
  private static GrupoArticuloRepository groupPartMaster
  private static GrupoArticuloDetRepository groupPartDetail
  private static TipoTransInvRepository trTypes
  private static OrdenPromDetRepository orderLinePromotionDetail
  private static OrdenPromRepository orderPromotionDetail*/
  private static TransInvRepository inventoryMaster
  private static TransInvDetalleRepository inventoryDetail
  private static NotaVentaRepository orders
  private static DetalleNotaVentaRepository orderLines
  private static ArticuloRepository partMaster
  private static PagoRepository payments
  /*private static PromocionRepository promotionCatalog
  private static PrecioRepository priceCatalog*/
  private static ParametroRepository registry
  /*private static ImpuestoRepository taxMaster
  private static ContribuyenteRepository rfcMaster
  private static EstadoRepository states
  private static MonedaRepository currencyCatalog
  private static MonedaDetalleRepository fxRates
  private static TerminalRepository posCatalog
  private static ModificacionRepository orderModifications
  private static CierreDiarioRepository dailyClose
  private static MensajeRepository msgCatalog*/
  private static EmpleadoRepository employeeCatalog
  /*private static AcuseRepository acknowledgements
  private static ModificacionRepository adjustments
  private static CotizacionRepository quotes
  private static CotizaDetRepository quoteDetail
  private static TipoTransInvRepository typeTransaction
  private static RemesasRepository pRemesasRepository*/
  private static SucursalRepository pSucursalRepository

  @Autowired
  RepositoryFactory( ArticuloRepository pArticuloRepository,
                     /*ClienteRepository pClienteRepository,
                     PrecioRepository pPrecioRepository,
                     PromocionRepository pPromocionRepository,
                     GrupoArticuloRepository pGrupoArticuloRepository,
                     GrupoArticuloDetRepository pGrupoArticuloDetRepository,
                     OrdenPromRepository pOrdenPromRepository,
                     OrdenPromDetRepository pOrdenPromDetRepository,*/
                     NotaVentaRepository pNotaVentaRepository,
                     DetalleNotaVentaRepository pDetalleNotaVentaRepository,
                     PagoRepository pPagoRepository,
                     ParametroRepository pParametroRepository,
                     TransInvRepository pTransInvRepository,
                     TransInvDetalleRepository pTransInvDetalleRepository,
                     /*DescuentoRepository pDescuentoRepository,
                     GenericoRepository pGenericoRepository,
                     TipoTransInvRepository pTipoTransInvRepository,
                     ImpuestoRepository pImpuestoRepository,
                     ContribuyenteRepository pContribuyenteRepository,
                     EstadoRepository pEstadoRepository,
                     MonedaRepository pMonedaRepository,
                     MonedaDetalleRepository pMonedaDetalleRepository,
                     TerminalRepository pTerminalRepository,
                     ModificacionRepository pModificationRepository,
                     CierreDiarioRepository pCierreDiarioRepository,
                     MensajeRepository pMensajeRepository,*/
                     EmpleadoRepository pEmpleadoRepository,
                     /*AcuseRepository pAcuseRepository,
                     ModificacionRepository pModificacionRepository,
        CotizacionRepository pCotizacionRepository, CotizaDetRepository pCotizaDetRepository,
        TipoTransInvRepository pTypeTransaction,
        RemesasRepository remesasRepository,*/
        SucursalRepository sucursalRepository
  ) {
    /*customerCatalog = pClienteRepository
    discounts = pDescuentoRepository
    groupPartMaster = pGrupoArticuloRepository
    groupPartDetail = pGrupoArticuloDetRepository
    */
    orders = pNotaVentaRepository
    orderLines = pDetalleNotaVentaRepository
    partMaster = pArticuloRepository
    /*priceCatalog = pPrecioRepository
    promotionCatalog = pPromocionRepository
    orderPromotionDetail = pOrdenPromRepository
    orderLinePromotionDetail = pOrdenPromDetRepository*/
    payments = pPagoRepository
    registry = pParametroRepository
    inventoryMaster = pTransInvRepository
    inventoryDetail = pTransInvDetalleRepository
    /*genres = pGenericoRepository
    trTypes = pTipoTransInvRepository
    taxMaster = pImpuestoRepository
    rfcMaster = pContribuyenteRepository
    states = pEstadoRepository
    currencyCatalog = pMonedaRepository
    fxRates = pMonedaDetalleRepository
    posCatalog = pTerminalRepository
    orderModifications = pModificationRepository
    dailyClose = pCierreDiarioRepository
    msgCatalog = pMensajeRepository*/
    employeeCatalog = pEmpleadoRepository
    /*acknowledgements = pAcuseRepository
    adjustments = pModificacionRepository
    quotes = pCotizacionRepository
    quoteDetail = pCotizaDetRepository
    typeTransaction = pTypeTransaction
    pRemesasRepository = remesasRepository*/
    pSucursalRepository = sucursalRepository
  }

  /*static ClienteRepository getCustomerCatalog( ) {
    return customerCatalog
  }


  static DescuentoRepository getDiscounts( ) {
    return discounts
  }

  static GenericoRepository getGenres( ) {
    return genres
  }

  static GrupoArticuloRepository getGroupPartMaster( ) {
    return groupPartMaster
  }

  static GrupoArticuloDetRepository getGroupPartDetail( ) {
    return groupPartDetail
  }

  static TipoTransInvRepository getTrTypes( ) {
    return trTypes
  }

  static OrdenPromRepository getOrderPromotionDetail( ) {
    return orderPromotionDetail
  }

  static OrdenPromDetRepository getOrderLinePromotionDetail( ) {
    return orderLinePromotionDetail
  }*/

  static TransInvDetalleRepository getInventoryDetail( ) {
    return inventoryDetail
  }

  static TransInvRepository getInventoryMaster( ) {
    return inventoryMaster
  }

  static NotaVentaRepository getOrders( ) {
    return orders
  }

  static DetalleNotaVentaRepository getOrderLines( ) {
    return orderLines
  }

  static ArticuloRepository getPartMaster( ) {
    return partMaster
  }

  static PagoRepository getPayments( ) {
    return payments
  }

    /*static PrecioRepository getPriceCatalog( ) {
      return priceCatalog
    }

    static PromocionRepository getPromotionCatalog( ) {
      return promotionCatalog
    }*/

  static ParametroRepository getRegistry( ) {
    return registry
  }

  /*static ImpuestoRepository getTaxMaster( ) {
    return taxMaster
  }

  static ContribuyenteRepository getRfcMaster( ) {
    return rfcMaster
  }

  static EstadoRepository getStates( ) {
    return states
  }

  static MonedaRepository getCurrencyCatalog( ) {
    return currencyCatalog
  }

  static MonedaDetalleRepository getFxRates( ) {
    return fxRates
  }

  static TerminalRepository getPosCatalog( ) {
    return posCatalog
  }

  static ModificacionRepository getOrderModifications( ) {
    return orderModifications
  }

  static CierreDiarioRepository getDailyClose( ) {
    return dailyClose
  }

  static MensajeRepository getMessageCatalog( ) {
    return msgCatalog
  }*/

  static EmpleadoRepository getEmployeeCatalog( ) {
    return employeeCatalog
  }

  /*static AcuseRepository getAcknowledgements( ) {
    return acknowledgements
  }

  static ModificacionRepository getAdjustments( ) {
    return adjustments
  }

  static CotizacionRepository getQuotes() {
    return quotes
  }

  static CotizaDetRepository getQuoteDetail() {
    return quoteDetail
  }

  static TipoTransInvRepository getTypeTransaction(){
      return typeTransaction
  }


  static RemesasRepository getRemittanceRepository() {
    return pRemesasRepository
  }*/

  static SucursalRepository getSiteRepository(){
    return pSucursalRepository
  }
}
