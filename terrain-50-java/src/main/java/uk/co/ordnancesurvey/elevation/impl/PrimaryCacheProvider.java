package uk.co.ordnancesurvey.elevation.impl;

import java.util.Map;

public interface PrimaryCacheProvider {

    /**
     * @param defaultValue to be used if empty cache - mem cache will be updated with this value
     * @return
     */
    Map<String, String> getPrimaryCache(Map<String, String> defaultValue);
}
