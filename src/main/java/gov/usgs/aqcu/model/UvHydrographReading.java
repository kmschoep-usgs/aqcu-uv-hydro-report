package gov.usgs.aqcu.model;

import java.time.temporal.Temporal;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Reading;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.ReadingType;

public class UvHydrographReading {
	private Temporal time;
    private String value;
    private ReadingType type;
    private String uncertainty;
    private String parameter;

    public UvHydrographReading(Reading source) {
        setTime(source.getTime());
        setType(source.getReadingType());
        setParameter(source.getParameter());

        if(source.getUncertainty() != null) {
            setUncertainty(source.getUncertainty().getDisplay());
        }
        
        if(source.getValue() != null) {
            setValue(source.getValue().getDisplay());
        }
    }

	public Temporal getTime() {
		return time;
	}

	public UvHydrographReading setTime(Temporal time) {
		this.time = time;
		return this;
	}

	public String getValue() {
		return value;
	}

	public UvHydrographReading setValue(String value) {
		this.value = value;
		return this;
    }
    
    public ReadingType getType() {
        return type;
    }

    public void setType(ReadingType type) {
        this.type = type;
    }

    public String getUncertainty() {
        return uncertainty;
    }

    public void setUncertainty(String uncertainty) {
        this.uncertainty = uncertainty;
    }
    
    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }
}
