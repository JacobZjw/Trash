package com.WeChat.main;

import com.WeChat.DataType.User;

import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataBuffer {

    /**
     * 服务器端套接字
     */
    public static ServerSocket serverSocket;
    /**
     * 在线用户的IO Map
     */
    public static Map<User, ClientIO> UserIOMap;

    public static User currentUser;

    public static CopyOnWriteArrayList<User> onlineUsersList;

    public static SimpleDateFormat time;

    static {
        // 初始化
        UserIOMap = new ConcurrentHashMap<User, ClientIO>();
        onlineUsersList=new CopyOnWriteArrayList<User>();
        try {
            currentUser = new User("system");
        } catch (Exception e) {
            e.printStackTrace();
        }
        time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

}
