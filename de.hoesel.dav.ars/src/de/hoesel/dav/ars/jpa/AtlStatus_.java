package de.hoesel.dav.ars.jpa;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-07-28T18:07:28.652+0200")
@StaticMetamodel(AtlStatus.class)
public class AtlStatus_ {
	public static volatile SingularAttribute<AtlStatus, Boolean> erfassung_NichtErfasst;
	public static volatile SingularAttribute<AtlStatus, Boolean> plFormal_WertMax;
	public static volatile SingularAttribute<AtlStatus, Boolean> plFormal_WertMin;
	public static volatile SingularAttribute<AtlStatus, Boolean> plLogisch_WertMaxLogisch;
	public static volatile SingularAttribute<AtlStatus, Boolean> plLogisch_WertMinLogisch;
	public static volatile SingularAttribute<AtlStatus, Boolean> messwertErsetzung_Implausiebel;
	public static volatile SingularAttribute<AtlStatus, Boolean> messwertErsetzung_Interpoliert;
	public static volatile SingularAttribute<AtlStatus, Long> db_id;
}
