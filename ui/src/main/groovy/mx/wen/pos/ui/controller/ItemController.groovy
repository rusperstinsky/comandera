package mx.wen.pos.ui.controller

import groovy.util.logging.Slf4j
import mx.wen.pos.model.Articulo
import mx.wen.pos.service.ArticuloService
import mx.wen.pos.ui.model.Item

/*import mx.wen.pos.model.Articulo
import mx.wen.pos.model.DetalleNotaVenta
import mx.wen.pos.model.Diferencia
import mx.wen.pos.model.InventarioFisico
import mx.wen.pos.model.MontoGarantia
import mx.wen.pos.model.NotaVenta
import mx.wen.pos.model.Sucursal
import mx.wen.pos.repository.impl.RepositoryFactory
import mx.wen.pos.service.ArticuloService
import mx.wen.pos.service.DetalleNotaVentaService
import mx.wen.pos.service.NotaVentaService
import mx.wen.pos.service.TicketService
import mx.wen.pos.ui.model.Differences
import mx.wen.pos.ui.model.Item
import mx.wen.pos.ui.view.dialog.ChargeDialog
import mx.wen.pos.ui.view.dialog.DifferencesDialog
import mx.wen.pos.ui.view.dialog.WaitDialog*/
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/*import mx.wen.pos.model.QArticulo
import mx.wen.pos.ui.view.dialog.ImportPartMasterDialog*/
import javax.swing.JOptionPane
import javax.swing.JDialog
import mx.wen.pos.service.business.Registry

import java.text.NumberFormat

@Slf4j
@Component
class ItemController {

  private static final String MSJ_ARCHIVO_GENERADO = 'El archivo de inventario fue cargado correctamente'
  private static final String TXT_ARCHIVO_GENERADO = 'Archivo de Inventario'
  private static final String MSJ_ARCHIVO_NO_GENERADO = 'No se genero correctamente el archivo de inventario'
  private static final String TXT_DIFERENCIAS = 'Diferencias'
  private static final String MSJ_DIFERENCIAS_NO_RECIBIDAS = 'No se recibieron correctamente las diferencias'

  private static final String MSJ_INV_FISICO_NO_ENVIADO = 'No se han podido enviar los datos del inventario'
  private static final String MSJ_INV_FISICO_ENVIADO = 'Se han enviado correctamente los datos del inventario'

  private static final String MSJ_INV_FISICO_NO_INICIALIZADO = 'No se han podido inicializar el inventario'
  private static final String MSJ_INV_FISICO_INICIALIZADO = 'Se ha inicializado correctamente el inventario'

  private static ArticuloService articuloService
  /*private static TicketService ticketService
  private static DetalleNotaVentaService detalleNotaVentaService
  private static NotaVentaService notaVentaService*/

  @Autowired
  public ItemController( ArticuloService articuloService/*, TicketService ticketService, DetalleNotaVentaService detalleNotaVentaService,
                         NotaVentaService notaVentaService*/ ) {
    this.articuloService = articuloService
    /*this.ticketService = ticketService
    this.detalleNotaVentaService = detalleNotaVentaService
    this.notaVentaService = notaVentaService*/
  }

  static Item findItem( Integer id ) {
    log.debug( "obteniendo articulo con id: ${id}" )
    Item.toItem( articuloService.obtenerArticulo( id ) )
  }

  static List<Item> findItems( String code ) {
    log.debug( "buscando articulos con articulo: ${code}" )
    def results = articuloService.listarArticulosPorCodigo( code )
    results.collect {
      Item.toItem( it )
    }
  }

  static List<Item> findItemsBySubtype( String subtype ) {
    log.debug( "buscando articulos con subtipo: ${subtype}" )
    def results = articuloService.listarArticulosPorSubtipo( subtype )
    results.collect {
      Item.toItem( it )
    }
  }

  static List<Item> findItemsLike( String input ) {
    log.debug( "buscando articulos con articulo similar a: $input" )
    def results = articuloService.listarArticulosPorCodigoSimilar( input )
    results.collect {
      Item.toItem( it )
    }
  }

  static List<Item> findItemsByQuery( final String query, String tipo ) {
    log.debug( "buscando de articulos con query: $query" )
    if ( StringUtils.isNotBlank( query ) || StringUtils.isNotBlank( tipo ) ) {
      List<Articulo> items = findPartsQuery( query, tipo )
      if (items.size() > 0) {
        log.debug( "Items:: ${items.first()?.dump()} " )
        return items?.collect { Item.toItem( it ) }
      }
    }
    return [ ]
  }

  static List<Articulo> findPartsQuery( final String query, String tipo ) {
    List<Articulo> items = [ ]
    if ( StringUtils.isNotBlank( query ) || StringUtils.isNotBlank( tipo ) ) {
      if( StringUtils.trimToEmpty( tipo ).length() > 0 ){
        log.debug( "busqueda por id exacto ${query}" )
        items = articuloService.obtenerArticuloPorTipo( tipo )
      } else if ( query.integer ) {
        log.debug( "busqueda por id exacto ${query}" )
        Articulo articulo = articuloService.obtenerArticulo( query.toInteger() )
        if( articulo != null ){
          items.add( articulo )
        }
      } else {
        def anyMatch = '*'
        if ( query.contains( anyMatch ) ) {
          def tokens = query.tokenize( anyMatch )
          def code = tokens?.first() ?: null
          log.debug( "busqueda con codigo similar: ${code}" )
          items = articuloService.listarArticulosPorCodigoSimilar( code ) ?: [ ]
        } else {
          def tokens = query.replaceAll( /[+|,]/, '|' ).tokenize( '|' )
          def code = tokens?.first() ?: null
          log.debug( "busqueda con codigo exacto: ${code}" )
          items = articuloService.listarArticulosPorCodigo( code ) ?: [ ]
        }
      }
    }
    return items
  }

    static List<Item> findItemByArticleAndColor( String query, String color  ) {
        log.debug( "buscando de un articulo con query: $query" )
        if ( StringUtils.isNotBlank( query ) ) {

            List<Articulo> items = new ArrayList<Articulo>()
            try{
            items = articuloService.findArticuloyColor( query, color )
            } catch( Exception e ){
                System.out.println( e )
            }
            return items?.collect { Item.toItem( it ) }
        }
        return [ ]
    }

  static String getManualPriceTypeList( ) {
    String list = articuloService.obtenerListaGenericosPrecioVariable()
    log.debug( "Determina la lista de Genericos precio variable: ${ list } " )
    return list
  }


  static void generateInventoryFile( ){
    log.debug( "generateInventoryFile( )" )
    Boolean archGenerado = articuloService.generarArchivoInventario()
    if( archGenerado ){
      JOptionPane.showMessageDialog( new JDialog(), String.format(MSJ_ARCHIVO_GENERADO, Registry.archivePath), TXT_ARCHIVO_GENERADO, JOptionPane.INFORMATION_MESSAGE )
    } else {
      JOptionPane.showMessageDialog( new JDialog(), MSJ_ARCHIVO_NO_GENERADO, TXT_ARCHIVO_GENERADO, JOptionPane.INFORMATION_MESSAGE )
    }
  }


  static void sendInventoryFile( ){
      log.debug( "sendInventoryFile( )" )
      Boolean archGenerado = articuloService.enviarInventario()
      if( archGenerado ){
          JOptionPane.showMessageDialog( new JDialog(), MSJ_INV_FISICO_ENVIADO, TXT_DIFERENCIAS, JOptionPane.INFORMATION_MESSAGE )
      } else {
          JOptionPane.showMessageDialog( new JDialog(), MSJ_INV_FISICO_NO_ENVIADO, TXT_DIFERENCIAS, JOptionPane.INFORMATION_MESSAGE )
      }
  }

}
