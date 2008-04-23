/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Config.java,v $
 * $Revision: 1.40 $
 * $Date: 2008/04/23 23:10:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.system;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Liest die System-Konfiguration aus config.xml. 
 * @author willuhn
 */
public final class Config
{
  /**
   * Definition des Default-Ports fuer die RMI-Kommunikation.
   */
  public final static int RMI_DEFAULT_PORT = 4840;

	private File workDir   	     = null;
  private File configDir       = null;

  private Locale locale        = null;

  private Settings settings    = null;

  private File systemPluginDir = null;
  private File userPluginDir   = null;
  private File[] pluginDirs    = null;

  /**
   * ct.
   */
  protected Config() throws Exception
  {
  }

  /**
   * Initialisiert die Konfiguration.
   * @throws Exception
   */
  protected void init() throws Exception
  {
    // Das init() koennen wir nicht im Konstruktor
    // machen, weil es sonst eine Rekursion gibt.
    // denn unten erzeugen wir ein Settings-Objekt,
    // welches wiederrum Application.getConfig()
    // aufruft, um an das Work-Dir zu kommen ;)
    if (this.workDir != null)
      return;
    
    this.workDir = Application.getPlatform().getWorkdir();

    this.configDir  = new File(this.workDir,"cfg");
    if (!this.configDir.exists())
    {
      Logger.info("creating " + this.configDir.getAbsolutePath());
      this.configDir.mkdir();
    }

    this.settings = new Settings(this.getClass());
    this.settings.setStoreWhenRead(true);
  }

  /**
   * Liefert den fuer die lokale RMI-Registry zu verwendenden TCP-Port.
   * @return Nummer des TCP-Ports.
   */
  public int getRmiPort()
  {
    return settings.getInt("jameica.system.rmi.serverport",RMI_DEFAULT_PORT);
  }

	/**
	 * Speichert den zu verwendenden TCP-Port fuer die lokale RMI-Registry.
   * @param port
   * @throws ApplicationException Wird geworfen, wenn die Port-Angabe ungueltig (kleiner 1 oder groesser 65535) ist
   * oder der Port bereits belegt.
   */
  public void setRmiPort(int port) throws ApplicationException
	{
    if (port < 1 || port > 65535)
      throw new ApplicationException(Application.getI18n().tr("TCP-Portnummer f�r Netzwerkbetrieb ausserhalb des g�ltigen Bereichs von {0} bis {1}", new String[]{""+1,""+65535}));

    ServerSocket s = null;
    try
    {
      // Wir machen einen Test auf dem Port wenn es nicht der aktuelle ist
      Logger.info("testing TCP port " + port);
      s = new ServerSocket(port);
    }
    catch (BindException e)
    {
      throw new ApplicationException(Application.getI18n().tr("Die angegebene TCP-Portnummer {0} ist bereits belegt",""+port));
    }
    catch (IOException ioe)
    {
      Logger.error("error while opening socket on port " + port);
      throw new ApplicationException(Application.getI18n().tr("Fehler beim Testen der TCP-Portnummer {0}. Ist der Port bereits belegt?",""+port));
    }
    finally
    {
      if (s != null)
      {
        try
        {
          s.close();
        }
        catch (Exception e)
        {
          // ignore
        }
      }
    }
    settings.setAttribute("jameica.system.rmi.serverport",port);
	}

	/**
	 * Prueft, ob die RMI-Verbindungen SSL-verschluesselt werden sollen.
   * @return true, wenn die Verwendung von SSL aktiv ist.
   */
  public boolean getRmiSSL()
	{
		return settings.getBoolean("jameica.system.rmi.enablessl",true);
	}

  /**
   * Prueft, ob bei SSL-verschluesselten RMI-Verbindungen Client-Authentifizierung verwendet werden soll.
   * @return true, wenn die Client-Authentifizierung verwendet wird.
   */
  public boolean getRmiUseClientAuth()
  {
    return settings.getBoolean("jameica.system.rmi.clientauth",true);
  }

  // BUGZILLA 44 http://www.willuhn.de/bugzilla/show_bug.cgi?id=44

  /**
   * Liefert einen ggf definierten Proxy, ueber den Jameica mit der Aussenwelt
   * kommunizieren soll.
   * @return Hostname/IP des Proxy oder <code>null</code> wenn keiner definiert ist.
   */
  public String getProxyHost()
  {
    return settings.getString("jameica.system.proxy.host",null);
  }

  /**
   * Liefert den TCP-Port des Proxys insofern einer definiert ist.
   * @return TCP-Portnummer des Proxys oder -1,
   */
  public int getProxyPort()
  {
    return settings.getInt("jameica.system.proxy.port",-1);
  }

  /**
   * Speichert den Proxy-Host,
   * @param host Proxy-Host.
   */
  public void setProxyHost(String host)
  {
    if ("".equals(host))
      host = null;
    settings.setAttribute("jameica.system.proxy.host",host);
  }
  
  /**
   * Speichert die TCP-Portnummer des Proxys.
   * @param port Port-Nummer.
   * @throws ApplicationException Bei Angabe eines ungueltigen Ports (kleiner 1 oder groesser 65535).
   * Es sei denn, es wurde "-1" angegeben. Der Wert steht fuer "nicht verwenden".
   */
  public void setProxyPort(int port) throws ApplicationException
  {
    if (port == -1)
    {
      settings.setAttribute("jameica.system.proxy.port",-1);
      return;
    }

    if (port < 1 || port > 65535)
      throw new ApplicationException(Application.getI18n().tr("TCP-Portnummer f�r Proxy ausserhalb des g�ltigen Bereichs von {0} bis {1}", new String[]{""+1,""+65535}));

    settings.setAttribute("jameica.system.proxy.port",port);
  }

  /**
   * Prueft, ob im Server-Mode die Dienste nach aussen freigegeben werden sollen.
   * Der Parameter wird nur im Server-Mode interpretiert.
   * @return true, wenn die Dienste freigegeben werden.
   */
  public boolean getShareServices()
  {
    return settings.getBoolean("jameica.system.rmi.shareservices",true);
  }

  /**
   * Prueft, ob im Server-Mode die Dienste via Multicast-Lookup im LAN announced werden sollen.
   * Der Parameter wird nur im Server-Mode interpretiert.
   * @return true, wenn die Dienste via Multicast-Lookup announced werden sollen.
   */
  public boolean getMulticastLookup()
  {
    return settings.getBoolean("jameica.system.multicastlookup",true);
  }

  /**
	 * Aktiviert oder deaktiviert die Verwendung von SSL fuer die RMI-Verbindungen.
   * @param b
   */
  public void setRmiSSL(boolean b)
	{
		settings.setAttribute("jameica.system.rmi.enablessl",b);
	}

  /**
   * Liefert das konfigurierte Locale (Sprach-Auswahl).
   * @return konfiguriertes Locale.
   */
  public Locale getLocale()
  {
    if (locale != null)
      return locale;

    Locale l = Locale.GERMANY;
    String lang = settings.getString("jameica.system.locale",l.getLanguage() + "_" + l.getCountry());
    String country = "";
    if (lang.indexOf("_") != -1)
    {
      int minus = lang.indexOf("_");
      country   = lang.substring(minus+1);
      lang      = lang.substring(0,minus);
    }
    
    Logger.info("configured language: " + lang);
    if (country.length() > 0)
      Logger.info("configured country: " + country);

    try {
      // Wir testen die Existenz der Bundles
      l = new Locale(lang,country);
      Logger.info("checking resource bundle for language");
      ResourceBundle.getBundle("lang/system_messages",l);
      this.locale = l;
      Logger.info("active language: " + this.locale.getDisplayName());
      Locale.setDefault(this.locale);
      return this.locale;
    }
    catch (Exception ex)
    {
      Logger.info("not found. fallback to system default");
    }
    return Locale.getDefault();

  }

	/**
	 * Speichert das Locale (Sprach-Auswahl).
   * @param l das zu verwendende Locale.
   */
  public void setLocale(Locale l)
	{
		if (l == null)
			return;
    this.locale = l;
    settings.setAttribute("jameica.system.locale",this.locale.getLanguage() + "_" + this.locale.getCountry());
	}

  /**
   * Liefert die in ~/.jameica/cfg/de.willuhn.jameica.system.Config.properties definierten
   * Pluginverzeichnisse.
   * @return Liste Plugin-Verzeichnisse.
   */
  public File[] getPluginDirs()
  {
    if (this.pluginDirs != null)
      return this.pluginDirs;

    // Abwaertskompatibilitaet
    // Diese beiden Plugin-Verzeichnisse standen frueher mit in der Config
    // drin. Da die jetzt separat abgefragt werden, schmeissen wir sie
    // hier raus, falls sie auftauchen
    File sysPluginDir = getSystemPluginDir();
    File usrPluginDir = getUserPluginDir();
    
    boolean found = false;
    
    ArrayList l = new ArrayList();

    String[] s = settings.getList("jameica.plugin.dir",null);
    if (s != null && s.length > 0)
    {
      for (int i=0;i<s.length;++i)
      {
        File f = new File(s[i]);

        try
        {
          // Mal schauen, ob wir das in einen kanonischen Pfad wandeln koennen
          f = f.getCanonicalFile();
        }
        catch (IOException e)
        {
          Logger.warn("unable to convert " + f.getAbsolutePath() + " into canonical path");
        }
        
        if (f.equals(sysPluginDir) || f.equals(usrPluginDir))
        {
          Logger.info("skipping system/user plugin dir in jameica.plugin.dir[" + i + "]");
          found = true;
          continue;
        }
        
        if (!f.canRead() || !f.isDirectory())
        {
          Logger.warn(f.getAbsolutePath() + " is no valid plugin dir, skipping");
          continue;
        }
        
        Logger.info("adding plugin dir " + f.getAbsolutePath());
        l.add(f);
      }
    }
    
    // Migration: Wir schreiben die Liste der Plugin-Verzeichnisse neu,
    // damit System- und User-Verzeichnis rausfliegen.
    if (found)
    {
      String[] newList = new String[l.size()];
      for (int i=0;i<l.size();++i)
      {
        newList[i] = ((File)l.get(i)).getAbsolutePath();
      }
      settings.setAttribute("jameica.plugin.dir",newList);
    }
    
    this.pluginDirs = (File[]) l.toArray(new File[l.size()]);
    return this.pluginDirs;
  }
  
  /**
   * Liefert das System-Plugin-Verzeichnis.
   * Das ist jenes, welches sich im Jameica-Verzeichnis befindet.
   * @return das System-Plugin-Verzeichnis.
   */
  public File getSystemPluginDir()
  {
    if (this.systemPluginDir == null)
    {
      this.systemPluginDir = new File("plugins");
      try
      {
        this.systemPluginDir = this.systemPluginDir.getCanonicalFile();
      }
      catch (IOException e)
      {
        Logger.warn("unable to convert " + this.systemPluginDir.getAbsolutePath() + " into canonical path");
      }
    }

    return this.systemPluginDir;
  }

  /**
   * Liefert das User-Plugin-Verzeichnis.
   * Das ist jenes, welches sich im Work-Verzeichnis des Users befindet.
   * In der Regel ist das ~/.jameica/plugins.
   * @return das user-Plugin-Verzeichnis.
   */
  public File getUserPluginDir()
  {
    if (this.userPluginDir == null)
    {
      // Wir erstellen noch ein userspezifisches Plugin-Verzeichnis
      this.userPluginDir = new File(this.workDir,"plugins");
      if (!userPluginDir.exists())
      {
        Logger.info("creating " + userPluginDir.getAbsolutePath());
        userPluginDir.mkdirs();
      }
      try
      {
        this.userPluginDir = this.userPluginDir.getCanonicalFile();
      }
      catch (IOException e)
      {
        Logger.warn("unable to convert " + this.userPluginDir.getAbsolutePath() + " into canonical path");
      }
    }
    return this.userPluginDir;
  }

  /**
   * Liefert Pfad und Dateiname des Log-Files.
   * @return Logfile.
   */
  public String getLogFile()
  {
    return getWorkDir() + File.separator + "jameica.log";
  }

  /**
   * Legt fest, ob Eingabe-Felder auf Pflichteingaben geprueft werden.
   * @return Pruefen von Pflichteingaben.
   */
  public boolean getMandatoryCheck()
  {
    return settings.getBoolean("jameica.system.checkmandatory",true);
  }

  /**
   * Legt fest, ob Eingabe-Felder auf Pflichteingaben geprueft werden.
   * @param check Pruefen von Pflichteingaben.
   */
  public void setMandatoryCheck(boolean check)
  {
    settings.setAttribute("jameica.system.checkmandatory",check);
  }

  /**
   * Legt fest, ob auch die Label vor Pflichtfeldern rot markiert werden sollen.
   * @return true, wenn auch die Label rot markiert werden sollen.
   */
  public boolean getMandatoryLabel()
  {
    return settings.getBoolean("jameica.system.mandatorylabel",false);
  }

  /**
   * Legt fest, ob auch die Label vor Pflichtfeldern rot markiert werden sollen.
   * @param check true, wenn auch die Label rot markiert werden sollen.
   */
  public void setMandatoryLabel(boolean check)
  {
    settings.setAttribute("jameica.system.mandatorylabel",check);
  }

  /**
   * Liefert den Namen des Loglevels.
   * @return Name des Loglevels.
   */
  public String getLogLevel()
  {
    return settings.getString("jameica.system.log.level",Level.DEFAULT.getName());
  }

	/**
	 * Legt den Log-Level fest.
   * @param name Name des Log-Levels.
   */
  public void setLoglevel(String name)
	{
    settings.setAttribute("jameica.system.log.level",name);
    // Aenderungen sofort uebernehmen
    Logger.setLevel(Level.findByName(name));
	}

  /**
   * Liefert den Pfad zum Config-Verzeichnis.
   * @return Pfad zum Config-Verzeichnis.
   */
  public String getConfigDir()
  {
    return configDir.getAbsolutePath();
  }

	/**
	 * Liefert das Work-Verzeichnis von Jameica.
   * @return das Work-Verzeichnis von Jameica.
   */
  public String getWorkDir()
	{
		try {
			return workDir.getCanonicalPath();
		}
		catch (IOException e)
		{
			return workDir.getAbsolutePath();
		}
	}
  
  /**
   * Liefert das Backup-Verzeichnis.
   * @return Backup-Verzeichnis.
   * @throws ApplicationException wenn das Verzeichnis ungueltig ist.
   */
  public String getBackupDir() throws ApplicationException
  {
    // Wir setzen hier bewusst "NULL" als Default-Wert ein,
    // weil wir nicht wollen, dass er (wegen absoluter Pfadangabe)
    // in der Configdatei landet sondern erst, wenn es der User
    // explizit angegeben hat.
    String defaultDir = getWorkDir();
    String dir = settings.getString("jameica.system.backup.dir",null);
    if (dir == null)
      return defaultDir;
    
    File f = new File(dir);
    if (f.exists() && f.isDirectory() && f.canWrite())
      return f.getAbsolutePath();
    
    Logger.warn("invalid backup dir " + dir +", resetting to default: " + defaultDir);
    setBackupDir(null);
    return defaultDir;
  }

  /**
   * Speichert das Backup-Verzeichnis.
   * Der Pfad wird nur gespeichert, wenn er vom Default-Wert abweicht.
   * Andernfalls wird der Wert in der Config resettet, damit wieder
   * das Standardverzeichnis genutzt wird.
   * @param dir das Backup-Verzeichnis.
   * @throws ApplicationException wenn das Verzeichnis ungueltig ist.
   */
  public void setBackupDir(String dir) throws ApplicationException
  {
    // Resetten
    if (dir == null || dir.length() == 0)
    {
      settings.setAttribute("jameica.system.backup.dir",(String)null);
      return;
    }
    
    // Angegebenes Verzeichnis ist das Work-Dir.
    // Also resetten
    File f = new File(dir);

    try
    {
      if (f.getCanonicalPath().equals(this.workDir.getCanonicalPath()))
      {
        settings.setAttribute("jameica.system.backup.dir",(String)null);
        return;
      }
    }
    catch (IOException e)
    {
      // Gna, dann halt ohne Aufloesen von Links
      if (f.equals(this.workDir))
      {
        settings.setAttribute("jameica.system.backup.dir",(String)null);
        return;
      }
    }
    
    if (!f.isDirectory() || !f.exists())
      throw new ApplicationException(Application.getI18n().tr("Bitte geben Sie ein g�ltiges Verzeichnis an"));
    
    if (!f.canWrite())
      throw new ApplicationException(Application.getI18n().tr("Sie besitzen keine Schreibrechte in diesem Verzeichnis"));
    
    settings.setAttribute("jameica.system.backup.dir",f.getAbsolutePath());
  }
  
  /**
   * Liefert die Anzahl zu erstellender Backups.
   * Default-Wert: 5.
   * @return Anzahl der Backups.
   */
  public int getBackupCount()
  {
    int count = settings.getInt("jameica.system.backup.count",5);
    if (count < 1)
    {
      Logger.warn("invalid backup count: " + count + ", resetting to default");
      setBackupCount(-1);
    }
    return count;
  }
  
  /**
   * Speichert die Anzahl zu erstellender Backups.
   * @param count Anzahl der Backups.
   */
  public void setBackupCount(int count)
  {
    settings.setAttribute("jameica.system.backup.count",count < 1 ? 5 : count);
  }
  
  /**
   * Prueft, ob ueberhaupt Backups erstellt werden sollen.
   * Default: true.
   * @return true, wenn Backups erstellt werden sollen.
   */
  public boolean getUseBackup()
  {
    return settings.getBoolean("jameica.system.backup.enabled",true);
  }

  /**
   * Speichert, ob ueberhaupt Backups erstellt werden sollen.
   * @param enabled true, wenn Backups erstellt werden sollen.
   */
  public void setUseBackup(boolean enabled)
  {
    settings.setAttribute("jameica.system.backup.enabled",enabled);
  }

}


/*********************************************************************
 * $Log: Config.java,v $
 * Revision 1.40  2008/04/23 23:10:14  willuhn
 * @N Platform-Klasse fuer Plattform-/OS-Spezifisches
 * @N Default-Workverzeichnis unter MacOS ist nun ~/Library/jameica
 *
 * Revision 1.39  2008/03/11 10:36:08  willuhn
 * @N Default-Wert auf true geaendert
 *
 * Revision 1.38  2008/03/11 00:13:08  willuhn
 * @N Backup scharf geschaltet
 *
 * Revision 1.37  2008/03/07 17:36:35  willuhn
 * @B Absoluten Backup-Pfad aus Config-Datei entfernen, wenn er dem Default-Pfad entspricht
 *
 * Revision 1.36  2008/03/07 16:31:48  willuhn
 * @N Implementierung eines Shutdown-Splashscreens zur Anzeige des Backup-Fortschritts
 *
 * Revision 1.35  2008/02/29 01:12:30  willuhn
 * @N Erster Code fuer neues Backup-System
 * @N DirectoryInput
 * @B Fixes an FileInput, TextInput
 *
 * Revision 1.34  2008/01/09 22:25:06  willuhn
 * @C Namensueberschneidung bei den Locales
 *
 * Revision 1.33  2007/12/14 13:29:05  willuhn
 * @N Multicast Lookup-Service
 *
 * Revision 1.32  2007/09/06 22:21:55  willuhn
 * @N Hervorhebung von Pflichtfeldern konfigurierbar
 * @N Neustart-Hinweis nur bei Aenderungen, die dies wirklich erfordern
 *
 * Revision 1.31  2007/08/20 12:27:08  willuhn
 * @C Pfad zur Log-Datei nicht mehr aenderbar. verursachte nur sinnlose absolute Pfadangaben in der Config
 **********************************************************************/
