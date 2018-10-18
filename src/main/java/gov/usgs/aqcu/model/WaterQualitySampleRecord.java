package gov.usgs.aqcu.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import gov.usgs.aqcu.deserializer.WaterQualitySampleRecordDeserializer;
import gov.usgs.aqcu.model.nwis.NwisRaTimeZones;

import java.time.OffsetDateTime;
import java.time.temporal.Temporal;

/**
 * AQCU representation of an NWIS-RA WaterQualitySampleRecord
 * 
 * @author thongsav
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = WaterQualitySampleRecordDeserializer.class)
public class WaterQualitySampleRecord {
	private String recordNumber;
	private String medium;
	private OffsetDateTime sampleStartDateTime;
	private WqValue value;
	private NwisRaTimeZones timeZone;

	/**
	 *
	 * @return The record number
	 */
	public String getRecordNumber() {
		return recordNumber;
	}

	/**
	 *
	 * @return The record medium
	 */
	public String getMedium() {
		return medium;
	}

	/**
	 *
	 * @return The record sample start date & time
	 */
	public Temporal getSampleStartDateTime() {
		return sampleStartDateTime;
	}

	/**
	 *
	 * @return The record value
	 */
	public WqValue getValue() {
		return value;
	}

	/**
	 *
	 * @return The record time zone
	 */
	public NwisRaTimeZones getTimeZone(){
		return timeZone;
	}

	/**
	 *
	 * @param recordNumber The record number to set
	 */
	public void setRecordNumber(String recordNumber) {
		this.recordNumber = recordNumber;
	}

	/**
	 *
	 * @param medium The record medium to set
	 */
	public void setMedium(String medium) {
		this.medium = medium;
	}

	/**
	 *
	 * @param sampleStartTemporal The record start date & time to set
	 */
	public void setSampleStartDateTime(OffsetDateTime sampleStartTemporal) {
		this.sampleStartDateTime = sampleStartTemporal;
	}

	/**
	 *
	 * @param value The record value to set
	 */
	public void setValue(WqValue value) {
		this.value = value;
	}

	/**
	 *
	 * @param timeZone The record time zone to set
	 */
	public void setTimeZone(String timeZone) {
		this.timeZone = NwisRaTimeZones.getByTimeZoneCode(timeZone);
	}
}
