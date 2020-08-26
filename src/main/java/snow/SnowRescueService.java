package snow;

import snow.dependencies.MunicipalServices;
import snow.dependencies.PressService;
import snow.dependencies.SnowplowMalfunctioningException;
import snow.dependencies.WeatherForecastService;

public class SnowRescueService {
	WeatherForecastService weatherForecastService;
	MunicipalServices municipalServices;
	PressService pressService;
	public SnowRescueService(WeatherForecastService weatherForecastService, MunicipalServices municipalServices, PressService pressService) {
		this.weatherForecastService=weatherForecastService;
		this.municipalServices=municipalServices;
		this.pressService=pressService;
	}

	public void checkForecastAndRescue() {
		sendSander();
		sendSnowplows();
		if(isAverageTemperatureBelow(-10) && isSnowFallHeightAbove(10)){
			pressService.sendWeatherAlert();
		}
	}

	private void sendSander() {
		if(isAverageTemperatureBelow(0)){
			municipalServices.sendSander();
		}
	}

	private boolean isAverageTemperatureBelow(final int temperature) {
		return weatherForecastService.getAverageTemperatureInCelsius() < temperature;
	}

	private void sendSnowplows() {
		if(isSnowFallHeightAbove(3)){
			sendSnowplow();
		}
		if(isSnowFallHeightAbove(5)){
			sendSnowplow();
		}
		if(isSnowFallHeightAbove(10) && isAverageTemperatureBelow(-10)){
			sendSnowplow();
		}
	}

	private boolean isSnowFallHeightAbove(final int snowHeight) {
		return weatherForecastService.getSnowFallHeightInMM() > snowHeight;
	}

	private void sendSnowplow() {
		try {
			municipalServices.sendSnowplow();
		} catch (final SnowplowMalfunctioningException snowplowMalfunctioningException) {
			municipalServices.sendSnowplow();
		}
	}

}
