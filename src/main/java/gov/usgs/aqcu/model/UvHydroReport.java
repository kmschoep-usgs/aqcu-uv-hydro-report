package gov.usgs.aqcu.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Grade;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Qualifier;

import gov.usgs.aqcu.model.nwis.WaterLevelRecords;

public class UvHydroReport {
	private UvHydroReportMetadata reportMetadata;
	private UvHydrographTimeSeries primarySeries;
	private UvHydrographTimeSeries upchainSeries;
	private UvHydrographTimeSeries referenceSeries;
	private UvHydrographTimeSeries firstStatDerived;
	private UvHydrographTimeSeries secondStatDerived;
	private UvHydrographTimeSeries thirdStatDerived;
	private UvHydrographTimeSeries fourthStatDerived;
	private UvHydrographTimeSeries comparisonSeries;
	private UvHydrographTimeSeries primarySeriesRaw;
	private UvHydrographTimeSeries upchainSeriesRaw;
	private List<ExtendedCorrection> primarySeriesCorrections;
	private List<ExtendedCorrection> upchainSeriesCorrections;
	private List<UvHydrographRatingShift> ratingShifts;
	private List<FieldVisitMeasurement> fieldVisitMeasurements;
	private List<UvHydrographReading> primaryReadings;
	private List<UvHydrographReading> upchainReadings;
	private List<WaterQualitySampleRecord> waterQuality;
	private WaterLevelRecords gwlevel;
	private UvHydrographEffectiveShifts effectiveShifts;
	
	private String simsUrl;

	public UvHydroReport() {
		reportMetadata = new UvHydroReportMetadata();
	}

	public void setPrimarySeries(UvHydrographTimeSeries primarySeries) {
		this.primarySeries = primarySeries;
	}

	public void setUpchainSeries(UvHydrographTimeSeries upchainSeries) {
		this.upchainSeries = upchainSeries;
	}

	public void setReferenceSeries(UvHydrographTimeSeries referenceSeries) {
		this.referenceSeries = referenceSeries;
	}

	public void setFirstStatDerived(UvHydrographTimeSeries firstStatDerived) {
		this.firstStatDerived = firstStatDerived;
	}

	public void setSecondStatDerived(UvHydrographTimeSeries secondStatDerived) {
		this.secondStatDerived = secondStatDerived;
	}

	public void setThirdStatDerived(UvHydrographTimeSeries thirdStatDerived) {
		this.thirdStatDerived = thirdStatDerived;
	}

	public void setFourthStatDerived(UvHydrographTimeSeries fourthStatDerived) {
		this.fourthStatDerived = fourthStatDerived;
	}

	public void setComparisonSeries(UvHydrographTimeSeries comparisonSeries) {
		this.comparisonSeries = comparisonSeries;
	}

	public void setPrimarySeriesRaw(UvHydrographTimeSeries primarySeriesRaw) {
		this.primarySeriesRaw = primarySeriesRaw;
	}

	public void setUpchainSeriesRaw(UvHydrographTimeSeries upchainSeriesRaw) {
		this.upchainSeriesRaw = upchainSeriesRaw;
	}

	public void setPrimarySeriesCorrections(List<ExtendedCorrection> primarySeriesCorrections) {
		this.primarySeriesCorrections = primarySeriesCorrections;
	}

	public void setUpchainSeriesCorrections(List<ExtendedCorrection> upchainSeriesCorrections) {
		this.upchainSeriesCorrections = upchainSeriesCorrections;
	}

	public void setReportMetadata(UvHydroReportMetadata val) {
		reportMetadata = val;
	}

	public void setRatingShifts(List<UvHydrographRatingShift> ratingShifts) {
		this.ratingShifts = ratingShifts;
	}
	
	public void setEffectiveShifts(UvHydrographEffectiveShifts effectiveShifts) {
		this.effectiveShifts = effectiveShifts;
	}

	public void setSimsUrl(String simsUrl) {
		this.simsUrl = simsUrl;
	}

	public void setFieldVisitMeasurements(List<FieldVisitMeasurement> fieldVisitMeasurements) {
		this.fieldVisitMeasurements = fieldVisitMeasurements;
	}

	public void setPrimaryReadings(List<UvHydrographReading> primaryReadings) {
		this.primaryReadings = primaryReadings;
	}

	public void setUpchainReadings(List<UvHydrographReading> upchainReadings) {
		this.upchainReadings = upchainReadings;
	}

	public void setGwlevel(WaterLevelRecords gwlevel) {
		this.gwlevel = gwlevel;
	}

	public void setWaterQuality(List<WaterQualitySampleRecord> waterQuality) {
		this.waterQuality = waterQuality;
	}

	public UvHydrographTimeSeries getPrimarySeries() {
		return primarySeries;
	}

	public UvHydrographTimeSeries getUpchainSeries() {
		return upchainSeries;
	}

	public UvHydrographTimeSeries getReferenceSeries() {
		return referenceSeries;
	}

	public UvHydrographTimeSeries getFirstStatDerived() {
		return firstStatDerived;
	}

	public UvHydrographTimeSeries getSecondStatDerived() {
		return secondStatDerived;
	}

	public UvHydrographTimeSeries getThirdStatDerived() {
		return thirdStatDerived;
	}

	public UvHydrographTimeSeries getFourthStatDerived() {
		return fourthStatDerived;
	}

	public UvHydrographTimeSeries getComparisonSeries() {
		return comparisonSeries;
	}

	public UvHydrographTimeSeries getPrimarySeriesRaw() {
		return primarySeriesRaw;
	}

	public UvHydrographTimeSeries getUpchainSeriesRaw() {
		return upchainSeriesRaw;
	}

	public List<ExtendedCorrection> getPrimarySeriesCorrections() {
		return primarySeriesCorrections;
	}

	public List<ExtendedCorrection> getUpchainSeriesCorrections() {
		return upchainSeriesCorrections;
	}
	
	public UvHydroReportMetadata getReportMetadata() {
		return reportMetadata;
	}

	public List<UvHydrographRatingShift> getRatingShifts() {
		return ratingShifts;
	}
	
	public UvHydrographEffectiveShifts getEffectiveShifts() {
		return effectiveShifts;
	}

	public String getSimsUrl() {
		return simsUrl;
	}

	public List<FieldVisitMeasurement> getFieldVisitMeasurements() {
		return fieldVisitMeasurements;
	}

	public List<UvHydrographReading> getPrimaryReadings() {
		return primaryReadings;
	}

	public List<UvHydrographReading> getUpchainReadings() {
		return upchainReadings;
	}

	public WaterLevelRecords getGwlevel() {
		return gwlevel;
	}

	public List<WaterQualitySampleRecord> getWaterQuality() {
		return waterQuality;
	}

	public List<Qualifier> getAllQualifiers() {
		Set<Qualifier> qualSet = new HashSet<>();

		if(getPrimarySeries() != null) {
			qualSet.addAll(getPrimarySeries().getQualifiers());
		}

		if(getUpchainSeries() != null) {
			qualSet.addAll(getUpchainSeries().getQualifiers());
		}

		if(getReferenceSeries() != null) {
			qualSet.addAll(getReferenceSeries().getQualifiers());
		}

		if(getFirstStatDerived() != null) {
			qualSet.addAll(getFirstStatDerived().getQualifiers());
		}

		if(getSecondStatDerived() != null) {
			qualSet.addAll(getSecondStatDerived().getQualifiers());
		}

		if(getThirdStatDerived() != null) {
			qualSet.addAll(getThirdStatDerived().getQualifiers());
		}

		if(getFourthStatDerived() != null) {
			qualSet.addAll(getFourthStatDerived().getQualifiers());
		}

		if(getComparisonSeries() != null) {
			qualSet.addAll(getComparisonSeries().getQualifiers());
		}

		return new ArrayList<>(qualSet);
	}

	public List<Grade> getAllGrades() {
		Set<Grade> gradeSet = new HashSet<>();

		if(getPrimarySeries() != null) {
			gradeSet.addAll(getPrimarySeries().getGrades());
		}

		if(getUpchainSeries() != null) {
			gradeSet.addAll(getUpchainSeries().getGrades());
		}

		if(getReferenceSeries() != null) {
			gradeSet.addAll(getReferenceSeries().getGrades());
		}

		if(getFirstStatDerived() != null) {
			gradeSet.addAll(getFirstStatDerived().getGrades());
		}

		if(getSecondStatDerived() != null) {
			gradeSet.addAll(getSecondStatDerived().getGrades());
		}

		if(getThirdStatDerived() != null) {
			gradeSet.addAll(getThirdStatDerived().getGrades());
		}

		if(getFourthStatDerived() != null) {
			gradeSet.addAll(getFourthStatDerived().getGrades());
		}

		if(getComparisonSeries() != null) {
			gradeSet.addAll(getComparisonSeries().getGrades());
		}

		return new ArrayList<>(gradeSet);
	}
}
	
