/*****************************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/Attic/AbstractView.java,v $
 * $Revision: 1.8 $
 * $Date: 2003/12/29 16:29:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 ****************************************************************************/
package de.willuhn.jameica.gui.views;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.ApplicationException;

/**
 * Basis-Klasse fuer alles Views.
 * @author willuhn
 */
public abstract class AbstractView
{

	Composite parent;
  Object currentObject;
  
  /**
   * Konstruktor.
   * @param o ein optionales Datenobjekt, welches in der View verarbeitet werden soll.
   */
  public AbstractView(Object o)
  {
    currentObject = o;
  }

  /**
   * Wird aufgerufen, wenn der Dialog geoeffnet wird.
   * Diese Methode muss von abgeleiteteten Klassen ueberschrieben werden, um dort den Content zu malen.
   */
  public abstract void bind();

  /**
   * Wird aufgerufen, wenn der Dialog verlassen wird.
   * Diese Methode muss von abgeleiteten Klassen ueberschrieben werden, um dort Aufraeumarbeiten vorzunehmen.
   * @throws ApplicationException darf vom Dialog geworfen werden, wenn der neue Dialog
   * nicht gestartet werden soll. Z.Bsp. weil Daten noch nicht gespeichert worden oder
   * ein Vorgang noch in Bearbeitung ist.
   */
  public abstract void unbind() throws ApplicationException;

	
	/**
   * Liefert das Composite, in dem der Dialog dargestellt werden soll.
   * @return das Composite, in dem der Dialog angezeigt werden soll.
   */
  public Composite getParent()
	{
		return parent;
	}

	/**
   * Speichert das Composite, in dem der Dialog dargestellt werden soll.
   * @param parent Composite, in dem der Dialog dargestellt werden soll.
   */
  public void setParent(Composite parent)
	{
		this.parent = parent;
	}

	/**
   * Liefert das dieser View uebergebene Daten-Objekt zurueck. 
   * @return Liefert das Business-Objekt fuer das der Dialog zustaendig ist.
   */
  public Object getCurrentObject()
	{
		return currentObject;
	}
}



/***************************************************************************
 * $Log: AbstractView.java,v $
 * Revision 1.8  2003/12/29 16:29:47  willuhn
 * @N javadoc
 *
 * Revision 1.7  2003/12/28 22:58:27  willuhn
 * @N synchronize mode
 *
 * Revision 1.6  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.5  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 * Revision 1.4  2003/11/20 03:48:42  willuhn
 * @N first dialogues
 *
 * Revision 1.3  2003/10/29 00:41:27  willuhn
 * *** empty log message ***
 *
 ***************************************************************************/