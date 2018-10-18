package gov.usgs.aqcu.model.nwis;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ParameterRecords {
	private List<ParameterRecord> records;

	public ParameterRecords() {
		records = new ArrayList<>();
	}

	public List<ParameterRecord> getRecords() {
		return records;
	}
	public void setRecords(List<ParameterRecord> records) {
		this.records = records;
	}
}
