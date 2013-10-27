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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.sys.funclib.dataIdentificationSettings.DataIdentification;
import de.bsvrz.sys.funclib.dataIdentificationSettings.EndOfSettingsListener;
import de.bsvrz.sys.funclib.dataIdentificationSettings.SettingsManager;
import de.bsvrz.sys.funclib.dataIdentificationSettings.UpdateListener;
import de.bsvrz.sys.funclib.debug.Debug;
import de.hoesel.dav.ars.jpa.DatenverteilerArchivDatensatz;
import de.hoesel.dav.ars.jpa.DefaultArchivData;
import de.hoesel.dav.ars.jpa.OdVerkehrsDatenKurzZeitFs;
import de.hoesel.dav.ars.jpa.OdVerkehrsDatenKurzZeitMq;
import de.hoesel.dav.ars.jpa.OdVerkehrsDatenLangZeitIntervall;
import de.hoesel.dav.ars.jpa.SystemObjectArchiv;

public class Archivator implements ClientReceiverInterface {

	public static final String PERSISTENCE_UNIT_NAME = "de.hoesel.dav.ars";
	private final EntityManagerFactory factory;

	private ClientDavInterface connection;

	private static final Debug logger = Debug.getLogger();

	private LinkedBlockingDeque<ResultData> data2StoreList = new LinkedBlockingDeque<ResultData>();

	/**
	 * Reagiert bei Änderung der Archivparameter und organisiert die An- und
	 * Abmeldungen für zu archivierende Datensätze.
	 * 
	 * @author Christian
	 * 
	 */
	private final class SettingsManagerUpdateListener implements
			UpdateListener, EndOfSettingsListener {

		private List<DataIdentification> neueAnmeldungen = new ArrayList<DataIdentification>();
		private List<DataIdentification> neueAbmeldungen = new ArrayList<DataIdentification>();

		@Override
		public synchronized void update(DataIdentification dataIdentification,
				Data oldSettings, Data newSettings) {
			if (oldSettings != null && newSettings == null) {
				archivParameterEntfernt(dataIdentification);
			} else {
				neueArchivParameter(dataIdentification, newSettings);
			}
		}

		private void archivParameterEntfernt(
				DataIdentification dataIdentification) {
			neueAbmeldungen.add(dataIdentification);
		}

		private void neueArchivParameter(DataIdentification dataIdentification,
				Data newSettings) {
			boolean archivieren = newSettings.getUnscaledValue("Archivieren")
					.intValue() > 0;

			if (archivieren) {
				neueAnmeldungen.add(dataIdentification);
			} else {
				neueAbmeldungen.add(dataIdentification);
			}
		}

		@Override
		public synchronized void inform() {
			logger.info("Neue Archivparameter eingelesen - Beginne mit Anmeldung/Ummeldung für Archivparameter. Es werden "
					+ neueAnmeldungen.size()
					+ " Anmeldungen und "
					+ neueAbmeldungen.size() + " Abmeldungen vorgenommen.");
			long start = System.currentTimeMillis();

			for (DataIdentification id : neueAbmeldungen) {
				connection.unsubscribeReceiver(Archivator.this, id.getObject(),
						id.getDataDescription());
			}
			neueAbmeldungen.clear();

			for (DataIdentification id : neueAnmeldungen) {
				try {
					connection.subscribeReceiver(Archivator.this,
							id.getObject(), id.getDataDescription(),
							ReceiveOptions.delayed(), ReceiverRole.receiver());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			neueAnmeldungen.clear();
			logger.info("An- und Abmeldung für Archivdatensaetze abgeschlossen - "
					+ (System.currentTimeMillis() - start) + " ms");
		}
	}

	/**
	 * {@link Thread} zur asynchronen Speicherung der ankommenden Datensätze.
	 * 
	 * @author Christian
	 * 
	 */
	private class DBStorageThread extends Thread {

		@Override
		public void run() {
			super.run();

			while (!isInterrupted()) {
				EntityManager entityManager = factory.createEntityManager();
				;
				try {
					int i = 0;
					ResultData rd = data2StoreList.take();
					// long start = System.currentTimeMillis();
					entityManager.getTransaction().begin();
					while (rd != null && i++ < 5000) {

						DatenverteilerArchivDatensatz neuerDatensatz = convert2DB(rd);
						SystemObjectArchiv sysObj = entityManager.find(
								SystemObjectArchiv.class, rd.getObject()
										.getId());
						if (sysObj != null) {
							// es gibt schon ArchivDatensätze für dieses
							// SystemObjekt
							neuerDatensatz.setSystemObject(sysObj);
						}
						// entityManager.persist(neuerDatensatz);
						entityManager.merge(neuerDatensatz);

						if (data2StoreList.isEmpty()) {
							break;
						}
						rd = data2StoreList.take();
					}
					entityManager.getTransaction().commit();
					// logger.info("Letzter Commit mit " + i + " Datensätzen ("
					// + (System.currentTimeMillis() - start) + "ms).");
				} catch (Exception ex) {
					// logger.error(
					// "Archivdaten konnten nicht gespeichert werden.", ex);
					entityManager.close();
					entityManager = factory.createEntityManager();
				} finally {

					entityManager.close();
				}

				// logger.info("Warteschlange = " + data2StoreList.size());
				yield();
			}

		}

	}

	private DatenverteilerArchivDatensatz convert2DB(final ResultData rd) {
		DataDescription desc = rd.getDataDescription();
		DatenverteilerArchivDatensatz result;
		if (desc.getAttributeGroup().getPid()
				.equals("atg.verkehrsDatenKurzZeitMq")) {
			result = new OdVerkehrsDatenKurzZeitMq(rd);
		} else if (desc.getAttributeGroup().getPid()
				.equals("atg.verkehrsDatenLangZeitIntervall")) {
			result = new OdVerkehrsDatenLangZeitIntervall(rd);
		} else if (desc.getAttributeGroup().getPid()
				.equals("atg.verkehrsDatenKurzZeitFs")) {
			result = new OdVerkehrsDatenKurzZeitFs(rd);
		} else {
			result = new DefaultArchivData(rd);
		}
		return result;
	}

	public Archivator(ClientDavInterface connection) {
		this(Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME),
				connection);
	}

	public Archivator(EntityManagerFactory emFactory,
			ClientDavInterface connection) {
		factory = emFactory;

		DBStorageThread s = new DBStorageThread();
		s.start();

		this.connection = connection;
		subscribeDavData();
	}

	private void subscribeDavData() {

		AttributeGroup atgArchiv = connection.getDataModel().getAttributeGroup(
				"atg.archiv");
		Aspect aspParameterSoll = connection.getDataModel().getAspect(
				"asp.parameterSoll");
		DataDescription desc = new DataDescription(atgArchiv, aspParameterSoll);
		DataIdentification dataId = new DataIdentification(
				connection.getLocalConfigurationAuthority(), desc);
		SettingsManager settingsManager = new SettingsManager(connection,
				dataId);

		SettingsManagerUpdateListener settingsManagerUpdateListener = new SettingsManagerUpdateListener();
		settingsManager.addUpdateListener(settingsManagerUpdateListener);
		settingsManager.addEndOfSettingsListener(settingsManagerUpdateListener);
		settingsManager.start();
		logger.info("Anmeldung am Datenverteiler abgeschlossen, jetzt gehts los...");
	}

	@Override
	public void update(final ResultData[] arg0) {
		if (arg0 != null) {
			try {
				for (ResultData rd : arg0) {
					data2StoreList.add(rd);
				}
			} catch (Exception e) {
				// FIXME: pauschales Exception werfen is böse
				logger.error(
						"Archiv kann Datensatz nicht der Warteschlange hinzufügen.",
						e);
			}
		}
	}
}
