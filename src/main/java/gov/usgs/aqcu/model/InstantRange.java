package gov.usgs.aqcu.model;

import java.time.Instant;

public class InstantRange {
	private Instant startDate;
	private Instant endDate;

	public InstantRange (Instant startDate, Instant endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public Instant getStartDate() {
		return startDate;
	}
	public void setStartDate(Instant startDate) {
		this.startDate = startDate;
	}
	public Instant getEndDate() {
		return endDate;
	}
	public void setEndDate(Instant endDate) {
		this.endDate = endDate;
	}

}