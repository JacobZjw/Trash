package com.WeChat.main;

import com.WeChat.DataType.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

public class ListenRequest implements Callable {
    private final Socket socket;
    private final ClientIO clientIO;

    public ListenRequest(Socket socket) throws IOException {
        this.socket = socket;
        this.clientIO = new ClientIO(new ObjectInputStream(socket.getInputStream()),
                new ObjectOutputStream(socket.getOutputStream()));
    }

    @Override
    public Object call() {
        try {
            Object obj;
            while ((obj = clientIO.getOis().readObject()) != null) {
                Request request = (Request) obj;
                RequestType type = request.getType();
                System.out.println("Server读取了客户端" + Thread.currentThread().getName() + "的请求");
                if (type.equals(RequestType.GET_USERS)) {
                    sendUsers();
                } else if (type.equals(RequestType.CHAT)) {
                    //聊天
                    chat(request);
                } else if (type.equals(RequestType.LOGIN)) {
                    //用户登录
                    login(request);
                } else if (type.equals(RequestType.LOGOUT)) {
                    //断开连接
                    logout(request);
                    return null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void login(Request request) throws IOException {
        Message msgObj = (Message) request.getObject();
        User newUser = msgObj.getFromUser();

        if (DataBuffer.UserIOMap.containsKey(newUser)) {
            //该用户已经存在
            clientIO.getOos().writeObject(new Response(ResponseType.LOGIN_REFUSED, new Message("你已在别处上线 !")));
            clientIO.getOos().flush();
        } else {
            //分配ID,更新在线列表
            DataBuffer.UserIOMap.put(newUser, clientIO);
            DataBuffer.onlineUsersList.add(newUser);
            //通知其他用户,你已上线
//            String str = "【" + newUser.getUserName() + "】" + "已上线";
            Response response = new Response(ResponseType.USER_LOGIN, new Message(newUser));
            for (ClientIO usersIo : DataBuffer.UserIOMap.values()) {
                sendResponse(usersIo, response);
            }
        }

    }

    public void logout(Request request) throws IOException {
        Message msgObj = (Message) request.getObject();
        User newUser = msgObj.getFromUser();

        DataBuffer.UserIOMap.remove(newUser);
        DataBuffer.onlineUsersList.remove(newUser);

        Response response = new Response(ResponseType.USER_LOGOUT, new Message(newUser));
        for (ClientIO userIo : DataBuffer.UserIOMap.values()) {
            sendResponse(userIo, response);
        }
//        DataBuffer.UserIOMap.values().forEach(usersIO->{
//            try {
//                sendResponse(usersIO, response);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });

    }

    public void chat(Request request) throws IOException {
        Message msgObj = (Message) request.getObject();

        Response response = new Response(ResponseType.CHAT, msgObj);

        User toUser = msgObj.getToUser();
        if (toUser != null) {
            //私聊
            ClientIO userIo = DataBuffer.UserIOMap.get(toUser);
            sendResponse(userIo, response);
            sendResponse(clientIO, response);
        } else {//群聊
            for (ClientIO userIo : DataBuffer.UserIOMap.values()) {
                sendResponse(userIo, response);
            }
        }
    }

    public void sendResponse(ClientIO userIo, Response response) throws IOException {
        ObjectOutputStream oos = userIo.getOos();
        oos.writeObject(response);
        oos.flush();
    }

    public void sendUsers() throws IOException {
        CopyOnWriteArrayList<User> onlineUsersList = new CopyOnWriteArrayList<User>();
        onlineUsersList.addAll(DataBuffer.onlineUsersList);

        Response response = new Response(ResponseType.UPDATE_USERS, onlineUsersList);

        ObjectOutputStream oos = clientIO.getOos();
        oos.writeObject(response);
        oos.flush();
    }
}
