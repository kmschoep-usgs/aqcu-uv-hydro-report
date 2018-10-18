package gov.usgs.aqcu.retrieval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.usgs.aqcu.model.FieldVisitMeasurement;
import gov.usgs.aqcu.model.MeasurementGrade;
import gov.usgs.aqcu.util.DoubleWithDisplayUtil;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.DischargeSummary;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.FieldVisitDataServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.FieldVisitDataServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Reading;

@Repository
public class FieldVisitDataService {
	private static final Logger LOG = LoggerFactory.getLogger(FieldVisitDataService.class);

	private AquariusRetrievalService aquariusRetrievalService;

	@Autowired
	public FieldVisitDataService(AquariusRetrievalService aquariusRetrievalService) {
		this.aquariusRetrievalService = aquariusRetrievalService;
	}

	public FieldVisitDataServiceResponse get(String fieldVisitIdentifier) {
		try {
			FieldVisitDataServiceRequest request = new FieldVisitDataServiceRequest()
					.setFieldVisitIdentifier(fieldVisitIdentifier)
					.setApplyRounding(true);
			FieldVisitDataServiceResponse fieldVisitResponse  = aquariusRetrievalService.executePublishApiRequest(request);
			return fieldVisitResponse;
		} catch (Exception e) {
			String msg = "An unexpected error occurred while attempting to fetch FieldVisitDataServiceRequest from Aquarius: ";
			LOG.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public List<Reading> extractFieldVisitReadings(FieldVisitDataServiceResponse response) {
		return response.getInspectionActivity().getReadings();
	}

	public List<FieldVisitMeasurement> extractFieldVisitMeasurements(FieldVisitDataServiceResponse response) {
		List<FieldVisitMeasurement> ret = new ArrayList<>();

		if (response.getDischargeActivities() != null) {
			ret = response.getDischargeActivities().stream()
				.filter(x -> x.getDischargeSummary() != null)
				.filter(y -> y.getDischargeSummary().getDischarge() != null).map(z -> {
					return createFieldVisitMeasurement(z.getDischargeSummary());
				}).collect(Collectors.toList());
		}

		return ret;
	}

	protected FieldVisitMeasurement createFieldVisitMeasurement(DischargeSummary dischargeSummary) {
		MeasurementGrade grade = MeasurementGrade.fromMeasurementGradeType(dischargeSummary.getMeasurementGrade());

		FieldVisitMeasurement fieldVisitMeasurement = calculateError(grade, dischargeSummary.getMeasurementId(),
				DoubleWithDisplayUtil.getRoundedValue(dischargeSummary.getDischarge()),
				dischargeSummary.getMeasurementStartTime(),
				dischargeSummary.isPublish());

		return fieldVisitMeasurement;
	}

	protected FieldVisitMeasurement calculateError(MeasurementGrade grade, String measurementNumber,
			BigDecimal dischargeValue, Instant dateTime, Boolean publish) 
	{

		BigDecimal errorAmt = dischargeValue.multiply(grade.getPercentageOfError());
		BigDecimal errorMaxDischargeInFeet = dischargeValue.add(errorAmt);
		BigDecimal errorMinDischargeInFeet = dischargeValue.subtract(errorAmt);

		FieldVisitMeasurement ret = new FieldVisitMeasurement(measurementNumber, dischargeValue,
				errorMaxDischargeInFeet, errorMinDischargeInFeet, dateTime, publish);

		return ret;
	}

}
