/**
 * @Title: ResponseStatus.java
 * @Package: com.Wechat.DataType
 * @author: JwZheng
 * @date: 2020��11��10�� ����10:10:22
 * @Description: 
 */

package com.WeChat.DataType;

/**
 * @ClassName: ResponseStatus
 * @author JwZheng
 * @date: 2020��11��10�� ����10:10:22
 * @Description:
 */
public enum ResponseType {
    /** ������ɹ� */
    OK,
    /** �������ڲ����� */
    SERVER_ERROR,
    LOGIN_REFUSED,
    USER_LOGIN,
    CHAT,
    USER_LOGOUT,
    SERVER_CLOSE,
    UPDATE_USERS

}
