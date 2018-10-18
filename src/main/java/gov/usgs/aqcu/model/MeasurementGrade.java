package gov.usgs.aqcu.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.MeasurementGradeType;

/**
 * Represents and Describes the possible measurement grades and their associated values
 */
public enum MeasurementGrade {

	EXCELLENT(MeasurementGradeType.Excellent, new BigDecimal("0.0200")),

	GOOD(MeasurementGradeType.Good, new BigDecimal("0.0500")),

	FAIR(MeasurementGradeType.Fair, new BigDecimal("0.0800")),

	POOR(MeasurementGradeType.Poor, new BigDecimal("0.100"));

	private final BigDecimal percentageOfError;
	private final MeasurementGradeType measurementGradeType;

	private static Map<MeasurementGradeType, MeasurementGrade> measurementGradeMap = new HashMap<>();

	static {
		for (MeasurementGrade measurementGrade : MeasurementGrade.values()) {
			measurementGradeMap.put(measurementGrade.measurementGradeType, measurementGrade);
		}
}
	private MeasurementGrade(MeasurementGradeType measurementGradeType, BigDecimal percentageOfError) {
		this.measurementGradeType = measurementGradeType;
		this.percentageOfError = percentageOfError;
	}

	public BigDecimal getPercentageOfError() {
		return percentageOfError;
	}

	/**
	 * @param measurementGradeType The measurementGradeType
	 * @return null if not valid otherwise the correct enum for the measurementGradeType
	 * value.
	 */
	public static MeasurementGrade fromMeasurementGradeType(MeasurementGradeType measurementGradeType) {
		MeasurementGrade ret = null;
		if (measurementGradeMap.containsKey(measurementGradeType)) {
			ret = measurementGradeMap.get(measurementGradeType);
		} else {
			ret = POOR; //default missing or other grades (Unknown, Unspecified) to poor
		}
		return ret;
	}

}