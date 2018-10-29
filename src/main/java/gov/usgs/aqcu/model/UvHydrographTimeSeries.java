package gov.usgs.aqcu.model;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Approval;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GapTolerance;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Grade;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.InterpolationType;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Note;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Qualifier;

public class UvHydrographTimeSeries {
    // List Types
    private List<UvHydrographPoint> points;
    private List<Note> notes;
    private List<DataGap> gaps;
    private List<Qualifier> qualifiers;
    private List<Grade> grades;
    private List<Approval> approvals;
    private List<GapTolerance> gapTolerances;
    private List<InterpolationType> interpolationTypes;
    private List<InstantRange> estimatedPeriods;

    // Additional Data
    private Temporal startTime;
    private Temporal endTime;
    private String name;
    private String type;
    private String unit;
    private String description;
    private Boolean inverted;
    private Boolean isVolumetricFlow;

    public UvHydrographTimeSeries(String tsUid) {
        this.name = tsUid;
        points = new ArrayList<>();
        estimatedPeriods = new ArrayList<>();
    }

    public List<UvHydrographPoint> getPoints() {
        return points;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public List<DataGap> getGaps() {
        return gaps;
    }

    public List<Qualifier> getQualifiers() {
        return qualifiers;
    }

    public List<Grade> getGrades() {
        return grades;
    }

    public List<Approval> getApprovals() {
        return approvals;
    }

    public List<GapTolerance> getGapTolerances() {
        return gapTolerances;
    }

    public List<InterpolationType> getInterpolationTypes() {
        return interpolationTypes;
    }

    public Temporal getStartTime() {
        return startTime;
    }

    public Temporal getEndTime() {
        return endTime;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getUnit() {
        return unit;
    }

    public String getDescription() {
        return description;
    }

    public Boolean isInverted() {
        return inverted;
    }

    public Boolean isVolumetricFlow() {
        return isVolumetricFlow;
    }

    public List<InstantRange> getEstimatedPeriods() {
        return estimatedPeriods;
    }

    public void setPoints(List<UvHydrographPoint> points) {
        this.points = points;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public void setGaps(List<DataGap> gaps) {
        this.gaps = gaps;
    }

    public void setQualifiers(List<Qualifier> qualifiers) {
        this.qualifiers = qualifiers;
    }

    public void setGrades(List<Grade> grades) {
        this.grades = grades;
    }

    public void setApprovals(List<Approval> approvals) {
        this.approvals = approvals;
    }

    public void setGapTolerances(List<GapTolerance> gapTolerances) {
        this.gapTolerances = gapTolerances;
    }

    public void setInterpolationTypes(List<InterpolationType> interpolationTypes) {
        this.interpolationTypes = interpolationTypes;
    }

    public void setStartTime(Temporal startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Temporal endTime) {
        this.endTime = endTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUnits(String unit) {
        this.unit = unit;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setInverted(Boolean inverted) {
        this.inverted = inverted;
    }

    public void isVolumetricFlow(Boolean isVolumetricFlow) {
        this.isVolumetricFlow = isVolumetricFlow;
    }

    public void setEstimatedPeriods(List<InstantRange> estimatedPeriods) {
        this.estimatedPeriods = estimatedPeriods;
    }
}
