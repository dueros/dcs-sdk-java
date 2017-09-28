package com.baidu.duer.dcs.devicemodule.screen.message;

import com.baidu.duer.dcs.framework.message.Payload;

import java.io.Serializable;
import java.util.List;

public class RenderHintPayload extends Payload implements Serializable {
    public List<String> cueWords;
}
