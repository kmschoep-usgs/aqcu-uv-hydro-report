package gov.usgs.aqcu.model;

import java.util.Map;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GradeMetadata;

public class UvHydroReportMetadata extends ReportMetadata {
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
	private String excludeCorrections;
	private Boolean excludeZeroNegative;
	private Boolean excludeDiscrete;
	private Map<String, GradeMetadata> gradeMetadata;
	private UvHydrographType uvType;

	public UvHydroReportMetadata() {
		super();
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

	public String getExcludeCorrections() {
		return excludeCorrections;
	}

	public Boolean getExcludeDiscrete() {
		return excludeDiscrete;
	}

	public Boolean getExcludeZeroNegative() {
		return excludeZeroNegative;
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

	public void setExcludeCorrections(String excludeCorrections) {
		this.excludeCorrections = excludeCorrections;
	}

	public void setExcludeDiscrete(Boolean excludeDiscrete) {
		this.excludeDiscrete = excludeDiscrete;
	}

	public void setExcludeZeroNegative(Boolean excludeZeroNegative) {
		this.excludeZeroNegative = excludeZeroNegative;
	}

	public void setUvType(UvHydrographType uvType) {
		this.uvType = uvType;
	}
}