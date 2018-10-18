package gov.usgs.aqcu.model;

import java.math.BigDecimal;

/**
 * AQCU representation of an NWIS-RA WaterQualityValue object
 * 
 * @author thongsav
 */
public class WqValue {
	private String parameter;
	private String remark;
	private BigDecimal value;
	
	/**
	 *
	 * @return The value's parameter
	 */
	public String getParameter() {
		return parameter;
	}

	/**
	 *
	 * @return The value's remark
	 */
	public String getRemark() {
		return remark;
	}

	/**
	 *
	 * @return The numeric value
	 */
	public BigDecimal getValue() {
		return value;
	}

	/**
	 *
	 * @param parameter The parameter to set
	 */
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	/**
	 *
	 * @param remark The remark to set
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}

	/**
	 *
	 * @param value The value to set
	 */
	public void setValue(BigDecimal value) {
		this.value = value;
	}
}
