package gov.usgs.aqcu.retrieval;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.EffectiveShift;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingModelEffectiveShiftsServiceResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import net.servicestack.client.IReturn;

@RunWith(SpringRunner.class)
public class EffectiveShiftsServiceTest {
    @MockBean
    private AquariusRetrievalService aquariusService;
    private EffectiveShiftsService service;

    public static final EffectiveShift SHIFT_A = new EffectiveShift()
        .setTimestamp(Instant.parse("2018-01-01T00:00:00Z"))
        .setValue(1.0);
    public static final EffectiveShift SHIFT_B = new EffectiveShift()
        .setTimestamp(Instant.parse("2018-01-01T00:15:00Z"))
        .setValue(2.0);
    public static final ArrayList<EffectiveShift> SHIFT_LIST = new ArrayList<>(Arrays.asList(SHIFT_A, SHIFT_B));

    @Before
    @SuppressWarnings("unchecked")  
    public void setup() {
        service = new EffectiveShiftsService(aquariusService);
        given(aquariusService.executePublishApiRequest(any(IReturn.class))).willReturn(
            new RatingModelEffectiveShiftsServiceResponse().setEffectiveShifts(SHIFT_LIST)
        );
    }

    @Test
    public void getTest() {
        List<EffectiveShift> shifts = service.get("test", "test@test", Instant.parse("2018-01-01T00:00:00Z"), Instant.parse("2018-01-01T00:00:00Z"));
        assertThat(shifts, containsInAnyOrder(SHIFT_A, SHIFT_B));
    }

}