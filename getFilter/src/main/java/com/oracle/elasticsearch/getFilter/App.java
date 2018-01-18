package com.oracle.elasticsearch.getFilter;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.json.*;

import java.util.*;

public class App 
{
	
// SAMPLE INPUT
	
//    	String category = "Mobile Phone"; 
//    	String filterJson = "{\r\n" + 
//    			"	\"Price\" : {\r\n" + 
//    			"		\"gte\" = 10000,\r\n" + 
//    			"		\"lte\" = 50000\r\n" + 
//    			"	},\"Brand\" : [\"Samsung\", \"One Plus\", \"Motorolla\"],\r\n" + 
//    			"	\"Screen size\" : 5.5\r\n" + 
//    			"}";
//    	
//    String sortJson = "{\r\n" + 
//    		"	\"Price\" : \"desc\",\r\n" + 
//    		"	\"RAM\" : \"desc\"\r\n" + 
//    		"}";
      
 
        
 
    
    public static String getFilterResult(String category, String filterJson) {
    	return getFilterResult(category, filterJson, "");
    }
    
    public static String getFilterResult(String category, String filterJson, String sortJson) {
    	String result = "";
    	String query = buildQuery(category, filterJson, sortJson);
    	String queryResult = executeQuery(query);
    	
    	try {
			JSONObject jObj = new JSONObject(queryResult);
			result = ((JSONArray) ((JSONObject) jObj.get("hits")).getJSONArray("hits")).toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return result;
    }
    
    private static String buildQuery(String category, String filterJson, String sortJson) {
    	String begin = "{\r\n";
    	String beginQuery =  "  \"query\": {\r\n" + 
    			"    \"bool\": {\r\n" + 
    			"      \"must\": [\r\n" + 
    			"        {\r\n" + 
    			"          \"match\": {\r\n" + 
    			"            \"category.keyword\": \"" + category + "\"\r\n" + 
    			"          }\r\n" + 
    			"        }\r\n" + 
    			"      ]";
    	
    	String endQuery = "    }\r\n" + 
    			"  }\r\n";
    	
    	String end = "}";
    	
    	ArrayList<String> filter = new ArrayList<String>();
    	String beginFilterQuery = ", \"filter\": [";
    	String mainFilterQuery = "";
    	String endFilterQuery = "]";
    	String beginSortQuery = "";
    	String mainSortQuery = "";
    	String endSortQuery = "";
    	String result = "";
    	
    	try {
			JSONObject jobj = new JSONObject(filterJson);
			Iterator it = jobj.keys();
			while (it.hasNext()) {
				String key = (String) (it.next());
				Object value = jobj.get(key);
				if (value instanceof JSONObject) {
					long gte = -1;
					long lte = -1;
					String mainRange = "";
					String beginRange =  "{" + "\"range\": {\r\n" + 
							"          \"" + key + "\": {";
					String endRange = " }\r\n" + 
							" }       }";
					Iterator tempIt = ((JSONObject) value).keys();
					while (tempIt.hasNext()) {
						String valueKey = (String) tempIt.next();
						if (valueKey.equals("gte")) {
							gte = ((JSONObject) value).getLong("gte");
							if (mainRange == "")
								mainRange += "\"gte\": " + Long.toString(gte);
							else
								mainRange += ",\r\n" + 
										"            \"gte\": " + Long.toString(gte);
						} else if (valueKey.equals("lte")) {
							lte = ((JSONObject) value).getLong("lte");
							if (mainRange == "")
								mainRange += "\"lte\": " + Long.toString(lte);
							else
								mainRange += ",\r\n" + 
										"            \"lte\": " + Long.toString(lte);
						} else {
							System.out.println("JSON Object is not in proper format");
						}
					}
					filter.add(beginRange + mainRange + endRange);
				} else if (value instanceof JSONArray) {
					if (((JSONArray) value).get(0) instanceof JSONObject) {
						String beginBoolQuery = "{\r\n" + 
								"        \"bool\" : {\r\n" + 
								"          \"should\" : [";
						String endBoolQuery = "]\r\n" + 
								"        }\r\n" + 
								"      }";
						String mainBoolQuery = "";
						for (int i = 0; i< ((JSONArray) value).length(); i++) {
							String beginRangeBool = "{\r\n" + 
									"            \"range\" : {\r\n" + 
									"              \"" + key +  "\" : {";
							String endRangeBool = "}\r\n" + 
									"            }\r\n" + 
									"          }";
							String mainRangeBool = "";
							Iterator tempIt = ((JSONObject) ((JSONArray) value).get(i)).keys();
							while (tempIt.hasNext()) {
								String valueKey = (String) tempIt.next();
								long gte = -1;
								long lte = -1;
								if (valueKey.equals("gte")) {
									gte = ((JSONObject) ((JSONArray) value).get(i)).getLong("gte");
									if (mainRangeBool == "")
										mainRangeBool += "\"gte\": " + Long.toString(gte);
									else
										mainRangeBool += ",\r\n" + 
												"            \"gte\": " + Long.toString(gte);
								} else if (valueKey.equals("lte")) {
									lte = ((JSONObject) ((JSONArray) value).get(i)).getLong("lte");
									if (mainRangeBool == "")
										mainRangeBool += "\"lte\": " + Long.toString(lte);
									else
										mainRangeBool += ",\r\n" + 
												"            \"lte\": " + Long.toString(lte);
								} else {
									System.out.println("JSON Object is not in proper format");
								}
							}
							if (mainBoolQuery.equals("")) {
								mainBoolQuery += (beginRangeBool + mainRangeBool + endRangeBool);
							} else 
								mainBoolQuery += ("," + beginRangeBool + mainRangeBool + endRangeBool);
						}
						filter.add(beginBoolQuery + mainBoolQuery + endBoolQuery);
					} else if (((JSONArray) value).get(0) instanceof String) {
						String beginTermsQuery = "{\r\n" + 
								"        \"terms\" :{\"" + key +".keyword\" : [";
						String mainTermsQuery = "";
						String endTermsQuery = "]}\r\n" + 
								"      }";
						for (int i = 0 ; i< ((JSONArray) value).length(); i++) {
							String term = ((JSONArray) value).getString(i);
							if (mainTermsQuery.equals(""))
								mainTermsQuery += ("\"" + term + "\"");
							else
								mainTermsQuery += (", \"" + term + "\"");
						}
						filter.add(beginTermsQuery + mainTermsQuery + endTermsQuery);
					} else {
						System.out.println("JSON Object is not in proper format");
					}
				} else {
					String beginTermQuery = "";
					String mainTermQuery = "";
					String endTermQuery = "}\r\n" + 
							"      }";
					if (value instanceof String) {
						mainTermQuery += "\"" + value + "\"";
						beginTermQuery += "{\r\n" + 
								"        \"term\" : {\"" + key + ".keyword\" : ";
					} else if (value instanceof Double){
						beginTermQuery += "{\r\n" + 
								"        \"term\" : {\"" + key + "\" : ";
						mainTermQuery += Double.toString((Double) value);
					} else {
						beginTermQuery += "{\r\n" + 
								"        \"term\" : {\"" + key + "\" : ";
						mainTermQuery += Integer.toString((Integer) value);
					}
					filter.add(beginTermQuery + mainTermQuery + endTermQuery);
				}
			}
			
			for (String s : filter) {
				if (mainFilterQuery.equals(""))
					mainFilterQuery += s;
				else
					mainFilterQuery += (", " + s);
			}
			
			if (!(sortJson.equals(""))) {
				beginSortQuery += ", \"sort\": [";
				endSortQuery += "  ]";
				String sortBuilder = "";
				
				JSONObject sortJobj = new JSONObject(sortJson);
				Iterator sortIt = sortJobj.keys();
				while (sortIt.hasNext()) {
					sortBuilder = "";
					String sortKey = (String) sortIt.next();
					sortBuilder += "{\r\n" + 
							"      \"" + sortKey + "\": {\r\n" + 
							"        \"order\": \"" + sortJobj.getString(sortKey) +"\"\r\n" + 
							"      }\r\n" + 
							"    }";
					if (mainSortQuery.equals(""))
						mainSortQuery += sortBuilder;
					else
						mainSortQuery += (", " + sortBuilder);
				}
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	result = begin + beginQuery + beginFilterQuery + mainFilterQuery + endFilterQuery + endQuery + beginSortQuery + mainSortQuery + endSortQuery + end;
    	return result;
    }
    
    private static String executeQuery(String query) {
    	Map<String, String> params = Collections.emptyMap();
    	RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http"),
    	        new HttpHost("localhost", 9201, "http")).build();
    	String queryResult = "";
    	
    	try {
    		HttpEntity entity = new NStringEntity(query , ContentType.APPLICATION_JSON);
			Response response = restClient.performRequest("GET", "/product/_search" , params, entity);
			queryResult +=  (EntityUtils.toString(response.getEntity()));
			
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return queryResult;
    }
}

