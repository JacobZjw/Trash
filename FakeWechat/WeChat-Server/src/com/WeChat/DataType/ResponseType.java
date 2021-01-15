package com.WeChat.DataType;

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
