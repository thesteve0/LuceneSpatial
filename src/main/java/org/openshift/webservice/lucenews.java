package org.openshift.webservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.openshift.lucene.FileHandler;
import org.openshift.lucene.QueryHandler;

@Path("/parks")
public class lucenews {
	
	QueryHandler queryHandler = new QueryHandler();
	
	@GET()
	@Produces("application/json")
    public List findParksWithin(@QueryParam("lat1") float lat1, @QueryParam("lon1") float lon1, @QueryParam("lat2") float lat2, @QueryParam("lon2") float lon2){
        
		List allParksList = queryHandler.getABoxOfPoints(lon2, lon1, lat2, lat1);
		
	    return allParksList;
	}
}