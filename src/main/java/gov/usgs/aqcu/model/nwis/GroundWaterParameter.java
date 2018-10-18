package gov.usgs.aqcu.model.nwis;

import java.util.HashMap;
import java.util.Map;

public enum GroundWaterParameter {

	//Current set
	AQ208("Water level, DTW MP", true, "M", null),
	Wat_LVL_BLSD("WaterLevel, BelowLSD", true, "L", null),
	AQ212("Water level, corr BP", false, "S", "NGVD29"),
	AQ207("WL, inclined, NGVD", false, "S", "NGVD29"),
	AQ176("Elevation above NGVD", false, "S", "NGVD29"),
	AQ211("Water level, NGVD", false, "S", "NGVD29"),
	AQ210("Water level, NAVD", false, "S", "NAVD88"),
	AQ180("GW level, ASVD02", false, "S", "ASVD02"),
	AQ181("GW level, ASVD62", false, "S", "ASVD02"),
	AQ182("GW level, GUVD04", false, "S", "GUVD04"),
	AQ183("GW level, GUVD63", false, "S", "GUVD04"),
	AQ184("GW level, HILOCAL", false, "S", "LMSL"),
	AQ185("GW level, NMVD03", false, "S", "NMVD03"),
	AQ186("GW level, PRVD02", false, "S", "PRVD02"),
	AQ209("Water level, MSL", false, "S", "LMSL"),

	//anticipated set (note the enum name might not be useable)
	FAQ208("Water level, depth MP", true, "M", null),
	FWat_LVL_BLSD("Water level, depth LSD", true, "L", null),
	FAQ212("Elevation, correct BP, NGVD29", false, "S", "NGVD29"),
	FAQ207("Elevation, water, inc, NGVD29", false, "S", "NGVD29"),
	FAQ176("Elevation, NGVD29", false, "S", "NGVD29"),
	FAQ211("Elevation, GW, NGVD29", false, "S", "NGVD29"),
	FAQ210("Elevation, GW, NADV88", false, "S", "NAVD88"),
	FAQ180("Elevation, GW, ASVD02", false, "S", "ASVD02"),
	FAQ181("Elevation, GW, ASVD62", false, "S", "ASVD02"),
	FAQ182("Elevation, GW, GUVD04", false, "S", "GUVD04"),
	FAQ183("Elevation, GW, GUVD63", false, "S", "GUVD04"),
	FAQ184("Elevation, GW, HILOCAL", false, "S", "LMSL"),
	FAQ185("Elevation, GW, NMVD03", false, "S", "NMVD03"),
	FAQ186("Elevation, GW, PRVD02", false, "S", "PRVD02"),
	FAQ209("Elevation, GW, MSL", false, "S", "LMSL");

	private String displayName;
	private boolean inverted;
	private String gwLevEnt;
	private String seaLevDatum;

	private static Map<String, GroundWaterParameter> displayNameMap; 
	static {
		displayNameMap = new HashMap<>();
		for(GroundWaterParameter p : GroundWaterParameter.values()) {
			displayNameMap.put(p.getDisplayName(), p);
		}
	}

	GroundWaterParameter(String displayName, boolean inverted, String gwLevEnt, String seaLevDatum) {
		this.displayName = displayName;
		this.inverted = inverted;
		this.gwLevEnt = gwLevEnt;
		this.seaLevDatum = seaLevDatum;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean isInverted() {
		return inverted;
	}

	public String getGwLevEnt() {
		return gwLevEnt;
	}

	public String getSeaLevDatum() {
		return seaLevDatum;
	}

	public static GroundWaterParameter getByDisplayName(String displayName) {
		return displayNameMap.get(displayName);
	}
}
