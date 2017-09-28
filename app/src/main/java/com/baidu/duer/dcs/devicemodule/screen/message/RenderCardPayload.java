package com.baidu.duer.dcs.devicemodule.screen.message;

import com.baidu.duer.dcs.framework.message.Payload;

import java.io.Serializable;
import java.util.List;

public class RenderCardPayload extends Payload implements Serializable {

    public Type type;
    public String title;
    public String content;
    public ImageStructure image;
    public LinkStructure link;
    public List<ListItem> list;
    public List<ImageStructure> imageList;

    public enum Type {
        TextCard,
        StandardCard,
        ListCard,
        ImageListCard,
    }

    public static final class LinkStructure implements Serializable {
        public String url;
        public String anchorText;
    }

    public static final class ImageStructure implements Serializable {
        public String src;
    }

    public static final class ListItem implements Serializable {
        public String title;
        public String content;
        public ImageStructure image;
        public String url;
    }
}
