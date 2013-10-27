/**
 * Copyright (C) 2013  Christian Hösel
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see [http://www.gnu.org/licenses/].
 */
package de.hoesel.dav.ars;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.persistence.config.PersistenceUnitProperties;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.hoesel.dav.ars.query.ArchivQueryReceiver;

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
				PersistenceUnitProperties.CREATE_OR_EXTEND);
		properties.put(PersistenceUnitProperties.DDL_GENERATION_MODE,
				PersistenceUnitProperties.DDL_DATABASE_GENERATION);

		EntityManagerFactory entityManagerFactory = Persistence
				.createEntityManagerFactory(Archivator.PERSISTENCE_UNIT_NAME,
						properties);

		// Offensichtlich werden beim ersten Erzeugen eines EntityManagers
		// diverse Caches oder so initialisiert.
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.close();

		// Archivierung initialisieren
		new Archivator(entityManagerFactory, connection);

		// Archivanfragen empfangen und deligieren
		ArchivQueryReceiver.getInstance(entityManagerFactory, connection);

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
