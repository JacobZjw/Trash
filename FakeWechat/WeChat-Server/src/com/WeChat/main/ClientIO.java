package com.WeChat.main;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ClientIO {



    /**
     * 对象输入流
     */
    private final ObjectInputStream ois;
    /**
     * 对象输出流
     */
    private final ObjectOutputStream oos;

    public ClientIO(ObjectInputStream ois, ObjectOutputStream oos){
        this.ois = ois;
        this.oos = oos;
    }

    public ObjectOutputStream getOos(){
        return oos;
    }

    public ObjectInputStream getOis() {
        return ois;
    }
}
