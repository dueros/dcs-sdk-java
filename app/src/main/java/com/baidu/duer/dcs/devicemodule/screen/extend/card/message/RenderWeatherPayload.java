package com.baidu.duer.dcs.devicemodule.screen.extend.card.message;

import com.baidu.duer.dcs.framework.message.Payload;

import java.io.Serializable;
import java.util.List;

public class RenderWeatherPayload extends Payload implements Serializable {
    public String city;
    public String askingDay;
    public String askingDate;
    public String askingDateDescription;
    public List<WeatherForecast> weatherForecast;

    public static final class WeatherForecast implements Serializable {
        public ImageStructure weatherIcon;
        public String highTemperature;
        public String lowTemperature;
        public String day;
        public String date;
        public String weatherCondition;
        public String windCondition;
        public String currentTemperature;
        public String currentPM25;
        public String currentAirQuality;
    }

    public static final class ImageStructure implements Serializable {
        public String src;
    }
}
