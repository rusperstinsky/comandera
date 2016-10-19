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
      sb.panel( this, layout: new MigLayout( 'fill,wrap 3','[fill,grow][fill,grow][fill,grow]','[fill,grow]') ) {
        button("refresco")
        button("agua")
        button("cerveza")
        button("tepache")
      }

    }

}
