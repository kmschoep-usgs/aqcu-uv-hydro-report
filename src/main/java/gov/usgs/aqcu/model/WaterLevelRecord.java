package gov.usgs.aqcu.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import gov.usgs.aqcu.deserializer.WaterLevelRecordDeserializer;
import gov.usgs.aqcu.model.nwis.NwisRaTimeZones;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = WaterLevelRecordDeserializer.class)
public class WaterLevelRecord {
	private String siteNumber;
	private BigDecimal groundWaterLevel;
	private OffsetDateTime recordDateTime;
	private NwisRaTimeZones timeZone;

	public String getSiteNumber() {
		return siteNumber;
	}
	public BigDecimal getGroundWaterLevel() {
		return groundWaterLevel;
	}
	public OffsetDateTime getDate() {
		return recordDateTime;
	}
	public NwisRaTimeZones getTimeZone() {
		return timeZone;
	}
	public void setTimeZone(String timeZone) {
		this.timeZone = NwisRaTimeZones.getByTimeZoneCode(timeZone);
	}
	public void setSiteNumber(String siteNumber) {
		this.siteNumber = siteNumber;
	}
	public void setGroundWaterLevel(BigDecimal groundWaterLevel) {
		this.groundWaterLevel = groundWaterLevel;
	}
	public void setDate(OffsetDateTime recordDateTime) {
		this.recordDateTime = recordDateTime;
	}
}
