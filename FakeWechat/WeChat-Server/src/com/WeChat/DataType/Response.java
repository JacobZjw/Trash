package com.WeChat.DataType;

import java.io.Serializable;

public class Response implements Serializable {
    private static final long serialVersionUID = 1689541820872288991L;

    private ResponseType type;

    private Object object;

    public Response() {
        this.object = new Object();
        this.type = ResponseType.OK;
    }

    public Response(ResponseType type, Object object) {
        this.object = object;
        this.type = type;
    }

    public void setType(ResponseType type) {
        this.type = type;
    }

    public ResponseType getType() {
        return type;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Object getObject() {
        return object;
    }
}
