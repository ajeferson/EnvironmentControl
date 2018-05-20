package br.com.environment.control.model;

import net.jini.core.entry.Entry;

public class Message implements Entry {

    public String content;
    public Integer senderId;
    public Integer receiverId;

    public void setContent(User user, String content) {
        this.content = user.getName() + ": " + content;
    }

}
