package de.hoesel.dav.ars.jpa;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-11-02T10:48:13.233+0100")
@StaticMetamodel(AtlProzent.class)
public class AtlProzent_ {
	public static volatile SingularAttribute<AtlProzent, Long> db_id;
	public static volatile SingularAttribute<AtlProzent, Integer> wert;
	public static volatile SingularAttribute<AtlProzent, AtlStatus> status;
	public static volatile SingularAttribute<AtlProzent, AtlGüte> guete;
}
