package de.hoesel.dav.ars.jpa;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import de.bsvrz.dav.daf.main.Data;

@Embeddable
public class AtlStatus {
	
//	@Id
//	@GeneratedValue(strategy=GenerationType.SEQUENCE)
//	private Long db_id;

	private Boolean erfassung_NichtErfasst;

	private Boolean plFormal_WertMax;

	private Boolean plFormal_WertMin;

	private Boolean plLogisch_WertMaxLogisch;

	private Boolean plLogisch_WertMinLogisch;

	private Boolean messwertErsetzung_Implausiebel;

	private Boolean messwertErsetzung_Interpoliert;

	public AtlStatus() {

	}

	public AtlStatus(Data item) {
		setErfassung_NichtErfasst(item.getItem("Erfassung")
				.getUnscaledValue("NichtErfasst").shortValue() > 0 ? true
				: false);
		setPlFormal_WertMax(item.getItem("PlFormal")
				.getUnscaledValue("WertMax").shortValue() > 0 ? true : false);
		setPlFormal_WertMin(item.getItem("PlFormal")
				.getUnscaledValue("WertMin").shortValue() > 0 ? true : false);
		setPlLogisch_WertMaxLogisch(item.getItem("PlLogisch")
				.getUnscaledValue("WertMaxLogisch").shortValue() > 0 ? true
				: false);
		setPlLogisch_WertMinLogisch(item.getItem("PlLogisch")
				.getUnscaledValue("WertMinLogisch").shortValue() > 0 ? true
				: false);
		setMesswertErsetzung_Implausiebel(item.getItem("MessWertErsetzung")
				.getUnscaledValue("Implausibel").shortValue() > 0 ? true
				: false);
		setMesswertErsetzung_Interpoliert(item.getItem("MessWertErsetzung")
				.getUnscaledValue("Interpoliert").shortValue() > 0 ? true
				: false);

	}

	public Boolean getErfassung_NichtErfasst() {
		return erfassung_NichtErfasst;
	}

	public void setErfassung_NichtErfasst(Boolean erfassung_NichtErfasst) {
		this.erfassung_NichtErfasst = erfassung_NichtErfasst;
	}

	public Boolean getPlFormal_WertMax() {
		return plFormal_WertMax;
	}

	public void setPlFormal_WertMax(Boolean plFormal_WertMax) {
		this.plFormal_WertMax = plFormal_WertMax;
	}

	public Boolean getPlFormal_WertMin() {
		return plFormal_WertMin;
	}

	public void setPlFormal_WertMin(Boolean plFormal_Wert_Min) {
		this.plFormal_WertMin = plFormal_Wert_Min;
	}

	public Boolean getPlLogisch_WertMaxLogisch() {
		return plLogisch_WertMaxLogisch;
	}

	public void setPlLogisch_WertMaxLogisch(Boolean plLogisch_WertMaxLogisch) {
		this.plLogisch_WertMaxLogisch = plLogisch_WertMaxLogisch;
	}

	public Boolean getPlLogisch_WertMinLogisch() {
		return plLogisch_WertMinLogisch;
	}

	public void setPlLogisch_WertMinLogisch(Boolean plLogisch_WertMinLogisch) {
		this.plLogisch_WertMinLogisch = plLogisch_WertMinLogisch;
	}

	public Boolean getMesswertErsetzung_Implausiebel() {
		return messwertErsetzung_Implausiebel;
	}

	public void setMesswertErsetzung_Implausiebel(
			Boolean messwertErsetzung_Implausiebel) {
		this.messwertErsetzung_Implausiebel = messwertErsetzung_Implausiebel;
	}

	public Boolean getMesswertErsetzung_Interpoliert() {
		return messwertErsetzung_Interpoliert;
	}

	public void setMesswertErsetzung_Interpoliert(
			Boolean messwertErsetzung_Interpoliert) {
		this.messwertErsetzung_Interpoliert = messwertErsetzung_Interpoliert;
	}

}
