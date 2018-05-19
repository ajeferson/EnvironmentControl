package br.com.environment.control.model;

import net.jini.core.entry.Entry;

import java.util.ArrayList;
import java.util.List;

public class EnvironmentList implements Entry {

    public List<String> environments;

    public void addEnvironment(Environment env) {
        environments.add(env.name);
    }

    public void removeEnvironment(Environment env) {
        int index = -1;
        for (int i = 0; i < environments.size() && index < 0; i++) {
            if(environments.get(i).equals(env.name)) {
                index = i;
            }
        }
        if(index >= 0) {
            environments.remove(index);
        }
    }

    public void initEnvironments() {
        environments = new ArrayList<>();
    }

}
