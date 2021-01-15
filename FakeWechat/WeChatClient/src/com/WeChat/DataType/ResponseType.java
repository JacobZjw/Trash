/**
 * @Title: ResponseStatus.java
 * @Package: com.Wechat.DataType
 * @author: JwZheng
 * @date: 2020年11月10日 下午10:10:22
 * @Description: 
 */

package com.WeChat.DataType;

/**
 * @ClassName: ResponseStatus
 * @author JwZheng
 * @date: 2020年11月10日 下午10:10:22
 * @Description:
 */
public enum ResponseType {
    /** 请求处理成功 */
    OK,
    /** 服务器内部出错 */
    SERVER_ERROR,
    LOGIN_REFUSED,
    USER_LOGIN,
    CHAT,
    USER_LOGOUT,
    SERVER_CLOSE,
    UPDATE_USERS

}
