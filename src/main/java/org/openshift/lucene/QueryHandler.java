package org.openshift.lucene;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.impl.RectangleImpl;

public class QueryHandler {
	
	@Inject
	private FileHandler fileHandler;
	
	SpatialContext spatialContext = new SpatialContext(true);

	
	public List getABoxOfPoints(double minX, double maxX, double minY, double maxY){
		ArrayList<Map> allParksList = new ArrayList<Map>();
		
		//ok time to make some spatial stuff
		//Make a rectangle to do the search - the spatial context will generate it
		Shape ourRectangle =  new RectangleImpl(minX, maxX, minY, maxY, spatialContext);
		
		//ok now to make a spatial args
		SpatialArgs spatialArgs = new SpatialArgs(SpatialOperation., shape)
		
		return allParksList;
	}

}
