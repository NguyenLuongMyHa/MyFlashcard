package com.myha.myflashcard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Word implements Serializable
{
    private int id;
    private Map<String,String> attributes;

    public Word()
    {
        this.attributes = new HashMap<String, String>();
    }

    public String getAttributeByKey(String key)
    {
        return attributes.get(key);
    }

    public void setAttributeByName(String key, String value)
    {
        attributes.put(key, value);
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public Map<String, String> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes)
    {
        this.attributes = attributes;
    }
}
