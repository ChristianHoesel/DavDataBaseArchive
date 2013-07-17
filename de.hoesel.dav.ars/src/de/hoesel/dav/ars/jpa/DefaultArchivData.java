package de.hoesel.dav.ars.jpa;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import de.bsvrz.dav.daf.main.ResultData;
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

	private static final int BUFFER_SIZE = 4096;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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
			if (resultData.hasData()) {
				serializer.writeData(resultData.getData());
				setData(bos.toByteArray());
			} else {
				// TODO
			}
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

}
