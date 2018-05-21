package br.com.environment.control.model;

import net.jini.core.entry.Entry;

import java.util.List;

public class Environment implements Entry {

    public Integer id;
    public List<Integer> users;
    public List<Integer> devices;

    public Environment() {
    }

    public Environment(Integer id) {
        this.id = id;
    }

    public String getName() {
        return "env" + id;
    }

    public void addUser(User user) {
        users.add(user.id);
    }

    public void removeUser(User user) {
        int index = -1;
        for (int i = 0; i < users.size() && index < 0; i++) {
            if(users.get(i).equals(user.id)) {
                index = i;
            }
        }
        users.remove(index);
    }

    public void addDevice(Device device) {
        devices.add(device.id);
    }

}
