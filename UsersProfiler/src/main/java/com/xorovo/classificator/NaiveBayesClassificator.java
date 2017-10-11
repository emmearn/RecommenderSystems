package com.xorovo.classificator;

import java.sql.SQLException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class NaiveBayesClassificator
{
	private DBCollection collectionProfiles, collectionNaive;
	private MongoOperations mongoOperations;
	private ApplicationContext context;
	private DBCursor cursor;
	
	public NaiveBayesClassificator()
	{
		try {
			this.setDataSource();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		this.naiveBayes();
	}
	
	public void setDataSource() throws SQLException
	{
		context = new ClassPathXmlApplicationContext("spring-config.xml");
		mongoOperations = (MongoOperations)context.getBean("mongoTemplate2");
	}
	
	public void naiveBayes()
	{
		DBObject temp;
		String id, tag;
		float average, sigma;
		double value;
		
		collectionProfiles = mongoOperations.getCollection("GJFO_profiles");
		collectionNaive = mongoOperations.getCollection("GJFO_NaiveBayes");
		cursor = collectionProfiles.find();
		
		while(cursor.hasNext())
		{
			temp = cursor.next();
			id = temp.get("userId").toString();
			tag = temp.get("tag").toString();
			average = Float.parseFloat(temp.get("average").toString());
			sigma = Float.parseFloat(temp.get("sigma").toString());
			
			value = (1 / Math.sqrt(2 * Math.PI * sigma)) * Math.pow(Math.E, (-1) * (Math.pow((1 - average), 2) / (2 * sigma)));
			
			BasicDBObject toInsert = new BasicDBObject();
			toInsert.put("userId", id);
			toInsert.put("tag", tag);
			toInsert.put("GaussianVariable", value);
			
			collectionNaive.insert(toInsert);
		}
	}
	
	public static void main(String[] args)
	{
		new NaiveBayesClassificator();
	}
}
