/**
 * 
 */
package de.hoesel.dav.ars.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import javax.persistence.EntityManagerFactory;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.archive.ArchiveDataKindCombination;
import de.bsvrz.dav.daf.main.archive.ArchiveDataSpecification;
import de.bsvrz.dav.daf.main.archive.ArchiveOrder;
import de.bsvrz.dav.daf.main.archive.ArchiveRequestOption;
import de.bsvrz.dav.daf.main.archive.ArchiveTimeSpecification;
import de.bsvrz.dav.daf.main.archive.TimingType;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.communicationStreams.StreamMultiplexer;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * @author Christian
 * 
 */
public class ArchivQueryReceiver implements ClientReceiverInterface, ClientSenderInterface {

	private static final Debug logger = Debug.getLogger();

	/** PID der Attributgruppe ArchivAnfrageSchnittstelle. */
	public static final String ATG_QUERY_PID = "atg.archivAnfrageSchnittstelle";

	/**
	 * PID des Anfrage-Aspekts bei der Attributgruppe
	 * ArchivAnfrageSchnittstelle.
	 */
	public static final String ASP_QUERY_PID = "asp.anfrage";

	/**
	 * PID des Antwort-Aspekts bei der Attributgruppe
	 * ArchivAnfrageSchnittstelle.
	 */
	public static final String ASP_RESPONSE_PID = "asp.antwort";

	/** Name des Attributs mit Referenz auf den Absender eines Datensatzes. */
	public final static String ATT_SENDER_NAME = "absender";

	/**
	 * Name des Attributs mit dem Anfrage-Index zur Unterscheidung mehrerer
	 * paralleler Anfragen.
	 */
	public final static String ATT_QUERY_IDX_NAME = "anfrageIndex";

	/** Name des Attributs mit dem Typ der Nachricht. */
	public final static String ATT_MESSAGE_TYP_NAME = "nachrichtenTyp";

	/** Name des Attributs mit den Datenbytes der Nachricht. */
	public final static String ATT_DATA_NAME = "daten";

	private ClientDavInterface con;

	private static ArchivQueryReceiver INSTANCE;

	private Map<ArchivQueryIdentifier, MyArchiveQueryTask> aktiveAnfragen = new HashMap<ArchivQueryIdentifier, MyArchiveQueryTask>();

	private ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(10);

	private EntityManagerFactory entityManagerFactory;

	private ArchivQueryReceiver(EntityManagerFactory entityManagerFactory,
			final ClientDavInterface con) {
		this.con = con;
		this.entityManagerFactory = entityManagerFactory;
		AttributeGroup attributeGroup = con.getDataModel().getAttributeGroup(
				ATG_QUERY_PID);

		Aspect aspectQuery = con.getDataModel().getAspect(ASP_QUERY_PID);
//		Aspect aspectResponse = con.getDataModel().getAspect(ASP_RESPONSE_PID);
		DataDescription desc = new DataDescription(attributeGroup, aspectQuery);
//
//		try {
//			this.con.subscribeSender(this, con.getLocalConfigurationAuthority(),
//					new DataDescription(attributeGroup, aspectResponse),
//					SenderRole.sender());
//		} catch (OneSubscriptionPerSendData e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// TODO: parametrierbar, für welches Archiv wir uns als Empfänger für
		// Archivanfragen zuständig fühlen.
		this.con.subscribeReceiver(this, con.getLocalConfigurationAuthority(),
				desc, ReceiveOptions.normal(), ReceiverRole.drain());
	}

	public static final ArchivQueryReceiver getInstance(
			EntityManagerFactory entityManagerFactory, ClientDavInterface con) {
		if (INSTANCE == null) {
			INSTANCE = new ArchivQueryReceiver(entityManagerFactory, con);
		}
		return INSTANCE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.bsvrz.dav.daf.main.ClientReceiverInterface#update(de.bsvrz.dav.daf
	 * .main.ResultData[])
	 */
	@Override
	public void update(ResultData[] results) {
		for (ResultData rd : results) {
			if (rd.hasData()) {
				int msgType = rd.getData()
						.getUnscaledValue(ATT_MESSAGE_TYP_NAME).intValue();
				switch (msgType) {
				case MessageType.QUERY: // Anfrage
					// XXX: Erstmal keine Priorisierung: Artikel 3 GG: Alle sind
					// gleich...
					createNewArchivAnfrage(rd);
					break;
				case MessageType.QUERY_INFO: // Informationsanfrage:
					// TODO: Vielleicht nicht implementieren
					logger.warning("Archiv Informationsanfrage - Noch nicht implementiert :(");
					break;
				case MessageType.BACKUP: // Sicherungsauftrag:
					logger.warning("Archiv Sicherungsauftrage -> Bitte wenden Sie sich an Ihr DBMS ;)");
					// XXX: Brauchen wir eigentlich nicht mehr, ist Aufgabe des
					// DBMS
					break;
				case MessageType.DELETE: // Loeschauftrag fuer eine
											// Simulationsvariante:
					logger.warning("Archiv Löschanfrage für Simulationen - Noch nicht implementiert :(");
					// TODO:Spaeter vielleicht
					break;
				case MessageType.DELETION_TIME: // Loeschzeitpunkt-Aenderung:
					logger.warning("Archiv Löschzeitpunkt Änderung -> Bitte wenden Sie sich an Ihr DBMS ;)");
					// XXX: Brauchen wir eigentlich nicht mehr, ist Aufgabe des
					// DBMS
					break;
				case MessageType.RESTORE: // Wiederherstellung:
					logger.warning("Archiv Wiederherstellung -> Bitte wenden Sie sich an Ihr DBMS ;)");
					// XXX: Brauchen wir eigentlich nicht mehr, ist Aufgabe des
					// DBMS
					break;
				case MessageType.HEADER_RESTORE: // Verwaltungsinformationsabgleich:
					logger.warning("Archiv Verwaltungsinformationsabgleich -> Deprecated");
					// XXX: Brauchen wir eigentlich nicht mehr.
					break;
				case MessageType.STREAM_CONTROL: // Flusskontrolle:
					processStreamControl(rd);
					break;
				case MessageType.REQUEST_DID:
				case MessageType.REQUEST_AUTOM:
					logger.warning("Nicht unterstützte Anfrage: "
							+ rd.getData());
					break;

				default:
					logger.warning("Nachrichtentyp erhalten, der nicht bearbeitet werden kann: "
							+ msgType + " aus: " + rd.getData());
				}
			}
		}
	}

	private void createNewArchivAnfrage(ResultData rd) {
		Data data = rd.getData(); // Datensatz
		SystemObject queryAppObj = data.getReferenceValue(ATT_SENDER_NAME)
				.getSystemObject(); // Anfrage-Applikation
		int queryIdx = data.getUnscaledValue(ATT_QUERY_IDX_NAME).intValue(); // Anfrage-Index
		ArchivQueryIdentifier identifier = new ArchivQueryIdentifier(
				queryAppObj, queryIdx);

		
		AttributeGroup attributeGroup = con.getDataModel().getAttributeGroup(
				ATG_QUERY_PID);
		Aspect aspectResponse = con.getDataModel().getAspect(ASP_RESPONSE_PID);
		try {
			this.con.subscribeSender(this, queryAppObj,
					new DataDescription(attributeGroup, aspectResponse),
					SenderRole.sender());
		} catch (OneSubscriptionPerSendData e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
		
		
		MyArchiveQueryTask task;
		try {
			task = new MyArchiveQueryTask(entityManagerFactory, con, rd);
			synchronized (aktiveAnfragen) {
				aktiveAnfragen.put(identifier, task);
			}
			scheduler.execute(task);
		} catch (NoSuchVersionException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}

	}

	private void processStreamControl(ResultData resultData) {
		Data data = resultData.getData(); // Datensatz
		SystemObject queryAppObj = data.getReferenceValue(ATT_SENDER_NAME)
				.getSystemObject(); // Anfrage-Applikation
		int queryIdx = data.getUnscaledValue(ATT_QUERY_IDX_NAME).intValue(); // Anfrage-Index
		ArchivQueryIdentifier identifier = new ArchivQueryIdentifier(
				queryAppObj, queryIdx);
		byte[] dataArray = data.getUnscaledArray(ATT_DATA_NAME).getByteArray(); // Datenbytes
																				// mit
																				// Steuerungspaket

		// Multiplexer suchen, der fuer diese Anfrage-Applikation/Anfrage-Index
		// registriert ist,
		// und Steuerungspaket zur Flusskontrolle uebergeben:
		synchronized (aktiveAnfragen) {
			MyArchiveQueryTask myArchiveQueryTask = aktiveAnfragen
					.get(identifier);
			if (myArchiveQueryTask != null) {
				try {
					myArchiveQueryTask
							.setMaximumStreamTicketIndexForStream(dataArray);
				} catch (IOException e) {
					logger.warning(
							"Flusskontroll-Steuerungspaket konnte nicht an StreamMultiplexer uebergeben werden.",
							e);
				}
			}
		}

	}

	@Override
	public void dataRequest(SystemObject object,
			DataDescription dataDescription, byte state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isRequestSupported(SystemObject object,
			DataDescription dataDescription) {
		// TODO Auto-generated method stub
		return false;
	}

}
