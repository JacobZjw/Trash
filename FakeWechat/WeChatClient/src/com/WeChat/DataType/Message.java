/**
 * @Title: Message.java
 * @Package: com.Wechat.DataType
 * @author: JwZheng
 * @date: 2020年11月10日 下午10:04:49
 * @Description: 
 */

package com.WeChat.DataType;

import java.io.Serializable;
import java.util.Date;

import com.WeChat.app.DataBuffer;

/**
 * @ClassName: Message
 * @author JwZheng
 * @date: 2020年11月10日 下午10:04:49
 * @Description:
 */
public class Message implements Serializable {
	private static final long serialVersionUID = 123L;

    private User toUser;
    private User fromUser;
    private String message;
    private Date sendTime;

    public Message() {
        this.fromUser = DataBuffer.currentUser;
        this.sendTime = new java.util.Date();
    }
    
    public Message(String msg) {
        this.fromUser = DataBuffer.currentUser;
        this.message = msg;
        this.sendTime = new java.util.Date();
    }

    public Message(User toUser, String msg) {
        this.fromUser = DataBuffer.currentUser;
        this.toUser = toUser;
        this.message = msg;
        this.sendTime = new java.util.Date();
    }
    
    public Message(User toUser) {
        this.fromUser = DataBuffer.currentUser;
        this.toUser = toUser;
        this.sendTime = new java.util.Date();
    }

    public Message(User fromUser, User toUser, String msg) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.message = msg;
        this.sendTime = new java.util.Date();
    }

    public User getToUser() {
        return toUser;
    }

    public void setToUser(User toUser) {
        this.toUser = toUser;
    }

    public User getFromUser() {
        return fromUser;
    }

    public void setFromUser(User fromUser) {
        this.fromUser = fromUser;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }
}