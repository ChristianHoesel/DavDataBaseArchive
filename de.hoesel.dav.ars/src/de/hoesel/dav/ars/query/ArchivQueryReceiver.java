/**
 * 
 */
package de.hoesel.dav.ars.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.communicationStreams.StreamMultiplexer;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * @author Christian
 * 
 */
public class ArchivQueryReceiver implements ClientReceiverInterface {

	private static final Debug logger = Debug.getLogger();

	/** PID der Attributgruppe ArchivAnfrageSchnittstelle. */
	private static final String ATG_QUERY_PID = "atg.archivAnfrageSchnittstelle";

	/**
	 * PID des Anfrage-Aspekts bei der Attributgruppe
	 * ArchivAnfrageSchnittstelle.
	 */
	private static final String ASP_QUERY_PID = "asp.anfrage";

	/**
	 * PID des Antwort-Aspekts bei der Attributgruppe
	 * ArchivAnfrageSchnittstelle.
	 */
	private static final String ASP_RESPONSE_PID = "asp.antwort";

	/** Name des Attributs mit Referenz auf den Absender eines Datensatzes. */
	private final static String ATT_SENDER_NAME = "absender";

	/**
	 * Name des Attributs mit dem Anfrage-Index zur Unterscheidung mehrerer
	 * paralleler Anfragen.
	 */
	private final static String ATT_QUERY_IDX_NAME = "anfrageIndex";

	/** Name des Attributs mit dem Typ der Nachricht. */
	private final static String ATT_MESSAGE_TYP_NAME = "nachrichtenTyp";

	/** Name des Attributs mit den Datenbytes der Nachricht. */
	private final static String ATT_DATA_NAME = "daten";

	private ClientDavInterface con;

	private static ArchivQueryReceiver INSTANCE;

	private Map<ArchivQueryIdentifier, StreamMultiplexer> aktiveAnfragen = new HashMap<ArchivQueryIdentifier, StreamMultiplexer>();

	private ArchivQueryReceiver(final ClientDavInterface con) {
		this.con = con;
		AttributeGroup attributeGroup = con.getDataModel().getAttributeGroup(
				ATG_QUERY_PID);
		Aspect aspect = con.getDataModel().getAspect(ASP_QUERY_PID);
		DataDescription desc = new DataDescription(attributeGroup, aspect);

		// TODO: parametrierbar, für welches Archiv wir uns als Empfänger für
		// Archivanfragen zuständig fühlen.
		this.con.subscribeReceiver(this, con.getLocalConfigurationAuthority(), desc,
				ReceiveOptions.normal(), ReceiverRole.drain());
	}

	public static final ArchivQueryReceiver getInstance(ClientDavInterface con) {
		if (INSTANCE == null) {
			INSTANCE = new ArchivQueryReceiver(con);
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
		// TODO Auto-generated method stub
		
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

			StreamMultiplexer mux = aktiveAnfragen.get(identifier);
			if (mux != null) {
				try {
					mux.setMaximumStreamTicketIndexForStream(dataArray);
				} catch (IOException e) {
					logger.warning(
							"Flusskontroll-Steuerungspaket konnte nicht an StreamMultiplexer uebergeben werden.",
							e);
				}
			}
		}

	}

}
