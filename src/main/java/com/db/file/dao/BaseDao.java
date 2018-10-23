package com.db.file.dao;

import com.db.file.context.Context;
import com.db.file.model.BaseModel;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class BaseDao<T extends BaseModel & Serializable> {

    protected Context context;

    private Class clazz = (Class) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];

    public BaseDao() {
        this.context = Context.getInstance();
        addMap();
    }


    public void insert(T t) {
        List<T> ts = get();
        if (ts.size() == 0) {
            t.setId(1L);
        } else {
            t.setId(ts.get(ts.size() - 1).getId() + 1);
        }
        ts.add(t);
    }

    public void insertWithId(T t) {
        get().add(t);
    }

    public void delete(T t, boolean invert) {
        Predicate<T> p = test(t);
        if (invert) {
            p = p.negate();
        }
        get().removeIf(p);
    }

    public void deleteById(Long id) {
        get().removeIf(t -> t.getId().equals(id));//通过id的equal判断元素是否存在
    }

    public List<T> query(T t) {
        //Predicate<T> p = test(t);
        return get().stream().filter(test(t)).collect(Collectors.toList());
    }

    public T queryById(Long id) {
        return get().stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);

    }

    public void update(T source ,T target) {
        //想一想怎么更新
        List<T> query = this.query(source);
        query.forEach(t -> {
            target.setId(t.getId());
            this.deleteById(t.getId());
            this.insertWithId(target);
        });

    }

    private Predicate<T> test(T t) {
        Field[] fields = t.getClass().getDeclaredFields();
        Predicate<T> p0 = t12 -> true;
        for (Field field : fields) {
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(t);
            } catch (IllegalAccessException e) {
                continue;
            }
            if (value != null) {
                Predicate<T> p = t1 -> {
                    try {
                        return field.get(t1).equals(value/*field.get(t)*/);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return false;
                };
                p0 = p0.and(p);
            }
        }
        return p0;
    }

    protected List<T> get() {//调用context的get方法
        return this.context.get(clazz);
    }

    private void addMap() {
        context.addMap(clazz);
    }

    public void flush() {
        context.shutdown();
    }
}
