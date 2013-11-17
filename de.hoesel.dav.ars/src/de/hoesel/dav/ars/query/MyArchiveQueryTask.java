package de.hoesel.dav.ars.query;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataState;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.archive.ArchiveDataKind;
import de.bsvrz.dav.daf.main.archive.ArchiveDataKindCombination;
import de.bsvrz.dav.daf.main.archive.ArchiveDataSpecification;
import de.bsvrz.dav.daf.main.archive.ArchiveOrder;
import de.bsvrz.dav.daf.main.archive.ArchiveRequestOption;
import de.bsvrz.dav.daf.main.archive.ArchiveTimeSpecification;
import de.bsvrz.dav.daf.main.archive.TimingType;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.impl.archive.ArchiveDataCompression;
import de.bsvrz.sys.funclib.communicationStreams.StreamMultiplexer;
import de.bsvrz.sys.funclib.communicationStreams.StreamMultiplexerDirector;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.hoesel.dav.ars.jpa.DefaultArchivData;
import de.hoesel.dav.ars.jpa.SystemObjectArchiv;

public class MyArchiveQueryTask implements Runnable {

	private final class ArchivStreamMultiplexerDiretor implements
			StreamMultiplexerDirector {
		private final Iterator iterator;

		private ArchivStreamMultiplexerDiretor(Iterator iterator) {
			this.iterator = iterator;
		}

		@Override
		public byte[] take(int indexOfStream) {

			if (iterator.hasNext()) {
				try {

					DefaultArchivData datum = (DefaultArchivData) (iterator
							.next());

					while (datum.getData() == null && iterator.hasNext()) {
						datum = (DefaultArchivData) iterator.next();
					}

					if (datum.getData() == null) {
						// letzter Datensatz
					}

					// Datenaufbereitung gemaess DatK 7.2.3.12
					bosResult.reset();

					Serializer serializer = SerializingFactory
							.createSerializer(serializerVersion, bosResult);

					// TODO:
					serializer.writeInt(ArchiveDataKind.ONLINE.getCode());
					// Datenzeitstempel
					serializer.writeLong(datum.getTimestamp().getTime());
					// Archivierungszeitstempel
					// TODO:
					serializer.writeLong(datum.getTimestamp().getTime());
					serializer.writeLong(datum.getDb_id());
					byte[] data = datum.getData();
					if (data == null || data.length == 0) {
						// TODO:
						serializer.writeInt(DataState.NO_DATA.getCode());
						serializer.writeInt(serializerVersion);
						serializer.writeByte(ArchiveDataCompression.NONE
								.getCode());
						serializer.writeInt(0);
					} else {
						// TODO:
						serializer.writeInt(DataState.DATA.getCode());
						serializer.writeInt(serializerVersion);
						serializer.writeByte(ArchiveDataCompression.NONE
								.getCode());

						serializer.writeInt(data.length);
						serializer.writeBytes(data);

					}
					// System.out.println("Versende ( "+datum.getSystemObject().getPid()+":Index "+datum.getDb_id()+": "+SimpleDateFormat.getDateTimeInstance().format(datum.getTimestamp())+"): "
					// + Arrays.toString(bosResult.toByteArray()));
					return bosResult.toByteArray();
				} catch (NoSuchVersionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			return null;
		}

		@Override
		public void streamAborted(int indexOfStream) {
			// TODO Auto-generated method stub

		}

		@Override
		public void sendData(byte[] streamDataPacket) {
			try {
				gData.getItem(ArchivQueryReceiver.ATT_MESSAGE_TYP_NAME)
						.asUnscaledValue().set(MessageType.STREAM_DATA);
				Data.Array data = gData
						.getArray(ArchivQueryReceiver.ATT_DATA_NAME);
				data.setLength(streamDataPacket.length);
				for (int i = 0; i < streamDataPacket.length; i++) {
					data.getItem(i).asUnscaledValue().set(streamDataPacket[i]);
				}

				gResultData.setDataTime(System.currentTimeMillis());
				con.sendData(gResultData);
			} catch (Exception e) {
				Logger.getLogger(getClass().getName())
						.log(Level.SEVERE,
								"Daten-Versand an Empfaenger-Applikations-Objekt fehlgeschlagen",
								e);
			}
		}
	}

	/**
	 * Default-Anzahl der Pakete, die pro Stream initial an den Empfaenger
	 * geschickt werden duerfen. Wert laut Datenkatalog.
	 */
	private static final int BLOCKING_FACTOR_MUX_DEFAULT = 10;

	/**
	 * Default-Groesse des Puffers im Stream-Multiplexer. Wert laut
	 * Datenkatalog.
	 */
	private static final int BUFFER_SIZE_MUX_DEFAULT = 100000;

	private StreamMultiplexer mux;
	private ClientDavInterface con;
	private ResultData rd;

	private SystemObject queryAppObj;
	private int queryIdx;
	// private Serializer serializer;
	private Deserializer deserializer;
	private int serializerVersion;

	/** Wiederverwendbarer ByteArrayOutputStream. */
	protected ByteArrayOutputStream bosResult = new ByteArrayOutputStream(
			16 * 1024);
	private ResultData gResultData;

	private EntityManager em;

	private Data gData;

	public MyArchiveQueryTask(EntityManagerFactory entityManagerFactory,
			final ClientDavInterface con, final ResultData resultData)
			throws NoSuchVersionException {
		this.con = con;
		this.rd = resultData;

		em = entityManagerFactory.createEntityManager();

		// TODO: Initialisierung vielleicht erst später und damit nicht im
		// Updater Thread des DAV.
		queryAppObj = resultData.getData()
				.getReferenceValue(ArchivQueryReceiver.ATT_SENDER_NAME)
				.getSystemObject();
		queryIdx = resultData.getData()
				.getUnscaledValue(ArchivQueryReceiver.ATT_QUERY_IDX_NAME)
				.intValue();

		AttributeGroup atgQuery = con.getDataModel().getAttributeGroup(
				ArchivQueryReceiver.ATG_QUERY_PID);
		Aspect asp = con.getDataModel().getAspect(
				ArchivQueryReceiver.ASP_RESPONSE_PID);
		DataDescription ddResponse = new DataDescription(atgQuery, asp);

		gData = con.createData(atgQuery);
		gData.getReferenceValue(ArchivQueryReceiver.ATT_SENDER_NAME)
				.setSystemObject(con.getDataModel().getConfigurationAuthority());
		gData.getItem(ArchivQueryReceiver.ATT_QUERY_IDX_NAME).asUnscaledValue()
				.set(queryIdx);

		byte[] dataArray = resultData.getData()
				.getUnscaledArray(ArchivQueryReceiver.ATT_DATA_NAME)
				.getByteArray();
		serializerVersion = getSerVersion(dataArray); // Erste 4 Bytes =
														// Serialisierer-Version
		bosResult.reset();

		InputStream stream = new ByteArrayInputStream(dataArray, 4,
				dataArray.length - 4);
		deserializer = SerializingFactory.createDeserializer(serializerVersion,
				stream);

		gResultData = new ResultData(queryAppObj, ddResponse, 0, gData); // Datenzeit
																			// wird
																			// spaeter
																			// gesetzt
	}

	/**
	 * 
	 * Dekodiert die Serialisiererversion wie im Datenkatalog spezifiziert. Da
	 * die Implementierungen der zum Schreiben der Container verwendeten
	 * {@link ByteIO}-Klasse sich aendern koennen, ist der Algorithmus extra
	 * aufgefuehrt. Kopiert aus Los B.
	 * 
	 * @param data
	 * 
	 * @return Serialisiererversion
	 */
	private int getSerVersion(byte[] data) {
		return ((data[0] & 0xFF) << 24) + ((data[1] & 0xFF) << 16)
				+ ((data[2] & 0xFF) << 8) + (data[3] & 0xFF);
	}

	@Override
	public void run() {

		try {
			ArchiveDataSpecification[] archiveDataSpec = parseArchiveDataSpec();

			ArchiveDataSpecification spec = archiveDataSpec[0];
			SystemObject object = spec.getObject();

			SystemObjectArchiv jpaObj = em.find(SystemObjectArchiv.class,
					object.getId());
			Query createQuery = em
					.createQuery("select archivData from DefaultArchivData as archivData where "
							+ "archivData.systemObject = :JPA_OBJECT"
							+ " and archivData.asp = :ASPECT"
							+ " and archivData.atg = :ATTRIBUTGROUP"
							+ " order by archivData.timestamp");
			createQuery.setParameter("JPA_OBJECT", jpaObj);
			createQuery.setParameter("ASPECT", spec.getDataDescription()
					.getAspect());
			createQuery.setParameter("ATTRIBUTGROUP", spec.getDataDescription()
					.getAttributeGroup());

			List resultList = createQuery.getResultList();

			final Iterator iterator = resultList.iterator();

			mux = new StreamMultiplexer(1, BLOCKING_FACTOR_MUX_DEFAULT,
					BUFFER_SIZE_MUX_DEFAULT, serializerVersion,
					new ArchivStreamMultiplexerDiretor(iterator));

			sendInitialResponse(true, "");
			mux.sendAllStreamData();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			// e1.printStackTrace();
			sendInitialResponse(true, e1.getLocalizedMessage());
		}
	}

	/**
	 * Interpretiert die empfangene Archivanfrage oder
	 * Archiv-Informationsanfrage und zerlegt sie in einzelne Unterabfragen vom
	 * Typ ArchiveDataSpecification.
	 * 
	 * @param ds
	 *            Deserialisierer mit der Anfrage.
	 * 
	 * @return Feld von Unterabfragen vom Typ ArchiveDataSpecification.
	 * 
	 * @throws IOException
	 */
	private ArchiveDataSpecification[] parseArchiveDataSpec()
			throws IOException {

		deserializer.readInt(); // Prioritaet; bereits vom InQueuesMgr erledigt
		int receiverBufferSize = deserializer.readInt(); // Groesse des
															// Empfangspuffers

		// ====== Analyse der empfangenen Daten erfolgt hier gemaess ======
		// ====== Datenkatalog Kapitel 7.2.3.12 ArchivAnfrageSchnittstelle
		// ======

		int anzQueries = deserializer.readInt(); // Groesse der Liste mit den
													// ArchiveDataSpecification
		ArchiveDataSpecification[] result = new ArchiveDataSpecification[anzQueries];

		for (int i = 0; i < anzQueries; i++) {
			TimingType tt = getTimingType(deserializer.readByte()); // Typ der
			// Timingangabe

			boolean relative = deserializer.readByte() == 1; // Intervallstart
																// relativ
			long start = deserializer.readLong(); // Intervallstart
			long end = deserializer.readLong(); // Intervallende

			ArchiveTimeSpecification timeSpec = new ArchiveTimeSpecification(
					tt, relative, start, end);

			boolean oa = deserializer.readByte() == 1; // Online aktuell
			boolean on = deserializer.readByte() == 1; // Online nachgeliefert
			boolean na = deserializer.readByte() == 1; // Nachgefordert aktuell
			boolean nn = deserializer.readByte() == 1; // Nachgefordert
														// nachgeliefert

			ArchiveDataKindCombination adkComb = getADKCombination(oa, on, na,
					nn);

			int orderCode = deserializer.readInt();
			ArchiveOrder order = orderCode != 0 ? ArchiveOrder
					.getInstance(orderCode) : null;

			int reqOptCode = deserializer.readInt();
			ArchiveRequestOption reqOpt = reqOptCode != 0 ? ArchiveRequestOption
					.getInstance(reqOptCode) : null;

			AttributeGroup atg = (AttributeGroup) deserializer
					.readObjectReference(con.getDataModel());
			Aspect asp = (Aspect) deserializer.readObjectReference(con
					.getDataModel());
			short sv = deserializer.readShort();
			SystemObject obj = deserializer.readObjectReference(con
					.getDataModel());

			result[i] = new ArchiveDataSpecification(timeSpec, adkComb, order,
					reqOpt, new DataDescription(atg, asp, sv), obj);
		}
		return result;
	}

	/**
	 * Liefert den {@link TimingType} mit dem angegebenen Integer-Typ,
	 * <code>null</code> falls nicht gefunden.
	 * 
	 * @param type
	 *            Interger-Darstellung
	 * 
	 * @return TimingType, <code>null</code> falls nicht gefunden
	 */
	private TimingType getTimingType(int type) {
		switch (type) {
		case 1:
			return TimingType.DATA_TIME;
		case 2:
			return TimingType.ARCHIVE_TIME;
		case 3:
			return TimingType.DATA_INDEX;
		default:
			return null;
		}
	}

	/**
	 * Erzeugt eine {@link ArchiveDataKindCombination} aus den angegebenen
	 * Parametern, sofern mindestens einer der vier Datensatzarten
	 * <code>true</code> ist.
	 * 
	 * @param oa
	 *            Datensatzart Online aktuell
	 * @param on
	 *            Datensatzart Online nachgeliefert
	 * @param na
	 *            Datensatzart Nachgefordert aktuell
	 * @param nn
	 *            Datensatzart Nachgefordert nachgeliefert
	 * 
	 * @return ArchiveDataKindCombination, <code>null</code> falls alle 4
	 *         Parameter <code>false</code> sind.
	 */
	private ArchiveDataKindCombination getADKCombination(boolean oa,
			boolean on, boolean na, boolean nn) {
		List<ArchiveDataKind> dataKinds = new ArrayList<ArchiveDataKind>();
		if (oa)
			dataKinds.add(ArchiveDataKind.ONLINE);
		if (on)
			dataKinds.add(ArchiveDataKind.ONLINE_DELAYED);
		if (na)
			dataKinds.add(ArchiveDataKind.REQUESTED);
		if (nn)
			dataKinds.add(ArchiveDataKind.REQUESTED_DELAYED);

		switch (dataKinds.size()) {
		case 1:
			return new ArchiveDataKindCombination(dataKinds.get(0));
		case 2:
			return new ArchiveDataKindCombination(dataKinds.get(0),
					dataKinds.get(1));
		case 3:
			return new ArchiveDataKindCombination(dataKinds.get(0),
					dataKinds.get(1), dataKinds.get(2));
		case 4:
			return new ArchiveDataKindCombination(dataKinds.get(0),
					dataKinds.get(1), dataKinds.get(2), dataKinds.get(3));
		default:
			return null; // Falls keine Datensatzart gewaehlt worden ist
		}
	}

	public void setMaximumStreamTicketIndexForStream(byte[] dataArray)
			throws IOException {
		if (mux != null) {
			mux.setMaximumStreamTicketIndexForStream(dataArray);
		}
	}

	/**
	 * Sendet die initiale Ergebnismeldung zur Archiv-Anfrage an das
	 * Empfaenger-Applikations-Objekt.
	 * 
	 * @param success
	 *            Anfrage erfolgreich
	 * @param errorMsg
	 *            Fehlermeldung bei Misserfolg
	 * @throws IOException
	 */
	private void sendInitialResponse(boolean success, String errorMsg) {
		// ====== Datenaufbereitung erfolgt hier gemaess ======
		// ====== Datenkatalog Kapitel 7.2.3.12 ArchivAnfrageSchnittstelle
		// ======
		// ====== Unterpunkt ArchivNachrichtenTyp 2 ======
		bosResult.reset();

		try {
			Serializer serializer = SerializingFactory.createSerializer(
					serializerVersion, bosResult);

			serializer.writeByte(success ? 1 : 0); // Anfrage erfolgreich
													// ja/nein
			serializer.writeString(errorMsg); // Fehlermeldung bei Misserfolg
			serializer.writeInt(BLOCKING_FACTOR_MUX_DEFAULT); // Blocking-Faktor
																// des
																// Stream-Multiplexers
																// und
																// -Demultiplexers
			gData.getItem(ArchivQueryReceiver.ATT_MESSAGE_TYP_NAME)
					.asUnscaledValue().set(MessageType.INITIAL_QUERY_RESULT);
			Data.Array data = gData.getArray(ArchivQueryReceiver.ATT_DATA_NAME);

			byte[] byteArray = bosResult.toByteArray();
			data.setLength(byteArray.length);
			for (int i = 0; i < byteArray.length; i++) {
				data.getItem(i).asUnscaledValue().set(byteArray[i]);
			}
			gResultData.setDataTime(System.currentTimeMillis());
			con.sendData(gResultData);
		} catch (Exception e) {
			Logger.getLogger(getClass().getName())
					.log(Level.SEVERE,
							"Daten-Versand an Empfaenger-Applikations-Objekt fehlgeschlagen",
							e);
		}
	}

}
