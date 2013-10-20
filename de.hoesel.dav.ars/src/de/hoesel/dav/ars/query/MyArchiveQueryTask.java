package de.hoesel.dav.ars.query;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
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
import de.bsvrz.sys.funclib.communicationStreams.StreamMultiplexer;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;

public class MyArchiveQueryTask implements Runnable {
	
	private StreamMultiplexer mux;
	private ClientDavInterface con;
	private ResultData rd;
	
	
	private SystemObject queryAppObj;
	private int queryIdx;
	private Serializer serializer;
	private Deserializer deserializer;
	private int serializerVersion;
	
	/** Wiederverwendbarer ByteArrayOutputStream. */
	protected ByteArrayOutputStream bosResult = new ByteArrayOutputStream(16 * 1024);
	private ResultData gResultData;
	

	public MyArchiveQueryTask(final ClientDavInterface con, final ResultData resultData) throws NoSuchVersionException {
		this.con = con;
		this.rd = resultData;
		
		//TODO: Initialisierung vielleicht erst später und damit nicht im Updater Thread des DAV.
		queryAppObj = resultData.getData().getReferenceValue(ArchivQueryReceiver.ATT_SENDER_NAME).getSystemObject();
		queryIdx = resultData.getData().getUnscaledValue(ArchivQueryReceiver.ATT_QUERY_IDX_NAME).intValue();
		
		AttributeGroup atgQuery = con.getDataModel().getAttributeGroup(ArchivQueryReceiver.ATG_QUERY_PID);
		Aspect asp = con.getDataModel().getAspect(ArchivQueryReceiver.ASP_RESPONSE_PID);
		DataDescription ddResponse = new DataDescription(atgQuery, asp);

		Data gData = con.createData(atgQuery);
		gData.getReferenceValue(ArchivQueryReceiver.ATT_SENDER_NAME).setSystemObject(con.getDataModel().getConfigurationAuthority());
		gData.getItem(ArchivQueryReceiver.ATT_QUERY_IDX_NAME).asUnscaledValue().set(queryIdx);

		byte[] dataArray = resultData.getData().getUnscaledArray(ArchivQueryReceiver.ATT_DATA_NAME).getByteArray();
		serializerVersion = getSerVersion(dataArray); // Erste 4 Bytes = Serialisierer-Version
		bosResult.reset();
		serializer = SerializingFactory.createSerializer(serializerVersion, bosResult);

		InputStream stream = new ByteArrayInputStream(dataArray, 4, dataArray.length - 4);
		deserializer = SerializingFactory.createDeserializer(serializerVersion, stream);

		gResultData = new ResultData(queryAppObj, ddResponse, 0, gData);	// Datenzeit wird spaeter gesetzt
		
		createStreamMultiplexer();
	}
	
	/**
	 * 
	 * Dekodiert die Serialisiererversion wie im Datenkatalog spezifiziert. Da die Implementierungen der zum Schreiben der Container verwendeten {@link
	 * ByteIO}-Klasse sich aendern koennen, ist der Algorithmus extra aufgefuehrt.
	 * Kopiert aus Los B.
	 *
	 * @param data
	 *
	 * @return Serialisiererversion
	 */
	private int getSerVersion(byte[] data) {
		return ((data[0] & 0xFF) << 24) + ((data[1] & 0xFF) << 16) + ((data[2] & 0xFF) << 8) + (data[3] & 0xFF);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}
	
	private void createStreamMultiplexer() {
		Data data = rd.getData(); // Datensatz
		SystemObject queryAppObj = data.getReferenceValue(ArchivQueryReceiver.ATT_SENDER_NAME)
				.getSystemObject(); // Anfrage-Applikation
		int queryIdx = data.getUnscaledValue(ArchivQueryReceiver.ATT_QUERY_IDX_NAME).intValue(); // Anfrage-Index
		ArchivQueryIdentifier identifier = new ArchivQueryIdentifier(
				queryAppObj, queryIdx);
		
		 mux = new StreamMultiplexer(numberOfStreams, blockingFactor, bufferSizeStreamMultiplexer, serializerVersion, director)
		
	}
	
	public StreamMultiplexer getStreamMultiplexer(){
		return mux;
	}
	
	
	/**
	 * Interpretiert die empfangene Archivanfrage oder Archiv-Informationsanfrage und zerlegt sie in einzelne Unterabfragen vom Typ ArchiveDataSpecification.
	 *
	 * @param ds Deserialisierer mit der Anfrage.
	 *
	 * @return Feld von Unterabfragen vom Typ ArchiveDataSpecification.
	 *
	 * @throws IOException
	 */
	private ArchiveDataSpecification[] parseArchiveDataSpec(Deserializer ds) throws IOException {
		// ======     Analyse der empfangenen Daten erfolgt hier gemaess     ======
		// ======  Datenkatalog Kapitel 7.2.3.12 ArchivAnfrageSchnittstelle  ======

		int anzQueries = deserializer.readInt();        // Groesse der Liste mit den ArchiveDataSpecification
		ArchiveDataSpecification[] result = new ArchiveDataSpecification[anzQueries];

		for(int i = 0; i < anzQueries; i++) {
			TimingType tt = getTimingType(ds.readByte());			   // Typ der Timingangabe

			boolean relative = ds.readByte() == 1;		        // Intervallstart relativ
			long start = ds.readLong();                // Intervallstart
			long end = ds.readLong();	              // Intervallende

			ArchiveTimeSpecification timeSpec = new ArchiveTimeSpecification(tt, relative, start, end);

			boolean oa = ds.readByte() == 1;			 // Online aktuell
			boolean on = ds.readByte() == 1;			 // Online nachgeliefert
			boolean na = ds.readByte() == 1;			 // Nachgefordert aktuell
			boolean nn = ds.readByte() == 1;			 // Nachgefordert nachgeliefert

			ArchiveDataKindCombination adkComb = getADKCombination(oa, on, na, nn);

			int orderCode = ds.readInt();
			ArchiveOrder order = orderCode != 0 ? ArchiveOrder.getInstance(orderCode) : null;

			int reqOptCode = ds.readInt();
			ArchiveRequestOption reqOpt = reqOptCode != 0 ? ArchiveRequestOption.getInstance(reqOptCode) : null;

			AttributeGroup atg = (AttributeGroup)ds.readObjectReference(con.getDataModel());
			Aspect asp = (Aspect)ds.readObjectReference(con.getDataModel());
			short sv = ds.readShort();
			SystemObject obj = ds.readObjectReference(con.getDataModel());

			result[i] = new ArchiveDataSpecification(timeSpec, adkComb, order, reqOpt, new DataDescription(atg, asp, sv), obj);
		}
		return result;
	}
	
	/**
	 * Liefert den {@link TimingType} mit dem angegebenen Integer-Typ, <code>null</code> falls nicht gefunden.
	 *
	 * @param type Interger-Darstellung
	 *
	 * @return TimingType, <code>null</code> falls nicht gefunden
	 */
	private TimingType getTimingType(int type) {
		switch(type) {
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
	 * Erzeugt eine {@link ArchiveDataKindCombination} aus den angegebenen Parametern, sofern mindestens einer der vier Datensatzarten <code>true</code> ist.
	 *
	 * @param oa Datensatzart Online aktuell
	 * @param on Datensatzart Online nachgeliefert
	 * @param na Datensatzart Nachgefordert aktuell
	 * @param nn Datensatzart Nachgefordert nachgeliefert
	 *
	 * @return ArchiveDataKindCombination, <code>null</code> falls alle 4 Parameter <code>false</code> sind.
	 */
	private ArchiveDataKindCombination getADKCombination(boolean oa, boolean on, boolean na, boolean nn) {
		List<ArchiveDataKind> dataKinds = new ArrayList<ArchiveDataKind>();
		if(oa) dataKinds.add(ArchiveDataKind.ONLINE);
		if(on) dataKinds.add(ArchiveDataKind.ONLINE_DELAYED);
		if(na) dataKinds.add(ArchiveDataKind.REQUESTED);
		if(nn) dataKinds.add(ArchiveDataKind.REQUESTED_DELAYED);

		switch(dataKinds.size()) {
			case 1:
				return new ArchiveDataKindCombination(dataKinds.get(0));
			case 2:
				return new ArchiveDataKindCombination(dataKinds.get(0), dataKinds.get(1));
			case 3:
				return new ArchiveDataKindCombination(dataKinds.get(0), dataKinds.get(1), dataKinds.get(2));
			case 4:
				return new ArchiveDataKindCombination(dataKinds.get(0), dataKinds.get(1), dataKinds.get(2), dataKinds.get(3));
			default:
				return null; // Falls keine Datensatzart gewaehlt worden ist
		}
	}

}
