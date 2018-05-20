package br.com.environment.control.model;

import net.jini.core.entry.Entry;

public class Meta implements Entry {

    public Integer environmentId;
    public Integer userId;

    public static Meta defaultMeta() {
        Meta meta = new Meta();
        meta.environmentId = 0;
        meta.userId = 0;
        return meta;
    }

}
