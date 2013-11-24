package de.hoesel.dav.ars.jpa;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-11-24T15:08:22.477+0100")
@StaticMetamodel(OdVerkehrsDatenKurzZeitMq.class)
public class OdVerkehrsDatenKurzZeitMq_ {
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Long> db_id;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, SystemObjectArchiv> systemObject;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, String> aspect;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Date> timestamp;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, AtlVerkehrsStärkeStunde> qKfz;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, AtlGeschwindigkeit> vKfz;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, AtlVerkehrsStärkeStunde> qLkw;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, AtlGeschwindigkeit> vLkw;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, AtlVerkehrsStärkeStunde> qPkw;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, AtlGeschwindigkeit> vPkw;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, AtlProzent> b;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Float> bMax;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Integer> sKfz;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Integer> vgKfz;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, AtlProzent> aLkw;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, AtlFahrzeugDichte> kKfz;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, AtlFahrzeugDichte> kPkw;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, AtlFahrzeugDichte> kLkw;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Float> qB;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Float> kB;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Float> vDelta;
}
