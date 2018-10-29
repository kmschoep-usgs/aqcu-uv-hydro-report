package gov.usgs.aqcu.parameter;

import java.util.ArrayList;
import java.util.List;

public class UvHydroRequestParameters extends ReportRequestParameters {
	private String upchainTimeseriesIdentifier;
	private String referenceTimeseriesIdentifier;
	private String firstStatDerivedIdentifier;
	private String secondStatDerivedIdentifier;
	private String thirdStatDerivedIdentifier;
	private String fourthStatDerivedIdentifier;
	private String comparisonTimeseriesIdentifier;
	private String primaryRatingModelIdentifier;
	private String referenceRatingModelIdentifier;
	private boolean excludeZeroNegative;
	private boolean excludeDiscrete;
	private List<String> excludedCorrections;

	public UvHydroRequestParameters() {
		excludedCorrections = new ArrayList<>();
	}

	public String getUpchainTimeseriesIdentifier() {
		return upchainTimeseriesIdentifier;
	}
	public void setUpchainTimeseriesIdentifier(String upchainTimeseriesIdentifier) {
		this.upchainTimeseriesIdentifier = upchainTimeseriesIdentifier;
	}
	public String getReferenceTimeseriesIdentifier() {
		return referenceTimeseriesIdentifier;
	}
	public void setReferenceTimeseriesIdentifier(String referenceTimeseriesIdentifier) {
		this.referenceTimeseriesIdentifier = referenceTimeseriesIdentifier;
	}
	public String getFirstStatDerivedIdentifier() {
		return firstStatDerivedIdentifier;
	}
	public void setFirstStatDerivedIdentifier(String firstStatDerivedIdentifier) {
		this.firstStatDerivedIdentifier = firstStatDerivedIdentifier;
	}
	public String getSecondStatDerivedIdentifier() {
		return secondStatDerivedIdentifier;
	}
	public void setSecondStatDerivedIdentifier(String secondStatDerivedIdentifier) {
		this.secondStatDerivedIdentifier = secondStatDerivedIdentifier;
	}
	public String getThirdStatDerivedIdentifier() {
		return thirdStatDerivedIdentifier;
	}
	public void setThirdStatDerivedIdentifier(String thirdStatDerivedIdentifier) {
		this.thirdStatDerivedIdentifier = thirdStatDerivedIdentifier;
	}
	public String getFourthStatDerivedIdentifier() {
		return fourthStatDerivedIdentifier;
	}
	public void setFourthStatDerivedIdentifier(String fourthStatDerivedIdentifier) {
		this.fourthStatDerivedIdentifier = fourthStatDerivedIdentifier;
	}
	public String getComparisonTimeseriesIdentifier() {
		return comparisonTimeseriesIdentifier;
	}
	public void setComparisonTimeseriesIdentifier(String comparisonTimeseriesIdentifier) {
		this.comparisonTimeseriesIdentifier = comparisonTimeseriesIdentifier;
	}
	public String getPrimaryRatingModelIdentifier() {
		return this.primaryRatingModelIdentifier;
	}
	public void setPrimaryRatingModelIdentifier(String primaryRatingModelIdentifier) {
		this.primaryRatingModelIdentifier = primaryRatingModelIdentifier;
	}
	public String getReferenceRatingModelIdentifier() {
		return this.referenceRatingModelIdentifier;
	}
	public void setReferenceRatingModelIdentifier(String referenceRatingModelIdentifier) {
		this.referenceRatingModelIdentifier = referenceRatingModelIdentifier;
	}
	public boolean isExcludeZeroNegative() {
		return excludeZeroNegative;
	}
	public void setExcludeZeroNegative(boolean excludeZeroNegative) {
		this.excludeZeroNegative = excludeZeroNegative;
	}
	public boolean isExcludeDiscrete() {
		return excludeDiscrete;
	}
	public void setExcludeDiscrete(boolean excludeDiscrete) {
		this.excludeDiscrete = excludeDiscrete;
	}
	public List<String> getExcludedCorrections() {
		return excludedCorrections;
	}
	public void setExcludedCorrections(List<String> excludedCorrections) {
		this.excludedCorrections = excludedCorrections;
	}
	public List<String> getTsUidList() {
		List<String> tsUidList = new ArrayList<>();
		tsUidList.add(getPrimaryTimeseriesIdentifier());

		if(getUpchainTimeseriesIdentifier() != null && !getUpchainTimeseriesIdentifier().isEmpty()) {
			tsUidList.add(getUpchainTimeseriesIdentifier());
		}

		if(getReferenceTimeseriesIdentifier() != null && !getReferenceTimeseriesIdentifier().isEmpty()) {
			tsUidList.add(getReferenceTimeseriesIdentifier());
		}

		if(getFirstStatDerivedIdentifier() != null && !getFirstStatDerivedIdentifier().isEmpty()) {
			tsUidList.add(getFirstStatDerivedIdentifier());
		}

		if(getSecondStatDerivedIdentifier() != null && !getSecondStatDerivedIdentifier().isEmpty()) {
			tsUidList.add(getSecondStatDerivedIdentifier());
		}

		if(getThirdStatDerivedIdentifier() != null && !getThirdStatDerivedIdentifier().isEmpty()) {
			tsUidList.add(getThirdStatDerivedIdentifier());
		}

		if(getFourthStatDerivedIdentifier() != null && !getFourthStatDerivedIdentifier().isEmpty()) {
			tsUidList.add(getFourthStatDerivedIdentifier());
		}

		if(getComparisonTimeseriesIdentifier() != null && !getComparisonTimeseriesIdentifier().isEmpty()) {
			tsUidList.add(getComparisonTimeseriesIdentifier());
		}

		return tsUidList;
	}
}
