/**
 * @Title: User.java
 * @Package: com.Wechat.DataType
 * @author: JwZheng
 * @date: 2020年11月10日 下午10:05:22
 * @Description: 
 */

package com.WeChat.DataType;

import java.io.Serializable;
import java.util.Objects;

/**
 * @ClassName: User
 * @author JwZheng
 * @date: 2020年11月10日 下午10:05:22
 * @Description:
 */

public class User implements Serializable {
	private static final long serialVersionUID = 123455L;

	private String userName;

	public User(String userName) throws Exception{
		if(userName.isEmpty())
			throw new Exception();
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userName, user.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName);
    }

}
