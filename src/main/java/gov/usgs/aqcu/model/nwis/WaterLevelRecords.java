package gov.usgs.aqcu.model.nwis;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import gov.usgs.aqcu.deserializer.WaterLevelRecordsDeserializer;
import gov.usgs.aqcu.model.WaterLevelRecord;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = WaterLevelRecordsDeserializer.class)
public class WaterLevelRecords {
	private List<WaterLevelRecord> records;
	private boolean allValid = true;

	public List<WaterLevelRecord> getRecords() {
		return records;
	}
	public void setRecords(List<WaterLevelRecord> records) {
		this.records = records;
	}
	public boolean allValid() {
		return allValid;
	}
	public void setAllValid(boolean val) {
		this.allValid = val;
	}
}