package com.bitmovin.api.encoding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Dominic Miglar [dominic.miglar@bitmovin.com] on 8/26/16 at 12:25 PM.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimpleApiDTO
{
    public SimpleApiDTO()
    {
    }

    public SimpleApiDTO(String id)
    {
        this.id = id;
    }

    private String id;

    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
}
