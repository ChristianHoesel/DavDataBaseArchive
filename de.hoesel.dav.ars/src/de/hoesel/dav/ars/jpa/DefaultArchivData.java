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

package de.hoesel.dav.ars.jpa;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;

import javax.naming.OperationNotSupportedException;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataState;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.Serializer;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;

/**
 * Implementierung eines allgemeinen generischer Archivdatensatzes.
 *  
 * @author christian
 * 
 */
@Entity
public class DefaultArchivData implements Serializable,
		DatenverteilerArchivDatensatz {

	private static final int BUFFER_SIZE = 16 * 1024;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	private Long db_id;

	private SystemObjectArchiv systemObject;

	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;

	@Lob
	private byte[] data;

	public DefaultArchivData() {

	}

	public DefaultArchivData(final ResultData resultData) {

		SystemObjectArchiv sysObjArchiv = new SystemObjectArchiv(
				resultData.getObject());
		setSystemObject(sysObjArchiv);
		setTimestamp(new Date(resultData.getDataTime()));

		ByteArrayOutputStream bos = new ByteArrayOutputStream(BUFFER_SIZE);
		Serializer serializer = SerializingFactory.createSerializer(bos);
		try {
			DataState type = resultData.getDataType();
			if (resultData.hasData() && type == DataState.DATA) {
				serializer.writeData(resultData.getData());
			} 
			setData(bos.toByteArray());
			bos.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				bos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hoesel.dav.ars.jpa.DatenverteilerArchivDatensatz#getTimestamp()
	 */
	@Override
	public final Date getTimestamp() {
		return timestamp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.hoesel.dav.ars.jpa.DatenverteilerArchivDatensatz#setTimestamp(java
	 * .util.Date)
	 */
	@Override
	public final void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public final byte[] getData() {
		return data;
	}

	public final void setData(byte[] data) {
		this.data = data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.hoesel.dav.ars.jpa.DatenverteilerArchivDatensatz#getSystemObject()
	 */
	@Override
	public SystemObjectArchiv getSystemObject() {
		return systemObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.hoesel.dav.ars.jpa.DatenverteilerArchivDatensatz#setSystemObject(de
	 * .hoesel.dav.ars.jpa.SystemObjectArchiv)
	 */
	@Override
	public void setSystemObject(SystemObjectArchiv systemObject) {
		this.systemObject = systemObject;
	}

	@Override
	public ResultData convert2ResultData(final ClientDavConnection con) {
		
		
		throw new IllegalStateException("Not implemented yet :(");
//		return null;
	}

	public Long getDb_id() {
		return db_id;
	}

	public void setDb_id(Long db_id) {
		this.db_id = db_id;
	}


}
