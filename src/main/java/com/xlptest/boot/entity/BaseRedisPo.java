package com.xlptest.boot.entity;

import com.alibaba.fastjson.annotation.JSONField;

public abstract class BaseRedisPo implements IRedisPo {

    @JSONField(serialize = false)
    private String key;
    @JSONField(serialize = false)
    private String primary;

    @Override
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }
}
