package snow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import snow.dependencies.MunicipalServices;
import snow.dependencies.PressService;
import snow.dependencies.SnowplowMalfunctioningException;
import snow.dependencies.WeatherForecastService;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class SnowRescueServiceTest {

	public WeatherForecastService weatherForecastService ;
	public MunicipalServices municipalServices ;
	public PressService pressService ;
	public SnowRescueService snowRescueService ;


	@BeforeEach
	public void setUp(){
		weatherForecastService = mock(WeatherForecastService.class);
		municipalServices = mock(MunicipalServices.class);
		pressService = mock(PressService.class);
		snowRescueService = new SnowRescueService(weatherForecastService, municipalServices, pressService);
	}

	@Test
	public void checkForecastAndRescue_shouldSendSanderAndSnowplow_whenTemperatureBelowZeroAndSnowFallHeightIsAbove3mm(){
		expectTemperatureInCelsius(-1);
		expectSnowFallHeight(5);
		checkForecastAndRescue();
		verifySendSander();
		verifySendSnowplow();
	}

	@Test
	public void checkForecastAndRescue_shouldNotSendSanderAndSnowplow_whenTemperatureAboveZeroAndSnowFallHeightIsBelow3mm(){
		expectTemperatureInCelsius(1);
		expectSnowFallHeight(1);
		checkForecastAndRescue();
		verifyDoNotSendSander();
		verifyDoNotSendSnowplow();
	}

	@Test
	public void checkForecastAndRescue_shouldNotSendSander_whenTemperatureIsZeroAndSnowFallHeightIs3mm(){
		expectTemperatureInCelsius(0);
		expectSnowFallHeight(3);
		checkForecastAndRescue();
		verifyDoNotSendSander();
		verifyDoNotSendSnowplow();
	}


	@Test
	public void checkForecastAndRescue_shouldSendSnowplow_whenSnowFallHeightAbove3mmAndTemperatureAboveZero(){
		expectTemperatureInCelsius(1);
		expectSnowFallHeight(5);
		checkForecastAndRescue();
		verifySendSnowplow();
		verifyDoNotSendSander();
	}

	@Test
	public void checkForecastAndRescue_shouldSendSander_whenSnowFallHeightBelow3mmAndTemperatureBelowZero(){
		expectTemperatureInCelsius(-2);
		expectSnowFallHeight(1);
		checkForecastAndRescue();
		verifyDoNotSendSnowplow();
		verifySendSander();
	}

	@Test
	public void checkForecastAndRescue_shouldSendSnowplow_whenFirstSnowPlowGivesExceptionAndTemperatureIsAbove0(){
		expectTemperatureInCelsius(2);
		expectSnowFallHeight(5);
		whenFirstSnowplowFails();
		checkForecastAndRescue();
		verifyDoNotSendSander();
		verifySendSnowplowTimes(2);
	}

	@Test
	public void checkForecastAndRescue_shouldSendTwoSnowplow_whenSnowFallHeightAbove5mmAndTemperatureIsAbove0(){
		expectTemperatureInCelsius(2);
		expectSnowFallHeight(8);
		checkForecastAndRescue();
		verifyDoNotSendSander();
		verifySendSnowplowTimes(2);
	}

	@Test
	public void checkForecastAndRescue_shouldTwoSendSnowplowOneSanderAndNotifyPress_whenSnowFallHeightAbove5mmAndTemperatureIsAbove0(){
		expectTemperatureInCelsius(-20);
		expectSnowFallHeight(18);
		checkForecastAndRescue();
		verifySendSander();
		verifySendSnowplowTimes(3);
		verify(pressService).sendWeatherAlert();
	}

	private void verifyDoNotSendSander() {
		verify(municipalServices,never()).sendSander();
	}

	private void verifySendSander() {
		verify(municipalServices).sendSander();
	}


	private void checkForecastAndRescue() {
		snowRescueService.checkForecastAndRescue();
	}

	private void verifySendSnowplow() {
		verify(municipalServices).sendSnowplow();
	}

	private void verifySendSnowplowTimes(final int times) {
		verify(municipalServices, times(times)).sendSnowplow();
	}

	private void expectSnowFallHeight(final int snowFallHeight) {
		when(weatherForecastService.getSnowFallHeightInMM()).thenReturn(snowFallHeight);
	}

	private void verifyDoNotSendSnowplow() {
		verify(municipalServices,never()).sendSnowplow();
	}

	private void whenFirstSnowplowFails() {
		doThrow(new SnowplowMalfunctioningException()).doNothing().when(municipalServices).sendSnowplow();
	}

	private void expectTemperatureInCelsius(final int temperature) {
		when(weatherForecastService.getAverageTemperatureInCelsius()).thenReturn(temperature);
	}
}
