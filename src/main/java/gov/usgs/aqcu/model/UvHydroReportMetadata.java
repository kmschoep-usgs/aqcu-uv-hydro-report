package gov.usgs.aqcu.model;

import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GradeMetadata;

import gov.usgs.aqcu.parameter.UvHydroRequestParameters;

public class UvHydroReportMetadata extends ReportMetadata {
	private UvHydroRequestParameters requestParameters;
	private String primaryParameter;
	private String upchainParameter;
	private String referenceParameter;
	private String comparisonParameter;
	private String firstStatDerivedLabel;
	private String secondStatDerivedLabel;
	private String thirdStatDerviedLabel;
	private String fourthStatDerviedLabel;
	private String comparisonStationId;
	private String requestingUser;
	private Map<String, GradeMetadata> gradeMetadata;
	private UvHydrographType uvType;

	public UvHydroReportMetadata() {
		super();
		gradeMetadata = new HashMap<>();
	}

	public String getPrimaryParameter() {
		return primaryParameter;
	}

	public String getUpchainParameter() {
		return upchainParameter;
	}

	public String getReferenceParameter() {
		return referenceParameter;
	}

	public String getComparisonParameter() {
		return comparisonParameter;
	}

	public String getFirstStatDerivedLabel() {
		return firstStatDerivedLabel;
	}

	public String getSecondStatDerivedLabel() {
		return secondStatDerivedLabel;
	}

	public String getThirdStatDerviedLabel() {
		return thirdStatDerviedLabel;
	}

	public String getFourthStatDerviedLabel() {
		return fourthStatDerviedLabel;
	}

	public String getComparisonStationId() {
		return comparisonStationId;
	}
	
	public UvHydroRequestParameters getRequestParameters() {
		return requestParameters;
	}

	public String getRequestingUser() {
		return requestingUser;
	}

	public Map<String, GradeMetadata> getGradeMetadata() {
		return gradeMetadata;
	}

	public UvHydrographType getUvType() {
		return uvType;
	}

	public void setRequestingUser(String val) {
		requestingUser = val;
	}

	public void setGradeMetadata(Map<String, GradeMetadata> gradeMetadata) {
		this.gradeMetadata = gradeMetadata;
	}

	public void setPrimaryParameter(String primaryParameter) {
		this.primaryParameter = primaryParameter;
	}

	public void setUpchainParameter(String upchainParameter) {
		this.upchainParameter = upchainParameter;
	}

	public void setReferenceParameter(String referenceParameter) {
		this.referenceParameter = referenceParameter;
	}

	public void setComparisonParameter(String comparisonParameter) {
		this.comparisonParameter = comparisonParameter;
	}

	public void setFirstStatDerivedLabel(String firstStatDerivedLabel) {
		this.firstStatDerivedLabel = firstStatDerivedLabel;
	}

	public void setSecondStatDerivedLabel(String secondStatDerivedLabel) {
		this.secondStatDerivedLabel = secondStatDerivedLabel;
	}

	public void setThirdStatDerviedLabel(String thirdStatDerviedLabel) {
		this.thirdStatDerviedLabel = thirdStatDerviedLabel;
	}

	public void setFourthStatDerviedLabel(String fourthStatDerviedLabel) {
		this.fourthStatDerviedLabel = fourthStatDerviedLabel;
	}

	public void setComparisonStationId(String comparisonStationId) {
		this.comparisonStationId = comparisonStationId;
	}

	public void setRequestParameters(UvHydroRequestParameters val) {
		requestParameters = val;
		//Report Period displayed should be exactly as received, so get as UTC
		setStartDate(val.getStartInstant(ZoneOffset.UTC));
		setEndDate(val.getEndInstant(ZoneOffset.UTC));
	}

	public void setUvType(UvHydrographType uvType) {
		this.uvType = uvType;
	}
}