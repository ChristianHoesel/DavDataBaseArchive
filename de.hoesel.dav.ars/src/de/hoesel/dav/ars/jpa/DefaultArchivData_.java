package de.hoesel.dav.ars.jpa;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-08-02T21:55:02.896+0200")
@StaticMetamodel(DefaultArchivData.class)
public class DefaultArchivData_ {
	public static volatile SingularAttribute<DefaultArchivData, Long> db_id;
	public static volatile SingularAttribute<DefaultArchivData, SystemObjectArchiv> systemObject;
	public static volatile SingularAttribute<DefaultArchivData, Date> timestamp;
	public static volatile SingularAttribute<DefaultArchivData, byte[]> data;
}
