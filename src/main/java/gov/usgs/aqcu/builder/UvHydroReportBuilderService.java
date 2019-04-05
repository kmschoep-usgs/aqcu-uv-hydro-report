package gov.usgs.aqcu.builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesPoint;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.EffectiveShift;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.FieldVisitDataServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.FieldVisitDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Grade;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.InspectionType;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.ParameterMetadata;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Qualifier;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurve;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingShift;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.aqcu.exception.AquariusRetrievalException;
import gov.usgs.aqcu.model.FieldVisitMeasurement;
import gov.usgs.aqcu.model.UvHydroReport;
import gov.usgs.aqcu.model.UvHydroReportMetadata;
import gov.usgs.aqcu.model.UvHydrographEffectiveShifts;
import gov.usgs.aqcu.model.UvHydrographPoint;
import gov.usgs.aqcu.model.UvHydrographRatingShift;
import gov.usgs.aqcu.model.UvHydrographReading;
import gov.usgs.aqcu.model.UvHydrographTimeSeries;
import gov.usgs.aqcu.model.UvHydrographType;
import gov.usgs.aqcu.model.nwis.GroundWaterParameter;
import gov.usgs.aqcu.parameter.UvHydroRequestParameters;
import gov.usgs.aqcu.retrieval.CorrectionListService;
import gov.usgs.aqcu.retrieval.EffectiveShiftsService;
import gov.usgs.aqcu.retrieval.FieldVisitDataService;
import gov.usgs.aqcu.retrieval.FieldVisitDescriptionService;
import gov.usgs.aqcu.retrieval.GradeLookupService;
import gov.usgs.aqcu.retrieval.LocationDescriptionListService;
import gov.usgs.aqcu.retrieval.NwisRaService;
import gov.usgs.aqcu.retrieval.ParameterListService;
import gov.usgs.aqcu.retrieval.QualifierLookupService;
import gov.usgs.aqcu.retrieval.RatingCurveListService;
import gov.usgs.aqcu.retrieval.TimeSeriesDescriptionListService;
import gov.usgs.aqcu.util.AqcuTimeUtils;
import gov.usgs.aqcu.util.DoubleWithDisplayUtil;
import gov.usgs.aqcu.util.LogExecutionTime;
import gov.usgs.aqcu.util.TimeSeriesUtils;
import gov.usgs.aqcu.util.AqcuReportUtils;
import gov.usgs.aqcu.retrieval.TimeSeriesDataService;

@Service
public class UvHydroReportBuilderService {
	private static final Logger LOG = LoggerFactory.getLogger(UvHydroReportBuilderService.class);
	private static final List<String> includeInspections = Arrays.asList(
		InspectionType.Other.name(), InspectionType.Unknown.name(), InspectionType.BubbleGage.name()
	);

	public static final String DISCHARGE_PARAMETER = "discharge";
    public static final String GAGE_HEIGHT_PARAMETER = "gage height";
	public static final String REPORT_TITLE = "UV Hydrograph";
	public static final String REPORT_TYPE = "uvhydrograph";

	private TimeSeriesDescriptionListService timeSeriesDescriptionListService;
	private LocationDescriptionListService locationDescriptionListService;
	private TimeSeriesDataService timeSeriesDataService;
	private DataGapListBuilderService dataGapListBuilderService;
	private CorrectionListService correctionListService;
	private GradeLookupService gradeLookupService;
	private QualifierLookupService qualifierLookupService;
	private RatingCurveListService ratingCurvesListService;
	private EffectiveShiftsService effectiveShiftsService;
	private FieldVisitDescriptionService fieldVisitDescriptionService;
	private FieldVisitDataService fieldVisitDataService;
	private FieldVisitMeasurementsBuilderService fieldVisitMeasurementsBuilderService;
	private FieldVisitReadingsBuilderService fieldVisitReadingsBuilderService;
	private ParameterListService parameterListService;
	private NwisRaService nwisRaService;

	@Value("${sims.base.url}")
	private String simsUrl;

	@Autowired
	public UvHydroReportBuilderService(
		LocationDescriptionListService locationDescriptionListService,
		TimeSeriesDescriptionListService timeSeriesDescriptionListService,
		TimeSeriesDataService timeSeriesDataService,
		DataGapListBuilderService dataGapListBuilderService,
		CorrectionListService correctionListService,
		GradeLookupService gradeLookupService,
		QualifierLookupService qualifierLookupService,
		RatingCurveListService ratingCurvesListService,
		EffectiveShiftsService effectiveShiftsService,
		FieldVisitDescriptionService fieldVisitDescriptionService,
		FieldVisitDataService fieldVisitDataService,
		FieldVisitMeasurementsBuilderService fieldVisitMeasurementsBuilderService,
		FieldVisitReadingsBuilderService fieldVisitReadingsBuilderService,
		ParameterListService parameterListService,
		NwisRaService nwisRaService) {
		this.locationDescriptionListService = locationDescriptionListService;
		this.timeSeriesDescriptionListService = timeSeriesDescriptionListService;
		this.timeSeriesDataService = timeSeriesDataService;
		this.dataGapListBuilderService = dataGapListBuilderService;
		this.correctionListService = correctionListService;
		this.gradeLookupService = gradeLookupService;
		this.qualifierLookupService = qualifierLookupService;
		this.ratingCurvesListService = ratingCurvesListService;
		this.effectiveShiftsService = effectiveShiftsService;
		this.fieldVisitDescriptionService = fieldVisitDescriptionService;
		this.fieldVisitDataService = fieldVisitDataService;
		this.fieldVisitMeasurementsBuilderService = fieldVisitMeasurementsBuilderService;
		this.fieldVisitReadingsBuilderService = fieldVisitReadingsBuilderService;
		this.parameterListService = parameterListService;
		this.nwisRaService = nwisRaService;
	}

	@LogExecutionTime
	public UvHydroReport buildReport(UvHydroRequestParameters requestParameters, String requestingUser) {
		// Time Series Metadata
		LOG.debug("Retrieve metadata for all requested timeseries");
		Map<String, TimeSeriesDescription> tsMetadata = timeSeriesDescriptionListService.getTimeSeriesDescriptionList(requestParameters.getTsUidList())
			.stream().collect(Collectors.toMap(t -> t.getUniqueId(), t -> t));
		TimeSeriesDescription primaryTsMetadata = tsMetadata.get(requestParameters.getPrimaryTimeseriesIdentifier());

		// Parameter Metadata
		LOG.debug("Get parameter metadata from Aquarius");
		Map<String, ParameterMetadata> parameterMetadata = parameterListService.getParameterMetadata();

		// Determine UV Hydrograph Type
		LOG.debug("Retrieve nwisPcode from NWIS-RA using parameter and unit.");
		String nwisPcode = nwisRaService.getNwisPcode(primaryTsMetadata.getParameter(), primaryTsMetadata.getUnit());
		GroundWaterParameter gwParam = GroundWaterParameter.getByDisplayName(primaryTsMetadata.getParameter());
		LOG.debug("Determine UV Hydro report type by PCode and GWParam");
		UvHydrographType type = determineReportType(primaryTsMetadata, gwParam, nwisPcode);

		// Build Report
		UvHydroReport report;
		if(type == UvHydrographType.SW) {
			report = buildSWReport(requestParameters, parameterMetadata, tsMetadata, requestingUser);
		} else if(type == UvHydrographType.GW) {
			report = buildGWReport(requestParameters, parameterMetadata, tsMetadata, gwParam, requestingUser);
		} else if(type == UvHydrographType.QW) {
			report = buildQWReport(requestParameters, parameterMetadata, tsMetadata, nwisPcode, requestingUser);
		} else {
			report = buildDefaultReport(requestParameters, parameterMetadata, tsMetadata, requestingUser);
		}

		// Add Final Pieces
		LOG.debug("Get Grade and Qualifier Metadata from Aquarius");
		List<Grade> reportGrades = report.getAllGrades();
		List<Qualifier> reportQuals = report.getAllQualifiers();
		if(!reportGrades.isEmpty()) {
			report.getReportMetadata().setGradeMetadata(gradeLookupService.getByGradeList(reportGrades));
		}
		
		if(!reportQuals.isEmpty()) {
			report.getReportMetadata().setQualifierMetadata(qualifierLookupService.getByQualifierList(reportQuals));
		}
		
		return report;
	}

	@LogExecutionTime
	protected UvHydroReport buildBaseReport(UvHydroRequestParameters requestParameters, Map<String, ParameterMetadata> parameterMetadata, 
			Map<String, TimeSeriesDescription> tsMetadata, List<FieldVisitDataServiceResponse> primaryFieldVisitData, 
			UvHydrographType uvType, String requestingUser) 
	{
		UvHydroReport base = new UvHydroReport();

		// TS Data
		LOG.debug("Get time series data (corrected and raw) from Aquarius for primary, reference, comparison, and stat-derived time series");
		TimeSeriesDescription primaryMetadata = tsMetadata.get(requestParameters.getPrimaryTimeseriesIdentifier());
		base.setPrimarySeries(getTimeSeriesData(requestParameters, parameterMetadata, primaryMetadata, false, false));
		base.setPrimarySeriesRaw(getTimeSeriesData(requestParameters, parameterMetadata, primaryMetadata, true, false));
		TimeSeriesDescription firstStatMetadata = tsMetadata.get(requestParameters.getFirstStatDerivedIdentifier());
		base.setFirstStatDerived(getTimeSeriesData(requestParameters, parameterMetadata, firstStatMetadata, false, true));
		TimeSeriesDescription secondStatMetadata = tsMetadata.get(requestParameters.getSecondStatDerivedIdentifier());
		base.setSecondStatDerived(getTimeSeriesData(requestParameters, parameterMetadata, secondStatMetadata, false, true));
		TimeSeriesDescription thirdStatMetadata = tsMetadata.get(requestParameters.getThirdStatDerivedIdentifier());
		base.setThirdStatDerived(getTimeSeriesData(requestParameters, parameterMetadata, thirdStatMetadata, false, true));
		TimeSeriesDescription fourthStatMetadata = tsMetadata.get(requestParameters.getFourthStatDerivedIdentifier());
		base.setFourthStatDerived(getTimeSeriesData(requestParameters, parameterMetadata, fourthStatMetadata, false, true));
		TimeSeriesDescription comparisonMetadata = tsMetadata.get(requestParameters.getComparisonTimeseriesIdentifier());
		base.setComparisonSeries(getTimeSeriesData(requestParameters, parameterMetadata, comparisonMetadata, false, false));
		TimeSeriesDescription referenceMetadata = tsMetadata.get(requestParameters.getReferenceTimeseriesIdentifier());
		base.setReferenceSeries(getTimeSeriesData(requestParameters, parameterMetadata, referenceMetadata, false, false));

		// Rating Shifts
		LOG.debug("Getting rating shifts from Aquarius using the proper rating model identifier");
		if(requestParameters.getPrimaryRatingModelIdentifier() != null && !requestParameters.getPrimaryRatingModelIdentifier().isEmpty()) {
			base.setRatingShifts(getRatingShifts(
				requestParameters, 
				requestParameters.getPrimaryRatingModelIdentifier(), 
				TimeSeriesUtils.getZoneOffset(primaryMetadata)
			));
		} else if(referenceMetadata != null && requestParameters.getReferenceRatingModelIdentifier() != null && !requestParameters.getReferenceRatingModelIdentifier().isEmpty()) {
			base.setRatingShifts(getRatingShifts(
				requestParameters, 
				requestParameters.getReferenceRatingModelIdentifier(), 
				TimeSeriesUtils.getZoneOffset(tsMetadata.get(requestParameters.getReferenceTimeseriesIdentifier()))
			));
		}

		// Location Data
		LOG.debug("Get location metadata");
		LocationDescription primaryLocation = locationDescriptionListService.getByLocationIdentifier(
			primaryMetadata.getLocationIdentifier()
		);

		// Base Report Metadata
		LOG.debug("Parse retrieved metadata");
		base.setReportMetadata(getBaseReportMetadata(requestParameters,
			requestingUser,
			primaryMetadata.getLocationIdentifier(),
			primaryLocation.getName(),
			comparisonMetadata != null ? comparisonMetadata.getLocationIdentifier() : null, 
			uvType,
			primaryMetadata.getIdentifier(),
			primaryMetadata.getUtcOffset()
		));

		// Aditional Metadata
		if(firstStatMetadata != null) {
			base.getReportMetadata().setFirstStatDerivedLabel(firstStatMetadata.getIdentifier());
		}
		if(secondStatMetadata != null) {
			base.getReportMetadata().setSecondStatDerivedLabel(secondStatMetadata.getIdentifier());
		}
		if(thirdStatMetadata != null) {
			base.getReportMetadata().setThirdStatDerviedLabel(thirdStatMetadata.getIdentifier());
		}
		if(fourthStatMetadata != null) {
			base.getReportMetadata().setFourthStatDerviedLabel(fourthStatMetadata.getIdentifier());
		}
		if(referenceMetadata != null) {
			base.getReportMetadata().setReferenceParameter(referenceMetadata.getIdentifier());
		}
		if(comparisonMetadata != null) {
			base.getReportMetadata().setComparisonParameter(comparisonMetadata.getIdentifier());
		}

		// SIMS URL
		base.setSimsUrl(AqcuReportUtils.getSimsUrl(primaryLocation.getIdentifier(), simsUrl));
		
		// Field Visit Readings
		LOG.debug("Get filtered field visit readings from Aquarius");
		if(primaryFieldVisitData != null) {
			base.setPrimaryReadings(getFilteredFieldVisitReadings(primaryFieldVisitData, primaryMetadata.getParameter()));
		}
		
		return base;
	}

	@LogExecutionTime
	protected UvHydroReport buildDefaultReport(UvHydroRequestParameters requestParameters, 
			Map<String, ParameterMetadata> parameterMetadata, Map<String, TimeSeriesDescription> tsMetadata, String requestingUser) 
	{
		ZoneOffset primaryZoneOffset = TimeSeriesUtils.getZoneOffset(tsMetadata.get(requestParameters.getPrimaryTimeseriesIdentifier()));

		// Primary Field Visit Data
		LOG.debug("Retrieve field visit data");
		List<FieldVisitDataServiceResponse> primaryFieldVisitData = null;
		if(requestParameters.getPrimaryRatingModelIdentifier() != null && !requestParameters.getPrimaryRatingModelIdentifier().isEmpty()) {
			primaryFieldVisitData = getFieldVisitData(
				requestParameters,
				tsMetadata.get(requestParameters.getPrimaryTimeseriesIdentifier()).getLocationIdentifier(), 
				primaryZoneOffset
			);
		}

		// Base Report
		return buildBaseReport(requestParameters, parameterMetadata, tsMetadata, primaryFieldVisitData, UvHydrographType.DEFAULT, requestingUser);
	}

	@LogExecutionTime
	protected UvHydroReport buildSWReport(UvHydroRequestParameters requestParameters, 
			Map<String, ParameterMetadata> parameterMetadata, Map<String, TimeSeriesDescription> tsMetadata, String requestingUser) 
	{
		ZoneOffset primaryZoneOffset = TimeSeriesUtils.getZoneOffset(tsMetadata.get(requestParameters.getPrimaryTimeseriesIdentifier()));
		
		// Primary Field Visit Data
		LOG.debug("Retrieve field visit data");
		List<FieldVisitDataServiceResponse> primaryFieldVisitData = null;
		primaryFieldVisitData = getFieldVisitData(
			requestParameters,
			tsMetadata.get(requestParameters.getPrimaryTimeseriesIdentifier()).getLocationIdentifier(), 
			primaryZoneOffset
		);

		// Base Report
		UvHydroReport report = buildBaseReport(requestParameters, parameterMetadata, tsMetadata, primaryFieldVisitData, UvHydrographType.SW, requestingUser);

		// Primary Corrections
		LOG.debug("Retrieve primary series corrections");
		report.setPrimarySeriesCorrections(correctionListService.getExtendedCorrectionList(
			requestParameters.getPrimaryTimeseriesIdentifier(), 
			requestParameters.getStartInstant(primaryZoneOffset), 
			requestParameters.getEndInstant(primaryZoneOffset), 
			requestParameters.getExcludedCorrections()
		));

		// Field Visit Measurements
		LOG.debug("Retrieve field visit measurements");
		if(requestParameters.getPrimaryRatingModelIdentifier() != null && !requestParameters.getPrimaryRatingModelIdentifier().isEmpty()) {
			report.setFieldVisitMeasurements(getFieldVisitMeasurements(primaryFieldVisitData, requestParameters.getPrimaryRatingModelIdentifier()));
		} else if(requestParameters.getReferenceRatingModelIdentifier() != null && !requestParameters.getReferenceRatingModelIdentifier().isEmpty()) {
			report.setFieldVisitMeasurements(getFieldVisitMeasurements(
				getFieldVisitData(
					requestParameters,
					tsMetadata.get(requestParameters.getReferenceTimeseriesIdentifier()).getLocationIdentifier(), 
					TimeSeriesUtils.getZoneOffset(tsMetadata.get(requestParameters.getReferenceTimeseriesIdentifier()))
				),
				requestParameters.getReferenceRatingModelIdentifier()
			));
		}

		// Upchain
		if(requestParameters.getUpchainTimeseriesIdentifier() != null && !requestParameters.getUpchainTimeseriesIdentifier().isEmpty()) {
			TimeSeriesDescription upchainMetadata = tsMetadata.get(requestParameters.getUpchainTimeseriesIdentifier());
			
			// Data
			LOG.debug("Retrieve upchain time series data (raw and corrected)");
			report.setUpchainSeries(getTimeSeriesData(requestParameters, parameterMetadata, upchainMetadata, false, false));
			report.setUpchainSeriesRaw(getTimeSeriesData(requestParameters, parameterMetadata, upchainMetadata, true, false));

			// Metadata
			report.getReportMetadata().setUpchainParameter(upchainMetadata.getIdentifier());

			// Corrections
			LOG.debug("Retrieve upchain series corrections");
			ZoneOffset upchainZoneOffset = TimeSeriesUtils.getZoneOffset(upchainMetadata);
			report.setUpchainSeriesCorrections(correctionListService.getExtendedCorrectionList(
				requestParameters.getUpchainTimeseriesIdentifier(), 
				requestParameters.getStartInstant(upchainZoneOffset), 
				requestParameters.getEndInstant(upchainZoneOffset), 
				requestParameters.getExcludedCorrections()
			));

			// Effective Shits
			LOG.debug("Retrieve effective shifts for the upchain series and primary rating model");
			if(requestParameters.getPrimaryRatingModelIdentifier() != null && !requestParameters.getPrimaryRatingModelIdentifier().isEmpty()) {
				report.setEffectiveShifts(getEffectiveShifts(requestParameters, 
					requestParameters.getUpchainTimeseriesIdentifier(), 
					requestParameters.getPrimaryRatingModelIdentifier(),
					report.getUpchainSeries().isVolumetricFlow(),
					upchainZoneOffset
				));
			}

			// Readings
			LOG.debug("Retrieve filtered upchain field visit readings");
			report.setUpchainReadings(getFilteredFieldVisitReadings(primaryFieldVisitData, upchainMetadata.getParameter()));
		}

		return report;
	}

	@LogExecutionTime
	protected UvHydroReport buildGWReport(UvHydroRequestParameters requestParameters, 
			Map<String, ParameterMetadata> parameterMetadata, Map<String, TimeSeriesDescription> tsMetadata, GroundWaterParameter gwParam, String requestingUser) 
	{
		ZoneOffset primaryZoneOffset = TimeSeriesUtils.getZoneOffset(tsMetadata.get(requestParameters.getPrimaryTimeseriesIdentifier()));
		
		// Primary Field Visit Data
		LOG.debug("Retrieve field visit data");
		List<FieldVisitDataServiceResponse> primaryFieldVisitData = null;
		primaryFieldVisitData = getFieldVisitData(
			requestParameters,
			tsMetadata.get(requestParameters.getPrimaryTimeseriesIdentifier()).getLocationIdentifier(), 
			primaryZoneOffset
		);
		
		// Base Report
		UvHydroReport report = buildBaseReport(requestParameters, parameterMetadata, tsMetadata, primaryFieldVisitData, UvHydrographType.GW, requestingUser);

		// Primary Corrections
		LOG.debug("Retrieve primary series corrections");
		report.setPrimarySeriesCorrections(correctionListService.getExtendedCorrectionList(
			requestParameters.getPrimaryTimeseriesIdentifier(), 
			requestParameters.getStartInstant(primaryZoneOffset), 
			requestParameters.getEndInstant(primaryZoneOffset), 
			requestParameters.getExcludedCorrections()
		));	

		// GW Levels
		if(!requestParameters.isExcludeDiscrete()) {
			LOG.debug("Retrieve Ground Water Levels data from NWIS-RA");
			report.setGwlevel(nwisRaService.getGwLevels(
				requestParameters,
				tsMetadata.get(requestParameters.getPrimaryTimeseriesIdentifier()).getLocationIdentifier(), 
				gwParam, 
				primaryZoneOffset
			));
		}

		return report;
	}

	@LogExecutionTime
	protected UvHydroReport buildQWReport(UvHydroRequestParameters requestParameters, 
			Map<String, ParameterMetadata> parameterMetadata, Map<String, TimeSeriesDescription> tsMetadata, String nwisPcode, String requestingUser) 
	{
		ZoneOffset primaryZoneOffset = TimeSeriesUtils.getZoneOffset(tsMetadata.get(requestParameters.getPrimaryTimeseriesIdentifier()));
		
		// Primary Field Visit Data
		LOG.debug("Retrieve field visit data");
		List<FieldVisitDataServiceResponse> primaryFieldVisitData = null;
		primaryFieldVisitData = getFieldVisitData(
			requestParameters,
			tsMetadata.get(requestParameters.getPrimaryTimeseriesIdentifier()).getLocationIdentifier(), 
			primaryZoneOffset
		);
		
		// Base Report
		UvHydroReport report = buildBaseReport(requestParameters, parameterMetadata, tsMetadata, primaryFieldVisitData, UvHydrographType.QW, requestingUser);

		// Primary Corrections
		LOG.debug("Retrieve primary series corrections");
		report.setPrimarySeriesCorrections(correctionListService.getExtendedCorrectionList(
			requestParameters.getPrimaryTimeseriesIdentifier(), 
			requestParameters.getStartInstant(primaryZoneOffset), 
			requestParameters.getEndInstant(primaryZoneOffset), 
			requestParameters.getExcludedCorrections()
		));

		// Effective Shits
		LOG.debug("Retrieve effective shifts for the refernce series if it exists or the primary series otherwise.");
		if(requestParameters.getReferenceRatingModelIdentifier() != null && !requestParameters.getReferenceTimeseriesIdentifier().isEmpty()) {
			report.setEffectiveShifts(getEffectiveShifts(requestParameters, 
				requestParameters.getReferenceTimeseriesIdentifier(), 
				requestParameters.getReferenceRatingModelIdentifier(),
				report.getReferenceSeries().isVolumetricFlow(),
				TimeSeriesUtils.getZoneOffset(tsMetadata.get(requestParameters.getReferenceTimeseriesIdentifier()))
			));
		} else if(requestParameters.getPrimaryRatingModelIdentifier() != null && !requestParameters.getPrimaryRatingModelIdentifier().isEmpty()) {
			report.setEffectiveShifts(getEffectiveShifts(requestParameters, 
				requestParameters.getPrimaryTimeseriesIdentifier(), 
				requestParameters.getPrimaryRatingModelIdentifier(),
				report.getPrimarySeries().isVolumetricFlow(),
				primaryZoneOffset
			));
		}

		// QW Data
		if(!requestParameters.isExcludeDiscrete()) {
			LOG.debug("Retrieve Water Quality Data from NWIS-RA");
			report.setWaterQuality(nwisRaService.getQwData(
				requestParameters,
				tsMetadata.get(requestParameters.getPrimaryTimeseriesIdentifier()).getLocationIdentifier(), 
				nwisPcode, 
				primaryZoneOffset
			));
		}

		return report;
	}

	protected UvHydrographTimeSeries getTimeSeriesData(UvHydroRequestParameters requestParameters, 
			Map<String, ParameterMetadata> parameterMetadata, TimeSeriesDescription tsDesc, Boolean isRaw, Boolean isDaily) 
	{
		if(tsDesc != null) {
			GroundWaterParameter gwParam = GroundWaterParameter.getByDisplayName(tsDesc.getParameter());
			UvHydrographTimeSeries timeSeries = new UvHydrographTimeSeries(tsDesc.getUniqueId());
			ZoneOffset zoneOffset = TimeSeriesUtils.getZoneOffset(tsDesc);

			// Basic Data
			timeSeries.setDescription(tsDesc.getDescription());
			timeSeries.setType(tsDesc.getParameter());
			timeSeries.setUnits(tsDesc.getUnit());

			// Time Series Data
			TimeSeriesDataServiceResponse dataResponse = timeSeriesDataService.get(tsDesc.getUniqueId(), requestParameters, zoneOffset, isDaily, isRaw, true, null);
			if(dataResponse != null) {
				timeSeries.isVolumetricFlow(parameterListService.isVolumetricFlow(parameterMetadata, dataResponse.getParameter()));
				timeSeries.setInverted(gwParam != null && gwParam.isInverted());
				timeSeries.setApprovals(dataResponse.getApprovals());
				timeSeries.setGapTolerances(dataResponse.getGapTolerances());
				timeSeries.setGrades(dataResponse.getGrades());
				timeSeries.setNotes(dataResponse.getNotes());
				timeSeries.setInterpolationTypes(dataResponse.getInterpolationTypes());
				timeSeries.setQualifiers(dataResponse.getQualifiers());

				if(timeSeries.getQualifiers() != null && !timeSeries.getQualifiers().isEmpty()) {
					timeSeries.setEstimatedPeriods(TimeSeriesUtils.getEstimatedPeriods(dataResponse.getQualifiers()));
				}
				
				// Point Data
				if(dataResponse.getPoints() != null && !dataResponse.getPoints().isEmpty()) {
					// Time Range (will be null if no points returned)
					if (dataResponse.getTimeRange() != null) {
						timeSeries.setStartTime(AqcuTimeUtils.getTemporal(dataResponse.getTimeRange().getStartTime(), isDaily, zoneOffset));
						timeSeries.setEndTime(AqcuTimeUtils.getTemporal(dataResponse.getTimeRange().getEndTime(), isDaily, zoneOffset));
					} else {
						LOG.error("Request for time series data returned points but no time range. Request:" +
							"\nTime Series Unique ID: " + tsDesc.getUniqueId() +
							"\nIs Daily: " + isDaily +
							"\nIs Raw: " + isRaw +
							"\nStart Date: " + requestParameters.getStartInstant(zoneOffset) + 
							"\nEnd Date: " + requestParameters.getEndInstant(zoneOffset)
						);
						throw new AquariusRetrievalException("Request for time series data returned points but no time range.");
					}
					
					timeSeries.setPoints(createUvHydroPointsFromTimeSeries(dataResponse.getPoints(), isDaily, zoneOffset));

					if(!isRaw) {
						timeSeries.setGaps(dataGapListBuilderService.buildGapList(dataResponse.getPoints(), isDaily, zoneOffset));
					} else {
						timeSeries.setGaps(new ArrayList<>());
					}
				} else {
					timeSeries.setPoints(new ArrayList<>());
					timeSeries.setGaps(new ArrayList<>());
				}
			} else {
				LOG.error("Failed to retireve time series data for ID: " + tsDesc.getUniqueId());
				throw new AquariusRetrievalException("Failed to retireve time series data for ID: " + tsDesc.getUniqueId());
			}	

			return timeSeries;
		}
		return null;		
	}

	protected List<UvHydrographPoint> createUvHydroPointsFromTimeSeries(List<TimeSeriesPoint> timeSeriesPoints, Boolean isDaily, ZoneOffset zoneOffset) {
		return timeSeriesPoints.parallelStream()
			.filter(x -> x.getValue().getNumeric() != null)
			.map(x -> new UvHydrographPoint(
				AqcuTimeUtils.getTemporal(x.getTimestamp(), isDaily, zoneOffset), 
				DoubleWithDisplayUtil.getRoundedValue(x.getValue())
			))
			.collect(Collectors.toList());
	}

	protected UvHydrographEffectiveShifts getEffectiveShifts(UvHydroRequestParameters requestParameters, String tsUid, String ratingModelIdentifier, Boolean isVolumetricFlow, ZoneOffset zoneOffset) {
		UvHydrographEffectiveShifts result = new UvHydrographEffectiveShifts();
		List<EffectiveShift> shifts = effectiveShiftsService.get(
			tsUid,
			ratingModelIdentifier,
			requestParameters.getStartInstant(zoneOffset),
			requestParameters.getEndInstant(zoneOffset)
		);
		result.isVolumetricFlow(isVolumetricFlow);
		result.setPoints(createUvHydroPointsFromEffectiveShifts(shifts));
		return result;
	}

	protected List<UvHydrographPoint> createUvHydroPointsFromEffectiveShifts(List<EffectiveShift> effectiveShifts) {
		return effectiveShifts.parallelStream()
			.filter(x -> x.getValue() != null)
			.map(x ->  new UvHydrographPoint(x.getTimestamp(), BigDecimal.valueOf(x.getValue())))
			.collect(Collectors.toList());
	}

	protected List<UvHydrographRatingShift> getRatingShifts(UvHydroRequestParameters requestParameters, String ratingModelIdentifier, ZoneOffset zoneOffset) {
		Instant startDate = requestParameters.getStartInstant(zoneOffset);
		Instant endDate = requestParameters.getEndInstant(zoneOffset);
		List<RatingCurve> ratingCurvesList = ratingCurvesListService.getAqcuFilteredRatingCurves(
			ratingCurvesListService.getRawResponse(ratingModelIdentifier, null, null, null).getRatingCurves(), 
			startDate, 
			endDate
		);
		List<RatingShift> ratingShiftList = ratingCurvesListService.getAqcuFilteredRatingShifts(
			ratingCurvesList,
			startDate,
			endDate
		);

		//Create UV Hydro Rating Shifts
		List<UvHydrographRatingShift> ratingShifts = new ArrayList<>();
		for(RatingShift shift : ratingShiftList) {
			for(RatingCurve curve : ratingCurvesList) {
				if(curve.getShifts() != null && curve.getShifts().contains(shift)) {
					UvHydrographRatingShift newShift = new UvHydrographRatingShift(shift, curve.getId());
					ratingShifts.add(newShift);
					break;
				}
			}
		}

		return ratingShifts;
	}

	protected List<FieldVisitDataServiceResponse> getFieldVisitData(UvHydroRequestParameters requestParameters, String locationIdentifier, ZoneOffset zoneOffset) {
		List<FieldVisitDataServiceResponse> result = new ArrayList<>();
		
		for(FieldVisitDescription desc : fieldVisitDescriptionService.getDescriptions(locationIdentifier, zoneOffset, requestParameters)) {
			result.add(fieldVisitDataService.get(desc.getIdentifier()));
		}

		return result;
	}

	protected List<UvHydrographReading> getFilteredFieldVisitReadings(List<FieldVisitDataServiceResponse> fieldVisitData, String parameter) {
		List<UvHydrographReading> result = new ArrayList<>();

		for(FieldVisitDataServiceResponse response : fieldVisitData) {
			result.addAll(fieldVisitReadingsBuilderService.extractReadings(null, response, parameter, includeInspections).stream()
				.map(reading -> new UvHydrographReading(reading, parameter))
				.collect(Collectors.toList()));
		}

		return result;
	}

	protected List<FieldVisitMeasurement> getFieldVisitMeasurements(List<FieldVisitDataServiceResponse> fieldVisitData, String ratingModelIdentifier) {
		List<FieldVisitMeasurement> result = new ArrayList<>();
		for(FieldVisitDataServiceResponse response : fieldVisitData) {
			result.addAll(fieldVisitMeasurementsBuilderService.extractFieldVisitMeasurements(response, ratingModelIdentifier));
		}
		return result;
	}

	protected UvHydroReportMetadata getBaseReportMetadata(UvHydroRequestParameters requestParameters, String requestingUser, 
			String stationId, String stationName, String comparisonStationId, UvHydrographType type, String primaryParameter, Double utcOffset) 
	{
		UvHydroReportMetadata metadata = new UvHydroReportMetadata();
		metadata.setTitle(REPORT_TITLE);
		metadata.setRequestingUser(requestingUser);
		metadata.setRequestParameters(requestParameters);
		metadata.setPrimaryParameter(primaryParameter);
		metadata.setStationName(stationName);
		metadata.setStationId(stationId);
		metadata.setComparisonStationId(comparisonStationId);
		metadata.setTimezone(utcOffset);
		metadata.setUvType(type);
		
		return metadata;
	}

	protected UvHydrographType determineReportType(TimeSeriesDescription primaryTsMetadata, GroundWaterParameter gwParam, String nwisPcode) {
		if(gwParam != null) {
            return UvHydrographType.GW;
        } else if(
			DISCHARGE_PARAMETER.contentEquals(primaryTsMetadata.getParameter().toLowerCase()) || 
			GAGE_HEIGHT_PARAMETER.contentEquals(primaryTsMetadata.getParameter().toLowerCase())
		) {
            return UvHydrographType.SW;
        } else if(nwisPcode != null && !nwisPcode.isEmpty()) {
            return UvHydrographType.QW;
        } else {
            return UvHydrographType.DEFAULT;
        }
	}
}