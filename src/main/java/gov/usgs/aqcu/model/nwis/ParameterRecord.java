package gov.usgs.aqcu.model.nwis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ParameterRecord {

	private String code;
	private String name;
	private String alias;
	private String unit;
	private String description;
	private String medium;

	public String getCode() {
		return code;
	}
	public String getName() {
		return name;
	}
	public String getAlias() {
		return alias;
	}
	public String getUnit() {
		return unit;
	}
	public String getDescription() {
		return description;
	}
	public String getMedium() {
		return medium;
	}
	@JsonProperty("PARM_CD")
	public void setCode(String code) {
		this.code = code;
	}
	@JsonProperty("PARM_NM")
	public void setName(String name) {
		this.name = name;
	}
	@JsonProperty("PARM_ALIAS_NM")
	public void setAlias(String alias) {
		this.alias = alias;
	}
	@JsonProperty("PARM_UNT_TX")
	public void setUnit(String unit) {
		this.unit = unit;
	}
	@JsonProperty("PARM_DS")
	public void setDescription(String description) {
		this.description = description;
	}
	@JsonProperty("PARM_MEDIUM_TX")
	public void setMedium(String medium) {
		this.medium = medium;
	}
}
