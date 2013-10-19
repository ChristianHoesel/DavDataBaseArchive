/*
 * Copyright 2005-2008 by beck et al. projects GmbH, Munich
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * Copyright 2010 by Kappich Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.ars.ars.
 * 
 * de.bsvrz.ars.ars is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.ars.ars is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.ars.ars; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.hoesel.dav.ars.query;

import de.bsvrz.ars.ars.mgmt.ArchiveManager;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.archive.ArchiveDataKind;
import de.bsvrz.dav.daf.main.archive.ArchiveDataKindCombination;
import de.bsvrz.dav.daf.main.archive.ArchiveDataSpecification;
import de.bsvrz.dav.daf.main.archive.ArchiveOrder;
import de.bsvrz.dav.daf.main.archive.ArchiveRequestOption;
import de.bsvrz.dav.daf.main.archive.ArchiveTimeSpecification;
import de.bsvrz.dav.daf.main.archive.TimingType;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.losb.datk.MessageType;
import de.bsvrz.sys.funclib.losb.datk.PidQuery;
import de.bsvrz.sys.funclib.losb.kernsoftware.ConnectionManager;
import de.bsvrz.sys.funclib.losb.util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Oberklasse fuer Tasks, die Archivanfragen oder Archiv-Informationsanfragen bearbeiten.
 *
 * @author beck et al. projects GmbH
 * @author Alexander Schmidt
 * @version $Revision: 8212 $ / $Date: 2010-09-28 16:37:41 +0200 (Tue, 28 Sep 2010) $ / ($Author: rs $)
 */
public abstract class QueryTask extends Thread implements ClientSenderInterface {
	
	/** Groesse des Byte-Arrays fuer temporaeren Speicher beim Abfragen der Indexe. */
	protected final static int INDEX_MEMORY_SIZE = 16 * 1024;

	protected Debug logger;

	/** Datensatzart Online Aktuell */
	protected static ArchiveDataKind OA = ArchiveDataKind.ONLINE;

	/** Datensatzart Online Nachgeliefert */
	protected static ArchiveDataKind ON = ArchiveDataKind.ONLINE_DELAYED;

	/** Datensatzart Nachgefordert Aktuell */
	protected static ArchiveDataKind NA = ArchiveDataKind.REQUESTED;

	/** Datensatzart Nachgefordert Nachgeliefert */
	protected static ArchiveDataKind NN = ArchiveDataKind.REQUESTED_DELAYED;

	/** Zustand der Sendesteuerung (Senden / Anhalten / Abbrechen). */
	protected static enum SendingState {

		SEND,
		STOP,
		ABORT
	}

	;

	/** Mindest-Anzahl an ContainerFile-Objekten, die der PersistenzManager fuer diesen Task vorhalten soll. */
	public static final int MIN_CONTAINER_FILES = 32;

	/** Aktueller Zustand der Sendesteuerung. */
	protected SendingState sendingState;

	/** Spezielles Objekt zur Synchronisiserung. Immer dieses anstatt this verwenden! */
	protected Object syncObj = new Object();

	/** Speicher fuer das Abfragen der Indexe. */
	protected byte[] tempMem = new byte[INDEX_MEMORY_SIZE];

	/** Applikations-Objekt des Absenders der Archiv-Anfrage. */
	protected SystemObject queryAppObj;

	/** Attributgruppe fuer Archiv-Anfragen. */
	protected static AttributeGroup atgQuery;

	/** Datenidentifikation der Archiv-Antwort. */
	protected static DataDescription ddResponse;

	/** Anfrage-Index der anfragenden Applikation zur Archiv-Anfrage. */
	protected int queryIdx;

	/** Serialisiererversion der anfragenden Applikation, mit der auch die Antwort erstellt wird. */
	protected int serializerVersion;

	/** Dieses Datenobjekt wird zum Senden des Ergebnisses verwendet */
	protected Data gData;

	/** Wird zum Senden verwendet (muss nur einmal angelegt werden) */
	protected ResultData gResultData;

	/** Wiederverwendbarer ByteArrayOutputStream. */
	protected ByteArrayOutputStream bosResult = new ByteArrayOutputStream(16 * 1024);

	/** Der Serialisierer aus DAF. */
	protected Serializer serializer;

	/** Der Deserialisierer aus DAF. */
	protected Deserializer deserializer;

	private ClientDavInterface davVerbindung;


	/**
	 * Konstruktor fuer den QueryTask.
	 *
	 * @param archiveMgr Der Archiv-Manager.
	 * @param tMgr       Der MultiTaskManager.
	 */
	public QueryTask(ClientDavInterface davVerbindung) {
		super();
		this.davVerbindung = davVerbindung;
		queryAppObj = null;
		queryIdx = 0;
		serializerVersion = 0;
		sendingState = SendingState.STOP;
	}

	/**
	 * In dieser Methode werden alle notwendigen Objekte von DAV geladen.
	 *
	 * @param davCon Verbindung zum Datenverteiler
	 *
	 * @throws ConfigurationException
	 */
	public static void getObjectsFromDAV(ClientDavInterface davCon) throws ConfigurationException {
		atgQuery = davCon.getDataModel().getAttributeGroup(PidQuery.ATG_QUERY_PID);
		ddResponse = new DataDescription(atgQuery, davCon.getDataModel().getAspect(PidQuery.ASP_RESPONSE_PID));
	}


	/** @see ClientSenderInterface#isRequestSupported(SystemObject,DataDescription) */
	public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
		return true; // Sendesteuerung ist erwuenscht und wird in dataRequest() verarbeitet.
	}

	/** @see ClientSenderInterface#dataRequest(SystemObject,DataDescription,byte) */
	public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {
		synchronized(syncObj) {
			if(state == ClientSenderInterface.START_SENDING) {
				sendingState = SendingState.SEND;
			}
			else {
				sendingState = SendingState.ABORT;
			}
			syncObj.notifyAll();
		}
	}

	/**
	 * Meldet den Task als Sender fuer Archiv-Antworten an das angegebene Empfaenger-Applikations-Objekt an. Registriert den StreamMultiplexer fuer den Empfang
	 * von Flusskontroll-Steuerungspaketen.
	 *
	 * @param receiver Empfaenger-Applikations-Objekt
	 *
	 * @return <code>true</code> bei erfolgreicher Anmeldung, false sonst
	 *
	 * @throws OneSubscriptionPerSendData
	 * @throws ConfigurationException
	 */
	protected void subscribeSender() throws ConfigurationException, OneSubscriptionPerSendData {
		ConnectionManager.subscribeSender(archMgr.getDavCon(), this, queryAppObj, ddResponse, SenderRole.sender());
	}

	/** Meldet den Task als Sender fuer Archiv-Antworten an das angegebene Empfaenger-Applikations-Objekt ab. */
	protected void unsubscribeSender() {
		try {
			ConnectionManager.unsubscribeSender(archMgr.getDavCon(), this, queryAppObj, ddResponse);
		}
		catch(Exception e) {
			logger.warning(getName() + " Sende-Abmeldung fehlgeschlagen.", e);
		}
	}

	/**
	 * Legt alle Datenstrukturen an, die zu Beginn der Anfrage ermittelt werden koennen.
	 *
	 * @throws NoSuchVersionException
	 */
	protected void createQueryData() throws NoSuchVersionException {
		queryAppObj = resultData.getData().getReferenceValue(PidQuery.ATT_SENDER_NAME).getSystemObject();
		queryIdx = resultData.getData().getUnscaledValue(PidQuery.ATT_QUERY_IDX_NAME).intValue();

		gData = archMgr.getDavCon().createData(atgQuery);
		gData.getReferenceValue(PidQuery.ATT_SENDER_NAME).setSystemObject(archMgr.getConfigAuth());
		gData.getItem(PidQuery.ATT_QUERY_IDX_NAME).asUnscaledValue().set(queryIdx);

		byte[] dataArray = resultData.getData().getUnscaledArray(PidQuery.ATT_DATA_NAME).getByteArray();
		serializerVersion = Util.getSerVersion(dataArray); // Erste 4 Bytes = Serialisierer-Version
		bosResult.reset();
		serializer = SerializingFactory.createSerializer(serializerVersion, bosResult);

		InputStream stream = new ByteArrayInputStream(dataArray, 4, dataArray.length - 4);
		deserializer = SerializingFactory.createDeserializer(serializerVersion, stream);

		gResultData = new ResultData(queryAppObj, ddResponse, 0, gData);	// Datenzeit wird spaeter gesetzt
	}

	/**
	 * Sendet das uebergebene Byte-Array in einem Ergebnis-Datensatz ({@link ResultData}) an das Empfaenger-Applikations-Objekt.
	 *
	 * @param resultBytes Zu versendendes Byte-Array
	 * @param msgType     ArchivNachrichtenTyp wie definiert in {@link MessageType}
	 */
	protected void sendResultData(byte[] resultBytes, int msgType) {
		try {
			gData.getItem(PidQuery.ATT_MESSAGE_TYP_NAME).asUnscaledValue().set(msgType);
			Data.Array data = gData.getArray(PidQuery.ATT_DATA_NAME);
			data.setLength(resultBytes.length);
			for(int i = 0; i < resultBytes.length; i++) {
				data.getItem(i).asUnscaledValue().set(resultBytes[i]);
			}

			gResultData.setDataTime(System.currentTimeMillis());
			archMgr.getDavCon().sendData(gResultData);
		}
		catch(Exception e) {
			logger.error(getName() + " Daten-Versand an Empfaenger-Applikations-Objekt fehlgeschlagen", e);
		}
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
	protected ArchiveDataSpecification[] parseArchiveDataSpec(Deserializer ds) throws IOException {
		// ======     Analyse der empfangenen Daten erfolgt hier gemaess     ======
		// ======  Datenkatalog Kapitel 7.2.3.12 ArchivAnfrageSchnittstelle  ======

		int anzQueries = deserializer.readInt();        // Groesse der Liste mit den ArchiveDataSpecification
		ArchiveDataSpecification[] result = new ArchiveDataSpecification[anzQueries];

		for(int i = 0; i < anzQueries; i++) {
			TimingType tt = Util.getTimingType(ds.readByte());			   // Typ der Timingangabe

			boolean relative = ds.readByte() == 1;		        // Intervallstart relativ
			long start = ds.readLong();                // Intervallstart
			long end = ds.readLong();	              // Intervallende

			ArchiveTimeSpecification timeSpec = new ArchiveTimeSpecification(tt, relative, start, end);

			boolean oa = ds.readByte() == 1;			 // Online aktuell
			boolean on = ds.readByte() == 1;			 // Online nachgeliefert
			boolean na = ds.readByte() == 1;			 // Nachgefordert aktuell
			boolean nn = ds.readByte() == 1;			 // Nachgefordert nachgeliefert

			ArchiveDataKindCombination adkComb = Util.getADKCombination(oa, on, na, nn);

			int orderCode = ds.readInt();
			ArchiveOrder order = orderCode != 0 ? ArchiveOrder.getInstance(orderCode) : null;

			int reqOptCode = ds.readInt();
			ArchiveRequestOption reqOpt = reqOptCode != 0 ? ArchiveRequestOption.getInstance(reqOptCode) : null;

			AttributeGroup atg = (AttributeGroup)ds.readObjectReference(archMgr.getDataModel());
			Aspect asp = (Aspect)ds.readObjectReference(archMgr.getDataModel());
			short sv = ds.readShort();
			SystemObject obj = ds.readObjectReference(archMgr.getDataModel());

			result[i] = new ArchiveDataSpecification(timeSpec, adkComb, order, reqOpt, new DataDescription(atg, asp, sv), obj);
		}
		return result;
	}
}
