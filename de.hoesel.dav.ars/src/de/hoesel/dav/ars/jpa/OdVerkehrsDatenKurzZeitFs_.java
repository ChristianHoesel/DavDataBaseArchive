package de.hoesel.dav.ars.jpa;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-08-17T21:02:29.119+0200")
@StaticMetamodel(OdVerkehrsDatenKurzZeitFs.class)
public class OdVerkehrsDatenKurzZeitFs_ {
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitFs, Long> db_id;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitFs, SystemObjectArchiv> systemObject;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitFs, String> aspect;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitFs, AtlVerkehrsStärkeStunde> qKfz;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitFs, AtlGeschwindigkeit> vKfz;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitFs, AtlVerkehrsStärkeStunde> qLkw;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitFs, AtlGeschwindigkeit> vLkw;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitFs, AtlVerkehrsStärkeStunde> qPkw;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitFs, AtlGeschwindigkeit> vPkw;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitFs, AtlProzent> b;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitFs, AtlGeschwindigkeit> sKfz;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitFs, AtlGeschwindigkeit> vgKfz;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitFs, AtlProzent> aLkw;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitFs, AtlFahrzeugDichte> kKfz;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitFs, AtlFahrzeugDichte> kLkw;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitFs, AtlFahrzeugDichte> kPkw;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitFs, Float> qB;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitFs, Float> kB;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitFs, Date> timestamp;
}
