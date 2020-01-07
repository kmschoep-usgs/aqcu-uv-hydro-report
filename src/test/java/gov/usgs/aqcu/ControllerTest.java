package gov.usgs.aqcu;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.gson.Gson;

import gov.usgs.aqcu.builder.UvHydroReportBuilderService;
import gov.usgs.aqcu.client.JavaToRClient;
import gov.usgs.aqcu.model.UvHydroReport;
import gov.usgs.aqcu.parameter.UvHydroRequestParameters;
import gov.usgs.aqcu.util.AqcuGsonBuilderFactory;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class ControllerTest {
	@MockBean
	private UvHydroReportBuilderService reportBuilderService;
	@MockBean
	private JavaToRClient client;

	private Gson gson;
	private Controller controller;
	byte[] resultBytes;
	
	UvHydroReport report;

	@Before
	public void setup() {
		gson = AqcuGsonBuilderFactory.getConfiguredGsonBuilder().create();
		report = new UvHydroReport();
		controller = new Controller(reportBuilderService, client, gson);
		resultBytes = gson.toJson(report).getBytes();
	}

	@Test
	public void getReportTest() throws Exception {
		given(reportBuilderService.buildReport(any(UvHydroRequestParameters.class), any(String.class)))
			.willReturn(report);
		given(client.render(any(String.class), any(String.class), any(String.class)))
			.willReturn(resultBytes);
		
		ResponseEntity<?> result = controller.getReport(new UvHydroRequestParameters());
		assertEquals(result.getBody(), resultBytes);
		assertEquals(result.getStatusCode(), HttpStatus.OK);
	}

	@Test
	public void getReportRawDataTest() throws Exception {
		given(reportBuilderService.buildReport(any(UvHydroRequestParameters.class), any(String.class)))
			.willReturn(report);
		ResponseEntity<?> result = controller.getReportRawData(new UvHydroRequestParameters());
		assertEquals(result.getBody(), gson.toJson(report));
		assertEquals(result.getStatusCode(), HttpStatus.OK);
	}

	@Test
	public void getRequestingUserTest() {
		Controller c = new Controller(null, null, null);
		assertEquals(Controller.UNKNOWN_USERNAME, c.getRequestingUser());
	}
}
