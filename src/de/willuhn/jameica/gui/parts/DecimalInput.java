/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Attic/DecimalInput.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/03/11 08:56:55 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.parts;

import java.text.DecimalFormat;
import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.util.Style;

/**
 * @author willuhn
 * Malt ein Eingabefeld, in das nur Dezimalzahlen eingegeben werden koennen.
 */
public class DecimalInput extends AbstractInput
{
  private double value;
  private DecimalFormat format;
  private Text text;

  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param value anzuzeigender Wert.
   * @param format Formatter fuer die Anzeige.
   */
  public DecimalInput(double value, DecimalFormat format)
  {
    this.value = value;
    this.format = format;
  }

  /**
   * @see de.willuhn.jameica.gui.parts.AbstractInput#getControl()
   */
  public Control getControl()
  {

		Composite comp = new Composite(getParent(),SWT.NONE);
		comp.setBackground(Style.COLOR_BORDER);
    
		text = new Text(comp, SWT.RIGHT);

		comp.setLayout(new FormLayout());

		FormData comboFD = new FormData();
		comboFD.left = new FormAttachment(0, 1);
		comboFD.top = new FormAttachment(0, 1);
		comboFD.right = new FormAttachment(100, -1);
		comboFD.bottom = new FormAttachment(100, -1);
		text.setLayoutData(comboFD);
   
		text.setBackground(Style.COLOR_WHITE);


    text.setText(format.format(value));
    text.addFocusListener(new FocusAdapter(){
      public void focusGained(FocusEvent e){
        text.setSelection(0, text.getText().length());
      }
    });
    text.addListener (SWT.Verify, new Listener() {
      public void handleEvent (Event e) {
        String t = e.text;
        char [] chars = new char [t.length ()];
        t.getChars (0, chars.length, chars, 0);
        for (int i=0; i<chars.length; i++) {
          if (!('0' <= chars[i] && chars[i] <= '9') && !(chars[i] == ',')) {
            e.doit = false;
            return;
          }
        }
      }
     });
    return comp;
  }

  /**
   * Die Funktion liefert ein Objekt des Typs java.lang.Double zurueck
   * oder <code>null</code> wenn die Zahl nicht ermittelt werden konnte.
   * @see de.willuhn.jameica.gui.parts.AbstractInput#getValue()
   */
  public Object getValue()
  {
    try {
      return new Double(format.parse(text.getText()).doubleValue());
    }
    catch (ParseException e)
    {
      Application.getLog().error("error while parsing from decimal input",e);
    }
    return null;
  }

  /**
   * Erwartet ein Objekt des Typs java.lang.Double.
   * @see de.willuhn.jameica.gui.parts.AbstractInput#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (value == null)
      return;
    if (!(value instanceof Double))
      return;

    this.text.setText(value.toString());
    this.text.redraw();
  }

  /**
   * @see de.willuhn.jameica.gui.parts.AbstractInput#focus()
   */
  public void focus()
  {
    text.setFocus();
  }

  /**
   * @see de.willuhn.jameica.gui.parts.AbstractInput#disable()
   */
  public void disable()
  {
    text.setEnabled(false);
  }

  /**
   * @see de.willuhn.jameica.gui.parts.AbstractInput#enable()
   */
  public void enable()
  {
    text.setEnabled(true);
  }


}

/*********************************************************************
 * $Log: DecimalInput.java,v $
 * Revision 1.4  2004/03/11 08:56:55  willuhn
 * @C some refactoring
 *
 * Revision 1.3  2004/03/06 18:24:23  willuhn
 * @D javadoc
 *
 * Revision 1.2  2004/02/18 20:28:45  willuhn
 * @N jameica now stores window position and size
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.5  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.4  2003/12/16 02:27:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.2  2003/12/01 21:22:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/12/01 20:28:58  willuhn
 * @B filter in DBIteratorImpl
 * @N InputFelder generalisiert
 *
 **********************************************************************/