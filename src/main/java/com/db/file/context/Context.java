package com.db.file.context;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context {

    private Map<Class,List> map;

    private static Context instance;

    private final static Object lock = new Object();

    private static final String FILENAME = "C:/db-file";

    private Context()  {
        map = new HashMap<>();//防止后面调用put时报空指针异常

        try {
            File file = new File(FILENAME);
            if (!file.exists()) {
                file.createNewFile();
            }
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            int count = in.readInt();
            for (int i = 0; i <count ; i++) {
                Class c = (Class)in.readObject();
                List l = (List)in.readObject();
                map.put(c,l);
            }
        }
        catch (EOFException e) {

        }
        catch (Exception e) {
            System.out.println("初始化失败" + e.getMessage());
            System.exit(1);
        }
    }

    public static Context getInstance() {
        synchronized (lock) {
            if (instance == null) {
                synchronized (lock) {
                    instance = new Context();
                }
            }
        }
        return instance;
    }

    public void shutdown() {
        try {

            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILENAME));
            out.writeInt(map.size());
            out.flush();
            map.forEach((k,v) -> {
                try {
                    out.writeObject(k);
                    out.writeObject(v);
                    out.flush();
                }
                catch(Exception e) {
                    System.out.println("异常是：" + e.getMessage());
                }
            });
        }
        catch (Exception e) {

        }
    }

    public List get(Class clazz) {
        return map.get(clazz);
    }

    public void addMap(Class clazz) {
        if (!map.containsKey(clazz)) {
            map.put(clazz, new ArrayList());
        }
    }
}
