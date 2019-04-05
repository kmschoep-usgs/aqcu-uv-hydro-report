package gov.usgs.aqcu.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import gov.usgs.aqcu.exception.AquariusRetrievalException;
import gov.usgs.aqcu.model.DataGap;
import gov.usgs.aqcu.model.ExtendedCorrection;
import gov.usgs.aqcu.model.FieldVisitMeasurement;
import gov.usgs.aqcu.model.FieldVisitReading;
import gov.usgs.aqcu.model.UvHydroReport;
import gov.usgs.aqcu.model.UvHydroReportMetadata;
import gov.usgs.aqcu.model.UvHydrographEffectiveShifts;
import gov.usgs.aqcu.model.UvHydrographPoint;
import gov.usgs.aqcu.model.UvHydrographRatingShift;
import gov.usgs.aqcu.model.UvHydrographReading;
import gov.usgs.aqcu.model.UvHydrographTimeSeries;
import gov.usgs.aqcu.model.UvHydrographType;
import gov.usgs.aqcu.model.nwis.GroundWaterParameter;
import gov.usgs.aqcu.model.nwis.WaterLevelRecord;
import gov.usgs.aqcu.model.nwis.WaterLevelRecords;
import gov.usgs.aqcu.model.nwis.WaterQualitySampleRecord;
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
import gov.usgs.aqcu.util.AqcuTimeUtils;
import gov.usgs.aqcu.util.TimeSeriesUtils;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.FieldVisitDataServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.FieldVisitDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GradeMetadata;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Processor;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Qualifier;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.QualifierMetadata;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurve;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurveListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingShift;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Reading;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Grade;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.StatisticalDateTimeOffset;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.StatisticalTimeRange;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesPoint;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.PeriodOfApplicability;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.DoubleWithDisplay;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.EffectiveShift;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class UvHydroReportBuilderTest {	 
	@MockBean
	private AquariusRetrievalService aquariusService;
	@MockBean
	private LocationDescriptionListService locationService;
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
	private FieldVisitMeasurementsBuilderService fieldVisitMeasurementsBuilderService;
	@MockBean
	private FieldVisitReadingsBuilderService fieldVisitReadingsBuilderService;
	@MockBean
	private ParameterListService paramService;
	@MockBean
	private NwisRaService nwisraService;

	private UvHydroReportBuilderService service;
	private final String REQUESTING_USER = "test-user";

	UvHydroReportMetadata metadata;
	TimeSeriesDescription primaryDesc = TimeSeriesDescriptionListServiceTest.DESC_1;
	TimeSeriesDescription procDesc = TimeSeriesDescriptionListServiceTest.DESC_2;
	LocationDescription primaryLoc = new LocationDescription().setIdentifier(primaryDesc.getLocationIdentifier()).setName("loc-name");
	List<Processor> upProcs = UpchainProcessorListServiceTest.PROCESSOR_LIST;
	List<Processor> downProcs = DownchainProcessorListServiceTest.PROCESSOR_LIST;

	@Before
	public void setup() {
		// Builder Servies
		service = new UvHydroReportBuilderService(locationService,descService,dataService,gapsService,corrService,gradeService,qualService,curvesService,effectiveService,fieldVisitDescriptionService,fieldVisitDataService,fieldVisitMeasurementsBuilderService,fieldVisitReadingsBuilderService,paramService,nwisraService);
		
		// Mock Returns
		given(locationService.getByLocationIdentifier(any(String.class))).willReturn(primaryLoc);
	}

	@Test
	public void determineReportTypeTest() {
		TimeSeriesDescription metadata = new TimeSeriesDescription();
		
		// GW
		metadata.setParameter("test");
		assertEquals(UvHydrographType.GW, service.determineReportType(metadata, GroundWaterParameter.AQ176, "test"));
		assertEquals(UvHydrographType.GW, service.determineReportType(metadata, GroundWaterParameter.AQ176, ""));
		assertEquals(UvHydrographType.GW, service.determineReportType(metadata, GroundWaterParameter.AQ176, null));
		metadata.setParameter(UvHydroReportBuilderService.DISCHARGE_PARAMETER);
		assertEquals(UvHydrographType.GW, service.determineReportType(metadata, GroundWaterParameter.AQ176, "test"));
		assertEquals(UvHydrographType.GW, service.determineReportType(metadata, GroundWaterParameter.AQ176, ""));
		assertEquals(UvHydrographType.GW, service.determineReportType(metadata, GroundWaterParameter.AQ176, null));
		metadata.setParameter(UvHydroReportBuilderService.GAGE_HEIGHT_PARAMETER);
		assertEquals(UvHydrographType.GW, service.determineReportType(metadata, GroundWaterParameter.AQ176, "test"));
		assertEquals(UvHydrographType.GW, service.determineReportType(metadata, GroundWaterParameter.AQ176, ""));
		assertEquals(UvHydrographType.GW, service.determineReportType(metadata, GroundWaterParameter.AQ176, null));

		// SW
		metadata.setParameter(UvHydroReportBuilderService.DISCHARGE_PARAMETER);
		assertEquals(UvHydrographType.SW, service.determineReportType(metadata, null, "test"));
		assertEquals(UvHydrographType.SW, service.determineReportType(metadata, null, ""));
		assertEquals(UvHydrographType.SW, service.determineReportType(metadata, null, null));
		metadata.setParameter(UvHydroReportBuilderService.GAGE_HEIGHT_PARAMETER);
		assertEquals(UvHydrographType.SW, service.determineReportType(metadata, null, "test"));
		assertEquals(UvHydrographType.SW, service.determineReportType(metadata, null, ""));
		assertEquals(UvHydrographType.SW, service.determineReportType(metadata, null, null));

		// QW
		metadata.setParameter("test");
		assertEquals(UvHydrographType.QW, service.determineReportType(metadata, null, "test"));

		// DEFAULT
		assertEquals(UvHydrographType.DEFAULT, service.determineReportType(metadata, null, ""));
		assertEquals(UvHydrographType.DEFAULT, service.determineReportType(metadata, null, null));
	}

	@Test
	public void getBaseReportMetadataTest() {
		UvHydroRequestParameters params = new UvHydroRequestParameters();
		UvHydroReportMetadata metadata;

		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setExcludeDiscrete(true);
		params.setExcludeZeroNegative(true);
		params.setExcludedCorrections(Arrays.asList("corr1", "corr2"));
		metadata = service.getBaseReportMetadata(params, REQUESTING_USER, "01", "Name", "02", UvHydrographType.GW, "test", 0.0D);
		assertEquals(UvHydroReportBuilderService.REPORT_TITLE, metadata.getTitle());
		assertEquals(REQUESTING_USER, metadata.getRequestingUser());
		assertEquals("01", metadata.getStationId());
		assertEquals("Name", metadata.getStationName());
		assertEquals("02", metadata.getComparisonStationId());
		assertEquals("test", metadata.getPrimaryParameter());
		assertEquals("Etc/GMT+0", metadata.getTimezone());
		assertEquals(Instant.parse("2018-01-01T00:00:00Z"), metadata.getStartDate());
		assertEquals(Instant.parse("2018-02-01T23:59:59.999999999Z"), metadata.getEndDate());
		assertEquals(Arrays.asList("corr1","corr2"), metadata.getRequestParameters().getExcludedCorrections());
		assertEquals(true, metadata.getRequestParameters().isExcludeDiscrete());
		assertEquals(true, metadata.getRequestParameters().isExcludeZeroNegative());
		assertEquals(UvHydrographType.GW, metadata.getUvType());

		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setExcludeDiscrete(false);
		params.setExcludeZeroNegative(false);
		params.setExcludedCorrections(Arrays.asList("corr1"));
		metadata = service.getBaseReportMetadata(params, REQUESTING_USER, "02", "Name", "03", UvHydrographType.SW, "test2", 2.0D);
		assertEquals(UvHydroReportBuilderService.REPORT_TITLE, metadata.getTitle());
		assertEquals(REQUESTING_USER, metadata.getRequestingUser());
		assertEquals("02", metadata.getStationId());
		assertEquals("Name", metadata.getStationName());
		assertEquals("03", metadata.getComparisonStationId());
		assertEquals("test2", metadata.getPrimaryParameter());
		assertEquals("Etc/GMT-2", metadata.getTimezone());
		assertEquals(Instant.parse("2018-01-01T00:00:00Z"), metadata.getStartDate());
		assertEquals(Instant.parse("2018-02-01T23:59:59.999999999Z"), metadata.getEndDate());
		assertEquals(Arrays.asList("corr1"), metadata.getRequestParameters().getExcludedCorrections());
		assertEquals(false, metadata.getRequestParameters().isExcludeDiscrete());
		assertEquals(false, metadata.getRequestParameters().isExcludeZeroNegative());
		assertEquals(UvHydrographType.SW, metadata.getUvType());

		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setExcludeDiscrete(false);
		params.setExcludeZeroNegative(true);
		params.setExcludedCorrections(new ArrayList<>());
		metadata = service.getBaseReportMetadata(params, REQUESTING_USER, "03", "Name", "04", UvHydrographType.DEFAULT, "test3", 3.0D);
		assertEquals(UvHydroReportBuilderService.REPORT_TITLE, metadata.getTitle());
		assertEquals(REQUESTING_USER, metadata.getRequestingUser());
		assertEquals("03", metadata.getStationId());
		assertEquals("Name", metadata.getStationName());
		assertEquals("04", metadata.getComparisonStationId());
		assertEquals("test3", metadata.getPrimaryParameter());
		assertEquals("Etc/GMT-3", metadata.getTimezone());
		assertEquals(Instant.parse("2018-01-01T00:00:00Z"), metadata.getStartDate());
		assertEquals(Instant.parse("2018-02-01T23:59:59.999999999Z"), metadata.getEndDate());
		assertEquals(new ArrayList<>(), metadata.getRequestParameters().getExcludedCorrections());
		assertEquals(false, metadata.getRequestParameters().isExcludeDiscrete());
		assertEquals(true, metadata.getRequestParameters().isExcludeZeroNegative());
		assertEquals(UvHydrographType.DEFAULT, metadata.getUvType());
	}

	@Test
	public void getFieldVisitMeasurementsTest() {
		FieldVisitMeasurement meas1 = new FieldVisitMeasurement();
		meas1.setDischarge(BigDecimal.valueOf(1.0D));
		meas1.setMeasurementNumber("1");
		FieldVisitMeasurement meas2 = new FieldVisitMeasurement();
		meas2.setDischarge(BigDecimal.valueOf(2.0D));
		meas2.setMeasurementNumber("2");

		given(fieldVisitMeasurementsBuilderService.extractFieldVisitMeasurements(any(FieldVisitDataServiceResponse.class), any(String.class))).willReturn(
			Arrays.asList(meas1,meas2)
		);

		List<FieldVisitMeasurement> result = service.getFieldVisitMeasurements(
			Arrays.asList(new FieldVisitDataServiceResponse(), new FieldVisitDataServiceResponse()),
			"test"
		);
		assertEquals(4, result.size());
		assertThat(result, containsInAnyOrder(meas1,meas1,meas2,meas2));
		result = service.getFieldVisitMeasurements(
			Arrays.asList(new FieldVisitDataServiceResponse()),
			"test"
		);
		assertEquals(2, result.size());
		assertThat(result, containsInAnyOrder(meas1,meas2));
		result = service.getFieldVisitMeasurements(
			new ArrayList<>(),
			"test"
		);
		assertEquals(0, result.size());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getFilteredFieldVisitReadingsTest() {
		Reading read1 = new Reading();
		read1.setIsValid(true);
		read1.setParameter("param1");
		Reading read2 = new Reading();
		read2.setIsValid(false);
		read2.setParameter("param2");
		FieldVisitReading fRead1 = new FieldVisitReading(null, "party1", "TODO", Arrays.asList(), read1);
		FieldVisitReading fRead2 = new FieldVisitReading(null, "party2", "TODO", Arrays.asList(), read2);

		given(fieldVisitReadingsBuilderService.extractReadings(eq(null), any(FieldVisitDataServiceResponse.class), eq("param1"), any(List.class))).willReturn(
			Arrays.asList(fRead1)
		);
		given(fieldVisitReadingsBuilderService.extractReadings(eq(null), any(FieldVisitDataServiceResponse.class), eq("param2"), any(List.class))).willReturn(
			Arrays.asList(fRead2)
		);
		given(fieldVisitReadingsBuilderService.extractReadings(eq(null), any(FieldVisitDataServiceResponse.class), eq("invalid"), any(List.class))).willReturn(
			new ArrayList<FieldVisitReading>()
		);

		List<UvHydrographReading> result = service.getFilteredFieldVisitReadings(Arrays.asList(new FieldVisitDataServiceResponse()), "param1");
		assertEquals(1, result.size());
		assertEquals("param1", result.get(0).getParameter());
		result = service.getFilteredFieldVisitReadings(Arrays.asList(new FieldVisitDataServiceResponse()), "param2");
		assertEquals(1, result.size());
		assertEquals("param2", result.get(0).getParameter());
		result = service.getFilteredFieldVisitReadings(Arrays.asList(new FieldVisitDataServiceResponse()), "invalid");
		assertEquals(0, result.size());
		result = service.getFilteredFieldVisitReadings(Arrays.asList(new FieldVisitDataServiceResponse(), new FieldVisitDataServiceResponse()), "param1");
		assertEquals(2, result.size());
	}

	@Test
	public void getFieldVisitDataTest() {
		FieldVisitDescription desc1 = new FieldVisitDescription();
		desc1.setStartTime(Instant.parse("2018-01-01T00:00:00Z"));
		desc1.setEndTime(Instant.parse("2018-02-01T00:00:00Z"));
		desc1.setIdentifier("test1");
		FieldVisitDescription desc2 = new FieldVisitDescription();
		desc2.setStartTime(Instant.parse("2018-01-01T00:00:00Z"));
		desc2.setEndTime(Instant.parse("2018-02-01T00:00:00Z"));
		desc2.setIdentifier("test2");
		FieldVisitDataServiceResponse resp1 = new FieldVisitDataServiceResponse();
		resp1.setSummary("test1");
		FieldVisitDataServiceResponse resp2 = new FieldVisitDataServiceResponse();
		resp2.setSummary("test2");

		given(fieldVisitDescriptionService.getDescriptions(eq("loc0"), any(ZoneOffset.class), any(UvHydroRequestParameters.class))).willReturn(
			Arrays.asList(desc1,desc2)
		);
		given(fieldVisitDescriptionService.getDescriptions(eq("loc1"), any(ZoneOffset.class), any(UvHydroRequestParameters.class))).willReturn(
			Arrays.asList(desc1)
		);
		given(fieldVisitDescriptionService.getDescriptions(eq("loc2"), any(ZoneOffset.class), any(UvHydroRequestParameters.class))).willReturn(
			Arrays.asList(desc2)
		);
		given(fieldVisitDescriptionService.getDescriptions(eq("loc3"), any(ZoneOffset.class), any(UvHydroRequestParameters.class))).willReturn(
			new ArrayList<>()
		);
		given(fieldVisitDataService.get("test1")).willReturn(resp1);
		given(fieldVisitDataService.get("test2")).willReturn(resp2);

		List<FieldVisitDataServiceResponse> result = service.getFieldVisitData(new UvHydroRequestParameters(), "loc0", ZoneOffset.UTC);
		assertEquals(result.size(), 2);
		assertThat(result, containsInAnyOrder(resp1,resp2));
		result = service.getFieldVisitData(new UvHydroRequestParameters(), "loc1", ZoneOffset.UTC);
		assertEquals(result.size(), 1);
		assertThat(result, containsInAnyOrder(resp1));
		result = service.getFieldVisitData(new UvHydroRequestParameters(), "loc2", ZoneOffset.UTC);
		assertEquals(result.size(), 1);
		assertThat(result, containsInAnyOrder(resp2));
		result = service.getFieldVisitData(new UvHydroRequestParameters(), "loc3", ZoneOffset.UTC);
		assertEquals(result.size(), 0);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getRatingShiftsTest() {
		UvHydroRequestParameters params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		RatingShift c1s1 = new RatingShift();
		c1s1.setPeriodOfApplicability(new PeriodOfApplicability()
			.setStartTime(Instant.parse("2018-01-01T00:00:00Z"))
			.setEndTime(Instant.parse("2018-02-01T00:00:00Z"))
		);
		RatingShift c1s2 = new RatingShift();
		c1s2.setPeriodOfApplicability(new PeriodOfApplicability()
			.setStartTime(Instant.parse("2018-02-01T00:00:00Z"))
			.setEndTime(Instant.parse("2018-03-01T00:00:00Z"))
		);
		RatingShift c2s1 = new RatingShift();
		c2s1.setPeriodOfApplicability(new PeriodOfApplicability()
			.setStartTime(Instant.parse("2017-01-01T00:00:00Z"))
			.setEndTime(Instant.parse("2017-02-01T00:00:00Z"))
		);
		RatingShift c2s2 = new RatingShift();
		c2s2.setPeriodOfApplicability(new PeriodOfApplicability()
			.setStartTime(Instant.parse("2017-02-01T00:00:00Z"))
			.setEndTime(Instant.parse("2017-03-01T00:00:00Z"))
		);
		RatingCurve curve1 = new RatingCurve();
		curve1.setId("1");
		curve1.setShifts(new ArrayList<>(Arrays.asList(c1s1,c1s2)));
		RatingCurve curve2 = new RatingCurve();
		curve2.setId("2");
		curve2.setShifts(new ArrayList<>(Arrays.asList(c2s1,c2s2)));

		given(curvesService.getRawResponse(any(String.class), any(Double.class), any(Instant.class), any(Instant.class))).willReturn(
			new RatingCurveListServiceResponse().setRatingCurves(Arrays.asList(curve1,curve2))
		);
		given(curvesService.getAqcuFilteredRatingCurves(any(List.class), any(Instant.class), any(Instant.class))).willReturn(
			Arrays.asList(curve1, curve2) 
		);
		given(curvesService.getAqcuFilteredRatingShifts(any(List.class), any(Instant.class), any(Instant.class))).willReturn(
			Arrays.asList(c1s1, c1s2, c2s1, c2s2)
		);

		List<UvHydrographRatingShift> result = service.getRatingShifts(params, "test", ZoneOffset.UTC);
		assertEquals(result.size(), 4);
	}

	@Test
	public void createUvHydroPointsFromEffectiveShiftsTest() {
		EffectiveShift shift1 = new EffectiveShift();
		shift1.setTimestamp(Instant.parse("2018-01-01T00:00:00Z"));
		shift1.setValue(1.0D);
		EffectiveShift shift2 = new EffectiveShift();
		shift2.setTimestamp(Instant.parse("2018-02-01T00:00:00Z"));
		shift2.setValue(null);
		EffectiveShift shift3 = new EffectiveShift();
		shift3.setTimestamp(Instant.parse("2018-03-01T00:00:00Z"));
		shift3.setValue(3.0D);

		List<UvHydrographPoint> result = service.createUvHydroPointsFromEffectiveShifts(Arrays.asList(shift1,shift2,shift3));
		assertEquals(result.size(), 2);
		assertEquals(result.get(0).getTime(), shift1.getTimestamp());
		assertEquals(result.get(0).getValue(), BigDecimal.valueOf(shift1.getValue()));
		assertEquals(result.get(1).getTime(), shift3.getTimestamp());
		assertEquals(result.get(1).getValue(), BigDecimal.valueOf(shift3.getValue()));
	}

	@Test
	public void getEffectiveShiftsTest() {
		UvHydroRequestParameters params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		EffectiveShift shift1 = new EffectiveShift();
		shift1.setTimestamp(Instant.parse("2018-01-01T00:00:00Z"));
		shift1.setValue(1.0D);
		EffectiveShift shift2 = new EffectiveShift();
		shift2.setTimestamp(Instant.parse("2018-02-01T00:00:00Z"));
		shift2.setValue(null);
		EffectiveShift shift3 = new EffectiveShift();
		shift3.setTimestamp(Instant.parse("2018-03-01T00:00:00Z"));
		shift3.setValue(3.0D);

		given(effectiveService.get(any(String.class), any(String.class), any(Instant.class), any(Instant.class))).willReturn(
			Arrays.asList(shift1,shift2,shift3)
		);

		UvHydrographEffectiveShifts result = service.getEffectiveShifts(params, "test", "test", false, ZoneOffset.UTC);
		assertEquals(2, result.getPoints().size());
		assertEquals(false, result.isVolumetricFlow());
		result = service.getEffectiveShifts(params, "test", "test", true, ZoneOffset.UTC);
		assertEquals(2, result.getPoints().size());
		assertEquals(true, result.isVolumetricFlow());
	}

	@Test
	public void createUvHydroPointsFromTimeSeriesTest() {
		TimeSeriesPoint pointA1 = new TimeSeriesPoint();
		pointA1.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false));
		pointA1.setValue(new DoubleWithDisplay().setNumeric(1.0D).setDisplay("1"));
		TimeSeriesPoint pointA2 = new TimeSeriesPoint();
		pointA2.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false));
		pointA2.setValue(new DoubleWithDisplay().setNumeric(null).setDisplay("2"));
		TimeSeriesPoint pointA3 = new TimeSeriesPoint();
		pointA3.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-03-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false));
		pointA3.setValue(new DoubleWithDisplay().setNumeric(3.0D).setDisplay("3"));
		TimeSeriesPoint pointB1 = new TimeSeriesPoint();
		pointB1.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T24:00:00Z")).setRepresentsEndOfTimePeriod(true));
		pointB1.setValue(new DoubleWithDisplay().setNumeric(1.0D).setDisplay("1"));
		TimeSeriesPoint pointB2 = new TimeSeriesPoint();
		pointB2.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T24:00:00Z")).setRepresentsEndOfTimePeriod(true));
		pointB2.setValue(new DoubleWithDisplay().setNumeric(null).setDisplay("2"));
		TimeSeriesPoint pointB3 = new TimeSeriesPoint();
		pointB3.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-03-01T24:00:00Z")).setRepresentsEndOfTimePeriod(true));
		pointB3.setValue(new DoubleWithDisplay().setNumeric(3.0D).setDisplay("3"));
		List<TimeSeriesPoint> sourcePointsA = Arrays.asList(pointA1,pointA2,pointA3);
		List<TimeSeriesPoint> sourcePointsB = Arrays.asList(pointB1,pointB2,pointB3);

		List<UvHydrographPoint> result = service.createUvHydroPointsFromTimeSeries(sourcePointsA, false, ZoneOffset.UTC);
		assertEquals(result.size(), 2);
		assertEquals(result.get(0).getTime(), pointA1.getTimestamp().getDateTimeOffset());
		assertEquals(result.get(0).getValue(), new BigDecimal(pointA1.getValue().getDisplay()));
		assertEquals(result.get(1).getTime(), pointA3.getTimestamp().getDateTimeOffset());
		assertEquals(result.get(1).getValue(), new BigDecimal(pointA3.getValue().getDisplay()));

		result = service.createUvHydroPointsFromTimeSeries(sourcePointsB, true, ZoneOffset.UTC);
		assertEquals(result.size(), 2);
		assertEquals(result.get(0).getTime(), LocalDate.parse("2018-01-01"));
		assertEquals(result.get(0).getValue(), new BigDecimal(pointB1.getValue().getDisplay()));
		assertEquals(result.get(1).getTime(), LocalDate.parse("2018-03-01"));
		assertEquals(result.get(1).getValue(), new BigDecimal(pointB3.getValue().getDisplay()));
	}

	@Test
	public void getTimeSeriesDataNullTest() {
		assertNull(service.getTimeSeriesData(null, null, null, false, false));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getTimeSeriesDataNoPointsTest() {
		UvHydroRequestParameters params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		TimeSeriesDescription desc1 = new TimeSeriesDescription();
		desc1.setParameter("param1");
		desc1.setUniqueId("test1");
		desc1.setUtcOffset(0.0D);
		desc1.setDescription("test1-desc");
		desc1.setUnit("test1-unit");
		TimeSeriesDataServiceResponse resp1 = new TimeSeriesDataServiceResponse();
		resp1.setTimeRange(null);
		resp1.setApprovals(new ArrayList<>());
		resp1.setGapTolerances(new ArrayList<>());
		resp1.setGrades(new ArrayList<>());
		resp1.setNotes(new ArrayList<>());
		resp1.setInterpolationTypes(new ArrayList<>());
		resp1.setQualifiers(new ArrayList<>());
		resp1.setPoints(new ArrayList<>());

		given(dataService.get(eq("test1"), any(UvHydroRequestParameters.class), any(ZoneOffset.class), eq(false), eq(false), eq(true), eq(null))).willReturn(
			resp1
		);
		given(paramService.isVolumetricFlow(any(Map.class), any(String.class))).willReturn(false);

		UvHydrographTimeSeries result = service.getTimeSeriesData(params, new HashMap<>(), desc1, false, false);
		assertNull(result.getStartTime());
		assertNull(result.getEndTime());
		assertTrue(result.getPoints().isEmpty());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getTimeSeriesDataNoTimeRangeTest() {
		UvHydroRequestParameters params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		TimeSeriesDescription desc1 = new TimeSeriesDescription();
		desc1.setParameter("param1");
		desc1.setUniqueId("test1");
		desc1.setUtcOffset(0.0D);
		desc1.setDescription("test1-desc");
		desc1.setUnit("test1-unit");
		TimeSeriesPoint point1A = new TimeSeriesPoint();
		point1A.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false));
		point1A.setValue(new DoubleWithDisplay().setNumeric(1.0D).setDisplay("1"));
		TimeSeriesPoint point1B = new TimeSeriesPoint();
		point1B.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false));
		point1B.setValue(new DoubleWithDisplay().setNumeric(null).setDisplay("2"));
		TimeSeriesDataServiceResponse resp1 = new TimeSeriesDataServiceResponse();
		resp1.setTimeRange(null);
		resp1.setApprovals(new ArrayList<>());
		resp1.setGapTolerances(new ArrayList<>());
		resp1.setGrades(new ArrayList<>());
		resp1.setNotes(new ArrayList<>());
		resp1.setInterpolationTypes(new ArrayList<>());
		resp1.setQualifiers(new ArrayList<>());
		resp1.setPoints(new ArrayList<>(Arrays.asList(point1A, point1B)));

		given(dataService.get(eq("test1"), any(UvHydroRequestParameters.class), any(ZoneOffset.class), eq(false), eq(false), eq(true), eq(null))).willReturn(
			resp1
		);
		given(paramService.isVolumetricFlow(any(Map.class), any(String.class))).willReturn(false);

		// Expect error test
		try {
			service.getTimeSeriesData(params, new HashMap<>(), desc1, false, false);
			fail("Expected AquariusRetrievalException when points returned with no time range, but got no exception.");
		} catch (AquariusRetrievalException e) {

		} catch (Exception e) {
			fail("Expected AquariusRetrievalException when points returned with no time range, but got " + e.getClass().getName() + ".");
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getTimeSeriesDataCorrectedTest() {
		Qualifier q1 = new Qualifier();
		q1.setStartTime(Instant.parse("2018-01-01T00:00:00Z"));
		q1.setEndTime(Instant.parse("2018-02-01T00:00:00Z"));
		q1.setIdentifier(TimeSeriesUtils.ESTIMATED_QUALIFIER_VALUE);
		DataGap gap1 = new DataGap();
		UvHydroRequestParameters params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		TimeSeriesPoint point1A = new TimeSeriesPoint();
		point1A.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false));
		point1A.setValue(new DoubleWithDisplay().setNumeric(1.0D).setDisplay("1"));
		TimeSeriesPoint point1B = new TimeSeriesPoint();
		point1B.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false));
		point1B.setValue(new DoubleWithDisplay().setNumeric(null).setDisplay("2"));
		TimeSeriesDescription desc1 = new TimeSeriesDescription();
		desc1.setParameter(GroundWaterParameter.Wat_LVL_BLSD.getDisplayName());
		desc1.setUniqueId("test1");
		desc1.setUtcOffset(0.0D);
		desc1.setDescription("test1-desc");
		desc1.setUnit("test1-unit");
		TimeSeriesDescription desc2 = new TimeSeriesDescription();
		desc2.setParameter("test2-param");
		desc2.setUniqueId("test2");
		desc2.setUtcOffset(0.0D);
		desc2.setDescription("test2-desc");
		desc2.setUnit("test2-unit");
		TimeSeriesDataServiceResponse resp1 = new TimeSeriesDataServiceResponse();
		resp1.setTimeRange(new StatisticalTimeRange()
			.setStartTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
			.setEndTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
		);
		resp1.setApprovals(new ArrayList<>());
		resp1.setGapTolerances(new ArrayList<>());
		resp1.setGrades(new ArrayList<>());
		resp1.setNotes(new ArrayList<>());
		resp1.setInterpolationTypes(new ArrayList<>());
		resp1.setQualifiers(new ArrayList<>());
		resp1.setPoints(new ArrayList<>(Arrays.asList(point1A,point1B)));
		TimeSeriesDataServiceResponse resp2 = new TimeSeriesDataServiceResponse();
		resp2.setTimeRange(new StatisticalTimeRange()
			.setStartTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
			.setEndTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T24:00:00Z")).setRepresentsEndOfTimePeriod(true))
		);
		resp2.setApprovals(new ArrayList<>());
		resp2.setGapTolerances(new ArrayList<>());
		resp2.setGrades(new ArrayList<>());
		resp2.setNotes(new ArrayList<>());
		resp2.setInterpolationTypes(new ArrayList<>());
		resp2.setQualifiers(new ArrayList<>(Arrays.asList(q1)));
		resp2.setPoints(new ArrayList<>());

		given(dataService.get(eq("test1"), any(UvHydroRequestParameters.class), any(ZoneOffset.class), eq(false), eq(false), eq(true), eq(null))).willReturn(
			resp1
		);
		given(dataService.get(eq("test2"), any(UvHydroRequestParameters.class), any(ZoneOffset.class), eq(true), eq(false), eq(true), eq(null))).willReturn(
			resp2
		);
		given(paramService.isVolumetricFlow(any(Map.class), any(String.class))).willReturn(false);
		given(gapsService.buildGapList(any(List.class), eq(false), any(ZoneOffset.class))).willReturn(Arrays.asList(gap1));
		given(gapsService.buildGapList(any(List.class), eq(true), any(ZoneOffset.class))).willReturn(new ArrayList<>());

		UvHydrographTimeSeries result = service.getTimeSeriesData(params, new HashMap<>(), desc1, false, false);
		assertEquals(result.getGaps().size(), 1);
		assertEquals(result.getPoints().size(), 1);
		assertEquals(result.getDescription(), desc1.getDescription());
		assertEquals(result.getType(), desc1.getParameter());
		assertEquals(result.getUnit(), desc1.getUnit());
		assertEquals(result.getStartTime(), resp1.getTimeRange().getStartTime().DateTimeOffset);
		assertEquals(result.getEndTime(), resp1.getTimeRange().getEndTime().DateTimeOffset);
		assertEquals(result.getApprovals(), resp1.getApprovals());
		assertEquals(result.getGapTolerances(), resp1.getGapTolerances());
		assertEquals(result.getGrades(), resp1.getGrades());
		assertEquals(result.getNotes(), resp1.getNotes());
		assertEquals(result.getInterpolationTypes(), resp1.getInterpolationTypes());
		assertEquals(result.getQualifiers(), resp1.getQualifiers());
		assertEquals(result.getEstimatedPeriods(), new ArrayList<>());
		assertEquals(result.isVolumetricFlow(), false);
		assertEquals(result.isInverted(), true);
		
		result = service.getTimeSeriesData(params, new HashMap<>(), desc2, false, true);
		assertEquals(result.getGaps().size(), 0);
		assertEquals(result.getPoints().size(), 0);
		assertEquals(result.getDescription(), desc2.getDescription());
		assertEquals(result.getType(), desc2.getParameter());
		assertEquals(result.getUnit(), desc2.getUnit());
		assertNull(result.getStartTime());
		assertNull(result.getEndTime());
		assertEquals(result.getApprovals(), resp2.getApprovals());
		assertEquals(result.getGapTolerances(), resp2.getGapTolerances());
		assertEquals(result.getGrades(), resp2.getGrades());
		assertEquals(result.getNotes(), resp2.getNotes());
		assertEquals(result.getInterpolationTypes(), resp2.getInterpolationTypes());
		assertEquals(result.getQualifiers(), resp2.getQualifiers());
		assertEquals(result.getEstimatedPeriods().size(), 1);
		assertEquals(result.isVolumetricFlow(), false);
		assertEquals(result.isInverted(), false);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getTimeSeriesDataRawTest() {
		Qualifier q1 = new Qualifier();
		q1.setStartTime(Instant.parse("2018-01-01T00:00:00Z"));
		q1.setEndTime(Instant.parse("2018-02-01T00:00:00Z"));
		q1.setIdentifier(TimeSeriesUtils.ESTIMATED_QUALIFIER_VALUE);
		UvHydroRequestParameters params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		TimeSeriesPoint point1A = new TimeSeriesPoint();
		point1A.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false));
		point1A.setValue(new DoubleWithDisplay().setNumeric(1.0D).setDisplay("1"));
		TimeSeriesPoint point1B = new TimeSeriesPoint();
		point1B.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false));
		point1B.setValue(new DoubleWithDisplay().setNumeric(null).setDisplay("2"));
		TimeSeriesDescription desc1 = new TimeSeriesDescription();
		desc1.setParameter(GroundWaterParameter.Wat_LVL_BLSD.getDisplayName());
		desc1.setUniqueId("test1");
		desc1.setUtcOffset(0.0D);
		desc1.setDescription("test1-desc");
		desc1.setUnit("test1-unit");
		TimeSeriesDescription desc2 = new TimeSeriesDescription();
		desc2.setParameter("test2-param");
		desc2.setUniqueId("test2");
		desc2.setUtcOffset(0.0D);
		desc2.setDescription("test2-desc");
		desc2.setUnit("test2-unit");
		TimeSeriesDataServiceResponse resp1 = new TimeSeriesDataServiceResponse();
		resp1.setTimeRange(new StatisticalTimeRange()
			.setStartTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
			.setEndTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
		);
		resp1.setApprovals(new ArrayList<>());
		resp1.setGapTolerances(new ArrayList<>());
		resp1.setGrades(new ArrayList<>());
		resp1.setNotes(new ArrayList<>());
		resp1.setInterpolationTypes(new ArrayList<>());
		resp1.setQualifiers(new ArrayList<>());
		resp1.setPoints(new ArrayList<>(Arrays.asList(point1A,point1B)));
		TimeSeriesDataServiceResponse resp2 = new TimeSeriesDataServiceResponse();
		resp2.setTimeRange(new StatisticalTimeRange()
			.setStartTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
			.setEndTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T24:00:00Z")).setRepresentsEndOfTimePeriod(true))
		);
		resp2.setApprovals(new ArrayList<>());
		resp2.setGapTolerances(new ArrayList<>());
		resp2.setGrades(new ArrayList<>());
		resp2.setNotes(new ArrayList<>());
		resp2.setInterpolationTypes(new ArrayList<>());
		resp2.setQualifiers(new ArrayList<>(Arrays.asList(q1)));
		resp2.setPoints(new ArrayList<>());

		given(dataService.get(eq("test1"), any(UvHydroRequestParameters.class), any(ZoneOffset.class), eq(false), eq(true), eq(true), eq(null))).willReturn(
			resp1
		);
		given(dataService.get(eq("test2"), any(UvHydroRequestParameters.class), any(ZoneOffset.class), eq(true), eq(true), eq(true), eq(null))).willReturn(
			resp2
		);
		given(paramService.isVolumetricFlow(any(Map.class), any(String.class))).willReturn(false);
		given(gapsService.buildGapList(any(List.class), any(Boolean.class), any(ZoneOffset.class))).willThrow(new RuntimeException("Should not have been called"));

		UvHydrographTimeSeries result = service.getTimeSeriesData(params, new HashMap<>(), desc1, true, false);
		assertEquals(result.getGaps().size(), 0);
		assertEquals(result.getPoints().size(), 1);
		assertEquals(result.getDescription(), desc1.getDescription());
		assertEquals(result.getType(), desc1.getParameter());
		assertEquals(result.getUnit(), desc1.getUnit());
		assertEquals(result.getStartTime(), AqcuTimeUtils.getTemporal(resp1.getTimeRange().getStartTime(), false, ZoneOffset.UTC));
		assertEquals(result.getEndTime(), AqcuTimeUtils.getTemporal(resp1.getTimeRange().getEndTime(), false, ZoneOffset.UTC));
		assertEquals(result.getApprovals(), resp1.getApprovals());
		assertEquals(result.getGapTolerances(), resp1.getGapTolerances());
		assertEquals(result.getGrades(), resp1.getGrades());
		assertEquals(result.getNotes(), resp1.getNotes());
		assertEquals(result.getInterpolationTypes(), resp1.getInterpolationTypes());
		assertEquals(result.getQualifiers(), resp1.getQualifiers());
		assertEquals(result.getEstimatedPeriods(), new ArrayList<>());
		assertEquals(result.isVolumetricFlow(), false);
		assertEquals(result.isInverted(), true);

		result = service.getTimeSeriesData(params, new HashMap<>(), desc2, true, true);
		assertEquals(result.getGaps().size(), 0);
		assertEquals(result.getPoints().size(), 0);
		assertEquals(result.getDescription(), desc2.getDescription());
		assertEquals(result.getType(), desc2.getParameter());
		assertEquals(result.getUnit(), desc2.getUnit());
		assertEquals(result.getApprovals(), resp2.getApprovals());
		assertEquals(result.getGapTolerances(), resp2.getGapTolerances());
		assertEquals(result.getGrades(), resp2.getGrades());
		assertEquals(result.getNotes(), resp2.getNotes());
		assertEquals(result.getInterpolationTypes(), resp2.getInterpolationTypes());
		assertEquals(result.getQualifiers(), resp2.getQualifiers());
		assertEquals(result.getEstimatedPeriods().size(), 1);
		assertEquals(result.isVolumetricFlow(), false);
		assertEquals(result.isInverted(), false);
		assertNull(result.getStartTime());
		assertNull(result.getEndTime());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void buildBaseReportTest() {
		Qualifier q1 = new Qualifier();
		q1.setStartTime(Instant.parse("2018-01-01T00:00:00Z"));
		q1.setEndTime(Instant.parse("2018-02-01T00:00:00Z"));
		q1.setIdentifier(TimeSeriesUtils.ESTIMATED_QUALIFIER_VALUE);
		TimeSeriesPoint point1A = new TimeSeriesPoint();
		point1A.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false));
		point1A.setValue(new DoubleWithDisplay().setNumeric(1.0D).setDisplay("1"));
		TimeSeriesPoint point1B = new TimeSeriesPoint();
		point1B.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false));
		point1B.setValue(new DoubleWithDisplay().setNumeric(null).setDisplay("2"));
		TimeSeriesDescription desc1 = new TimeSeriesDescription();
		desc1.setParameter(GroundWaterParameter.Wat_LVL_BLSD.getDisplayName());
		desc1.setUniqueId("test1");
		desc1.setUtcOffset(0.0D);
		desc1.setDescription("test1-desc");
		desc1.setUnit("test1-unit");
		desc1.setIdentifier("test1-identifier");
		desc1.setLocationIdentifier("test1-loc");
		TimeSeriesDescription desc2 = new TimeSeriesDescription();
		desc2.setParameter("test2-param");
		desc2.setUniqueId("test2");
		desc2.setUtcOffset(0.0D);
		desc2.setDescription("test2-desc");
		desc2.setUnit("test2-unit");
		desc2.setIdentifier("test2-identifier");
		desc2.setLocationIdentifier("test2-loc");
		TimeSeriesDataServiceResponse resp1 = new TimeSeriesDataServiceResponse();
		resp1.setTimeRange(new StatisticalTimeRange()
			.setStartTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
			.setEndTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
		);
		resp1.setApprovals(new ArrayList<>());
		resp1.setGapTolerances(new ArrayList<>());
		resp1.setGrades(new ArrayList<>());
		resp1.setNotes(new ArrayList<>());
		resp1.setInterpolationTypes(new ArrayList<>());
		resp1.setQualifiers(new ArrayList<>());
		resp1.setPoints(new ArrayList<>(Arrays.asList(point1A,point1B)));
		TimeSeriesDataServiceResponse resp2 = new TimeSeriesDataServiceResponse();
		resp2.setTimeRange(new StatisticalTimeRange()
			.setStartTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
			.setEndTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T24:00:00Z")).setRepresentsEndOfTimePeriod(true))
		);
		resp2.setApprovals(new ArrayList<>());
		resp2.setGapTolerances(new ArrayList<>());
		resp2.setGrades(new ArrayList<>());
		resp2.setNotes(new ArrayList<>());
		resp2.setInterpolationTypes(new ArrayList<>());
		resp2.setQualifiers(new ArrayList<>(Arrays.asList(q1)));
		resp2.setPoints(new ArrayList<>());		
		HashMap<String, TimeSeriesDescription> tsMetadata = new HashMap<>();
		tsMetadata.put(desc1.getUniqueId(), desc1);
		tsMetadata.put(desc2.getUniqueId(), desc2);
		ReflectionTestUtils.setField(service, "simsUrl", "www.test.org");
		given(dataService.get(eq("test1"), any(UvHydroRequestParameters.class), any(ZoneOffset.class), eq(false), anyBoolean(), eq(true), eq(null))).willReturn(
			resp1
		);
		given(dataService.get(eq("test2"), any(UvHydroRequestParameters.class), any(ZoneOffset.class), eq(true), anyBoolean(), eq(true), eq(null))).willReturn(
			resp2
		);
		given(paramService.isVolumetricFlow(any(Map.class), any(String.class))).willReturn(false);
		given(gapsService.buildGapList(any(List.class), anyBoolean(), any(ZoneOffset.class))).willReturn(new ArrayList<>());
		given(locationService.getByLocationIdentifier(any(String.class))).willReturn(primaryLoc);
		given(curvesService.getAqcuFilteredRatingCurves(any(List.class), any(Instant.class), any(Instant.class))).willReturn(new ArrayList<>());
		given(curvesService.getAqcuFilteredRatingShifts(any(List.class), any(Instant.class), any(Instant.class))).willReturn(new ArrayList<>());
		given(curvesService.getRawResponse(any(String.class), any(Double.class), any(Instant.class), any(Instant.class))).willReturn(
			new RatingCurveListServiceResponse().setRatingCurves(new ArrayList<>())
		);
		given(fieldVisitDescriptionService.getDescriptions(any(String.class), any(ZoneOffset.class), any(UvHydroRequestParameters.class))).willReturn(
			new ArrayList<>()
		);
		given(fieldVisitDataService.get(any(String.class))).willReturn(
			new FieldVisitDataServiceResponse()
		);
		given(fieldVisitMeasurementsBuilderService.extractFieldVisitMeasurements(any(FieldVisitDataServiceResponse.class), any(String.class))).willReturn(
			new ArrayList<>()
		);
		given(fieldVisitReadingsBuilderService.extractReadings(eq(null), any(FieldVisitDataServiceResponse.class), any(String.class), any(List.class))).willReturn(
			new ArrayList<>()
		);

		UvHydroRequestParameters params;
		UvHydroReport result;

		params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setPrimaryTimeseriesIdentifier(desc1.getUniqueId());
		params.setReferenceTimeseriesIdentifier(desc1.getUniqueId());
		params.setComparisonTimeseriesIdentifier(desc1.getUniqueId());
		params.setUpchainTimeseriesIdentifier(desc1.getUniqueId());
		params.setFirstStatDerivedIdentifier(desc2.getUniqueId());
		params.setSecondStatDerivedIdentifier(desc2.getUniqueId());
		params.setThirdStatDerivedIdentifier(desc2.getUniqueId());
		params.setFourthStatDerivedIdentifier(desc2.getUniqueId());
		params.setPrimaryRatingModelIdentifier("test");
		params.setReferenceRatingModelIdentifier("test");

		result = service.buildBaseReport(params, new HashMap<>(), tsMetadata, new ArrayList<>(), UvHydrographType.GW, REQUESTING_USER);

		assertNotNull(result.getPrimarySeries());
		assertNotNull(result.getPrimarySeriesRaw());
		assertNotNull(result.getFirstStatDerived());
		assertNotNull(result.getSecondStatDerived());
		assertNotNull(result.getThirdStatDerived());
		assertNotNull(result.getFourthStatDerived());
		assertNotNull(result.getReferenceSeries());
		assertNotNull(result.getComparisonSeries());
		assertNotNull(result.getRatingShifts());
		assertNotNull(result.getPrimaryReadings());
		assertNotNull(result.getSimsUrl());
		assertNotNull(result.getReportMetadata());
		assertNull(result.getGwlevel());
		assertNull(result.getWaterQuality());
		assertNull(result.getUpchainSeries());
		assertNull(result.getUpchainSeriesRaw());
		assertNull(result.getPrimarySeriesCorrections());
		assertNull(result.getUpchainSeriesCorrections());
		assertNull(result.getUpchainReadings());
		assertNull(result.getFieldVisitMeasurements());
		assertNull(result.getEffectiveShifts());
		assertNotNull(result.getReportMetadata().getPrimaryParameter());
		assertNotNull(result.getReportMetadata().getReferenceParameter());
		assertNotNull(result.getReportMetadata().getComparisonParameter());
		assertNotNull(result.getReportMetadata().getFirstStatDerivedLabel());
		assertNotNull(result.getReportMetadata().getSecondStatDerivedLabel());
		assertNotNull(result.getReportMetadata().getThirdStatDerviedLabel());
		assertNotNull(result.getReportMetadata().getFourthStatDerviedLabel());
		assertNotNull(result.getReportMetadata().getStationId());
		assertNotNull(result.getReportMetadata().getStationName());
		assertNotNull(result.getReportMetadata().getComparisonStationId());
		assertNull(result.getReportMetadata().getUpchainParameter());
		assertEquals(result.getPrimarySeries().getName(), desc1.getUniqueId());
		assertEquals(result.getReferenceSeries().getName(), desc1.getUniqueId());
		assertEquals(result.getComparisonSeries().getName(), desc1.getUniqueId());
		assertEquals(result.getFirstStatDerived().getName(), desc2.getUniqueId());
		assertEquals(result.getSecondStatDerived().getName(), desc2.getUniqueId());
		assertEquals(result.getThirdStatDerived().getName(), desc2.getUniqueId());
		assertEquals(result.getFourthStatDerived().getName(), desc2.getUniqueId());
		assertEquals(result.getReportMetadata().getPrimaryParameter(), desc1.getIdentifier());
		assertEquals(result.getReportMetadata().getReferenceParameter(), desc1.getIdentifier());
		assertEquals(result.getReportMetadata().getComparisonParameter(), desc1.getIdentifier());
		assertEquals(result.getReportMetadata().getFirstStatDerivedLabel(), desc2.getIdentifier());
		assertEquals(result.getReportMetadata().getSecondStatDerivedLabel(), desc2.getIdentifier());
		assertEquals(result.getReportMetadata().getThirdStatDerviedLabel(), desc2.getIdentifier());
		assertEquals(result.getReportMetadata().getFourthStatDerviedLabel(), desc2.getIdentifier());
		assertEquals(result.getReportMetadata().getStationId(), desc1.getLocationIdentifier());
		assertEquals(result.getReportMetadata().getStationName(), primaryLoc.getName());
		assertEquals(result.getReportMetadata().getComparisonStationId(), desc1.getLocationIdentifier());

		params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setPrimaryTimeseriesIdentifier(desc1.getUniqueId());

		result = service.buildBaseReport(params, new HashMap<>(), tsMetadata, new ArrayList<>(), UvHydrographType.GW, REQUESTING_USER);

		assertNotNull(result.getPrimarySeries());
		assertNotNull(result.getPrimarySeriesRaw());
		assertNotNull(result.getPrimaryReadings());
		assertNotNull(result.getSimsUrl());
		assertNotNull(result.getReportMetadata());
		assertNull(result.getFirstStatDerived());
		assertNull(result.getSecondStatDerived());
		assertNull(result.getThirdStatDerived());
		assertNull(result.getFourthStatDerived());
		assertNull(result.getReferenceSeries());
		assertNull(result.getComparisonSeries());
		assertNull(result.getRatingShifts());
		assertNull(result.getGwlevel());
		assertNull(result.getWaterQuality());
		assertNull(result.getUpchainSeries());
		assertNull(result.getUpchainSeriesRaw());
		assertNull(result.getPrimarySeriesCorrections());
		assertNull(result.getUpchainSeriesCorrections());
		assertNull(result.getUpchainReadings());
		assertNull(result.getFieldVisitMeasurements());
		assertNull(result.getEffectiveShifts());
		assertNotNull(result.getReportMetadata().getPrimaryParameter());
		assertNull(result.getReportMetadata().getReferenceParameter());
		assertNull(result.getReportMetadata().getComparisonParameter());
		assertNull(result.getReportMetadata().getFirstStatDerivedLabel());
		assertNull(result.getReportMetadata().getSecondStatDerivedLabel());
		assertNull(result.getReportMetadata().getThirdStatDerviedLabel());
		assertNull(result.getReportMetadata().getFourthStatDerviedLabel());
		assertNotNull(result.getReportMetadata().getStationId());
		assertNotNull(result.getReportMetadata().getStationName());
		assertNull(result.getReportMetadata().getComparisonStationId());
		assertNull(result.getReportMetadata().getUpchainParameter());
		assertEquals(result.getPrimarySeries().getName(), desc1.getUniqueId());
		assertEquals(result.getReportMetadata().getPrimaryParameter(), desc1.getIdentifier());
		assertEquals(result.getReportMetadata().getStationId(), desc1.getLocationIdentifier());
		assertEquals(result.getReportMetadata().getStationName(), primaryLoc.getName());

		params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setPrimaryTimeseriesIdentifier(desc1.getUniqueId());
		params.setPrimaryRatingModelIdentifier("primary-rating-1");
		params.setReferenceTimeseriesIdentifier(desc1.getUniqueId());
		params.setReferenceRatingModelIdentifier("ref-rating-1");

		result = service.buildBaseReport(params, new HashMap<>(), tsMetadata, new ArrayList<>(), UvHydrographType.GW, REQUESTING_USER);
		assertNotNull(result.getRatingShifts());
		verify(curvesService).getRawResponse(eq("primary-rating-1"), any(Double.class), any(Instant.class), any(Instant.class));
		verify(curvesService, never()).getRawResponse(eq("ref-rating-1"), any(Double.class), any(Instant.class), any(Instant.class));

		params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setPrimaryTimeseriesIdentifier(desc1.getUniqueId());
		params.setReferenceTimeseriesIdentifier(desc1.getUniqueId());
		params.setPrimaryRatingModelIdentifier("primary-rating-2");

		result = service.buildBaseReport(params, new HashMap<>(), tsMetadata, new ArrayList<>(), UvHydrographType.GW, REQUESTING_USER);
		assertNotNull(result.getRatingShifts());
		verify(curvesService).getRawResponse(eq("primary-rating-2"), any(Double.class), any(Instant.class), any(Instant.class));

		params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setPrimaryTimeseriesIdentifier(desc1.getUniqueId());
		params.setReferenceTimeseriesIdentifier(desc1.getUniqueId());
		params.setReferenceRatingModelIdentifier("ref-rating-2");

		result = service.buildBaseReport(params, new HashMap<>(), tsMetadata, new ArrayList<>(), UvHydrographType.GW, REQUESTING_USER);
		assertNotNull(result.getRatingShifts());
		verify(curvesService).getRawResponse(eq("ref-rating-2"), any(Double.class), any(Instant.class), any(Instant.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void buildQWReportTest() {
		Qualifier q1 = new Qualifier();
		q1.setStartTime(Instant.parse("2018-01-01T00:00:00Z"));
		q1.setEndTime(Instant.parse("2018-02-01T00:00:00Z"));
		q1.setIdentifier(TimeSeriesUtils.ESTIMATED_QUALIFIER_VALUE);
		TimeSeriesPoint point1A = new TimeSeriesPoint();
		point1A.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false));
		point1A.setValue(new DoubleWithDisplay().setNumeric(1.0D).setDisplay("1"));
		TimeSeriesPoint point1B = new TimeSeriesPoint();
		point1B.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false));
		point1B.setValue(new DoubleWithDisplay().setNumeric(null).setDisplay("2"));
		TimeSeriesDescription desc1 = new TimeSeriesDescription();
		desc1.setParameter(GroundWaterParameter.Wat_LVL_BLSD.getDisplayName());
		desc1.setUniqueId("test1");
		desc1.setUtcOffset(0.0D);
		desc1.setDescription("test1-desc");
		desc1.setUnit("test1-unit");
		desc1.setIdentifier("test1-identifier");
		desc1.setLocationIdentifier("test1-loc");
		TimeSeriesDescription desc2 = new TimeSeriesDescription();
		desc2.setParameter("test2-param");
		desc2.setUniqueId("test2");
		desc2.setUtcOffset(0.0D);
		desc2.setDescription("test2-desc");
		desc2.setUnit("test2-unit");
		desc2.setIdentifier("test2-identifier");
		desc2.setLocationIdentifier("test2-loc");
		TimeSeriesDataServiceResponse resp1 = new TimeSeriesDataServiceResponse();
		resp1.setTimeRange(new StatisticalTimeRange()
			.setStartTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
			.setEndTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
		);
		resp1.setApprovals(new ArrayList<>());
		resp1.setGapTolerances(new ArrayList<>());
		resp1.setGrades(new ArrayList<>());
		resp1.setNotes(new ArrayList<>());
		resp1.setInterpolationTypes(new ArrayList<>());
		resp1.setQualifiers(new ArrayList<>());
		resp1.setPoints(new ArrayList<>(Arrays.asList(point1A,point1B)));
		TimeSeriesDataServiceResponse resp2 = new TimeSeriesDataServiceResponse();
		resp2.setTimeRange(new StatisticalTimeRange()
			.setStartTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
			.setEndTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T24:00:00Z")).setRepresentsEndOfTimePeriod(true))
		);
		resp2.setApprovals(new ArrayList<>());
		resp2.setGapTolerances(new ArrayList<>());
		resp2.setGrades(new ArrayList<>());
		resp2.setNotes(new ArrayList<>());
		resp2.setInterpolationTypes(new ArrayList<>());
		resp2.setQualifiers(new ArrayList<>(Arrays.asList(q1)));
		resp2.setPoints(new ArrayList<>());		
		HashMap<String, TimeSeriesDescription> tsMetadata = new HashMap<>();
		tsMetadata.put(desc1.getUniqueId(), desc1);
		tsMetadata.put(desc2.getUniqueId(), desc2);
		ReflectionTestUtils.setField(service, "simsUrl", "www.test.org");
		
		given(dataService.get(eq("test1"), any(UvHydroRequestParameters.class), any(ZoneOffset.class), eq(false), anyBoolean(), eq(true), eq(null))).willReturn(
			resp1
		);
		given(dataService.get(eq("test2"), any(UvHydroRequestParameters.class), any(ZoneOffset.class), eq(true), anyBoolean(), eq(true), eq(null))).willReturn(
			resp2
		);
		given(paramService.isVolumetricFlow(any(Map.class), any(String.class))).willReturn(false);
		given(gapsService.buildGapList(any(List.class), anyBoolean(), any(ZoneOffset.class))).willReturn(new ArrayList<>());
		given(locationService.getByLocationIdentifier(any(String.class))).willReturn(primaryLoc);
		given(curvesService.getAqcuFilteredRatingCurves(any(List.class), any(Instant.class), any(Instant.class))).willReturn(new ArrayList<>());
		given(curvesService.getAqcuFilteredRatingShifts(any(List.class), any(Instant.class), any(Instant.class))).willReturn(new ArrayList<>());
		given(curvesService.getRawResponse(any(String.class), any(Double.class), any(Instant.class), any(Instant.class))).willReturn(
			new RatingCurveListServiceResponse().setRatingCurves(new ArrayList<>())
		);
		given(fieldVisitDescriptionService.getDescriptions(any(String.class), any(ZoneOffset.class), any(UvHydroRequestParameters.class))).willReturn(
			new ArrayList<>()
		);
		given(fieldVisitDataService.get(any(String.class))).willReturn(
			new FieldVisitDataServiceResponse()
		);
		given(fieldVisitMeasurementsBuilderService.extractFieldVisitMeasurements(any(FieldVisitDataServiceResponse.class), any(String.class))).willReturn(
			new ArrayList<>()
		);
		given(fieldVisitReadingsBuilderService.extractReadings(eq(null), any(FieldVisitDataServiceResponse.class), any(String.class), any(List.class))).willReturn(
			new ArrayList<>()
		);
		given(corrService.getExtendedCorrectionList(any(String.class), any(Instant.class), any(Instant.class), any(List.class))).willReturn(
			new ArrayList<>(Arrays.asList(new ExtendedCorrection()))
		);
		given(effectiveService.get(any(String.class), any(String.class), any(Instant.class), any(Instant.class))).willReturn(
			new ArrayList<>(Arrays.asList(new EffectiveShift().setTimestamp(Instant.parse("2018-01-01T00:00:00Z")).setValue(1.0D)))
		);
		given(nwisraService.getQwData(any(UvHydroRequestParameters.class), any(String.class), any(String.class), any(ZoneOffset.class))).willReturn(
			new ArrayList<>(Arrays.asList(new WaterQualitySampleRecord()))
		);

		UvHydroRequestParameters params;
		UvHydroReport result;

		params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setPrimaryTimeseriesIdentifier(desc1.getUniqueId());
		params.setReferenceTimeseriesIdentifier(desc1.getUniqueId());
		params.setPrimaryRatingModelIdentifier("primary-rating");
		params.setReferenceRatingModelIdentifier("ref-rating");

		result = service.buildQWReport(params, new HashMap<>(), tsMetadata, "pcode", REQUESTING_USER);
		
		assertNotNull(result.getPrimarySeries());
		assertNotNull(result.getPrimarySeriesRaw());
		assertNotNull(result.getReferenceSeries());
		assertNotNull(result.getPrimaryReadings());
		assertNotNull(result.getSimsUrl());
		assertNotNull(result.getReportMetadata());
		assertNull(result.getFirstStatDerived());
		assertNull(result.getSecondStatDerived());
		assertNull(result.getThirdStatDerived());
		assertNull(result.getFourthStatDerived());
		assertNull(result.getComparisonSeries());
		assertNotNull(result.getRatingShifts());
		assertNull(result.getGwlevel());
		assertNotNull(result.getWaterQuality());
		assertNull(result.getUpchainSeries());
		assertNull(result.getUpchainSeriesRaw());
		assertNotNull(result.getPrimarySeriesCorrections());
		assertNull(result.getUpchainSeriesCorrections());
		assertNull(result.getUpchainReadings());
		assertNull(result.getFieldVisitMeasurements());
		assertNotNull(result.getEffectiveShifts());
		assertNotNull(result.getReportMetadata().getPrimaryParameter());
		assertNotNull(result.getReportMetadata().getReferenceParameter());
		assertNull(result.getReportMetadata().getComparisonParameter());
		assertNull(result.getReportMetadata().getFirstStatDerivedLabel());
		assertNull(result.getReportMetadata().getSecondStatDerivedLabel());
		assertNull(result.getReportMetadata().getThirdStatDerviedLabel());
		assertNull(result.getReportMetadata().getFourthStatDerviedLabel());
		assertNotNull(result.getReportMetadata().getStationId());
		assertNotNull(result.getReportMetadata().getStationName());
		assertNull(result.getReportMetadata().getComparisonStationId());
		assertNull(result.getReportMetadata().getUpchainParameter());
		assertEquals(result.getPrimarySeries().getName(), desc1.getUniqueId());
		assertEquals(result.getReferenceSeries().getName(), desc1.getUniqueId());
		assertEquals(result.getReportMetadata().getPrimaryParameter(), desc1.getIdentifier());
		assertEquals(result.getReportMetadata().getStationId(), desc1.getLocationIdentifier());
		assertEquals(result.getReportMetadata().getStationName(), primaryLoc.getName());
		assertEquals(result.getPrimarySeriesCorrections().size(), 1);
		assertEquals(result.getEffectiveShifts().getPoints().size(), 1);
		assertEquals(result.getWaterQuality().size(), 1);

		params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setPrimaryTimeseriesIdentifier(desc1.getUniqueId());
		params.setReferenceTimeseriesIdentifier(desc1.getUniqueId());
		params.setExcludeDiscrete(true);

		result = service.buildQWReport(params, new HashMap<>(), tsMetadata, "pcode", REQUESTING_USER);
		
		assertNotNull(result.getPrimarySeries());
		assertNotNull(result.getPrimarySeriesRaw());
		assertNotNull(result.getReferenceSeries());
		assertNotNull(result.getPrimaryReadings());
		assertNotNull(result.getSimsUrl());
		assertNotNull(result.getReportMetadata());
		assertNull(result.getFirstStatDerived());
		assertNull(result.getSecondStatDerived());
		assertNull(result.getThirdStatDerived());
		assertNull(result.getFourthStatDerived());
		assertNull(result.getComparisonSeries());
		assertNull(result.getRatingShifts());
		assertNull(result.getGwlevel());
		assertNull(result.getWaterQuality());
		assertNull(result.getUpchainSeries());
		assertNull(result.getUpchainSeriesRaw());
		assertNotNull(result.getPrimarySeriesCorrections());
		assertNull(result.getUpchainSeriesCorrections());
		assertNull(result.getUpchainReadings());
		assertNull(result.getFieldVisitMeasurements());
		assertNull(result.getEffectiveShifts());
		assertNotNull(result.getReportMetadata().getPrimaryParameter());
		assertNotNull(result.getReportMetadata().getReferenceParameter());
		assertNull(result.getReportMetadata().getComparisonParameter());
		assertNull(result.getReportMetadata().getFirstStatDerivedLabel());
		assertNull(result.getReportMetadata().getSecondStatDerivedLabel());
		assertNull(result.getReportMetadata().getThirdStatDerviedLabel());
		assertNull(result.getReportMetadata().getFourthStatDerviedLabel());
		assertNotNull(result.getReportMetadata().getStationId());
		assertNotNull(result.getReportMetadata().getStationName());
		assertNull(result.getReportMetadata().getComparisonStationId());
		assertNull(result.getReportMetadata().getUpchainParameter());
		assertEquals(result.getPrimarySeries().getName(), desc1.getUniqueId());
		assertEquals(result.getReferenceSeries().getName(), desc1.getUniqueId());
		assertEquals(result.getReportMetadata().getPrimaryParameter(), desc1.getIdentifier());
		assertEquals(result.getReportMetadata().getStationId(), desc1.getLocationIdentifier());
		assertEquals(result.getReportMetadata().getStationName(), primaryLoc.getName());
		assertEquals(result.getPrimarySeriesCorrections().size(), 1);

		params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setPrimaryTimeseriesIdentifier(desc1.getUniqueId());
		params.setPrimaryRatingModelIdentifier("primary-rating-1");
		params.setReferenceTimeseriesIdentifier(desc1.getUniqueId());
		params.setReferenceRatingModelIdentifier("ref-rating-1");

		result = service.buildQWReport(params, new HashMap<>(), tsMetadata, "pcode", REQUESTING_USER);
		assertNotNull(result.getEffectiveShifts());
		verify(effectiveService, never()).get(any(String.class), eq("primary-rating-1"), any(Instant.class), any(Instant.class));
		verify(effectiveService).get(any(String.class), eq("ref-rating-1"), any(Instant.class), any(Instant.class));

		params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setPrimaryTimeseriesIdentifier(desc1.getUniqueId());
		params.setReferenceTimeseriesIdentifier(desc1.getUniqueId());
		params.setPrimaryRatingModelIdentifier("primary-rating-2");

		result = service.buildQWReport(params, new HashMap<>(), tsMetadata, "pcode", REQUESTING_USER);
		assertNotNull(result.getEffectiveShifts());
		verify(effectiveService).get(any(String.class), eq("primary-rating-2"), any(Instant.class), any(Instant.class));

		params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setPrimaryTimeseriesIdentifier(desc1.getUniqueId());
		params.setReferenceTimeseriesIdentifier(desc1.getUniqueId());
		params.setReferenceRatingModelIdentifier("ref-rating-2");

		result = service.buildQWReport(params, new HashMap<>(), tsMetadata, "pcode", REQUESTING_USER);
		assertNotNull(result.getEffectiveShifts());
		verify(effectiveService).get(any(String.class), eq("ref-rating-2"), any(Instant.class), any(Instant.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void buildGWReportTest() {
		Qualifier q1 = new Qualifier();
		q1.setStartTime(Instant.parse("2018-01-01T00:00:00Z"));
		q1.setEndTime(Instant.parse("2018-02-01T00:00:00Z"));
		q1.setIdentifier(TimeSeriesUtils.ESTIMATED_QUALIFIER_VALUE);
		TimeSeriesPoint point1A = new TimeSeriesPoint();
		point1A.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false));
		point1A.setValue(new DoubleWithDisplay().setNumeric(1.0D).setDisplay("1"));
		TimeSeriesPoint point1B = new TimeSeriesPoint();
		point1B.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false));
		point1B.setValue(new DoubleWithDisplay().setNumeric(null).setDisplay("2"));
		TimeSeriesDescription desc1 = new TimeSeriesDescription();
		desc1.setParameter(GroundWaterParameter.Wat_LVL_BLSD.getDisplayName());
		desc1.setUniqueId("test1");
		desc1.setUtcOffset(0.0D);
		desc1.setDescription("test1-desc");
		desc1.setUnit("test1-unit");
		desc1.setIdentifier("test1-identifier");
		desc1.setLocationIdentifier("test1-loc");
		TimeSeriesDescription desc2 = new TimeSeriesDescription();
		desc2.setParameter("test2-param");
		desc2.setUniqueId("test2");
		desc2.setUtcOffset(0.0D);
		desc2.setDescription("test2-desc");
		desc2.setUnit("test2-unit");
		desc2.setIdentifier("test2-identifier");
		desc2.setLocationIdentifier("test2-loc");
		TimeSeriesDataServiceResponse resp1 = new TimeSeriesDataServiceResponse();
		resp1.setTimeRange(new StatisticalTimeRange()
			.setStartTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
			.setEndTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
		);
		resp1.setApprovals(new ArrayList<>());
		resp1.setGapTolerances(new ArrayList<>());
		resp1.setGrades(new ArrayList<>());
		resp1.setNotes(new ArrayList<>());
		resp1.setInterpolationTypes(new ArrayList<>());
		resp1.setQualifiers(new ArrayList<>());
		resp1.setPoints(new ArrayList<>(Arrays.asList(point1A,point1B)));
		TimeSeriesDataServiceResponse resp2 = new TimeSeriesDataServiceResponse();
		resp2.setTimeRange(new StatisticalTimeRange()
			.setStartTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
			.setEndTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T24:00:00Z")).setRepresentsEndOfTimePeriod(true))
		);
		resp2.setApprovals(new ArrayList<>());
		resp2.setGapTolerances(new ArrayList<>());
		resp2.setGrades(new ArrayList<>());
		resp2.setNotes(new ArrayList<>());
		resp2.setInterpolationTypes(new ArrayList<>());
		resp2.setQualifiers(new ArrayList<>(Arrays.asList(q1)));
		resp2.setPoints(new ArrayList<>());		
		HashMap<String, TimeSeriesDescription> tsMetadata = new HashMap<>();
		tsMetadata.put(desc1.getUniqueId(), desc1);
		tsMetadata.put(desc2.getUniqueId(), desc2);
		ReflectionTestUtils.setField(service, "simsUrl", "www.test.org");
		WaterLevelRecords records = new WaterLevelRecords();
		records.setRecords(Arrays.asList(new WaterLevelRecord()));
		given(dataService.get(eq("test1"), any(UvHydroRequestParameters.class), any(ZoneOffset.class), eq(false), anyBoolean(), eq(true), eq(null))).willReturn(
			resp1
		);
		given(dataService.get(eq("test2"), any(UvHydroRequestParameters.class), any(ZoneOffset.class), eq(true), anyBoolean(), eq(true), eq(null))).willReturn(
			resp2
		);
		given(paramService.isVolumetricFlow(any(Map.class), any(String.class))).willReturn(false);
		given(gapsService.buildGapList(any(List.class), anyBoolean(), any(ZoneOffset.class))).willReturn(new ArrayList<>());
		given(locationService.getByLocationIdentifier(any(String.class))).willReturn(primaryLoc);
		given(curvesService.getAqcuFilteredRatingCurves(any(List.class), any(Instant.class), any(Instant.class))).willReturn(new ArrayList<>());
		given(curvesService.getAqcuFilteredRatingShifts(any(List.class), any(Instant.class), any(Instant.class))).willReturn(new ArrayList<>());
		given(curvesService.getRawResponse(any(String.class), any(Double.class), any(Instant.class), any(Instant.class))).willReturn(
			new RatingCurveListServiceResponse().setRatingCurves(new ArrayList<>())
		);
		given(fieldVisitDescriptionService.getDescriptions(any(String.class), any(ZoneOffset.class), any(UvHydroRequestParameters.class))).willReturn(
			new ArrayList<>()
		);
		given(fieldVisitDataService.get(any(String.class))).willReturn(
			new FieldVisitDataServiceResponse()
		);
		given(fieldVisitMeasurementsBuilderService.extractFieldVisitMeasurements(any(FieldVisitDataServiceResponse.class), any(String.class))).willReturn(
			new ArrayList<>()
		);
		given(fieldVisitReadingsBuilderService.extractReadings(eq(null), any(FieldVisitDataServiceResponse.class), any(String.class), any(List.class))).willReturn(
			new ArrayList<>()
		);
		given(corrService.getExtendedCorrectionList(any(String.class), any(Instant.class), any(Instant.class), any(List.class))).willReturn(
			new ArrayList<>(Arrays.asList(new ExtendedCorrection()))
		);
		given(effectiveService.get(any(String.class), any(String.class), any(Instant.class), any(Instant.class))).willReturn(
			new ArrayList<>(Arrays.asList(new EffectiveShift().setTimestamp(Instant.parse("2018-01-01T00:00:00Z")).setValue(1.0D)))
		);
		given(nwisraService.getGwLevels(any(UvHydroRequestParameters.class), any(String.class), any(GroundWaterParameter.class), any(ZoneOffset.class))).willReturn(
			records
		);

		UvHydroRequestParameters params;
		UvHydroReport result;

		params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setPrimaryTimeseriesIdentifier(desc1.getUniqueId());
		params.setReferenceTimeseriesIdentifier(desc1.getUniqueId());
		params.setPrimaryRatingModelIdentifier("primary-rating");
		params.setReferenceRatingModelIdentifier("ref-rating");

		result = service.buildGWReport(params, new HashMap<>(), tsMetadata, GroundWaterParameter.Wat_LVL_BLSD, REQUESTING_USER);
		
		assertNotNull(result.getPrimarySeries());
		assertNotNull(result.getPrimarySeriesRaw());
		assertNotNull(result.getReferenceSeries());
		assertNotNull(result.getPrimaryReadings());
		assertNotNull(result.getSimsUrl());
		assertNotNull(result.getReportMetadata());
		assertNull(result.getFirstStatDerived());
		assertNull(result.getSecondStatDerived());
		assertNull(result.getThirdStatDerived());
		assertNull(result.getFourthStatDerived());
		assertNull(result.getComparisonSeries());
		assertNotNull(result.getRatingShifts());
		assertNotNull(result.getGwlevel());
		assertNull(result.getWaterQuality());
		assertNull(result.getUpchainSeries());
		assertNull(result.getUpchainSeriesRaw());
		assertNotNull(result.getPrimarySeriesCorrections());
		assertNull(result.getUpchainSeriesCorrections());
		assertNull(result.getUpchainReadings());
		assertNull(result.getFieldVisitMeasurements());
		assertNull(result.getEffectiveShifts());
		assertNotNull(result.getReportMetadata().getPrimaryParameter());
		assertNotNull(result.getReportMetadata().getReferenceParameter());
		assertNull(result.getReportMetadata().getComparisonParameter());
		assertNull(result.getReportMetadata().getFirstStatDerivedLabel());
		assertNull(result.getReportMetadata().getSecondStatDerivedLabel());
		assertNull(result.getReportMetadata().getThirdStatDerviedLabel());
		assertNull(result.getReportMetadata().getFourthStatDerviedLabel());
		assertNotNull(result.getReportMetadata().getStationId());
		assertNotNull(result.getReportMetadata().getStationName());
		assertNull(result.getReportMetadata().getComparisonStationId());
		assertNull(result.getReportMetadata().getUpchainParameter());
		assertEquals(result.getPrimarySeries().getName(), desc1.getUniqueId());
		assertEquals(result.getReferenceSeries().getName(), desc1.getUniqueId());
		assertEquals(result.getReportMetadata().getPrimaryParameter(), desc1.getIdentifier());
		assertEquals(result.getReportMetadata().getStationId(), desc1.getLocationIdentifier());
		assertEquals(result.getReportMetadata().getStationName(), primaryLoc.getName());
		assertEquals(result.getPrimarySeriesCorrections().size(), 1);
		assertEquals(result.getGwlevel().getRecords().size(), 1);

		params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setPrimaryTimeseriesIdentifier(desc1.getUniqueId());
		params.setReferenceTimeseriesIdentifier(desc1.getUniqueId());
		params.setExcludeDiscrete(true);

		result = service.buildGWReport(params, new HashMap<>(), tsMetadata, GroundWaterParameter.Wat_LVL_BLSD, REQUESTING_USER);
		
		assertNotNull(result.getPrimarySeries());
		assertNotNull(result.getPrimarySeriesRaw());
		assertNotNull(result.getReferenceSeries());
		assertNotNull(result.getPrimaryReadings());
		assertNotNull(result.getSimsUrl());
		assertNotNull(result.getReportMetadata());
		assertNull(result.getFirstStatDerived());
		assertNull(result.getSecondStatDerived());
		assertNull(result.getThirdStatDerived());
		assertNull(result.getFourthStatDerived());
		assertNull(result.getComparisonSeries());
		assertNull(result.getRatingShifts());
		assertNull(result.getGwlevel());
		assertNull(result.getWaterQuality());
		assertNull(result.getUpchainSeries());
		assertNull(result.getUpchainSeriesRaw());
		assertNotNull(result.getPrimarySeriesCorrections());
		assertNull(result.getUpchainSeriesCorrections());
		assertNull(result.getUpchainReadings());
		assertNull(result.getFieldVisitMeasurements());
		assertNull(result.getEffectiveShifts());
		assertNotNull(result.getReportMetadata().getPrimaryParameter());
		assertNotNull(result.getReportMetadata().getReferenceParameter());
		assertNull(result.getReportMetadata().getComparisonParameter());
		assertNull(result.getReportMetadata().getFirstStatDerivedLabel());
		assertNull(result.getReportMetadata().getSecondStatDerivedLabel());
		assertNull(result.getReportMetadata().getThirdStatDerviedLabel());
		assertNull(result.getReportMetadata().getFourthStatDerviedLabel());
		assertNotNull(result.getReportMetadata().getStationId());
		assertNotNull(result.getReportMetadata().getStationName());
		assertNull(result.getReportMetadata().getComparisonStationId());
		assertNull(result.getReportMetadata().getUpchainParameter());
		assertEquals(result.getPrimarySeries().getName(), desc1.getUniqueId());
		assertEquals(result.getReferenceSeries().getName(), desc1.getUniqueId());
		assertEquals(result.getReportMetadata().getPrimaryParameter(), desc1.getIdentifier());
		assertEquals(result.getReportMetadata().getStationId(), desc1.getLocationIdentifier());
		assertEquals(result.getReportMetadata().getStationName(), primaryLoc.getName());
		assertEquals(result.getPrimarySeriesCorrections().size(), 1);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void buildSWReportTest() {
		Qualifier q1 = new Qualifier();
		q1.setStartTime(Instant.parse("2018-01-01T00:00:00Z"));
		q1.setEndTime(Instant.parse("2018-02-01T00:00:00Z"));
		q1.setIdentifier(TimeSeriesUtils.ESTIMATED_QUALIFIER_VALUE);
		TimeSeriesPoint point1A = new TimeSeriesPoint();
		point1A.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false));
		point1A.setValue(new DoubleWithDisplay().setNumeric(1.0D).setDisplay("1"));
		TimeSeriesPoint point1B = new TimeSeriesPoint();
		point1B.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false));
		point1B.setValue(new DoubleWithDisplay().setNumeric(null).setDisplay("2"));
		TimeSeriesDescription desc1 = new TimeSeriesDescription();
		desc1.setParameter(GroundWaterParameter.Wat_LVL_BLSD.getDisplayName());
		desc1.setUniqueId("test1");
		desc1.setUtcOffset(0.0D);
		desc1.setDescription("test1-desc");
		desc1.setUnit("test1-unit");
		desc1.setIdentifier("test1-identifier");
		desc1.setLocationIdentifier("test1-loc");
		TimeSeriesDescription desc2 = new TimeSeriesDescription();
		desc2.setParameter("test2-param");
		desc2.setUniqueId("test2");
		desc2.setUtcOffset(0.0D);
		desc2.setDescription("test2-desc");
		desc2.setUnit("test2-unit");
		desc2.setIdentifier("test2-identifier");
		desc2.setLocationIdentifier("test2-loc");
		TimeSeriesDataServiceResponse resp1 = new TimeSeriesDataServiceResponse();
		resp1.setTimeRange(new StatisticalTimeRange()
			.setStartTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
			.setEndTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
		);
		resp1.setApprovals(new ArrayList<>());
		resp1.setGapTolerances(new ArrayList<>());
		resp1.setGrades(new ArrayList<>());
		resp1.setNotes(new ArrayList<>());
		resp1.setInterpolationTypes(new ArrayList<>());
		resp1.setQualifiers(new ArrayList<>());
		resp1.setPoints(new ArrayList<>(Arrays.asList(point1A,point1B)));
		TimeSeriesDataServiceResponse resp2 = new TimeSeriesDataServiceResponse();
		resp2.setTimeRange(new StatisticalTimeRange()
			.setStartTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
			.setEndTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T24:00:00Z")).setRepresentsEndOfTimePeriod(true))
		);
		resp2.setApprovals(new ArrayList<>());
		resp2.setGapTolerances(new ArrayList<>());
		resp2.setGrades(new ArrayList<>());
		resp2.setNotes(new ArrayList<>());
		resp2.setInterpolationTypes(new ArrayList<>());
		resp2.setQualifiers(new ArrayList<>(Arrays.asList(q1)));
		resp2.setPoints(new ArrayList<>());		
		HashMap<String, TimeSeriesDescription> tsMetadata = new HashMap<>();
		tsMetadata.put(desc1.getUniqueId(), desc1);
		tsMetadata.put(desc2.getUniqueId(), desc2);
		ReflectionTestUtils.setField(service, "simsUrl", "www.test.org");
		
		given(dataService.get(eq("test1"), any(UvHydroRequestParameters.class), any(ZoneOffset.class), eq(false), anyBoolean(), eq(true), eq(null))).willReturn(
			resp1
		);
		given(dataService.get(eq("test2"), any(UvHydroRequestParameters.class), any(ZoneOffset.class), eq(true), anyBoolean(), eq(true), eq(null))).willReturn(
			resp2
		);
		given(paramService.isVolumetricFlow(any(Map.class), any(String.class))).willReturn(false);
		given(gapsService.buildGapList(any(List.class), anyBoolean(), any(ZoneOffset.class))).willReturn(new ArrayList<>());
		given(locationService.getByLocationIdentifier(any(String.class))).willReturn(primaryLoc);
		given(curvesService.getAqcuFilteredRatingCurves(any(List.class), any(Instant.class), any(Instant.class))).willReturn(new ArrayList<>());
		given(curvesService.getAqcuFilteredRatingShifts(any(List.class), any(Instant.class), any(Instant.class))).willReturn(new ArrayList<>());
		given(curvesService.getRawResponse(any(String.class), any(Double.class), any(Instant.class), any(Instant.class))).willReturn(
			new RatingCurveListServiceResponse().setRatingCurves(new ArrayList<>())
		);
		given(fieldVisitDescriptionService.getDescriptions(any(String.class), any(ZoneOffset.class), any(UvHydroRequestParameters.class))).willReturn(
			new ArrayList<>()
		);
		given(fieldVisitDataService.get(any(String.class))).willReturn(
			new FieldVisitDataServiceResponse()
		);
		given(fieldVisitMeasurementsBuilderService.extractFieldVisitMeasurements(any(FieldVisitDataServiceResponse.class), any(String.class))).willReturn(
			new ArrayList<>()
		);
		given(fieldVisitReadingsBuilderService.extractReadings(eq(null), any(FieldVisitDataServiceResponse.class), any(String.class), any(List.class))).willReturn(
			new ArrayList<>()
		);
		given(corrService.getExtendedCorrectionList(any(String.class), any(Instant.class), any(Instant.class), any(List.class))).willReturn(
			new ArrayList<>(Arrays.asList(new ExtendedCorrection()))
		);
		given(effectiveService.get(any(String.class), any(String.class), any(Instant.class), any(Instant.class))).willReturn(
			new ArrayList<>(Arrays.asList(new EffectiveShift().setTimestamp(Instant.parse("2018-01-01T00:00:00Z")).setValue(1.0D)))
		);
		given(nwisraService.getQwData(any(UvHydroRequestParameters.class), any(String.class), any(String.class), any(ZoneOffset.class))).willReturn(
			new ArrayList<>(Arrays.asList(new WaterQualitySampleRecord()))
		);

		UvHydroRequestParameters params;
		UvHydroReport result;

		params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setPrimaryTimeseriesIdentifier(desc1.getUniqueId());
		params.setUpchainTimeseriesIdentifier(desc1.getUniqueId());
		params.setPrimaryRatingModelIdentifier("primary-rating");

		result = service.buildSWReport(params, new HashMap<>(), tsMetadata, REQUESTING_USER);
		
		assertNotNull(result.getPrimarySeries());
		assertNotNull(result.getPrimarySeriesRaw());
		assertNull(result.getReferenceSeries());
		assertNotNull(result.getPrimaryReadings());
		assertNotNull(result.getSimsUrl());
		assertNotNull(result.getReportMetadata());
		assertNull(result.getFirstStatDerived());
		assertNull(result.getSecondStatDerived());
		assertNull(result.getThirdStatDerived());
		assertNull(result.getFourthStatDerived());
		assertNull(result.getComparisonSeries());
		assertNotNull(result.getRatingShifts());
		assertNull(result.getGwlevel());
		assertNull(result.getWaterQuality());
		assertNotNull(result.getUpchainSeries());
		assertNotNull(result.getUpchainSeriesRaw());
		assertNotNull(result.getPrimarySeriesCorrections());
		assertNotNull(result.getUpchainSeriesCorrections());
		assertNotNull(result.getUpchainReadings());
		assertNotNull(result.getFieldVisitMeasurements());
		assertNotNull(result.getEffectiveShifts());
		assertNotNull(result.getReportMetadata().getPrimaryParameter());
		assertNull(result.getReportMetadata().getReferenceParameter());
		assertNull(result.getReportMetadata().getComparisonParameter());
		assertNull(result.getReportMetadata().getFirstStatDerivedLabel());
		assertNull(result.getReportMetadata().getSecondStatDerivedLabel());
		assertNull(result.getReportMetadata().getThirdStatDerviedLabel());
		assertNull(result.getReportMetadata().getFourthStatDerviedLabel());
		assertNotNull(result.getReportMetadata().getStationId());
		assertNotNull(result.getReportMetadata().getStationName());
		assertNull(result.getReportMetadata().getComparisonStationId());
		assertNotNull(result.getReportMetadata().getUpchainParameter());
		assertEquals(result.getPrimarySeries().getName(), desc1.getUniqueId());
		assertEquals(result.getUpchainSeries().getName(), desc1.getUniqueId());
		assertEquals(result.getReportMetadata().getPrimaryParameter(), desc1.getIdentifier());
		assertEquals(result.getReportMetadata().getUpchainParameter(), desc1.getIdentifier());
		assertEquals(result.getReportMetadata().getStationId(), desc1.getLocationIdentifier());
		assertEquals(result.getReportMetadata().getStationName(), primaryLoc.getName());
		assertEquals(result.getPrimarySeriesCorrections().size(), 1);
		assertEquals(result.getUpchainSeriesCorrections().size(), 1);
		assertEquals(result.getEffectiveShifts().getPoints().size(), 1);

		params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setPrimaryTimeseriesIdentifier(desc1.getUniqueId());

		result = service.buildSWReport(params, new HashMap<>(), tsMetadata, REQUESTING_USER);
		
		assertNotNull(result.getPrimarySeries());
		assertNotNull(result.getPrimarySeriesRaw());
		assertNull(result.getReferenceSeries());
		assertNotNull(result.getPrimaryReadings());
		assertNotNull(result.getSimsUrl());
		assertNotNull(result.getReportMetadata());
		assertNull(result.getFirstStatDerived());
		assertNull(result.getSecondStatDerived());
		assertNull(result.getThirdStatDerived());
		assertNull(result.getFourthStatDerived());
		assertNull(result.getComparisonSeries());
		assertNull(result.getRatingShifts());
		assertNull(result.getGwlevel());
		assertNull(result.getWaterQuality());
		assertNull(result.getUpchainSeries());
		assertNull(result.getUpchainSeriesRaw());
		assertNotNull(result.getPrimarySeriesCorrections());
		assertNull(result.getUpchainSeriesCorrections());
		assertNull(result.getUpchainReadings());
		assertNull(result.getFieldVisitMeasurements());
		assertNull(result.getEffectiveShifts());
		assertNotNull(result.getReportMetadata().getPrimaryParameter());
		assertNull(result.getReportMetadata().getReferenceParameter());
		assertNull(result.getReportMetadata().getComparisonParameter());
		assertNull(result.getReportMetadata().getFirstStatDerivedLabel());
		assertNull(result.getReportMetadata().getSecondStatDerivedLabel());
		assertNull(result.getReportMetadata().getThirdStatDerviedLabel());
		assertNull(result.getReportMetadata().getFourthStatDerviedLabel());
		assertNotNull(result.getReportMetadata().getStationId());
		assertNotNull(result.getReportMetadata().getStationName());
		assertNull(result.getReportMetadata().getComparisonStationId());
		assertNull(result.getReportMetadata().getUpchainParameter());
		assertEquals(result.getPrimarySeries().getName(), desc1.getUniqueId());
		assertEquals(result.getReportMetadata().getPrimaryParameter(), desc1.getIdentifier());
		assertEquals(result.getReportMetadata().getStationId(), desc1.getLocationIdentifier());
		assertEquals(result.getReportMetadata().getStationName(), primaryLoc.getName());
		assertEquals(result.getPrimarySeriesCorrections().size(), 1);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void buildDefaultReportTest() {
		Qualifier q1 = new Qualifier();
		q1.setStartTime(Instant.parse("2018-01-01T00:00:00Z"));
		q1.setEndTime(Instant.parse("2018-02-01T00:00:00Z"));
		q1.setIdentifier(TimeSeriesUtils.ESTIMATED_QUALIFIER_VALUE);
		TimeSeriesPoint point1A = new TimeSeriesPoint();
		point1A.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false));
		point1A.setValue(new DoubleWithDisplay().setNumeric(1.0D).setDisplay("1"));
		TimeSeriesPoint point1B = new TimeSeriesPoint();
		point1B.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false));
		point1B.setValue(new DoubleWithDisplay().setNumeric(null).setDisplay("2"));
		TimeSeriesDescription desc1 = new TimeSeriesDescription();
		desc1.setParameter(GroundWaterParameter.Wat_LVL_BLSD.getDisplayName());
		desc1.setUniqueId("test1");
		desc1.setUtcOffset(0.0D);
		desc1.setDescription("test1-desc");
		desc1.setUnit("test1-unit");
		desc1.setIdentifier("test1-identifier");
		desc1.setLocationIdentifier("test1-loc");
		TimeSeriesDescription desc2 = new TimeSeriesDescription();
		desc2.setParameter("test2-param");
		desc2.setUniqueId("test2");
		desc2.setUtcOffset(0.0D);
		desc2.setDescription("test2-desc");
		desc2.setUnit("test2-unit");
		desc2.setIdentifier("test2-identifier");
		desc2.setLocationIdentifier("test2-loc");
		TimeSeriesDataServiceResponse resp1 = new TimeSeriesDataServiceResponse();
		resp1.setTimeRange(new StatisticalTimeRange()
			.setStartTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
			.setEndTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
		);
		resp1.setApprovals(new ArrayList<>());
		resp1.setGapTolerances(new ArrayList<>());
		resp1.setGrades(new ArrayList<>());
		resp1.setNotes(new ArrayList<>());
		resp1.setInterpolationTypes(new ArrayList<>());
		resp1.setQualifiers(new ArrayList<>());
		resp1.setPoints(new ArrayList<>(Arrays.asList(point1A,point1B)));
		TimeSeriesDataServiceResponse resp2 = new TimeSeriesDataServiceResponse();
		resp2.setTimeRange(new StatisticalTimeRange()
			.setStartTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
			.setEndTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T24:00:00Z")).setRepresentsEndOfTimePeriod(true))
		);
		resp2.setApprovals(new ArrayList<>());
		resp2.setGapTolerances(new ArrayList<>());
		resp2.setGrades(new ArrayList<>());
		resp2.setNotes(new ArrayList<>());
		resp2.setInterpolationTypes(new ArrayList<>());
		resp2.setQualifiers(new ArrayList<>(Arrays.asList(q1)));
		resp2.setPoints(new ArrayList<>());		
		HashMap<String, TimeSeriesDescription> tsMetadata = new HashMap<>();
		tsMetadata.put(desc1.getUniqueId(), desc1);
		tsMetadata.put(desc2.getUniqueId(), desc2);
		ReflectionTestUtils.setField(service, "simsUrl", "www.test.org");
		given(dataService.get(eq("test1"), any(UvHydroRequestParameters.class), any(ZoneOffset.class), eq(false), anyBoolean(), eq(true), eq(null))).willReturn(
			resp1
		);
		given(dataService.get(eq("test2"), any(UvHydroRequestParameters.class), any(ZoneOffset.class), eq(true), anyBoolean(), eq(true), eq(null))).willReturn(
			resp2
		);
		given(paramService.isVolumetricFlow(any(Map.class), any(String.class))).willReturn(false);
		given(gapsService.buildGapList(any(List.class), anyBoolean(), any(ZoneOffset.class))).willReturn(new ArrayList<>());
		given(locationService.getByLocationIdentifier(any(String.class))).willReturn(primaryLoc);
		given(curvesService.getAqcuFilteredRatingCurves(any(List.class), any(Instant.class), any(Instant.class))).willReturn(new ArrayList<>());
		given(curvesService.getAqcuFilteredRatingShifts(any(List.class), any(Instant.class), any(Instant.class))).willReturn(new ArrayList<>());
		given(curvesService.getRawResponse(any(String.class), any(Double.class), any(Instant.class), any(Instant.class))).willReturn(
			new RatingCurveListServiceResponse().setRatingCurves(new ArrayList<>())
		);
		given(fieldVisitDescriptionService.getDescriptions(any(String.class), any(ZoneOffset.class), any(UvHydroRequestParameters.class))).willReturn(
			new ArrayList<>()
		);
		given(fieldVisitDataService.get(any(String.class))).willReturn(
			new FieldVisitDataServiceResponse()
		);
		given(fieldVisitMeasurementsBuilderService.extractFieldVisitMeasurements(any(FieldVisitDataServiceResponse.class), any(String.class))).willReturn(
			new ArrayList<>()
		);
		given(fieldVisitReadingsBuilderService.extractReadings(eq(null), any(FieldVisitDataServiceResponse.class), any(String.class), any(List.class))).willReturn(
			new ArrayList<>()
		);

		UvHydroRequestParameters params;
		UvHydroReport result;

		params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setPrimaryTimeseriesIdentifier(desc1.getUniqueId());
		params.setReferenceTimeseriesIdentifier(desc1.getUniqueId());
		params.setComparisonTimeseriesIdentifier(desc1.getUniqueId());
		params.setUpchainTimeseriesIdentifier(desc1.getUniqueId());
		params.setFirstStatDerivedIdentifier(desc2.getUniqueId());
		params.setSecondStatDerivedIdentifier(desc2.getUniqueId());
		params.setThirdStatDerivedIdentifier(desc2.getUniqueId());
		params.setFourthStatDerivedIdentifier(desc2.getUniqueId());
		params.setPrimaryRatingModelIdentifier("test");
		params.setReferenceRatingModelIdentifier("test");

		result = service.buildDefaultReport(params, new HashMap<>(), tsMetadata, REQUESTING_USER);

		assertNotNull(result.getPrimarySeries());
		assertNotNull(result.getPrimarySeriesRaw());
		assertNotNull(result.getFirstStatDerived());
		assertNotNull(result.getSecondStatDerived());
		assertNotNull(result.getThirdStatDerived());
		assertNotNull(result.getFourthStatDerived());
		assertNotNull(result.getReferenceSeries());
		assertNotNull(result.getComparisonSeries());
		assertNotNull(result.getRatingShifts());
		assertNotNull(result.getPrimaryReadings());
		assertNotNull(result.getSimsUrl());
		assertNotNull(result.getReportMetadata());
		assertNull(result.getGwlevel());
		assertNull(result.getWaterQuality());
		assertNull(result.getUpchainSeries());
		assertNull(result.getUpchainSeriesRaw());
		assertNull(result.getPrimarySeriesCorrections());
		assertNull(result.getUpchainSeriesCorrections());
		assertNull(result.getUpchainReadings());
		assertNull(result.getFieldVisitMeasurements());
		assertNull(result.getEffectiveShifts());
		assertNotNull(result.getReportMetadata().getPrimaryParameter());
		assertNotNull(result.getReportMetadata().getReferenceParameter());
		assertNotNull(result.getReportMetadata().getComparisonParameter());
		assertNotNull(result.getReportMetadata().getFirstStatDerivedLabel());
		assertNotNull(result.getReportMetadata().getSecondStatDerivedLabel());
		assertNotNull(result.getReportMetadata().getThirdStatDerviedLabel());
		assertNotNull(result.getReportMetadata().getFourthStatDerviedLabel());
		assertNotNull(result.getReportMetadata().getStationId());
		assertNotNull(result.getReportMetadata().getStationName());
		assertNotNull(result.getReportMetadata().getComparisonStationId());
		assertNull(result.getReportMetadata().getUpchainParameter());
		assertEquals(result.getPrimarySeries().getName(), desc1.getUniqueId());
		assertEquals(result.getReferenceSeries().getName(), desc1.getUniqueId());
		assertEquals(result.getComparisonSeries().getName(), desc1.getUniqueId());
		assertEquals(result.getFirstStatDerived().getName(), desc2.getUniqueId());
		assertEquals(result.getSecondStatDerived().getName(), desc2.getUniqueId());
		assertEquals(result.getThirdStatDerived().getName(), desc2.getUniqueId());
		assertEquals(result.getFourthStatDerived().getName(), desc2.getUniqueId());
		assertEquals(result.getReportMetadata().getPrimaryParameter(), desc1.getIdentifier());
		assertEquals(result.getReportMetadata().getReferenceParameter(), desc1.getIdentifier());
		assertEquals(result.getReportMetadata().getComparisonParameter(), desc1.getIdentifier());
		assertEquals(result.getReportMetadata().getFirstStatDerivedLabel(), desc2.getIdentifier());
		assertEquals(result.getReportMetadata().getSecondStatDerivedLabel(), desc2.getIdentifier());
		assertEquals(result.getReportMetadata().getThirdStatDerviedLabel(), desc2.getIdentifier());
		assertEquals(result.getReportMetadata().getFourthStatDerviedLabel(), desc2.getIdentifier());
		assertEquals(result.getReportMetadata().getStationId(), desc1.getLocationIdentifier());
		assertEquals(result.getReportMetadata().getStationName(), primaryLoc.getName());
		assertEquals(result.getReportMetadata().getComparisonStationId(), desc1.getLocationIdentifier());

		params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setPrimaryTimeseriesIdentifier(desc1.getUniqueId());

		result = service.buildDefaultReport(params, new HashMap<>(), tsMetadata, REQUESTING_USER);

		assertNotNull(result.getPrimarySeries());
		assertNotNull(result.getPrimarySeriesRaw());
		assertNull(result.getPrimaryReadings());
		assertNotNull(result.getSimsUrl());
		assertNotNull(result.getReportMetadata());
		assertNull(result.getFirstStatDerived());
		assertNull(result.getSecondStatDerived());
		assertNull(result.getThirdStatDerived());
		assertNull(result.getFourthStatDerived());
		assertNull(result.getReferenceSeries());
		assertNull(result.getComparisonSeries());
		assertNull(result.getRatingShifts());
		assertNull(result.getGwlevel());
		assertNull(result.getWaterQuality());
		assertNull(result.getUpchainSeries());
		assertNull(result.getUpchainSeriesRaw());
		assertNull(result.getPrimarySeriesCorrections());
		assertNull(result.getUpchainSeriesCorrections());
		assertNull(result.getUpchainReadings());
		assertNull(result.getFieldVisitMeasurements());
		assertNull(result.getEffectiveShifts());
		assertNotNull(result.getReportMetadata().getPrimaryParameter());
		assertNull(result.getReportMetadata().getReferenceParameter());
		assertNull(result.getReportMetadata().getComparisonParameter());
		assertNull(result.getReportMetadata().getFirstStatDerivedLabel());
		assertNull(result.getReportMetadata().getSecondStatDerivedLabel());
		assertNull(result.getReportMetadata().getThirdStatDerviedLabel());
		assertNull(result.getReportMetadata().getFourthStatDerviedLabel());
		assertNotNull(result.getReportMetadata().getStationId());
		assertNotNull(result.getReportMetadata().getStationName());
		assertNull(result.getReportMetadata().getComparisonStationId());
		assertNull(result.getReportMetadata().getUpchainParameter());
		assertEquals(result.getPrimarySeries().getName(), desc1.getUniqueId());
		assertEquals(result.getReportMetadata().getPrimaryParameter(), desc1.getIdentifier());
		assertEquals(result.getReportMetadata().getStationId(), desc1.getLocationIdentifier());
		assertEquals(result.getReportMetadata().getStationName(), primaryLoc.getName());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void buildReportTest() {
		Qualifier q1 = new Qualifier();
		q1.setStartTime(Instant.parse("2018-01-01T00:00:00Z"));
		q1.setEndTime(Instant.parse("2018-02-01T00:00:00Z"));
		q1.setIdentifier(TimeSeriesUtils.ESTIMATED_QUALIFIER_VALUE);
		TimeSeriesPoint point1A = new TimeSeriesPoint();
		point1A.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false));
		point1A.setValue(new DoubleWithDisplay().setNumeric(1.0D).setDisplay("1"));
		TimeSeriesPoint point1B = new TimeSeriesPoint();
		point1B.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false));
		point1B.setValue(new DoubleWithDisplay().setNumeric(null).setDisplay("2"));
		TimeSeriesDescription desc1 = new TimeSeriesDescription();
		desc1.setParameter(GroundWaterParameter.Wat_LVL_BLSD.getDisplayName());
		desc1.setUniqueId("test1");
		desc1.setUtcOffset(0.0D);
		desc1.setDescription("test1-desc");
		desc1.setUnit("test1-unit");
		desc1.setIdentifier("test1-identifier");
		desc1.setLocationIdentifier("test1-loc");
		TimeSeriesDescription desc2 = new TimeSeriesDescription();
		desc2.setParameter("Discharge");
		desc2.setUniqueId("test2");
		desc2.setUtcOffset(0.0D);
		desc2.setDescription("test2-desc");
		desc2.setUnit("test2-unit");
		desc2.setIdentifier("test2-identifier");
		desc2.setLocationIdentifier("test2-loc");
		TimeSeriesDescription desc3 = new TimeSeriesDescription();
		desc3.setParameter("pcode-param");
		desc3.setUniqueId("test3");
		desc3.setUtcOffset(0.0D);
		desc3.setDescription("test3-desc");
		desc3.setUnit("test3-unit");
		desc3.setIdentifier("test3-identifier");
		desc3.setLocationIdentifier("test3-loc");
		TimeSeriesDescription desc4 = new TimeSeriesDescription();
		desc4.setParameter("test4-param");
		desc4.setUniqueId("test4");
		desc4.setUtcOffset(0.0D);
		desc4.setDescription("test4-desc");
		desc4.setUnit("test4-unit");
		desc4.setIdentifier("test4-identifier");
		desc4.setLocationIdentifier("test4-loc");
		TimeSeriesDataServiceResponse resp1 = new TimeSeriesDataServiceResponse();
		resp1.setTimeRange(new StatisticalTimeRange()
			.setStartTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
			.setEndTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
		);
		resp1.setApprovals(new ArrayList<>());
		resp1.setGapTolerances(new ArrayList<>());
		resp1.setGrades(new ArrayList<>());
		resp1.setNotes(new ArrayList<>());
		resp1.setInterpolationTypes(new ArrayList<>());
		resp1.setQualifiers(new ArrayList<>());
		resp1.setPoints(new ArrayList<>(Arrays.asList(point1A,point1B)));
		TimeSeriesDataServiceResponse resp2 = new TimeSeriesDataServiceResponse();
		resp2.setTimeRange(new StatisticalTimeRange()
			.setStartTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-01-01T00:00:00Z")).setRepresentsEndOfTimePeriod(false))
			.setEndTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2018-02-01T24:00:00Z")).setRepresentsEndOfTimePeriod(true))
		);
		resp2.setApprovals(new ArrayList<>());
		resp2.setGapTolerances(new ArrayList<>());
		resp2.setGrades(new ArrayList<>(Arrays.asList(new Grade())));
		resp2.setNotes(new ArrayList<>());
		resp2.setInterpolationTypes(new ArrayList<>());
		resp2.setQualifiers(new ArrayList<>(Arrays.asList(q1)));
		resp2.setPoints(new ArrayList<>());		
		HashMap<String, TimeSeriesDescription> tsMetadata = new HashMap<>();
		tsMetadata.put(desc1.getUniqueId(), desc1);
		tsMetadata.put(desc2.getUniqueId(), desc2);
		tsMetadata.put(desc3.getUniqueId(), desc3);
		tsMetadata.put(desc4.getUniqueId(), desc4);
		ReflectionTestUtils.setField(service, "simsUrl", "www.test.org");
		HashMap<String, QualifierMetadata> qMeta = new HashMap<>();
		qMeta.put("q1", new QualifierMetadata());
		HashMap<String, GradeMetadata> gMeta = new HashMap<>();
		gMeta.put("g1", new GradeMetadata());

		given(descService.getTimeSeriesDescriptionList(any(List.class))).willReturn(Arrays.asList(desc1,desc2,desc3,desc4));
		given(dataService.get(eq("test1"), any(UvHydroRequestParameters.class), any(ZoneOffset.class), anyBoolean(), anyBoolean(), eq(true), eq(null))).willReturn(
			resp1
		);
		given(dataService.get(eq("test2"), any(UvHydroRequestParameters.class), any(ZoneOffset.class), anyBoolean(), anyBoolean(), eq(true), eq(null))).willReturn(
			resp2
		);
		given(dataService.get(eq("test3"), any(UvHydroRequestParameters.class), any(ZoneOffset.class), anyBoolean(), anyBoolean(), eq(true), eq(null))).willReturn(
			resp1
		);
		given(dataService.get(eq("test4"), any(UvHydroRequestParameters.class), any(ZoneOffset.class), anyBoolean(), anyBoolean(), eq(true), eq(null))).willReturn(
			resp2
		);
		given(nwisraService.getNwisPcode(eq("pcode-param"), any(String.class))).willReturn("pcode");
		given(paramService.isVolumetricFlow(any(Map.class), any(String.class))).willReturn(false);
		given(gapsService.buildGapList(any(List.class), anyBoolean(), any(ZoneOffset.class))).willReturn(new ArrayList<>());
		given(locationService.getByLocationIdentifier(any(String.class))).willReturn(primaryLoc);
		given(curvesService.getAqcuFilteredRatingCurves(any(List.class), any(Instant.class), any(Instant.class))).willReturn(new ArrayList<>());
		given(curvesService.getAqcuFilteredRatingShifts(any(List.class), any(Instant.class), any(Instant.class))).willReturn(new ArrayList<>());
		given(curvesService.getRawResponse(any(String.class), any(Double.class), any(Instant.class), any(Instant.class))).willReturn(
			new RatingCurveListServiceResponse().setRatingCurves(new ArrayList<>())
		);
		given(fieldVisitDescriptionService.getDescriptions(any(String.class), any(ZoneOffset.class), any(UvHydroRequestParameters.class))).willReturn(
			new ArrayList<>()
		);
		given(fieldVisitDataService.get(any(String.class))).willReturn(
			new FieldVisitDataServiceResponse()
		);
		given(fieldVisitMeasurementsBuilderService.extractFieldVisitMeasurements(any(FieldVisitDataServiceResponse.class), any(String.class))).willReturn(
			new ArrayList<>()
		);
		given(fieldVisitReadingsBuilderService.extractReadings(eq(null), any(FieldVisitDataServiceResponse.class), any(String.class), any(List.class))).willReturn(
			new ArrayList<>()
		);
		given(qualService.getByQualifierList(any(List.class))).willReturn(
			qMeta
		);
		given(gradeService.getByGradeList(any(List.class))).willReturn(
			gMeta
		);

		UvHydroRequestParameters params;
		UvHydroReport result;

		params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setPrimaryTimeseriesIdentifier(desc1.getUniqueId());

		result = service.buildReport(params, REQUESTING_USER);

		assertEquals(result.getReportMetadata().getUvType(), UvHydrographType.GW);
		assertTrue(result.getReportMetadata().getGradeMetadata().isEmpty());
		assertTrue(result.getReportMetadata().getQualifierMetadata().isEmpty());

		params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setPrimaryTimeseriesIdentifier(desc2.getUniqueId());

		result = service.buildReport(params, REQUESTING_USER);

		assertEquals(result.getReportMetadata().getUvType(), UvHydrographType.SW);
		assertFalse(result.getReportMetadata().getGradeMetadata().isEmpty());
		assertFalse(result.getReportMetadata().getQualifierMetadata().isEmpty());

		params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setPrimaryTimeseriesIdentifier(desc3.getUniqueId());

		result = service.buildReport(params, REQUESTING_USER);

		assertEquals(result.getReportMetadata().getUvType(), UvHydrographType.QW);
		assertTrue(result.getReportMetadata().getGradeMetadata().isEmpty());
		assertTrue(result.getReportMetadata().getQualifierMetadata().isEmpty());

		params = new UvHydroRequestParameters();
		params.setStartDate(LocalDate.parse("2018-01-01"));
		params.setEndDate(LocalDate.parse("2018-02-01"));
		params.setPrimaryTimeseriesIdentifier(desc4.getUniqueId());

		result = service.buildReport(params, REQUESTING_USER);

		assertEquals(result.getReportMetadata().getUvType(), UvHydrographType.DEFAULT);
		assertFalse(result.getReportMetadata().getGradeMetadata().isEmpty());
		assertFalse(result.getReportMetadata().getQualifierMetadata().isEmpty());
	}
}
