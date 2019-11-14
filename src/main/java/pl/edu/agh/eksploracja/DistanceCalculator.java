package pl.edu.agh.eksploracja;

import pl.edu.agh.eksploracja.domain.Location;

class DistanceCalculator
{
    static double distance(Double lat, Double lon, Location location) {
        Double lat2 = location.getLat();
        Double lon2 = location.getLng();
        if ((lat.equals(lat2)) && (lon.equals(lon2))) {
            return 0;
        }
        else {
            double theta = lon - lon2;
            double dist = Math.sin(Math.toRadians(lat)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;
            return (dist);
        }
    }
}
