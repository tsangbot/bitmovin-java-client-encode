package com.bitmovin.api.encoding.encodings.drms;

import com.bitmovin.api.encoding.encodings.drms.cencSystems.CencMarlin;
import com.bitmovin.api.encoding.encodings.drms.cencSystems.CencPlayReady;
import com.bitmovin.api.encoding.encodings.drms.cencSystems.CencWidevine;

public class CencDrm extends Drm
{
    private String key;
    private String kid;
    private CencWidevine widevine;
    private CencPlayReady playReady;
    private CencMarlin marlin;

    public CencPlayReady getPlayReady()
    {
        return this.playReady;
    }
    public void setPlayReady(CencPlayReady playReady)
    {
        this.playReady = playReady;
    }
    public CencWidevine getWidevine()
    {

        return this.widevine;
    }
    public void setWidevine(CencWidevine widevine)
    {
        this.widevine = widevine;
    }

    public CencMarlin getMarlin()
    {
        return this.marlin;
    }

    public void setMarlin(CencMarlin marlin)
    {
        this.marlin = marlin;
    }

    public String getKey()
    {
        return this.key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getKid()
    {
        return this.kid;
    }

    public void setKid(String kid)
    {
        this.kid = kid;
    }
}
