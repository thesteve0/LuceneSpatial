package org.openshift.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.impl.RectangleImpl;

public class QueryHandler {
	
	//@Inject
	//private FileHandler fileHandler;
	
	private SpatialStrategy strategy;
	private SpatialContext spatialContext;
	
	
	private void setupSpatial(){
		this.spatialContext = new SpatialContext(true);

		//We also need a lucene strategy that matches the strategy used to build the index
		SpatialPrefixTree grid = new GeohashPrefixTree(spatialContext, 11);
		this.strategy  = new RecursivePrefixTreeStrategy(grid, "position");
	}



	
	public List getABoxOfPoints(double minX, double maxX, double minY, double maxY, FileHandler fileHandler){
		ArrayList<Map> allParksList = new ArrayList<Map>();
		this.setupSpatial();
		
		//TODO - dummy values
		minX = -110.0;
		maxX = -100.0;
		maxY = 40.0;
		minY = 30.0;
		
		//ok time to make some spatial stuff
		//Make a rectangle to do the search - the spatial context will generate it
		Shape ourRectangle =  new RectangleImpl(minX, maxX, minY, maxY, spatialContext);
		
		//ok now to make a spatial args
		SpatialArgs spatialArgs = new SpatialArgs(SpatialOperation.Intersects, ourRectangle);
		
		Filter filter = strategy.makeFilter(spatialArgs);
		IndexSearcher searcher = fileHandler.getIndexSearcher();
		
		
		try {
			//we set the number of docs returned to some number higher than the complete number of docs
			//Using a filter with a Query provides a way to further refine the query. Since spatial is only
			// available as a filter we query all docs and then filter them by the spatial criteria. 
			
			TopDocs returnedDocs  = searcher.search(new MatchAllDocsQuery(), filter, 700);
			int numDoc = returnedDocs.totalHits;
			
			for (int i = 0; i < numDoc; i++){
				Document doc1 = searcher.doc(returnedDocs.scoreDocs[i].doc); 
				//System.out.println("some doc stuff: " + doc1.toString());
				//put the name in the hashmap
				HashMap park = new HashMap();
				park.put("name", doc1.get("name"));
				
				//now get the coords and put them in an array list
				ArrayList<Float> coords = new ArrayList<Float>();
				String coordinates = doc1.get("coords");
				String[] coordinateArray = coordinates.split(" ");
				
				//TODO check these are in the right order given leaflet
				coords.add(new Float(coordinateArray[0]));
				coords.add(new Float(coordinateArray[1]));
				
				park.put("position", coords);

				System.out.println("just to add a debug");
				
				allParksList.add(park);
			}
			
			
		} catch (Exception e) {
			System.out.println("Exception searching: " + e.getClass() + " :: " + e.getMessage());
		}
		
		return allParksList;
	}

}
