/**
 * 
 */
package de.hoesel.dav.ars;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.persistence.config.PersistenceUnitProperties;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

/**
 * @author Christian
 * 
 */
public class Archiv implements StandardApplication {

	private String jdbcDriver;
	private String jdbcUrl;
	private String jdbcUser;
	private String jdbcPassword;

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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void initialize(final ClientDavInterface connection)
			throws Exception {
		
		Map properties = new HashMap();
		properties.put(PersistenceUnitProperties.JDBC_DRIVER, jdbcDriver);
		properties.put(PersistenceUnitProperties.JDBC_URL, jdbcUrl);
		properties.put(PersistenceUnitProperties.JDBC_USER, jdbcUser);
		properties.put(PersistenceUnitProperties.JDBC_PASSWORD, jdbcPassword);
		properties.put(PersistenceUnitProperties.DDL_GENERATION,
				PersistenceUnitProperties.DROP_AND_CREATE);
		properties.put(PersistenceUnitProperties.DDL_GENERATION_MODE,
				PersistenceUnitProperties.DDL_DATABASE_GENERATION);

		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(
				Archivator.PERSISTENCE_UNIT_NAME, properties);
		
		new Archivator(entityManagerFactory,connection);

	}

	public void parseArguments(final ArgumentList argumentList)
			throws Exception {
		jdbcDriver = argumentList.fetchArgument(
				PersistenceUnitProperties.JDBC_DRIVER
						+ "=org.apache.derby.jdbc.EmbeddedDriver").asString();
		jdbcUrl = argumentList.fetchArgument(
				PersistenceUnitProperties.JDBC_URL
						+ "=jdbc:derby:davArchiv;create=true").asString();
		jdbcUser = argumentList.fetchArgument(
				PersistenceUnitProperties.JDBC_USER + "=test").asString();
		jdbcPassword = argumentList.fetchArgument(
				PersistenceUnitProperties.JDBC_PASSWORD + "=").asString();
	}
}
