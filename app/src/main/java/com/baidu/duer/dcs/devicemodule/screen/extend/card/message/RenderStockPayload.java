package com.baidu.duer.dcs.devicemodule.screen.extend.card.message;

import com.baidu.duer.dcs.framework.message.Payload;

import java.io.Serializable;

public class RenderStockPayload extends Payload implements Serializable {
    public double changeInPrice;
    public double changeInPercentage;
    public double marketPrice;
    public String marketStatus;
    public String marketName;
    public String name;
    public String datetime;
    public double openPrice;
    public double previousClosePrice;
    public double dayHighPrice;
    public double dayLowPrice;
    public double priceEarningRatio;
    public long marketCap;
    public long dayVolume;
}
