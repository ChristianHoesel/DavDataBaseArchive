package de.hoesel.dav.ars.jpa;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-11-24T15:08:33.806+0100")
@StaticMetamodel(AtlFahrzeugDichte.class)
public class AtlFahrzeugDichte_ {
	public static volatile SingularAttribute<AtlFahrzeugDichte, Long> db_id;
	public static volatile SingularAttribute<AtlFahrzeugDichte, Short> wert;
	public static volatile SingularAttribute<AtlFahrzeugDichte, AtlStatus> status;
	public static volatile SingularAttribute<AtlFahrzeugDichte, AtlGüte> güte;
}
