package gov.usgs.aqcu.retrieval;

import java.util.List;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.usgs.aqcu.util.LogExecutionTime;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.EffectiveShift;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingModelEffectiveShiftsServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingModelEffectiveShiftsServiceResponse;

@Repository
public class EffectiveShiftsService {
	private AquariusRetrievalService aquariusRetrievalService;

	@Autowired
	public EffectiveShiftsService(
		AquariusRetrievalService aquariusRetrievalService
	) {
		this.aquariusRetrievalService = aquariusRetrievalService;
	}

	@LogExecutionTime
	public List<EffectiveShift> get(String tsUid, String ratingModelIdentifier, Instant startDate, Instant endDate) {
		RatingModelEffectiveShiftsServiceRequest request = new RatingModelEffectiveShiftsServiceRequest()
			.setQueryFrom(startDate)
			.setQueryTo(endDate)
			.setTimeSeriesUniqueId(tsUid)
			.setRatingModelIdentifier(ratingModelIdentifier);
		RatingModelEffectiveShiftsServiceResponse effectiveShiftsResponse = aquariusRetrievalService.executePublishApiRequest(request);
		return effectiveShiftsResponse.getEffectiveShifts();
	}
}