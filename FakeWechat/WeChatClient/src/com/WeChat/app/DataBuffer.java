/**
 * @Title: DataBuffer.java
 * @Package: com.Wechat.app
 * @author: JwZheng
 * @date: 2020��11��10�� ����10:15:50
 * @Description: 
 */

package com.WeChat.app;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.concurrent.CopyOnWriteArrayList;

import com.WeChat.Daemon.GetUsersThread;
import com.WeChat.DataType.User;

/**
 * @ClassName: DataBuffer
 * @author JwZheng
 * @date: 2020��11��10�� ����10:15:50
 * @Description:
 */
public class DataBuffer {
	// �����û�Map
	public static CopyOnWriteArrayList<User> onlineUsersList;
	public static ObjectInputStream ois; // ����������
	public static ObjectOutputStream oos; // ���������
	public static Socket socket;
	public static User currentUser;
	public static SimpleDateFormat time;
	public static RequestBuilder requestBuilder;
	public static GetUsersThread getUsersThread;

	static {
		// ��ʼ��
		time = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
		onlineUsersList=new CopyOnWriteArrayList<User>();
		requestBuilder = new RequestBuilder();
	}
}
