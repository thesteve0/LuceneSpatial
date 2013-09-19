package org.openshift.webservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.UniqueConstraint;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.openshift.lucene.FileHandler;
import org.openshift.lucene.QueryHandler;

@Path("/parks")
public class lucenews {
	
	QueryHandler queryHandler = new QueryHandler();
	
	//TODO why does injection work here but not in QueryHandler
	@Inject
	private FileHandler fileHandler;
	
	///////////get parks near a coord  circle?lat=37.5&lon=-83.0&radius=3 and a radius in degrees
	@GET()
	@Produces("application/json")
	@Path("circle")
	public List findParksNear(@QueryParam("lat") float lat, @QueryParam("lon") float lon, @QueryParam("radius")float radius){
		ArrayList<Map> allParksList = new ArrayList<Map>();
		
			
		allParksList = (ArrayList) queryHandler.getParksNear(lat, lon, radius, fileHandler);
		
		return allParksList;
	}
	
	///////////get parks near a coord sorted by distance nearpoint?lat=37.5&lon=-83.0&number=10 number of results to return
	@GET()
	@Produces("application/json")
	@Path("nearpoint")
	public List findNumNear(@QueryParam("lat") float lat, @QueryParam("lon") float lon, @QueryParam("number")int numberResults){
		ArrayList<Map> allParksList = new ArrayList<Map>();
		
			
		allParksList = (ArrayList) queryHandler.getNumNear(lat, lon, numberResults, fileHandler);
		
		return allParksList;
	}
	
	//Now handle a distance and string query - going to return all the results in sorted distance order
	//nearpoint/washington?lat=37.5&lon=-83.0
	@GET()
	@Produces("application/json")
	@Path("nearpoint/{name}")
	public List findNameNear(@PathParam("name") String name, @QueryParam("lat") float lat, @QueryParam("lon") float lon){
		ArrayList<Map> allParksList = new ArrayList<Map>();
		
		allParksList = (ArrayList) queryHandler.getNameNear(lat, lon, name, fileHandler);
		
		return allParksList;
	}
	
	
	@GET()
	@Produces("application/json")
	@Path("within")
    public List findParksWithin(@QueryParam("lat1") float lat1, @QueryParam("lon1") float lon1, @QueryParam("lat2") float lat2, @QueryParam("lon2") float lon2){
        
		List allParksList = queryHandler.getABoxOfPoints(lon2, lon1, lat2, lat1, fileHandler);
		
	    return allParksList;
	}
	
	@GET()
	@Produces("application/json")
	public List getAllParks(){
		ArrayList allParksList = new ArrayList();
		
		allParksList = (ArrayList) queryHandler.getAllParks(fileHandler);
		
		return allParksList;
	}
}