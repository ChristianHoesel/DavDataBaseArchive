package de.hoesel.dav.ars.jpa;

import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-11-17T18:00:56.992+0100")
@StaticMetamodel(DefaultArchivData.class)
public class DefaultArchivData_ {
	public static volatile SingularAttribute<DefaultArchivData, Long> db_id;
	public static volatile SingularAttribute<DefaultArchivData, SystemObjectArchiv> systemObject;
	public static volatile SingularAttribute<DefaultArchivData, Date> timestamp;
	public static volatile SingularAttribute<DefaultArchivData, byte[]> data;
	public static volatile SingularAttribute<DefaultArchivData, AttributeGroup> atg;
	public static volatile SingularAttribute<DefaultArchivData, Aspect> asp;
}
