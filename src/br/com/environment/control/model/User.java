package br.com.environment.control.model;

import net.jini.core.entry.Entry;

public class User implements Entry {

    public Integer id;
    public Integer environmentId;

    public User(Integer id) {
        this.id = id;
    }

    public User() {
    }

    public String getName() {
        return "user" + id;
    }

    public Boolean isWithinEnvironment() {
        return environmentId != null;
    }

}
