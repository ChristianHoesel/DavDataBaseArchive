package de.hoesel.dav.ars.jpa;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-08-03T21:35:26.147+0200")
@StaticMetamodel(AtlVerkehrsStärkeStunde.class)
public class AtlVerkehrsStärkeStunde_ {
	public static volatile SingularAttribute<AtlVerkehrsStärkeStunde, Long> db_id;
	public static volatile SingularAttribute<AtlVerkehrsStärkeStunde, Integer> wert;
	public static volatile SingularAttribute<AtlVerkehrsStärkeStunde, AtlStatus> status;
	public static volatile SingularAttribute<AtlVerkehrsStärkeStunde, AtlGüte> guete;
}
