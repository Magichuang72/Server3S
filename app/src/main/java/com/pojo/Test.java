package com.pojo;

import java.io.Serializable;

/**
 * Created by magichuang on 17-9-4.
 */
public class Test implements Serializable {
    String name;
    int id;

    public Test(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
