package gov.usgs.aqcu.model;

import java.util.Map;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GradeMetadata;

public class UvHydroReportMetadata extends ReportMetadata {
	private String primarySeriesLabel;
	private String primaryTsIdentifier;
	private String requestingUser;
	private Map<String, GradeMetadata> gradeMetadata;

	public UvHydroReportMetadata() {
		super();
	}

	public String getPrimaryTsIdentifier() {
		return primaryTsIdentifier;
	}
	
	public String getPrimarySeriesLabel() {
		return primarySeriesLabel;
	}

	public String getRequestingUser() {
		return requestingUser;
	}

	public Map<String, GradeMetadata> getGradeMetadata() {
		return gradeMetadata;
	}
	
	public void setPrimaryTsIdentifier(String val) {
		primaryTsIdentifier = val;
	}

	public void setPrimarySeriesLabel(String val) {
		primarySeriesLabel = val;
	}

	public void setRequestingUser(String val) {
		requestingUser = val;
	}

	public void setGradeMetadata(Map<String, GradeMetadata> gradeMetadata) {
		this.gradeMetadata = gradeMetadata;
	}
}