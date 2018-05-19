package br.com.environment.control.model;

import net.jini.core.entry.Entry;

public class Environment implements Entry {

    public String name;

    public Environment() {
    }

    public Environment(String name) {
        this.name = name;
    }

}
