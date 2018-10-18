package gov.usgs.aqcu.retrieval;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.FieldVisitDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.FieldVisitDescriptionListServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.FieldVisitDescriptionListServiceResponse;

import gov.usgs.aqcu.parameter.DateRangeRequestParameters;

@Repository
public class FieldVisitDescriptionService {
	private static final Logger LOG = LoggerFactory.getLogger(FieldVisitDescriptionService.class);

	private AquariusRetrievalService aquariusRetrievalService;

	@Autowired
	public FieldVisitDescriptionService(AquariusRetrievalService aquariusRetrievalService) {
		this.aquariusRetrievalService = aquariusRetrievalService;
	}

	public List<FieldVisitDescription> getDescriptions(String stationId, ZoneOffset zoneOffset, DateRangeRequestParameters requestParameters) {
		List<FieldVisitDescription> descriptions = new ArrayList<>();
		try {
			FieldVisitDescriptionListServiceResponse fieldVisitResponse = get(stationId,
					requestParameters.getStartInstant(zoneOffset),
					requestParameters.getEndInstant(zoneOffset));
			descriptions = fieldVisitResponse.getFieldVisitDescriptions();
		} catch (Exception e) {
			String msg = "An unexpected error occurred while attempting to fetch FieldVisitDescriptionListServiceRequest from Aquarius: ";
			LOG.error(msg, e);
			throw new RuntimeException(msg, e);
		}
		return descriptions;
	}

	protected FieldVisitDescriptionListServiceResponse get(String stationId, Instant startDate, Instant endDate) throws Exception {
		FieldVisitDescriptionListServiceRequest request = new FieldVisitDescriptionListServiceRequest()
				.setLocationIdentifier(stationId)
				.setQueryFrom(startDate)
				.setQueryTo(endDate);
		FieldVisitDescriptionListServiceResponse fieldVisitResponse  = aquariusRetrievalService.executePublishApiRequest(request);
		return fieldVisitResponse;
	}

}
