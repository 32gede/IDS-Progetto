package com.example.progetto.data.model;

import java.util.Date;

public class ItemUtils {
    private String name;
    private String url;

    // Costruttore
    public ItemUtils(String name, String url) {
        this.name = name;
        this.url = url;
    }

    // Getter e Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}