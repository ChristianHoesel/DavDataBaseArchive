/**
 * 
 */
package de.hoesel.dav.ars;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.modell.DefaultObjektFactory;
import de.bsvrz.sys.funclib.bitctrl.modell.ObjektFactory;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

/**
 * @author Christian
 * 
 */
public class Archiv implements StandardApplication {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

			public void uncaughtException(final Thread t, final Throwable e) {
				Logger.getLogger(getClass().getName()).log(Level.INFO,
						"*** Application wird beendet ***", e); //$NON-NLS-1$
			}

		});
		StandardApplicationRunner.run(new Archiv(), args);

		try {
			Object lock = new Object();
			synchronized (lock) {
				lock.wait();	
			}
			
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}

	public void initialize(final ClientDavInterface connection)
			throws Exception {
		ObjektFactory objF = DefaultObjektFactory.getInstanz();

		objF.setDav(connection);
		
		new Archivator();
		
		
		
		
	}

	/**
	 * Zusätzliche Kommandozeilenargumente sind:
	 * <ul>
	 * <li><code>-kb</code> die kommagetrennte Liste der PIDs von zu
	 * konvertierenden Konfigurationsbereichen.</li>
	 * <li><code>-out</code> das Zielverzeichnis in dem generierten Java-Quellen
	 * abgelegt werden sollen.</li>
	 * <li><code></code></li>
	 * </ul>
	 * 
	 * {@inheritDoc}
	 */
	public void parseArguments(final ArgumentList argumentList)
			throws Exception {
		
	}
}
