package com.baidu.duer.dcs.devicemodule.screen.extend.card.message;

import com.baidu.duer.dcs.framework.message.Payload;

import java.io.Serializable;

public class RenderDatePayload extends Payload implements Serializable {
    public String datetime;
    public String timeZoneName;
    public String day;
}
