package gov.usgs.aqcu.deserializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import gov.usgs.aqcu.model.WaterQualitySampleRecord;
import gov.usgs.aqcu.model.WqValue;
import gov.usgs.aqcu.model.nwis.NwisRaTimeZones;

/**
 * NWIS RA QW records can have a dynamic set of columns. This deserializer assumes one PXXXXX column and one RXXXXX column and
 * will map those values to value and remark respectively. 
 * 
 * @author thongsav
 *
 */
public class WaterQualitySampleRecordDeserializer extends JsonDeserializer<WaterQualitySampleRecord> {
	private static String PCODE_PATTERN = "P[0-9][0-9][0-9][0-9][0-9]";

	@Override
	public WaterQualitySampleRecord deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		WaterQualitySampleRecord wqRecord = new WaterQualitySampleRecord();

		JsonNode node = jp.getCodec().readTree(jp);

		wqRecord.setRecordNumber(node.get("QW_RECORD_NO").asText());
		wqRecord.setMedium(node.get("QW_MEDIUM_NM").asText());
		String timeZone = node.get("QW_SAMPLE_START_TZ_CD").asText();
		if(timeZone != null){
			wqRecord.setTimeZone(timeZone.trim());
		}

		String dateTimeString = node.get("QW_SAMPLE_START_LOCAL_DISP_FM").asText();
		NwisRaTimeZones utcOffset = wqRecord.getTimeZone();

		if(dateTimeString != null && utcOffset != null){
			OffsetDateTime tmpTime = OffsetDateTime.from(DateTimeFormatter.ofPattern("yyyyMMddHHmmX").parse(dateTimeString+utcOffset.toOffsetString()));
			wqRecord.setSampleStartDateTime(tmpTime);
		} else{
			wqRecord.setSampleStartDateTime(null);
		}
		Iterator<String> fields = node.fieldNames();
		while(fields.hasNext()){
			String field = fields.next();
			if(field.matches(PCODE_PATTERN)) {
				String pcode = field.substring(1);
				WqValue val = new WqValue();
				val.setParameter(pcode);
				val.setValue(BigDecimal.valueOf(node.get(field).asDouble()));
				val.setRemark(node.get("R" + pcode).asText());
				wqRecord.setValue(val);
			}
		}
		return wqRecord;
	}

}
