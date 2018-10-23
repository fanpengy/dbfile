package com.db.file.context;

import java.io.*;
import java.util.*;

public class Context {

    private Map<Class,List> map;

    private static Context instance;

    private final static Object lock = new Object();

    private static final String FILENAME = "/db-file";

    private Context()  {
        map = new HashMap<>();//防止后面调用put时报空指针异常

        File file = new File(System.getProperty("catalina.home") + FILENAME);
        try {

            System.out.println("生成的文件在" + System.getProperty("catalina.home"));
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
            System.out.println("初始化失败，文件将被删除，所有数据丢失" + e.getMessage());
            file.delete();
            map.clear();
            try {
                file.createNewFile();
            } catch (Exception ex) {
                System.out.println("重新创建文件异常" + ex.getMessage());
            }
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
