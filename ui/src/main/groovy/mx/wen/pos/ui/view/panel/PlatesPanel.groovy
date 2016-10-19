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

    private JTextField txtOdEsfera
    private JTextField txtOdCil
    private JTextField txtOdEje
    private JTextField txtOdAd
    private JTextField txtOdAv
    private JTextField txtOdDm
    private JTextField txtOdPrisma
    private JTextField txtOdUbic

    private JTextField txtOiEsfera
    private JTextField txtOiCil
    private JTextField txtOiEje
    private JTextField txtOiAd
    private JTextField txtOiAv
    private JTextField txtOiDm
    private JTextField txtOiPrisma
    private JTextField txtOiUbic

    private JTextField txtDICerca
    private JTextField txtAltOblea
    private JTextField txtDILejos

    private JTextField txtDh
    private JTextField txtDv
    private JTextField txtPte
    private JTextField txtBase

    private JTextField txtObservaciones

    private JPanel empleadoPanel
    private JPanel usoRxPanel1
    private JPanel usoRxPanel2
    private JPanel measuresFramePanel


    public boolean cancel = false

    public PlatesPanel( ) {
      sb = new SwingBuilder()
      buildUI()
    }

    private void buildUI( ) {
      sb.panel( this, layout: new MigLayout( 'fill,wrap 3','[fill,grow][fill,grow][fill,grow]','[fill,grow]') ) {
        button("milanesa")
        button("pollo")
        button("pechuga asada")
        button("atun")
        button("chamorro")
        button("cochinita")
        button("bisteck")
        button("mole verde")
      }

    }

}
