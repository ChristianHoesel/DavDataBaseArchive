package de.hoesel.dav.ars;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
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
				long start = System.currentTimeMillis();
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
//				Logger.getLogger(getClass().getName()).info(
//						resultData.length + " Datensaetze gespeichert. "
//								+ (System.currentTimeMillis() - start) + "ms");
			} catch (Exception ex) {
				ex.printStackTrace();
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

		for (SystemObject t : connection.getDataModel().getTypeTypeObject()
				.getObjects()) {

			SystemObjectType type = ((SystemObjectType) t);
			for (AttributeGroup atg : type.getAttributeGroups()) {
				for (Aspect asp : atg.getAspects()) {
					DataDescription desc = new DataDescription(atg, asp);
					try {
						connection.subscribeReceiver(this, type.getElements(),
								desc, ReceiveOptions.normal(),
								ReceiverRole.receiver());
						// System.out.println("Anmeldung: " + desc);
					} catch (Exception e) {
						// e.printStackTrace();
					}

				}
			}
		}
		Logger.getLogger(getClass().getName()).info(
				"Anmeldung am Datenverteiler abgeschlossen, jetzt gehts los.");
	}

	@Override
	public void update(final ResultData[] arg0) {
		if (arg0 != null) {
			threadPool.execute(new DBThread(arg0));
		}

	}
}
