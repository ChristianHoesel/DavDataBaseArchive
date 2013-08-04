package de.hoesel.dav.ars.jpa;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-08-02T21:55:02.914+0200")
@StaticMetamodel(OdVerkehrsDatenKurzZeitMq.class)
public class OdVerkehrsDatenKurzZeitMq_ {
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Long> db_id;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, SystemObjectArchiv> systemObject;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, String> aspect;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Date> timestamp;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Integer> qKfz;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Integer> vKfz;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Integer> qLkw;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Integer> vLkw;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Integer> qPkw;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Integer> vPkw;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Float> b;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Float> bMax;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Integer> sKfz;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Integer> vgKfz;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Float> aLkw;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Integer> kKfz;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Integer> kPkw;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Integer> kLkw;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Float> qB;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Float> kB;
	public static volatile SingularAttribute<OdVerkehrsDatenKurzZeitMq, Float> vDelta;
}
