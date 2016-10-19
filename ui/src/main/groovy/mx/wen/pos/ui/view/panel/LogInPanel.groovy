package mx.wen.pos.ui.view.panel

import mx.wen.pos.ui.controller.AccessController
import groovy.swing.SwingBuilder
import mx.wen.pos.ui.model.UpperCaseDocument
import mx.wen.pos.ui.model.User
import net.miginfocom.swing.MigLayout
import org.apache.commons.lang3.StringUtils

import javax.swing.*
import javax.swing.border.TitledBorder
import java.awt.*
import java.awt.event.KeyEvent
import java.awt.event.KeyListener

class LogInPanel extends JPanel implements KeyListener{

  private SwingBuilder sb
  private JTextField username
  private JPasswordField password
  private JLabel messages
  private JButton logInButton
  private String priceListPending
  private Closure doAction
  private String version
  Boolean loggedOk = false

  LogInPanel( Closure doAction, String version ) {
    this.version = version
    this.priceListPending = ""
    this.doAction = doAction ?: {}
    sb = new SwingBuilder()
    buildUI()
  }

  private void buildUI( ) {
    sb.panel( this, layout: new MigLayout( 'wrap,center', '[fill,center]', '[fill,center]' ) ) {
      panel( border: new TitledBorder( 'Ingresa tus datos:' ),
          layout: new MigLayout( 'wrap 2', '[fill,100!][fill,130!]', '[fill,30!][fill,30!][][]' ),
      ) {
        label( 'Empleado' )
        username = textField( font: new Font( '', Font.BOLD, 14 ),
            document: new UpperCaseDocument(),
            horizontalAlignment: JTextField.CENTER,
            actionPerformed: {logInButton.doClick()}
        )
        username.addKeyListener( this )

        label( 'Contrase\u00f1a' )
        password = passwordField( font: new Font( '', Font.BOLD, 14 ),
            horizontalAlignment: JTextField.CENTER,
            actionPerformed: {logInButton.doClick()}
        )

        messages = label( foreground: Color.RED, constraints: "span 2" )
        label()
        label( text: version, horizontalAlignment: JLabel.RIGHT )
      }

      logInButton = button( 'Acceder', actionPerformed: doLogIn, constraints: 'right,span,w 125!,h 40!' )

      //label( text: "Listas de Precios pendientes por cargar: ${this.priceListPending}" )
    }
  }

  private def doLogIn = {
    logInButton.enabled = false
    User user = AccessController.logIn( username.text, password.text )
    if ( user?.username != null ) {
      messages.text = null
      messages.visible = false
      doAction()
      loggedOk = true
    } else {
      messages.text = 'Empleado/Contrase\u00f1a incorrectos'
      messages.visible = true
    }
    username.text = null
    password.text = null
    logInButton.enabled = true
  }

    @Override
    void keyTyped(KeyEvent e) {
    }

    @Override
    void keyPressed(KeyEvent e) {
    }

    @Override
    void keyReleased(KeyEvent e) {
      /*if( !MainWindow.instance.openSoi ){
        User user = AccessController.getUser( username.text )
        if( user != null ){
            password.text = user.password.trim()
        }
      }*/
    }
}
