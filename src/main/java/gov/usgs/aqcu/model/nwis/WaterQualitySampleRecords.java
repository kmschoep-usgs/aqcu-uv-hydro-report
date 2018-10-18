package gov.usgs.aqcu.model.nwis;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.usgs.aqcu.model.WaterQualitySampleRecord;

/**
 * Simple wrapper to make deserialization of NWIS-RA responses easier
 * 
 * @author thongsav
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WaterQualitySampleRecords {
	private List<WaterQualitySampleRecord> records;

	/**
	 *
	 * @return The list of records
	 */
	public List<WaterQualitySampleRecord> getRecords() {
		return records;
	}

	/**
	 *
	 * @param records The list of records to set
	 */
	public void setRecords(List<WaterQualitySampleRecord> records) {
		this.records = records;
	}
}
