package mx.wen.pos.ui.view.dialog

import groovy.swing.SwingBuilder
import mx.wen.pos.ui.controller.*
import mx.wen.pos.ui.model.Item
import mx.wen.pos.ui.view.renderer.MoneyCellRenderer
import org.apache.commons.lang.StringUtils
import org.springframework.core.io.ClassPathResource

import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JFrame
import java.awt.Component
import java.awt.Image
import java.awt.event.MouseEvent
import javax.swing.JDialog
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities
import java.awt.Dimension
import javax.swing.JCheckBox
import groovy.model.DefaultTableModel
import java.awt.BorderLayout
import javax.swing.BorderFactory
import mx.wen.pos.ui.resources.UI_Standards
import javax.swing.JLabel
import javax.swing.SwingConstants
import java.awt.event.ComponentListener
import javax.swing.JTable
import javax.swing.event.ListSelectionListener
import javax.swing.event.ListSelectionEvent
import java.awt.FontMetrics

class SuggestedItemsDialog extends JDialog {

  private SwingBuilder sb
  private String code
  private List<Item> suggestions = new ArrayList<Item>()
  private List<Item> allSuggestions = new ArrayList<Item>()
  private Item item
  private Item itemDesc
  private JCheckBox cbExistencias
  private JButton lblDescripcion
  private DefaultTableModel model
  private JTable tableItems
  private static final Integer COLUMN_ID = 1

  SuggestedItemsDialog( Component parent, String code, List<Item> suggestions ) {
    this.code = code
    this.suggestions.addAll( suggestions )
    this.allSuggestions.addAll( suggestions )
    sb = new SwingBuilder()
    item = null
    buildUI( parent )
  }

  Item getItem( ) {
    return item
  }

  private void buildUI( Component parent ) {
    sb.dialog( this,
        title: "Art\u00edculos sugeridos con: ${code ?: ''}",
        location: parent.location,//[80,80],
        resizable: true,
        //extendedState: JFrame.MAXIMIZED_BOTH,
        preferredSize: [ 720  , 650 ] as Dimension,
        modal: true,
        pack: true,
    ) {
      panel(border: BorderFactory.createEmptyBorder(5, 8, 5, 8)) {
      borderLayout()
      panel(constraints: BorderLayout.PAGE_START, border: BorderFactory.createEmptyBorder(0,0,3,0)) {
        borderLayout()
        label( " Se encontraron ${suggestions.size()} art\u00edculos similares a: ${code}",
               constraints: BorderLayout.PAGE_START)
        label( minimumSize: [10, 3] as Dimension)
        cbExistencias = checkBox(
            text:'Solo con existencias',
            constraints: BorderLayout.PAGE_END,
            actionPerformed: { doValueChange() } )
      }
      scrollPane( constraints: BorderLayout.CENTER ) {
        tableItems = table( selectionMode: ListSelectionModel.SINGLE_SELECTION, mouseClicked: doItemClick, rowHeight: 60 ) {
          model = tableModel( list: suggestions ) {
            closureColumn( header: '', read: {Item tmp -> changeImg(imageIcon( url: new ClassPathResource( "img/${StringUtils.trimToEmpty(tmp?.id.toString())}.png" )?.URL ))},
                    type: ImageIcon,  preferredWidth: 110 )
            closureColumn( header: 'Sku', read: {Item tmp -> tmp?.id}, preferredWidth: 60 )
            closureColumn( header: 'Art\u00edculo', read: {Item tmp -> tmp?.name}, preferredWidth: 150 )
            closureColumn( header: 'Descripci\u00f3n', read: {Item tmp -> "<html><p align=\"center\">${tmp?.desc}</p></html>"}, preferredWidth: 210 )
             closureColumn( header: 'Precio', read: {Item tmp -> tmp?.price}, cellRenderer: new MoneyCellRenderer(), preferredWidth: 100 )
            closureColumn( header: 'Existencia', read: {Item tmp -> tmp?.stock}, type: Integer, preferredWidth: 80 )
          } as DefaultTableModel
        }
        /*tableItems.selectionModel.addListSelectionListener( new ListSelectionListener() {
          @Override
          void valueChanged( ListSelectionEvent ev ) {

            String idArticle = tableItems.getValueAt( tableItems.selectedRow, COLUMN_ID ).toString()
            List<Item> item = ItemController.findItemsByQuery( idArticle, "" )
            if( item.first() != null ){
              String imgStr = "img/${StringUtils.trimToEmpty(item.first().id.toString())}.png"
              lblDescripcion.setIcon(null)
              Icon icon = new ImageIcon(new ClassPathResource( imgStr )?.URL);
              Image image = icon.getImage();
              Image newimg = image.getScaledInstance(120, 120,  Image.SCALE_SMOOTH);
              icon = new ImageIcon(newimg);
              lblDescripcion.setIcon(icon)
              lblDescripcion.revalidate()
            } else {
              lblDescripcion.text = ' '
            }
          }
        } )*/
      }

        panel( constraints: BorderLayout.PAGE_END ) {
          borderLayout()
          //lblDescripcion = label( text: '<html> <br> <html> ', border: titledBorder( '' ), constraints: BorderLayout.PAGE_START )
          //lblDescripcion = button( maximumSize: [100,100], constraints: BorderLayout.PAGE_START )
          button( 'Cerrar',
              defaultButton: true,
              preferredSize: UI_Standards.BUTTON_SIZE,
              constraints: BorderLayout.LINE_END,
              actionPerformed: {dispose()} )
        }
    }
    }
  }

  private def doItemClick = { MouseEvent ev ->
    if ( SwingUtilities.isLeftMouseButton( ev ) ) {
      if ( ev.clickCount == 2 ) {
        item = ev.source.selectedElement
        dispose()
      }
    }
  }

  private void doValueChange() {
    suggestions.clear()
    if( cbExistencias.selected ){
      for( Item item : allSuggestions ){
        if( item.stock != 0 ){
          suggestions.add( item )
        }
      }
    } else {
      suggestions.addAll( allSuggestions )
    }
    model.fireTableDataChanged()
  }

  @Override
  ImageIcon changeImg( ImageIcon img ) {
    Icon icon = img
    Image image = icon.getImage();
    Image newimg = image.getScaledInstance(60, 60,  Image.SCALE_SMOOTH);
    return new ImageIcon(newimg);
  }
}
