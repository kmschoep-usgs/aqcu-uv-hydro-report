package gov.usgs.aqcu.deserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.usgs.aqcu.model.WaterLevelRecord;
import gov.usgs.aqcu.model.nwis.WaterLevelRecords;

/**
 *
 * @author zmoore
 */
public class WaterLevelRecordsDeserializer extends JsonDeserializer<WaterLevelRecords> {
	private static final Logger log = LoggerFactory.getLogger(WaterLevelRecordsDeserializer.class);

	@Override
	public WaterLevelRecords deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		WaterLevelRecords returnObj = new WaterLevelRecords();
		List<WaterLevelRecord> recordList = new ArrayList<WaterLevelRecord>();
		JsonNode listNode = (JsonNode)(p.getCodec().readTree(p)).get("records");

		if(listNode.isArray()){
			for(final JsonNode recordNode : listNode){
				//Skip values that failed to parse
				try {
					ObjectMapper mapper = new ObjectMapper();
					recordList.add(mapper.treeToValue(recordNode, WaterLevelRecord.class));
				} catch(Exception ex) {
					log.debug("Failed to parse WaterLevel record: \n" + recordNode.toString());
					returnObj.setAllValid(false);
				}
			}
		}

		returnObj.setRecords(recordList);
		return returnObj;
	}
}