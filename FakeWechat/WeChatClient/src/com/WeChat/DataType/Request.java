/**
 * @Title: Request.java
 * @Package: com.Wechat.DataType
 * @author: JwZheng
 * @date: 2020年11月10日 下午10:10:45
 * @Description: 
 */

package com.WeChat.DataType;

import java.io.Serializable;

/**
 * @ClassName: Request
 * @author JwZheng
 * @date: 2020年11月10日 下午10:10:45
 * @Description:
 */

public class Request implements Serializable {
	 private static final long serialVersionUID = 456L;

	    private RequestType type;

	    private Object object;

	    public Request() {
	        this.object = new Object();
	        this.type = RequestType.GET_USERS;
	    }

	    public Request(RequestType type, Object object) {
	        this.object = object;
	        this.type = type;
	    }

	    public Object getObject() {
	        return object;
	    }

	    public void setObject(Object object) {
	        this.object = object;
	    }

	    public RequestType getType() {
	        return type;
	    }

	    public void setType(RequestType type) {
	        this.type = type;
	    }
}
