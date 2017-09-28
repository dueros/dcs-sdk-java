package com.baidu.duer.dcs.devicemodule.screen.extend.card.message;

import com.baidu.duer.dcs.framework.message.Payload;

import java.io.Serializable;

public class RenderAirQualityPayload extends Payload implements Serializable {
    public String city;
    public String currentTemperature;
    public String pm25;
    public String airQuality;
    public String day;
    public String date;
    public String dateDescription;
    public String tips;
}
