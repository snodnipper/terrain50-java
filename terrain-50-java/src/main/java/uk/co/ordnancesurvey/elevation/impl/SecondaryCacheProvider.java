package uk.co.ordnancesurvey.elevation.impl;

import java.util.Map;

public interface SecondaryCacheProvider {

    /**
     * @param defaultValue to be used if empty cache - mem cache will be updated with this value
     * @return a map between filename and the contents in bytes[]
     */
    Map<String, byte[]> getSecondaryCache(Map<String, byte[]> defaultValue);
}
