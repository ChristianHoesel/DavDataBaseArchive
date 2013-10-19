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
import de.bsvrz.ars.ars.mgmt.InQueuesMgr.DataReceiver;
import de.bsvrz.ars.ars.mgmt.datatree.DIdNodeNotFoundException;
import de.bsvrz.ars.ars.mgmt.datatree.DataIdentNode;
import de.bsvrz.ars.ars.persistence.ContainerDataIterator;
import de.bsvrz.ars.ars.persistence.ContainerFile;
import de.bsvrz.ars.ars.persistence.ContainerHdr;
import de.bsvrz.ars.ars.persistence.PersistenceException;
import de.bsvrz.ars.ars.persistence.index.IndexException;
import de.bsvrz.ars.ars.persistence.index.IndexResult;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataState;
import de.bsvrz.dav.daf.main.archive.ArchiveDataKind;
import de.bsvrz.dav.daf.main.archive.ArchiveDataKindCombination;
import de.bsvrz.dav.daf.main.archive.ArchiveDataSpecification;
import de.bsvrz.dav.daf.main.archive.ArchiveOrder;
import de.bsvrz.dav.daf.main.archive.ArchiveRequestOption;
import de.bsvrz.dav.daf.main.archive.ArchiveTimeSpecification;
import de.bsvrz.dav.daf.main.archive.TimingType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.impl.archive.ArchiveDataCompression;
import de.bsvrz.sys.funclib.communicationStreams.StreamMultiplexer;
import de.bsvrz.sys.funclib.communicationStreams.StreamMultiplexerDirector;
import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.losb.datk.MessageType;
import de.bsvrz.sys.funclib.losb.datk.PidQuery;
import de.bsvrz.sys.funclib.losb.kernsoftware.ConnectionManager;
import de.bsvrz.sys.funclib.losb.ringbuffer.SimpleRingBuffer;
import de.bsvrz.sys.funclib.losb.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.Inflater;

/**
 * Task fuer Bearbeitung von Archivanfragen.
 * <p/>
 * Die Archivanfrage wird analysiert und in ihre Unterabfragen {@link Query} zerlegt. Die Beantwortung erfolgt streambasiert mit einem Stream pro Unterabfrage.
 *
 * @author beck et al. projects GmbH
 * @author Thomas Schäfer
 * @author Alexander Schmidt
 * @version $Revision: 9801 $ / $Date: 2011-12-23 01:24:40 +0100 (Fri, 23 Dec 2011) $ / ($Author: rs $)
 */
public class ArchiveQueryTask extends QueryTask implements StreamMultiplexerDirector {

    /**
     * Default-Anzahl der Pakete, die pro Stream initial an den Empfaenger geschickt werden duerfen. Wert laut Datenkatalog.
     */
    private static final int BLOCKING_FACTOR_MUX_DEFAULT = 10;

    /**
     * Parametrierte Anzahl der Pakete, die pro Stream an den Empfaenger geschickt werden duerfen.
     */
    private static int blockingFactorMuxParam = BLOCKING_FACTOR_MUX_DEFAULT;

    /**
     * Tatsaechlich benutzte Anzahl der Pakete, die pro Stream an den Empfaenger geschickt werden, angepasst an die Groesse des Empfaengerpuffers.
     */
    private int blockingFactorMuxUsed;

    /**
     * Default-Groesse des Puffers im Stream-Multiplexer. Wert laut Datenkatalog.
     */
    private static final int BUFFER_SIZE_MUX_DEFAULT = 100000;

    /**
     * Parametrierte Groesse des Puffers im Stream-Multiplexer.
     */
    private static int bufferSizeMuxParam = BUFFER_SIZE_MUX_DEFAULT;

    /**
     * Tatsaechlich benutzte Groesse des Puffers im Stream-Multiplexer, angepasst an die Groesse des Empfaengerpuffers.
     */
    private int bufferSizeMuxUsed;

    /**
     * Der StreamMultiplexer zum Verpacken der Ergebnis-Datensaetze.
     */
    private StreamMultiplexer mux;

    /**
     * Task zur Verarbeitung der Flusskontroll-Steuerungspakete.
     */
    private FlowControlTask flowCtrl;

    /**
     * Liste der einzelnen Unterabfragen aus der aktuellen Archivanfrage.
     */
    private List<Query> queries = new ArrayList<Query>();

    /**
     * Groesse des Empfangspuffers der anfragenden Applikation.
     */
    private int receiverBufferSize;

    /**
     * Wurde die initiale Antwort (kann auch Fehlermeldung sein) verschickt?
     */
    private boolean initialResponseSent;

    /**
     * Zum Dekomprimieren. Wird hier gahalten, damit sowenig Instanzen wie moeglich angelegt werden (hoher Speicherverbrauch)
     */
    private Inflater decompresser = new Inflater();

    /**
     * Maximale Anzahl paralleler Archivanfragen je Applikation
     */
    public static final int MAXIMUM_REQUESTS_PER_APPLICATION = 5;

    /**
     * Erzeugt eine neue Query-Task.
     *
     * @param archiveMgr Archiv-Verwaltung
     * @param tMgr       Zugeordneter Task-Manager
     * @param flowCtrl   Verwaltung der Flusskontrolle
     */
    public ArchiveQueryTask(ArchiveManager archiveMgr,  FlowControlTask flowCtrl) {
        super(archiveMgr);
        logger = Debug.getLogger();
        this.flowCtrl = flowCtrl;
    }

    /**
     * Fuehrt alle notwendigen Anmeldungen durch.
     *
     * @param archMgr  Archiv-Verwaltung
     * @param receiver Empfaengerobjekt
     */
    public static void subscribeObjects(ArchiveManager archMgr, DataReceiver receiver) {
        String atgPid = PidQuery.ATG_QUERY_PID;
        String aspPid = PidQuery.ASP_QUERY_PID;
        try {
            ConnectionManager.subscrDrainNormal(archMgr.getDavCon(), receiver, archMgr.getConfigAuth(), atgPid, aspPid);
        } catch (Exception e) {
            Debug.getLogger().error(
                    "Archivsystem konnte sich nicht als Senke für Archivanfragen anmelden: " + archMgr.getConfigAuth() + "/" + atgPid + "/" + aspPid + Debug
                            .NEWLINE + e.getMessage()
            );
        }
    }

    /**
     * Ueber diese Methode kann die Parametrierung den Blocking-Faktor des StreamMultiplexers auf den parametrierten Wert setzen (Attribut "AnzahlBlocks" in
     * Attributgruppe "ArchivEinstellung").
     *
     * @param bFMuxParam Parametrierter Blocking-Faktor des StreamMultiplexers
     */
    public static void setBlockingFactorMux(int bFMuxParam) {
        blockingFactorMuxParam = bFMuxParam;
    }

    /**
     * Ueber diese Methode kann die Parametrierung die Puffergroesse des StreamMultiplexers auf den parametrierten Wert setzen (Attribut "AnzahlBytes" in
     * Attributgruppe "ArchivEinstellung").
     *
     * @param bSMuxParam Parametrierte Puffergroesse des StreamMultiplexers
     */
    public static void setBufferSizeMux(int bSMuxParam) {
        bufferSizeMuxParam = bSMuxParam;
    }

    /**
     * @see #setBlockingFactorMux(int)
     */
    public static int getBlockingFactorMux() {
        return blockingFactorMuxParam;
    }

    /**
     * @see #setBlockingFactorMux(int)
     */
    public static int getBufferSizeMux() {
        return bufferSizeMuxParam;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Task Methoden
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @see de.bsvrz.ars.ars.mgmt.tasks.MultiTask#work()
     */
    public void work() {
        //logger.finer(getName() + " Archivanfrage erhalten: " + resultData);
        init();
        try {
            boolean analyzeError = false;
            try {
                analyze();
            } catch (Throwable e) {
                analyzeError = true;
                // resultData wird vor Aufruf der Debug-Methode in einen String konvertiert, um verschachtelte Debug-Aufrufe mit Deadlock-Gefahr zu vermeiden
                logger.warning(getName() + ": Fehler beim Analysieren der Archivanfrage: " + e.getMessage() + ", Anfrage: " + resultData.toString(), e);
            }
            if (queryAppObj != null) {
                final int archivQueryCountForApplication = archMgr.increaseArchiveQueryCountForApplication(queryAppObj);
                try {
                    sendingState = SendingState.STOP;
                    subscribeSender();
                    int malformedIntervalIndex = getIntervalErrorQueryIdx();
                    if (malformedIntervalIndex >= 0)
                        logger.finest(getName() + " Query-Idx " + malformedIntervalIndex + " hat vertauschte Anfangs-/Endzeiten.");

                    // Stream-Multiplexer nur vorbereiten, falls Unterabfragen zu beantworten sind (ein Stream pro Unterabfrage):
                    if (queries.size() > 0 && malformedIntervalIndex < 0) {
                        configureMuxParameter();
                        mux = new StreamMultiplexer(queries.size(), blockingFactorMuxUsed, bufferSizeMuxUsed, serializerVersion, this);
                        flowCtrl.subscribe(queryAppObj, queryIdx, mux);
                    }

                    final SendingState state;
                    // Nach 120 Sekunden ohne Sendesteuerung wird der Auftrag abgebrochen
                    final long timeoutTime = System.currentTimeMillis() + (120 * 1000);
                    synchronized (syncObj) {
                        while (sendingState == SendingState.STOP) {
                            final long delta = timeoutTime - System.currentTimeMillis();
                            if (delta <= 0) break;
                            syncObj.wait(delta);
                        }
                        state = sendingState;
                    }
                    if (state == SendingState.SEND) {
                        if (archivQueryCountForApplication > MAXIMUM_REQUESTS_PER_APPLICATION) {
                            sendInitialResponse(false, "Zu viele parallele Archivanfragen (" + archivQueryCountForApplication + ") von der Applikation '" + queryAppObj + "'. Maximum ist: " + MAXIMUM_REQUESTS_PER_APPLICATION);
                        } else if (analyzeError) {
                            sendInitialResponse(false, "Fehler beim Auswerten der Archivanfrage");
                        } else if (queries.size() <= 0) {
                            sendInitialResponse(false, "Kein Intervall gegeben");
                        } else if (malformedIntervalIndex >= 0) {
                            sendInitialResponse(false, "Ungueltiges Intervall (" + (malformedIntervalIndex + 1) + ")");
                        } else {
                            sendInitialResponse(true, "");
                            mux.sendAllStreamData();
                        }
                    }
                } finally {
                    archMgr.decreaseArchiveQueryCountForApplication(queryAppObj);
                }
            }
        } catch (InterruptedException e) {
            logger.finer(getName() + ": Archivanfrage durch Interrupt beendet.");
            mux.killAllStreams();
        } catch (Throwable e) {
            logger.error(getName() + ": Fehler beim Bearbeiten der Archivanfrage: " + resultData, e);
            mux.killAllStreams();
        } finally {
            // Synchronisiert sich mit der Methode dataRequest und verhindert, dass bereits die Verarbeitung einer neuen Anfrage begonnen wird
            // während die Sendesteuerung noch verarbeitet wird.
            synchronized (queries) {
                abortQueries();      // Alle Unterabfragen verwerfen
                queries.clear();     // Speicher sparen: Referenzen auf Query-Objekte loeschen
                unsubscribeSender(); // Als Sender der Archiv-Antwort abmelden
            }
            persMgr.removeContainerFileObjects(this, MIN_CONTAINER_FILES); // ContainerFiles aufraeumen
            logger.finer(
                    getName() + " Archivanfrage beendet." + (initialResponseSent ? "" : " Wegen eines Fehlers konnte keine initiale Antwort gesendet werden")
            );
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // StreamMultiplexerDirector Methoden
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @see StreamMultiplexerDirector#sendData(byte[])
     */
    public void sendData(byte[] streamDataPacket) {
        sendResultData(streamDataPacket, MessageType.STREAM_DATA);
    }

    /**
     * @see StreamMultiplexerDirector#take(int)
     */
    public byte[] take(int indexOfStream) {
        Query query = null;
        try {
            query = queries.get(indexOfStream); // Unterabfrage zum Stream
            if (query.done) {
//				System.out.println("ArchiveQueryTask.take query.done ########################################");
                return null;        // Abbruch, falls Query fertig
            }

            SimpleArchiveData arcData = query.getNextData(); // Naechster zu sendender Datensatz
            if (arcData == null) {
//				System.out.println("ArchiveQueryTask.take arcData == null ########################################");
                return null;                // Abbruch, falls Query fertig
            }

            // Datenaufbereitung gemaess DatK 7.2.3.12
            bosResult.reset();
            serializer.writeInt(arcData.dataKind.getCode());
            serializer.writeLong(arcData.dataTime);
            serializer.writeLong(arcData.arcTime);
            serializer.writeLong(arcData.dataIdx);
            serializer.writeInt(arcData.dataState.getCode());
            serializer.writeInt(serializerVersion);
            serializer.writeByte(arcData.compressed ? ArchiveDataCompression.ZIP.getCode() : ArchiveDataCompression.NONE.getCode());

            if (arcData.rawData != null) {
                serializer.writeInt(arcData.rawData.length);
                serializer.writeBytes(arcData.rawData);
            } else {
                serializer.writeInt(0);
            }
            return bosResult.toByteArray();
        } catch (InterruptedException iex) {
            logger.fine("Archivanfrage wurde angehalten. Warten wurde abgebrochen");
        } catch (Exception e) {
            logger.error(getName() + ": Unterabfrage " + indexOfStream + ": Erstellung Datensatz fuer Archivantwort fehlgeschlagen.", e);
        }
        if (query != null) query.abort();     // Query beenden, Speicher freigeben
//		System.out.println("ArchiveQueryTask.take nach Exception ########################################");
        return null;                        // Kennzeichen für "keine Daten mehr"
    }

    /**
     * @see StreamMultiplexerDirector#streamAborted(int)
     */
    public void streamAborted(int indexOfStream) {
        logger.finest(getName() + " QueryTask verwirft Stream mit Index " + indexOfStream);
        assert indexOfStream >= 0 && indexOfStream < queries.size();
        queries.get(indexOfStream).abort();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ClientSenderInterface Methoden
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @see ClientSenderInterface#dataRequest(SystemObject, DataDescription, byte)
     */
    public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {
        if (state != ClientSenderInterface.START_SENDING) {
            // Synchronisiert sich mit der Methode Work und verhindert, dass bereits die Verarbeitung einer neuen Anfrage begonnen wird
            // während die Sendesteuerung noch verarbeitet wird.
            synchronized (queries) {
                mux.killAllStreams();
                abortQueries();
            }
        }
        super.dataRequest(object, dataDescription, state);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private Methoden
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Initialisiert den Task fuer eine neue Archivanfrage.
     */
    protected void init() {
        super.init();
        queries.clear();
        receiverBufferSize = 0;
        mux = null;
        blockingFactorMuxUsed = 0;
        bufferSizeMuxUsed = 0;
        initialResponseSent = false;
    }

    /**
     * Analysiert die aktuelle Archivanfrage und fuellt die Liste der {@link Query}.
     *
     * @throws NoSuchVersionException
     * @throws IOException
     * @throws NoSuchVersionException
     * @throws IOException
     * @throws IndexException
     * @throws PersistenceException
     * @throws IndexException
     * @throws PersistenceException
     */
    private void analyze() throws NoSuchVersionException, IOException, PersistenceException, IndexException {
        createQueryData();
        deserializer.readInt();                         // Prioritaet; bereits vom InQueuesMgr erledigt
        receiverBufferSize = deserializer.readInt();     // Groesse des Empfangspuffers

        ArchiveDataSpecification[] ads = parseArchiveDataSpec(deserializer);
        for (int i = 0; i < ads.length; i++) {
            boolean intervalErr = ads[i].getTimeSpec().getIntervalStart() > ads[i].getTimeSpec().getIntervalEnd();
            queries.add(i, new Query(ads[i], i, intervalErr));
        }
    }

    /**
     * Meldet den Task als Sender fuer Archiv-Antworten an das angegebene Empfaenger-Applikations-Objekt ab. Deregistriert den StreamMultiplexer fuer den Empfang
     * von Flusskontroll-Steuerungspaketen.
     */
    protected void unsubscribeSender() {
        if (mux != null) flowCtrl.unsubscribe(queryAppObj, queryIdx);
        super.unsubscribeSender();
    }

    /**
     * Sendet die initiale Ergebnismeldung zur Archiv-Anfrage an das Empfaenger-Applikations-Objekt.
     *
     * @param success  Anfrage erfolgreich
     * @param errorMsg Fehlermeldung bei Misserfolg
     * @throws IOException
     */
    private void sendInitialResponse(boolean success, String errorMsg) throws IOException {
        // ======           Datenaufbereitung erfolgt hier gemaess           ======
        // ======  Datenkatalog Kapitel 7.2.3.12 ArchivAnfrageSchnittstelle  ======
        // ======             Unterpunkt ArchivNachrichtenTyp 2              ======
        bosResult.reset();
        serializer.writeByte(success ? 1 : 0);      // Anfrage erfolgreich ja/nein
        serializer.writeString(errorMsg);           // Fehlermeldung bei Misserfolg
        serializer.writeInt(blockingFactorMuxUsed); // Blocking-Faktor des Stream-Multiplexers und -Demultiplexers
        sendResultData(bosResult.toByteArray(), MessageType.INITIAL_QUERY_RESULT);
        initialResponseSent = true;
    }

    /**
     * Markiert alle Unterabfragen als erledigt und schliesst alle noch offenen Datensatz-Iteratoren und Container.
     */
    private void abortQueries() {
        for (Query query : queries) {
            query.abort();
        }
    }

    /**
     * Liefert den Index der Query, bei der die angefragten Intervallgrenzen vertauscht sind.
     *
     * @return Index der Query mit Fehler, -1 wenn alle Queries okay sind.
     */
    private int getIntervalErrorQueryIdx() {
        for (int idx = 0; idx < queries.size(); idx++) {
            if (queries.get(idx).requestedIntervalError) return idx;
        }
        return -1;
    }

    /**
     * Bestimmt die Parameter bufferSizeStreamMultiplexer und blockingFactor, mit denen der StreamMultiplexer per Konstruktor parametriert wird, anhand der Werte
     * aus den Archivsystem-Einstellungen und dem tatsaechlich vorhandenen Empfangspuffer des Empfaengers.
     */
    private void configureMuxParameter() {
        // Parametrierte sendeseitige Puffergroesse:
        bufferSizeMuxUsed = bufferSizeMuxParam;
        blockingFactorMuxUsed = blockingFactorMuxParam;

        // Negative Werte und Null ausschliessen:
        if (bufferSizeMuxUsed < 1) bufferSizeMuxUsed = 1;
        if (blockingFactorMuxUsed < 1) blockingFactorMuxUsed = 1;

        if (receiverBufferSize > 1) {
            // Empfangspuffer hat sinnvollen Wert -> Sender daran anpassen
            if (receiverBufferSize >= bufferSizeMuxUsed) {
                // Fall 1: Empfangspuffer groesser Sendepuffer (Dafault-Fall)
                blockingFactorMuxUsed = receiverBufferSize / bufferSizeMuxUsed;
            } else {
                // Fall 2: Empfangspuffer kleiner Sendepuffer
                blockingFactorMuxUsed = 1;
                bufferSizeMuxUsed = receiverBufferSize;
            }
        }
    }

    /**
     * Klasse, die einen Archivdatensatz mit den zugehoerigen Zeitstempeln und Datenindex kapselt.
     */
    private class SimpleArchiveData {

        /**
         * Zeitstempel / Datenindex
         */
        private long arcTime, dataTime, dataIdx;

        /**
         * Datensatzart
         */
        private ArchiveDataKind dataKind;

        /**
         * Zustand des Datensatzes (Datensatztyp).
         */
        private DataState dataState;

        /**
         * serialisierter Datensatz
         */
        private byte[] rawData;

        /**
         * komprimiert?
         */
        private boolean compressed;

        /**
         * Erstellt ein neues SimpleArchiveData mit den angegebenen Werten.
         *
         * @param arcTime    Archivzeit
         * @param dataTime   Datenzeit
         * @param dataIdx    Datenindex
         * @param dataKind   Datensatzart
         * @param dataState  Datensatztyp
         * @param compressed Flag, das besagt, ob der Datensatz komprimiert ist.
         * @param rawData    Datensatz
         */
        private SimpleArchiveData(
                long arcTime, long dataTime, long dataIdx, ArchiveDataKind dataKind, DataState dataState, boolean compressed, byte[] rawData) {
            this.arcTime = arcTime;
            this.dataTime = dataTime;
            this.dataIdx = dataIdx;
            this.dataKind = dataKind;
            this.dataState = dataState;
            this.rawData = rawData;
            this.compressed = compressed;
        }

        /**
         * Liefert den Zeitstempel/Datenindex, der durch den angegebenen {@link TimingType} spezifiziert ist.
         *
         * @param timingType Typ der zu liefernden Timingangabe
         * @return Archivzeitstempel, Datenzeitstempel oder Datenindex
         */
        private long getTimeIndex(TimingType timingType) {
            if (timingType == TimingType.ARCHIVE_TIME) {
                return arcTime;
            } else if (timingType == TimingType.DATA_TIME) {
                return dataTime;
            } else {
                return dataIdx;
            }
        }

        public String toString() {
            return "dx=" + Util.dIdx2StrExt(dataIdx) + " (" + dataIdx + ")  dt=" + Util.timestrMillisFormatted(dataTime) + "  at="
                    + Util.timestrMillisFormatted(arcTime) + "  " + dataKind + "  " + dataState;
        }
    }
}
