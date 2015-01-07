# Code #
include a gradle reference:

    uk.co.ordnancesurvey.api:elevation:0.1.0
build the elevation service:

        Configuration configuration = new Configuration.Builder()
                .setStrategy(Strategy.MAX_PERFORMANCE)
                .terrain50DataUrl("https://github.com/snodnipper/terrain50-java/raw/master/data/")
                .terrain50FileSuffix("_OST50GRID_20130611")
                .build();
        ElevationService elevationService = ElevationServiceProvider.getInstance(configuration);
use it:

        double elevation = elevationService.getElevation(51.50722, -0.12750);
        double elevation2 = elevationService.getElevation(SpatialReference.EPSG_27700, 530050, 180361);

# Online Demo #
## London ##
* http://os-elevation.appspot.com/elevation?latitude=51.50722&longitude=-0.12750
* http://os-elevation.appspot.com/elevation?easting=530050&northing=180361

## Ordnance Survey HQ ##
* http://os-elevation.appspot.com/elevation?latitude=50.937947&longitude=-1.470624
* http://os-elevation.appspot.com/elevation?easting=437293&northing=115522

# Maintainer Repo Configuration #
The maintainer should consider adding the following to their properties file:

    ~/.gradle/gradle.properties
       mavenUser=nexusUser
       mavenPassword=nexusPassword
