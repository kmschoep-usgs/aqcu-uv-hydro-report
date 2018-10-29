package gov.usgs.aqcu.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Grade;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Qualifier;

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class UvHydroReportTest {    
    UvHydroReport test1;
    UvHydroReport test2;
    UvHydroReport test3;
    UvHydroReport test4;
    UvHydroReport test5;
    Qualifier q1;
    Qualifier q2;
    Qualifier q3;
    Qualifier q4;
    Grade g1;
    Grade g2;
    Grade g3;
    Grade g4;

    @Before
    public void setup() {
        q1 = new Qualifier();
        q2 = new Qualifier();
        q3 = new Qualifier();
        q4 = new Qualifier();
        g1 = new Grade();
        g2 = new Grade();
        g3 = new Grade();
        g4 = new Grade();
        UvHydrographTimeSeries test_series_1 = new UvHydrographTimeSeries("test1");
        test_series_1.setQualifiers(Arrays.asList(q1));
        test_series_1.setGrades(Arrays.asList(g1));
        UvHydrographTimeSeries test_series_2 = new UvHydrographTimeSeries("test2");
        test_series_2.setQualifiers(Arrays.asList(q2, q3));
        UvHydrographTimeSeries test_series_3 = new UvHydrographTimeSeries("test3");
        test_series_3.setQualifiers(Arrays.asList(q3, q4));
        test_series_3.setGrades(Arrays.asList(g1,g2,g3,g4));
        UvHydrographTimeSeries test_series_4 = new UvHydrographTimeSeries("test4");
        test1 = new UvHydroReport();
        test1.setPrimarySeries(test_series_1);
        test1.setReferenceSeries(test_series_2);
        test1.setComparisonSeries(test_series_3);
        test1.setFourthStatDerived(test_series_4);
        test2 = new UvHydroReport();
        test2.setPrimarySeries(test_series_1);
        test2.setReferenceSeries(test_series_2);
        test2.setComparisonSeries(test_series_3);
        test2.setFirstStatDerived(test_series_1);
        test2.setSecondStatDerived(test_series_2);
        test2.setThirdStatDerived(test_series_3);
        test2.setFourthStatDerived(test_series_4);
        test3 = new UvHydroReport();
        test3.setPrimarySeries(test_series_1);
        test4 = new UvHydroReport();
        test4.setPrimarySeries(test_series_4);
        test5 = new UvHydroReport();
    }

    @Test
    public void getAllQualifiersTest() {
        List<Qualifier> result = test1.getAllQualifiers();
        assertEquals(result.size(), 4);
        assertThat(result, containsInAnyOrder(q1, q2, q3, q4));
        result = test2.getAllQualifiers();
        assertEquals(result.size(), 4);
        assertThat(result, containsInAnyOrder(q1, q2, q3, q4));
        result = test3.getAllQualifiers();
        assertEquals(result.size(), 1);
        assertThat(result, containsInAnyOrder(q1));
        result = test4.getAllQualifiers();
        assertEquals(result.size(), 0);
        result = test5.getAllQualifiers();
        assertEquals(result.size(), 0);
    }

    @Test
    public void getAllGradesTest() {
        List<Grade> result = test1.getAllGrades();
        assertEquals(result.size(), 4);
        assertThat(result, containsInAnyOrder(g1, g2, g3, g4));
        result = test2.getAllGrades();
        assertEquals(result.size(), 4);
        assertThat(result, containsInAnyOrder(g1, g2, g3, g4));
        result = test3.getAllGrades();
        assertEquals(result.size(), 1);
        assertThat(result, containsInAnyOrder(g1));
        result = test4.getAllGrades();
        assertEquals(result.size(), 0);
        result = test5.getAllGrades();
        assertEquals(result.size(), 0);
    }
}