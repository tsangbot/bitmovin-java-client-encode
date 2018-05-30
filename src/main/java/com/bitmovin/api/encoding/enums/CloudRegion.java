package com.bitmovin.api.encoding.enums;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by
 * Ferdinand Koeppen [ferdinand.koeppen@bitmovin.com]
 * on 25.07.16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public enum CloudRegion
{
    AWS_US_EAST_1,
    AWS_US_EAST_2,
    AWS_US_WEST_1,
    AWS_US_WEST_2,
    AWS_EU_WEST_1,
    AWS_EU_CENTRAL_1,
    AWS_AP_SOUTHEAST_1,
    AWS_AP_SOUTHEAST_2,
    AWS_AP_NORTHEAST_1,
    AWS_AP_NORTHEAST_2,
    AWS_AP_SOUTH_1,
    AWS_SA_EAST_1,
    GOOGLE_US_CENTRAL_1,
    GOOGLE_US_EAST_1,
    GOOGLE_ASIA_EAST_1,
    GOOGLE_EUROPE_WEST_1,
    GOOGLE_US_WEST_1,

    NORTH_AMERICA,
    SOUTH_AMERICA,
    EUROPE,
    AFRICA,
    ASIA,
    AUSTRALIA,
    AWS,
    GOOGLE,
    KUBERNETES,
    EXTERNAL,

    AUTO

}
