package gov.usgs.aqcu.deserializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import gov.usgs.aqcu.model.WaterLevelRecord;
import gov.usgs.aqcu.model.nwis.NwisRaTimeZones;

/**
 * Customized JSON Deserializer for water level record JSON
 * 
 * @author dpattermann
 */
public class WaterLevelRecordDeserializer extends JsonDeserializer<WaterLevelRecord>{
	
	@Override
	public WaterLevelRecord deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {

		WaterLevelRecord wlRecord = new WaterLevelRecord();

		JsonNode node = jp.getCodec().readTree(jp);
		wlRecord.setSiteNumber(node.get("SITE_NO").asText());
		wlRecord.setTimeZone(node.get("LEV_TZ_CD").asText().trim());
		//Data from nwisGW comes back as "   ", which is translated to 0.0 in BigDecimal.
		if(node.get("LEV_VA").asText().trim().isEmpty() && node.get("SL_LEV_VA").asText().trim().isEmpty()){
			wlRecord.setGroundWaterLevel(null);
		} else{
			if(node.get("SL_LEV_VA").asText().trim().isEmpty()) {
				wlRecord.setGroundWaterLevel(BigDecimal.valueOf(node.get("LEV_VA").asDouble()));
			}
			else{
				wlRecord.setGroundWaterLevel(BigDecimal.valueOf(node.get("SL_LEV_VA").asDouble()));
			}
		}
		String dateString = node.get("GW_LOCAL_LEV_DT").asText();
		String timeString = node.get("GW_LOCAL_LEV_TM").asText();
		NwisRaTimeZones utcOffset = wlRecord.getTimeZone();

		if(dateString != null && timeString != null && utcOffset != null){
			OffsetDateTime tmpTime = OffsetDateTime.from(DateTimeFormatter.ofPattern("yyyyMMddHHmmX").parse(dateString+timeString+utcOffset.toOffsetString()));
			wlRecord.setDate(tmpTime);
		} else{
			wlRecord.setDate(null);
		}

		return wlRecord;
	}

}
