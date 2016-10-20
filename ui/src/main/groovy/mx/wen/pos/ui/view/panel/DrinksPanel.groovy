package mx.wen.pos.ui.view.panel

import groovy.swing.SwingBuilder
import net.miginfocom.swing.MigLayout

import javax.swing.*

class DrinksPanel extends JPanel {

    private static final String TXT_TAB_TITLE = 'Platillos'
    private SwingBuilder sb

    public DrinksPanel( ) {
      sb = new SwingBuilder()
      buildUI()
    }

    private void buildUI( ) {
      sb.panel( this, layout: new MigLayout( 'wrap 4','[]','[]'), border: BorderFactory.createEmptyBorder(0, 0, 0, 0) ) {
        button("refresco", preferredSize: [125,100])
        button("agua", preferredSize: [125,100])
        button("cerveza", preferredSize: [125,100])
        button("tepache", preferredSize: [125,100])
      }

    }

}
