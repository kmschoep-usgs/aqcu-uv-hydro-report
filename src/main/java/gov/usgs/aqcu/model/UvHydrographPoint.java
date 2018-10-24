package gov.usgs.aqcu.model;

import java.math.BigDecimal;
import java.time.temporal.Temporal;

/** 
 * This class is a substitute for com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesPoint
 * UV Hydrograph requires Points that:
 * a) Have an optional time component to the temporal value;
 * b) Have values expressed as BigDecimal, rather than imprecise Double approximations.
 */
public class UvHydrographPoint {
	private Temporal time;
	private BigDecimal value;

	public UvHydrographPoint(Temporal time, BigDecimal value) {
		setTime(time);
		setValue(value);
	}

	public Temporal getTime() {
		return time;
	}

	public UvHydrographPoint setTime(Temporal time) {
		this.time = time;
		return this;
	}

	public BigDecimal getValue() {
		return value;
	}

	public UvHydrographPoint setValue(BigDecimal value) {
		this.value = value;
		return this;
	}
}
