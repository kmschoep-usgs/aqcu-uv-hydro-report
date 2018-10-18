package gov.usgs.aqcu.model.nwis;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author dpattermann
 */
public enum NwisRaTimeZones {
	ACST("ACST", 9,30),
	ACSST("ACSST",10,30),
	AEST("AEST", 10,0),
	AESST("AESST", 11,0),
	AFT("AFT", 04,30),
	AKST("AKST", -9,0),
	AKDT("AKDT", -8,0),
	AST("AST", -4,0),
	ADT("ADT", -3,0),
	AWST("AWST", 8,0),
	AWSST("AWSST", 9,0),
	BT("BT", 3,0),
	CAST("CAST", 9,30),
	CADT("CADT", 10,30),
	CCT("CCT", 8,0),
	CET("CET", 1,0),
	CETDST("CETDST", 2,0),
	CST("CST", -6,0),
	CDT("CDT", -5,0),
	DNT("DNT", 1,0),
	DST("DST", 1,0),
	EAST("EAST", 10,0),
	EASST("EASST", 11,0),
	EET("EET", 2,0),
	EETDST("EETDST", 3,0),
	EST("EST", -05,0),
	EDT("EDT", -06,0),
	FST("FST", 01,0),
	FWT("FWT", 02,0),
	GMT("GMT", 00,0),
	BST("BST", 01,0),
	GST("GST", 10,0),
	HST("HST", -10,0),
	HDT("HDT", -9,0),
	IDLE("IDLE", 12,0),
	IDLW("IDLW", -12,0),
	IST("IST", 02,0),
	IT("IT", 03,30),
	JST("JST", 9,0),
	JT("JT", 07,30),
	KST("KST", 9,0),
	LIGT("LIGT", 10,0),
	MET("MET", 01,0),
	METDST("METDST", 02,0),
	MEWT("MEWT", 01,0),
	MEST("MEST", 02,0),
	MEZ("MEZ", 01,0),
	MST("MST", -07,0),
	MDT("MDT", -06,0),
	MT("MT", 8,30),
	NFT("NFT", -03,30),
	NDT("NDT", -02,30),
	NOR("NOR", 01,0),
	NST("NST", -03,30),
	NZST("NZST", 12,0),
	NZDT("NZDT", 13,0),
	NZT("NZT", 12,0),
	PST("PST", -8,0),
	PDT("PDT", -07,0),
	SAT("SAT", 9,30),
	SADT("SADT", 10,30),
	SET("SET", 01,0),
	SWT("SWT", 01,0),
	SST("SST", 02,0),
	UTC("UTC", 00,0),
	WAST("WAST", 07,0),
	WADT("WADT", 8,0),
	WAT("WAT", -01,0),
	WET("WET", 00,0),
	WETDST("WETDST", 01,0),
	WST("WST", 8,0),
	WDT("WDT", 9,0),
	ZPM11("ZP-11", -11,0),
	ZPM2("ZP-2", -02,0),
	ZPM3("ZP-3", -03,0),
	ZP11("ZP+11", 11,0),
	ZP4("ZP+4", 04,0),
	ZP5("ZP+5", 05,0),
	ZP6("ZP+6", 06,0)
	;

	private int utcHoursOffset;
	private int utcMinutesOffset;

	private String timeZoneCode;

	private static Map<String, NwisRaTimeZones> timeZoneMap;
		static {
		timeZoneMap = new HashMap<>();
		for(NwisRaTimeZones z : NwisRaTimeZones.values()) {
			timeZoneMap.put(z.getTimeZoneCode(), z);
		}
	}

	NwisRaTimeZones(String timeZoneCode, int utcHoursOffset, int utcMinutesOffset){
		this.utcHoursOffset = utcHoursOffset;
		this.utcMinutesOffset = utcMinutesOffset;
		this.timeZoneCode = timeZoneCode;
	}

	/**
	 *
	 * @param timeZoneCode The timezone code to search with
	 * @return The found NwisRaTimeZone
	 */
	public static NwisRaTimeZones getByTimeZoneCode(String timeZoneCode){
		return timeZoneMap.get(timeZoneCode);
	}

	/**
	 *
	 * @return The timezone code
	 */
	public String getTimeZoneCode(){
		return timeZoneCode;
	}

	/**
	 *
	 * @return The UTC hours offset
	 */
	public int getUtcHoursOffset() {
		return utcHoursOffset;
	}

	/**
	 *
	 * @return The UTC minutes offset
	 */
	public int getUtcMinutesOffset(){
		return utcMinutesOffset;
	}

	/**
	 *
	 * @return Converts the UTC hours and minutes offset to an offset string
	 */
	public String toOffsetString() {
		String sign = "+";

		if(this.utcHoursOffset < 0) {
			sign = "-";
		}

		String hour = String.valueOf(Math.abs(this.utcHoursOffset));
		if(hour.length() == 1) {
			hour = "0" + hour;
		}

		String minutes = String.valueOf(this.utcMinutesOffset);
		if(minutes.length() == 1) {
			minutes = "0" + minutes;
		}

		return sign + hour + minutes;
	}
}
