package de.hoesel.dav.ars;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
import de.hoesel.dav.ars.jpa.OdVerkehrsDatenKurzZeitMq;
import de.hoesel.dav.ars.jpa.SystemObjectArchiv;

public class Archivator implements ClientReceiverInterface {

	public static final String PERSISTENCE_UNIT_NAME = "de.hoesel.dav.ars";
	private final EntityManagerFactory factory;

	/*
	 * Damit synchronisieren wir unsere Warteschlagen und stellen sicher, dass
	 * immer nur ein Commit gleichzeitig gemacht wird. Das ist notwendig, weil
	 * wir schauen müssen, ob für des jeweilige SystemObjekt bereits ein
	 * Archivdatensatz gespeichert wurde, ist das der Fall, so verwenden wir das
	 * bereits vorhandene SystemObjekt. Wenn mehrere Thread gleichzeitig mit der
	 * DB agieren, kann es sonst passieren, dass 2 gleichzeitig dasselbe
	 * SystemObjekt anlegen.
	 */
	private Executor threadPool = Executors.newSingleThreadExecutor();// newCachedThreadPool();
	private ClientDavInterface connection;

	private static final Debug logger = Debug.getLogger();

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

	private class DBThread implements Runnable {

		private final ResultData[] resultData;
		private EntityManager em;

		public DBThread(ResultData[] resultData) {
			this.resultData = resultData;
			em = factory.createEntityManager();
		}

		@Override
		public void run() {
			try {
				em.getTransaction().begin();
				for (ResultData rd : resultData) {

					DatenverteilerArchivDatensatz neuerDatensatz = convert2DB(rd);
					SystemObjectArchiv sysObj = em.find(
							SystemObjectArchiv.class, rd.getObject().getId());
					if (sysObj != null) {
						// es gibt schon ArchivDatensätze für dieses
						// SystemObjekt
						neuerDatensatz.setSystemObject(sysObj);
					}
					em.merge(neuerDatensatz);
				}
				em.getTransaction().commit();
			} catch (Exception ex) {
				logger.error("Archivdaten konnten nicht gespeichert werden.", ex);
			} finally {
				em.close();
			}
		}
	}

	private DatenverteilerArchivDatensatz convert2DB(final ResultData rd) {
		DataDescription desc = rd.getDataDescription();
		DatenverteilerArchivDatensatz result;
		if (desc.getAttributeGroup().getPid()
				.equals("atg.verkehrsDatenKurzZeitMq")) {
			result = new OdVerkehrsDatenKurzZeitMq(rd);
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
		logger.info(
				"Anmeldung am Datenverteiler abgeschlossen, jetzt gehts los.");
	}

	@Override
	public void update(final ResultData[] arg0) {
		if (arg0 != null) {
			threadPool.execute(new DBThread(arg0));
		}
	}
}
