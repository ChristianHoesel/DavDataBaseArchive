package de.hoesel.dav.ars.jpa;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-07-28T18:40:03.609+0200")
@StaticMetamodel(AtlGeschwindigkeit.class)
public class AtlGeschwindigkeit_ {
	public static volatile SingularAttribute<AtlGeschwindigkeit, Long> db_id;
	public static volatile SingularAttribute<AtlGeschwindigkeit, Float> wert;
	public static volatile SingularAttribute<AtlGeschwindigkeit, AtlStatus> status;
	public static volatile SingularAttribute<AtlGeschwindigkeit, AtlGüte> güte;
}
