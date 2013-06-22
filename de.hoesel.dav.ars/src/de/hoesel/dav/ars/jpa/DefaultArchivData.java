package de.hoesel.dav.ars.jpa;
/**
 * 
 */


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
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
 * @author christian
 * 
 */
@Entity
public class DefaultArchivData implements Serializable  {

	private static final int BUFFER_SIZE = 4096;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long db_id;

	@Embedded
	private SystemObjectArchiv systemObject;

	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;

	@Lob
	private byte[] data;

	public DefaultArchivData() {

	}

	public DefaultArchivData(final ResultData resultData) {

//		EntityManagerFactory factory = Persistence
//				.createEntityManagerFactory(Archivator.PERSISTENCE_UNIT_NAME);
//		EntityManager em = factory.createEntityManager();
		SystemObjectArchiv sysObjArchiv = new SystemObjectArchiv(
				resultData.getObject());
//		SystemObjectArchiv existingSysObj = em.find(SystemObjectArchiv.class,
//				sysObjArchiv.getId());
//		if (existingSysObj != null) {
//			setSystemObject(existingSysObj);
//		} else {
			setSystemObject(sysObjArchiv);
//		}
//		em.close();
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

	public final Date getTimestamp() {
		return timestamp;
	}

	public final void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public final byte[] getData() {
		return data;
	}

	public final void setData(byte[] data) {
		this.data = data;
	}

	public SystemObjectArchiv getSystemObject() {
		return systemObject;
	}

	public void setSystemObject(SystemObjectArchiv systemObject) {
		this.systemObject = systemObject;
	}

}
