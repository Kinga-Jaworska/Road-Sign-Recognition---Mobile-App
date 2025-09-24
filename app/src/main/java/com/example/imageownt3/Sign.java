package com.example.imageownt3;

public class Sign
{
    private String name;
    private String link;
    private String type;
    public String getName() {
        return name;
    }
    public String getLink() {
        return link;
    }
    public String getType() {
        return type;
    }

    public Sign(String name, String link, String type)
    {
        this.name = name;
        this.link = link;
        this.type = type;
    }

    public Sign()
    {
    }
}