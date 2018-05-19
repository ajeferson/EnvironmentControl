package br.com.environment.control.model;

import net.jini.core.entry.Entry;

import java.util.ArrayList;
import java.util.List;

public class TupleEnvironments implements Entry {

    public Integer id;
    public List<String> environments;
//    public String environments;

    public TupleEnvironments() {
        this.id = DEFAULT_ID;
    }

    public void addEnvironment(Environment env) {
//        environments += (SEPARATOR + env.name);
        environments.add(env.name);
    }

    public void initEnvironments() {
        environments = new ArrayList<>();
    }

//    public List<Environment> getList() {
//        String[] names = environments.split(SEPARATOR);
//        List<Environment> envs = new ArrayList<>();
//        for (String name : names) {
//            envs.add(new Environment(name));
//        }
//        return envs;
//    }

    private static final Integer DEFAULT_ID = 1;
//    private static final String SEPARATOR = "---";

}
