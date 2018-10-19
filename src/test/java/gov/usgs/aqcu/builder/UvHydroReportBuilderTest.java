package gov.usgs.aqcu.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import gov.usgs.aqcu.model.UvHydroReport;
import gov.usgs.aqcu.model.UvHydroReportMetadata;
import gov.usgs.aqcu.parameter.UvHydroRequestParameters;
import gov.usgs.aqcu.retrieval.AquariusRetrievalService;
import gov.usgs.aqcu.retrieval.CorrectionListService;
import gov.usgs.aqcu.retrieval.DownchainProcessorListServiceTest;
import gov.usgs.aqcu.retrieval.EffectiveShiftsService;
import gov.usgs.aqcu.retrieval.FieldVisitDataService;
import gov.usgs.aqcu.retrieval.FieldVisitDescriptionService;
import gov.usgs.aqcu.retrieval.GradeLookupService;
import gov.usgs.aqcu.retrieval.LocationDescriptionListService;
import gov.usgs.aqcu.retrieval.NwisRaService;
import gov.usgs.aqcu.retrieval.ParameterListService;
import gov.usgs.aqcu.retrieval.QualifierLookupService;
import gov.usgs.aqcu.retrieval.RatingCurveListService;
import gov.usgs.aqcu.retrieval.TimeSeriesDataService;
import gov.usgs.aqcu.retrieval.TimeSeriesDescriptionListService;
import gov.usgs.aqcu.retrieval.TimeSeriesDescriptionListServiceTest;
import gov.usgs.aqcu.retrieval.UpchainProcessorListServiceTest;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Processor;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class UvHydroReportBuilderTest {	 
	@MockBean
	private AquariusRetrievalService aquariusService;
	@MockBean
	private LocationDescriptionListService locService;
	@MockBean
	private TimeSeriesDescriptionListService descService;
	@MockBean
	private TimeSeriesDataService dataService;
	@MockBean
	private DataGapListBuilderService gapsService;
	@MockBean
	private CorrectionListService corrService;
	@MockBean
	private GradeLookupService gradeService;
	@MockBean
	private QualifierLookupService qualService;
	@MockBean
	private RatingCurveListService curvesService;
	@MockBean
	private EffectiveShiftsService effectiveService;
	@MockBean
	private FieldVisitDescriptionService fieldVisitDescriptionService;
	@MockBean
	private FieldVisitDataService fieldVisitDataService;
	@MockBean
	private ParameterListService paramService;
	@MockBean
	private NwisRaService nwisraService;

	private UvHydroReportBuilderService service;
	private final String REQUESTING_USER = "test-user";
	private UvHydroRequestParameters requestParams;

	UvHydroReportMetadata metadata;
	TimeSeriesDescription primaryDesc = TimeSeriesDescriptionListServiceTest.DESC_1;
	TimeSeriesDescription procDesc = TimeSeriesDescriptionListServiceTest.DESC_2;
	LocationDescription primaryLoc = new LocationDescription().setIdentifier(primaryDesc.getLocationIdentifier()).setName("loc-name");
	List<Processor> upProcs = UpchainProcessorListServiceTest.PROCESSOR_LIST;
	List<Processor> downProcs = DownchainProcessorListServiceTest.PROCESSOR_LIST;

	@Before
	public void setup() {
		// Builder Servies
		service = new UvHydroReportBuilderService(locService,descService,dataService,gapsService,corrService,gradeService,qualService,curvesService,effectiveService,fieldVisitDescriptionService,fieldVisitDataService,paramService,nwisraService);

		// Request Parameters
		requestParams = new UvHydroRequestParameters();
		requestParams.setPrimaryTimeseriesIdentifier(primaryDesc.getUniqueId());

		// Metadata
		metadata = new UvHydroReportMetadata();
		metadata.setStationId(primaryDesc.getLocationIdentifier());
		metadata.setStationName(primaryLoc.getName());
		metadata.setTimezone(primaryDesc.getUtcOffset());
		metadata.setTitle(UvHydroReportBuilderService.REPORT_TITLE);
	}

	@Test
	public void stubTest() {
		
	}
	
	/*
	@Test
	public void buildReportBasicTest() {
		given(descService.getTimeSeriesDescriptionList(any(List.class)))
			.willReturn(Arrays.asList(primaryDesc));
		given(locService.getByLocationIdentifier(metadata.getStationId()))
			.willReturn(primaryLoc);
		given(dataService.get(any(String.class), requestParameters, any(Boolean.class), any(Boolean.class), any(ZoneOffset.class)))
			.willReturn()
		
		UvHydroReport report = service.buildReport(requestParams, REQUESTING_USER);
		assertNotNull(report);
		assertNotNull(report.getReportMetadata());
		assertEquals(report.getReportMetadata().getRequestingUser(), REQUESTING_USER);
		assertEquals(report.getReportMetadata().getStartDate(), metadata.getStartDate());
		assertEquals(report.getReportMetadata().getEndDate(), metadata.getEndDate());
		assertEquals(report.getReportMetadata().getStationId(), primaryDesc.getLocationIdentifier());
		assertEquals(report.getReportMetadata().getTimezone(), metadata.getTimezone());
		assertEquals(report.getReportMetadata().getStationName(), metadata.getStationName());
		assertEquals(report.getReportMetadata().getQualifierMetadata(), new HashMap<>());
	}
	*/

	/*
	@Test
	public void getReportMetadataTest() {
		given(locService.getByLocationIdentifier(metadata.getStationId()))
			.willReturn(primaryLoc);

		UvHydroReportMetadata newMetadata = service.getReportMetadata(requestParams, REQUESTING_USER, primaryLoc.getIdentifier(), primaryDesc.getIdentifier(), primaryDesc.getUtcOffset());
		assertNotNull(newMetadata);
		assertEquals(newMetadata.getRequestingUser(), REQUESTING_USER);
		assertEquals(newMetadata.getStartDate(), metadata.getStartDate());
		assertEquals(newMetadata.getEndDate(), metadata.getEndDate());
		assertEquals(newMetadata.getStationId(), primaryDesc.getLocationIdentifier());
		assertEquals(newMetadata.getStationName(), primaryLoc.getName());
		assertEquals(newMetadata.getTimezone(), metadata.getTimezone());
		assertEquals(newMetadata.getQualifierMetadata(), new HashMap<>());
	}
	*/
}
