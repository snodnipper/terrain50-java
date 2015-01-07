# Online Demo #
## London ##
* http://os-elevation.appspot.com/elevation?latitude=51.50722&longitude=-0.12750
* http://os-elevation.appspot.com/elevation?easting=530050&northing=180361

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

# Server #
It is trivial to run independently.  Simply:
* download [OS Terrain 50](https://www.ordnancesurvey.co.uk/opendatadownload/products.html)
* rename the files using the rename.py
* upload the renamed files to a client accessable webserver (e.g. apache)
* point clients to webserver endpoint via the .terrain50DataUrl(base_url) method

# Maintainer Repo Configuration #
The maintainer should consider adding the following to their properties file:

    ~/.gradle/gradle.properties
       mavenUser=nexusUser
       mavenPassword=nexusPassword
