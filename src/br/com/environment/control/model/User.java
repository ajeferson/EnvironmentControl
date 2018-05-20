package br.com.environment.control.model;

import net.jini.core.entry.Entry;

public class User implements Entry {

    public Integer id;

    public String getName() {
        return "user" + id;
    }

}
