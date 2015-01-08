package uk.co.ordnancesurvey.elevation;

/**
 * Note to maintainer:
 * {@link Strategy#MAX_PERFORMANCE} assumes a cloud server with
 * approximately 128MiB RAM for us to use.  In the future it would be nice to dynamically make
 * calculations (e.g. amount of free memory, the number of requests per second) but ultimately this
 * is a configuration by the app and inputs can certainly be used as hints.
 * The {@link Strategy#MAX_PERFORMANCE} option is in contrast
 * to a role where minimising resource use is the top priority, which can be specified by
 * {@link Strategy#CONSERVE_RESOURCE}.  That profile may be more
 * appropriate for a mobile phone, which despite having a potentially large amount of memory
 * (e.g. 2GiB) has limited resource (e.g. battery).
 */
public enum Strategy {
    CONSERVE_RESOURCE, MAX_PERFORMANCE
}
