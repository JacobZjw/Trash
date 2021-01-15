/**
 * @Title: Send.java
 * @Package: com.Wechat.messages
 * @author: JwZheng
 * @date: 2020年11月2日 下午3:53:11
 * @Description: 
 */

package com.WeChat.app;

import com.WeChat.DataType.RequestType;
import com.WeChat.DataType.User;
import com.WeChat.DataType.Message;
import com.WeChat.DataType.Request;

/**
 * @ClassName: Send
 * @author JwZheng
 * @date: 2020年11月2日 下午3:53:11
 * @Description:
 */
public class RequestBuilder {

	public Request getLoginRequest() {
		return new Request(RequestType.LOGIN, new Message());
	}

	public Request getLogoutRequest() {
		return new Request(RequestType.LOGOUT, new Message());
	}

	public Request getChatRequest(User toUser, String msg) {
		return new Request(RequestType.CHAT, new Message(toUser, msg));
	}

	public Request getUsersRequest() {
		return new Request(RequestType.GET_USERS, new Message());
	}

}
