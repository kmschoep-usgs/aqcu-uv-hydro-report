package gov.usgs.aqcu.client;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="nwisRa", url="${nwis-ra.service.endpoint}")
public interface NwisRaClient {

	@RequestMapping(method=RequestMethod.GET, value="/data/view/parameters/json", consumes="application/json")
	ResponseEntity<String> getParameters(@RequestParam("parameters.parm_alias_cd") String parameterName);

	@RequestMapping(method=RequestMethod.GET, value="/report/WaterLevels/json", consumes="application/json")
	ResponseEntity<String> getWaterLevelRecords(
			@RequestParam("sitefile.site_no.like.varchar.trim") String siteId,
			@RequestParam("columnGroups") String columnGroups,
			@RequestParam("groundwater.lev_dt.within.partialdate") String dateRange,
			@RequestParam("groundwater.lev_ent_cd") String gwLevEnt,
			@RequestParam("groundwater.sl_datum_cd") String seaLevelDatum);

	@RequestMapping(method=RequestMethod.GET, value="/report/WaterQualityBySample/json", consumes="application/json")
	ResponseEntity<String> getWaterQualitySampleRecords(
			@RequestParam("sitefile.site_no.like.varchar.trim") String siteId,
			@RequestParam("columnGroups") String columnGroups,
			@RequestParam("separateQwRemark") String separateQwRemark,
			@RequestParam("roundQwValues") String roundQwValues,
			@RequestParam("qwPivotPcodes") String qwPivotPcodes,
			@RequestParam("waterquality.parm_cd.recordsWithValuesIn.composite_record_no") String filterPcode,
			@RequestParam("waterquality.sample_start_dt.within.partialdate") String dateRange);

}
