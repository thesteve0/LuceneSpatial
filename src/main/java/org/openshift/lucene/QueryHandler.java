package org.openshift.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.lucene.util.Version;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.impl.CircleImpl;
import com.spatial4j.core.shape.impl.PointImpl;
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


	public List getAllParks(FileHandler fileHandler){
		
		
		ArrayList<Map> allParksList = new ArrayList<Map>();
		try {
			
			IndexSearcher searcher = fileHandler.getIndexSearcher();
			
			TopDocs returnedDocs  = searcher.search(new MatchAllDocsQuery(), searcher.getIndexReader().numDocs());
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
				
				coords.add(new Float(coordinateArray[0]));
				coords.add(new Float(coordinateArray[1]));
				
				park.put("position", coords);

				
				allParksList.add(park);
			}
			
			
		} catch (Exception e) {
			System.out.println("Exception searching: " + e.getClass() + " :: " + e.getMessage());
		}
		return allParksList;
	}
	
	///////////get parks near a coord  ?lat=37.5&lon=-83.0&radius  in degrees
	public List getParksNear(double lat, double lon, double radius, FileHandler fileHandler){
		ArrayList<Map> allParksList = new ArrayList<Map>();
		
		//get the spatial stuff all set up
		this.setupSpatial();
		
		//ok time to make some spatial stuff
		//Gonna make a circle to do our search
		Point ourCenterPoint = new PointImpl(lon, lat, spatialContext);
		Shape ourCircle =  new CircleImpl(ourCenterPoint, radius, spatialContext);
		
		//ok now to make a spatial args
		SpatialArgs spatialArgs = new SpatialArgs(SpatialOperation.Intersects, ourCircle);
		
		Filter filter = strategy.makeFilter(spatialArgs);
		IndexSearcher searcher = fileHandler.getIndexSearcher();
		
		
		try {
			
			
			TopDocs returnedDocs  = searcher.search(new MatchAllDocsQuery(), filter, searcher.getIndexReader().numDocs());
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

				
				allParksList.add(park);
			}
			
			
		} catch (Exception e) {
			System.out.println("Exception searching: " + e.getClass() + " :: " + e.getMessage());
		}

		
		return allParksList;
	}
	
	

public List getNumNear(double lat, double lon, int numberResults, FileHandler fileHandler){
	ArrayList<Map> allParksList = new ArrayList<Map>();
	
	//get the spatial stuff all set up
	this.setupSpatial();
	IndexSearcher searcher = fileHandler.getIndexSearcher();
	
	//ok time to make some spatial stuff
	//point to center our search
	Point ourCenterPoint = new PointImpl(lon, lat, spatialContext);
	
	//Now to make the function to get distance from the point - going to be in KM
	//double degToKm = DistanceUtils.degrees2Dist(1, DistanceUtils.EARTH_MEAN_RADIUS_KM);
	ValueSource valueSource = strategy.makeDistanceValueSource(ourCenterPoint);
	
	//No filter needed because we are going to return all the docs in sort order and then return x number
	
	try {
		
		//now to make the sort function to pass into the search
		Sort distSort = new Sort(valueSource.getSortField(false)).rewrite(searcher);
		
		TopDocs returnedDocs  = searcher.search(new MatchAllDocsQuery(), numberResults, distSort);
		
		
		for (int i = 0; i < numberResults; i++){
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

			
			allParksList.add(park);
		}
		
		
	} catch (Exception e) {
		System.out.println("Exception searching: " + e.getClass() + " :: " + e.getMessage());
	}

	
	return allParksList;
}

	public List getNameNear(double lat, double lon, String name, FileHandler fileHandler){
		ArrayList<Map> allParksList = new ArrayList<Map>();
		
		//get the spatial stuff all set up
		this.setupSpatial();
		IndexSearcher searcher = fileHandler.getIndexSearcher();
		
		//ok time to make some spatial stuff
		//point to center our search
		Point ourCenterPoint = new PointImpl(lon, lat, spatialContext);
		
		//Now to make the function to get distance from the point - going to be in KM
		//double degToKm = DistanceUtils.degrees2Dist(1, DistanceUtils.EARTH_MEAN_RADIUS_KM);
		ValueSource valueSource = strategy.makeDistanceValueSource(ourCenterPoint);
		
		//No filter needed because we are going to return all the docs in sort order and then return x number
		
		try {
			
			//now to make the sort function to pass into the search
			Sort distSort = new Sort(valueSource.getSortField(false)).rewrite(searcher);
			
			//set up the things needed for a text search
			String nameField = "name";
			//the analyzer needs to match the analyzer used to build the index
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
			QueryParser queryParser = new QueryParser(Version.LUCENE_44, nameField, analyzer);
			
			//Ok time to parse the query
			Query query = queryParser.parse(name);
			
			//get how many documents there are in the index
			
			int numDocs = (int) searcher.collectionStatistics("name").docCount();
			
			System.out.println("Query = " + query + " :: numDocs = " + numDocs );
			//do the search
			TopDocs returnedDocs  = searcher.search(query, numDocs,  distSort);
			
			//How many docs did we match
			int numHits = returnedDocs.totalHits;

			
			for (int i = 0; i < numHits; i++){
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

				allParksList.add(park);
			}
			
			
		} catch (Exception e) {
			System.out.println("Exception searching: " + e.getClass() + " :: " + e.getMessage());
		}

		
		return allParksList;
		
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
			
			TopDocs returnedDocs  = searcher.search(new MatchAllDocsQuery(), filter, searcher.getIndexReader().numDocs());
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

				
				allParksList.add(park);
			}
			
			
		} catch (Exception e) {
			System.out.println("Exception searching bounding box: " + e.getClass() + " :: " + e.getMessage());
		}
		
		return allParksList;
	}

}
