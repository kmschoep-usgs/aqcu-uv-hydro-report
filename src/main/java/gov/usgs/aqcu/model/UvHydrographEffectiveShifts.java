package gov.usgs.aqcu.model;

import java.util.ArrayList;
import java.util.List;

public class UvHydrographEffectiveShifts {
    private List<UvHydrographPoint> points;
    private final String name = "Effective Shifts";
    private final String type = "Effective Shifts";
    private Boolean isVolumetricFlow;

    public UvHydrographEffectiveShifts() {
        points = new ArrayList<>();
    }
    
    public void setPoints(List<UvHydrographPoint> points) {
        this.points = points;
    }

    public void isVolumetricFlow(Boolean isVolumetricFlow) {
        this.isVolumetricFlow = isVolumetricFlow;
    }

    public List<UvHydrographPoint> getPoints() {
        return points;
    }

    public Boolean isVolumetricFlow() {
        return isVolumetricFlow;
    }

    public final String getName() {
        return name;
    }

    public final String getType() {
        return type;
    }
}
