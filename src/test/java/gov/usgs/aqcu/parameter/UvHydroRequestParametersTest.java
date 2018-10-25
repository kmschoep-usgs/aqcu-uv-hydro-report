package gov.usgs.aqcu.parameter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class UvHydroRequestParametersTest {
    @Test
    public void getTsUidListTest() {
        UvHydroRequestParameters params = new UvHydroRequestParameters();
        params.setPrimaryTimeseriesIdentifier("primary");
        params.setReferenceTimeseriesIdentifier("ref");
        params.setUpchainTimeseriesIdentifier("upchain");
        params.setComparisonTimeseriesIdentifier("comparison");
        params.setFirstStatDerivedIdentifier("first");
        params.setSecondStatDerivedIdentifier("second");
        params.setThirdStatDerivedIdentifier("third");
        params.setFourthStatDerivedIdentifier("fourth");

        List<String> result = params.getTsUidList();
        assertEquals(result.size(), 8);
        assertThat(result, containsInAnyOrder("primary", "ref", "upchain", "comparison", "first", "second", "third", "fourth"));

        params = new UvHydroRequestParameters();
        params.setPrimaryTimeseriesIdentifier("primary");

        result = params.getTsUidList();
        assertEquals(result.size(), 1);
        assertThat(result, containsInAnyOrder("primary"));

        params = new UvHydroRequestParameters();
        params.setReferenceTimeseriesIdentifier("ref");
        params.setComparisonTimeseriesIdentifier("comparison");
        params.setSecondStatDerivedIdentifier("second");
        params.setFourthStatDerivedIdentifier("fourth");

        result = params.getTsUidList();
        assertEquals(result.size(), 5);
        assertThat(result, containsInAnyOrder(null, "ref", "comparison", "second", "fourth"));
    }
}
