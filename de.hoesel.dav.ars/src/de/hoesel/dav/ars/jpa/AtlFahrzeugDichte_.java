package de.hoesel.dav.ars.jpa;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-08-17T18:15:05.713+0200")
@StaticMetamodel(AtlFahrzeugDichte.class)
public class AtlFahrzeugDichte_ {
	public static volatile SingularAttribute<AtlFahrzeugDichte, Long> db_id;
	public static volatile SingularAttribute<AtlFahrzeugDichte, Short> wert;
	public static volatile SingularAttribute<AtlFahrzeugDichte, AtlStatus> status;
	public static volatile SingularAttribute<AtlFahrzeugDichte, AtlGüte> güte;
}
