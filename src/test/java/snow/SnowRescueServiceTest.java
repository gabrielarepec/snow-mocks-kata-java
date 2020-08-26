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
import static org.mockito.Mockito.verifyNoInteractions;
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
		when(weatherForecastService.getAverageTemperatureInCelsius()).thenReturn(-1);
		when(weatherForecastService.getSnowFallHeightInMM()).thenReturn(5);
		snowRescueService.checkForecastAndRescue();
		verify(municipalServices).sendSander();
		verify(municipalServices).sendSnowplow();
	}

	@Test
	public void checkForecastAndRescue_shouldNotSendSanderAndSnowplow_whenTemperatureAboveZeroAndSnowFallHeightIsBelow3mm(){
		when(weatherForecastService.getAverageTemperatureInCelsius()).thenReturn(1);
		when(weatherForecastService.getSnowFallHeightInMM()).thenReturn(1);
		snowRescueService.checkForecastAndRescue();
		verify(municipalServices,never()).sendSander();
		verify(municipalServices,never()).sendSnowplow();
	}

	@Test
	public void checkForecastAndRescue_shouldNotSendSander_whenTemperatureIsZeroAndSnowFallHeightIs3mm(){
		when(weatherForecastService.getAverageTemperatureInCelsius()).thenReturn(0);
		when(weatherForecastService.getSnowFallHeightInMM()).thenReturn(3);
		snowRescueService.checkForecastAndRescue();
		verify(municipalServices,never()).sendSander();
		verify(municipalServices,never()).sendSnowplow();

	}


	@Test
	public void checkForecastAndRescue_shouldSendSnowplow_whenSnowFallHeightAbove3mmAndTemperatureAboveZero(){
		when(weatherForecastService.getAverageTemperatureInCelsius()).thenReturn(1);
		when(weatherForecastService.getSnowFallHeightInMM()).thenReturn(5);
		snowRescueService.checkForecastAndRescue();
		verify(municipalServices).sendSnowplow();
		verify(municipalServices,never()).sendSander();
	}

	@Test
	public void checkForecastAndRescue_shouldSendSander_whenSnowFallHeightBelow3mmAndTemperatureBelowZero(){
		when(weatherForecastService.getAverageTemperatureInCelsius()).thenReturn(-2);
		when(weatherForecastService.getSnowFallHeightInMM()).thenReturn(1);
		snowRescueService.checkForecastAndRescue();
		verify(municipalServices,never()).sendSnowplow();
		verify(municipalServices).sendSander();
	}

	@Test
	public void checkForecastAndRescue_shouldSendSnowplow_whenFirstSnowPlowGivesExceptionAndTemperatureIsAbove0(){
		when(weatherForecastService.getAverageTemperatureInCelsius()).thenReturn(2);
		when(weatherForecastService.getSnowFallHeightInMM()).thenReturn(5);
		doThrow(new SnowplowMalfunctioningException()).doNothing().when(municipalServices).sendSnowplow();
		snowRescueService.checkForecastAndRescue();
		verify(municipalServices,never()).sendSander();
		verify(municipalServices,times(2)).sendSnowplow();
	}
	@Test
	public void checkForecastAndRescue_shouldSendTwoSnowplow_whenSnowFallHeightAbove5mmAndTemperatureIsAbove0(){
		when(weatherForecastService.getAverageTemperatureInCelsius()).thenReturn(2);
		when(weatherForecastService.getSnowFallHeightInMM()).thenReturn(8);
		snowRescueService.checkForecastAndRescue();
		verify(municipalServices,never()).sendSander();
		verify(municipalServices,times(2)).sendSnowplow();
	}

	@Test
	public void checkForecastAndRescue_shouldTwoSendSnowplowOneSanderAndNotifyPress_whenSnowFallHeightAbove5mmAndTemperatureIsAbove0(){
		when(weatherForecastService.getAverageTemperatureInCelsius()).thenReturn(-20);
		when(weatherForecastService.getSnowFallHeightInMM()).thenReturn(18);
		snowRescueService.checkForecastAndRescue();
		verify(municipalServices).sendSander();
		verify(municipalServices,times(3)).sendSnowplow();
		verify(pressService).sendWeatherAlert();
	}


}
