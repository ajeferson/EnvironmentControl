package br.com.environment.control.model;

import net.jini.core.entry.Entry;

public class Environment implements Entry {

    public Integer id;
    public Integer users = 0;
    public Integer devices = 0;

    public Environment() {
    }

    public Environment(Integer id) {
        this.id = id;
    }

    public String getName() {
        return "env" + id;
    }

}
