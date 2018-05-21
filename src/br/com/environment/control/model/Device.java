package br.com.environment.control.model;

import net.jini.core.entry.Entry;

public class Device implements Entry {

    public Integer id;
    public Integer environmentId;

    public Device() {
    }

    public Device(Integer id) {
        this.id = id;
    }

    public String getName() {
        return "dev" + id;
    }

}
