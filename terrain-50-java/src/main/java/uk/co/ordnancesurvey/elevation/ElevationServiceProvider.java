package uk.co.ordnancesurvey.elevation;

public class ElevationServiceProvider {

    private volatile static ElevationService mInstance;

    private ElevationServiceProvider(){}

    /** Returns singleton class instance */
    public static ElevationService getInstance() {
        Configuration configuration = new Configuration.Builder().build();
        return getInstance(configuration);
    }

    /**
     * Multiple requests with possibly different configurations will have no effect - only the first
     * configuration is used.
     */
    public static ElevationService getInstance(Configuration configuration) {
        if (configuration == null) {
            configuration = new Configuration.Builder().build();
        }

        if (mInstance == null) {
            synchronized (ElevationService.class) {
                if (mInstance == null) {
                    mInstance = new ElevationServiceImpl(configuration.getElevationProvider());
                }
            }
        }
        return mInstance;
    }
}
