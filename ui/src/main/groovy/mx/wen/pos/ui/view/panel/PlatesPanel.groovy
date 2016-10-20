package mx.wen.pos.ui.view.panel

import groovy.model.DefaultTableModel
import groovy.swing.SwingBuilder
import net.miginfocom.swing.MigLayout
import org.apache.commons.lang.StringUtils

import javax.swing.*
import java.awt.*
import java.awt.event.MouseEvent
import java.util.List

class PlatesPanel extends JPanel {

    private static final String TXT_TAB_TITLE = 'Platillos'
    private SwingBuilder sb

    public boolean cancel = false

    public PlatesPanel( ) {
      sb = new SwingBuilder()
      buildUI()
    }

    private void buildUI( ) {
        sb.panel(this, layout: new MigLayout('wrap', '[fill,grow]', '[]')){
          scrollPane( constraints: BorderLayout.CENTER ) {
            panel(layout: new MigLayout('wrap 4', '5[fill]18[fill]18[fill]18[fill]1', '[fill]20[fill]')) {
                button("milanesa", preferredSize: [100, 100])
                button("pollo", preferredSize: [100, 100])
                button("pechuga asada", preferredSize: [100, 100])
                button("atun", preferredSize: [100, 100])
                button("chamorro", preferredSize: [100, 100])
                button("cochinita", preferredSize: [100, 100])
                button("bisteck", preferredSize: [100, 100])
                button("mole verde", preferredSize: [100, 100])
                button("milanesa", preferredSize: [100, 100])
                button("pollo", preferredSize: [100, 100])
                button("pechuga asada", preferredSize: [100, 100])
                button("atun", preferredSize: [100, 100])
                button("chamorro", preferredSize: [100, 100])
                button("cochinita", preferredSize: [100, 100])
                button("bisteck", preferredSize: [100, 100])
                button("mole verde", preferredSize: [100, 100])
                button("milanesa", preferredSize: [100, 100])
                button("pollo", preferredSize: [100, 100])
                button("pechuga asada", preferredSize: [100, 100])
                button("atun", preferredSize: [100, 100])
                button("chamorro", preferredSize: [100, 100])
                button("cochinita", preferredSize: [100, 100])
                button("bisteck", preferredSize: [100, 100])
                button("mole verde", preferredSize: [100, 100])
                button("milanesa", preferredSize: [100, 100])
                button("pollo", preferredSize: [100, 100])
                button("pechuga asada", preferredSize: [100, 100])
                button("atun", preferredSize: [100, 100])
                button("chamorro", preferredSize: [100, 100])
                button("cochinita", preferredSize: [100, 100])
                button("bisteck", preferredSize: [100, 100])
                button("mole verde", preferredSize: [100, 100])
                button("milanesa", preferredSize: [100, 100])
                button("pollo", preferredSize: [100, 100])
                button("pechuga asada", preferredSize: [100, 100])
                button("atun", preferredSize: [100, 100])
                button("chamorro", preferredSize: [100, 100])
                button("cochinita", preferredSize: [100, 100])
                button("bisteck", preferredSize: [100, 100])
                button("mole verde", preferredSize: [100, 100])
                button("milanesa", preferredSize: [100, 100])
                button("pollo", preferredSize: [100, 100])
                button("pechuga asada", preferredSize: [100, 100])
                button("atun", preferredSize: [100, 100])
                button("chamorro", preferredSize: [100, 100])
                button("cochinita", preferredSize: [100, 100])
                button("bisteck", preferredSize: [100, 100])
                button("mole verde", preferredSize: [100, 100])
                button("milanesa", preferredSize: [100, 100])
                button("pollo", preferredSize: [100, 100])
                button("pechuga asada", preferredSize: [100, 100])
                button("atun", preferredSize: [100, 100])
                button("chamorro", preferredSize: [100, 100])
                button("cochinita", preferredSize: [100, 100])
                button("bisteck", preferredSize: [100, 100])
                button("mole verde", preferredSize: [100, 100])
                button("milanesa", preferredSize: [100, 100])
                button("pollo", preferredSize: [100, 100])
                button("pechuga asada", preferredSize: [100, 100])
                button("atun", preferredSize: [100, 100])
                button("chamorro", preferredSize: [100, 100])
                button("cochinita", preferredSize: [100, 100])
                button("bisteck", preferredSize: [100, 100])
                button("mole verde", preferredSize: [100, 100])
            }
        }
      }
    }

}
