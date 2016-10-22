package mx.wen.pos.ui.view.panel

import groovy.model.DefaultTableModel
import groovy.swing.SwingBuilder
import mx.wen.pos.ui.controller.ItemController
import mx.wen.pos.ui.model.Item
import net.miginfocom.swing.MigLayout
import org.apache.commons.lang.StringUtils

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import java.util.List

class PlatesPanel extends JPanel {

    private static final String TXT_TAB_TITLE = 'Platillos'
    private SwingBuilder sb
    private List<Item> items
    private JPanel mainPanel
    private OrderPanel orderPanel

    public boolean cancel = false

    public PlatesPanel( OrderPanel orderPanel ) {
      sb = new SwingBuilder()
      this.orderPanel = orderPanel
      items = ItemController.findItemsBySubtype( "" )
      buildUI()
      addButtons( )
    }

    private void buildUI( ) {
      sb.panel(this, layout: new MigLayout('wrap', '[fill,grow]', '[fill,grow]')){
        scrollPane( constraints: BorderLayout.CENTER ) {
          mainPanel = panel(layout: new MigLayout('wrap 4', '5[fill]18[fill]18[fill]18[fill]1', '[fill]20[fill]')) {

          }
        }
      }
    }

    private void addButtons( ){
      for(Item i : items){
        JButton btn = new JButton()
        //"<html>${description.substring(0,80)}<br>${description.substring(80)}<br><html>"
        String plate = ""
        String[] data = i.name.split(" ")
        for(String d : data){
          plate = plate+"${d}<br>"
        }
        btn.text =  "<html>${plate}<html>"
        btn.preferredSize = [100, 100]
        btn.name = StringUtils.trimToEmpty(i.id.toString())
        btn.addActionListener( new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
              orderPanel.itemSearch.text = StringUtils.trimToEmpty(btn.name)
              orderPanel.doItemSearch( "" )
            }
        })
        mainPanel.add( btn )
      }
    }
}
