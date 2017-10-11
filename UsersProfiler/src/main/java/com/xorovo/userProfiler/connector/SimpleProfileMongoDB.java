package com.xorovo.userProfiler.connector;

import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mysql.jdbc.Driver;
import com.xorovo.exception.DBEmptyException;

/**
 * @author Marco Arnone
 */
public class SimpleProfileMongoDB implements IDBItem
{
	private MongoOperations mongoOperationSource;
	private MongoOperations mongoOperationDestination;
	private ApplicationContext context;
	private JdbcTemplate jdbcTemplate;
	private Driver mySqlDriver;
	private Set<String> tags = new HashSet<String>();
	
	public SimpleProfileMongoDB()
	{
		try {
			this.setDataSource();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method takes care of connecting the two DB: Item DB and DB Profile.
	 * @throws SQLException 
	 * @throws UnknownHostException
	 */
	public void setDataSource() throws SQLException
	{
		context = new ClassPathXmlApplicationContext("spring-config.xml");
		mongoOperationSource = (MongoOperations)context.getBean("mongoTemplate1");
		mongoOperationDestination = (MongoOperations)context.getBean("mongoTemplate2");
		mySqlDriver = new com.mysql.jdbc.Driver();
		DataSource dataSource = new SimpleDriverDataSource(mySqlDriver, "jdbc:mysql://localhost/sistem", "root", "xzgjwl3");
        jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	public void addEvent(Event toAdd) throws DBEmptyException
	{
		DBObject tempSource, tempDestination;
		int tempVisits = 0;
		boolean tagFound;
		String tempTagSource;
		int tempScoreSource, tempScoreDestination;
		
		DBCollection collectionUsers, collectionVisits;
		DBCursor cursor, cursor2;
		
		BasicDBObject articleDBObject = new BasicDBObject();
		articleDBObject.put("originalId", Integer.toString(toAdd.getArticles()));
		articleDBObject.put("type", "categories");
		BasicDBObject visitDBObject = new BasicDBObject();
		visitDBObject.put("user", toAdd.getDevice());
		
		cursor = mongoOperationSource.getCollection("GJFO_tags").find(articleDBObject);
		collectionUsers = mongoOperationDestination.getCollection("GJFO_users");
		collectionVisits = mongoOperationDestination.getCollection("GJFO_visits");
		cursor2 = collectionVisits.find(visitDBObject);
		
		if(cursor2.hasNext())
			tempVisits = Integer.parseInt(cursor2.next().get("visits").toString());
		
		BasicDBObject visitsToInsert = new BasicDBObject();
		visitsToInsert.put("user", toAdd.getDevice());
		visitsToInsert.put("visits", tempVisits + 1);
		
		collectionVisits.remove(visitDBObject);
		collectionVisits.insert(visitsToInsert);
		
		while(cursor.hasNext())
		{			
			tempSource = cursor.next();
			tempTagSource = tempSource.get("tag").toString();
			tempScoreSource = (int) (Float.parseFloat(tempSource.get("score").toString()));
			
			if(!(tags.contains(tempTagSource)))
				tags.add(tempTagSource);
			
			//tagFound = false;
			
			BasicDBObject tagDBObject = new BasicDBObject();
			tagDBObject.put("userId", Integer.toString(toAdd.getDevice()));
			tagDBObject.put("tag", tempTagSource);

			/*cursor2 = collectionDestination.find(tagDBObject);
				
			while(cursor2.hasNext()) 
			{
				tempDestination = cursor2.next();
				tempScoreDestination = (int) (Float.parseFloat(tempDestination.get("score").toString()));

				BasicDBObject toInsert = new BasicDBObject();
				toInsert.put("userId", Integer.toString(toAdd.getId()));
				toInsert.put("tag", tempTagSource);
				toInsert.put("score", (tempScoreDestination + (tempScoreSource * toAdd.getNumber())));
				collectionDestination.remove(tempDestination);
				collectionDestination.insert(toInsert);
				tagFound = true;
			}*/
				
				/*if(!tagFound)
				{*/
					BasicDBObject toInsert = new BasicDBObject();
					toInsert.put("userId", Integer.toString(toAdd.getDevice()));
					toInsert.put("tag", tempTagSource);
					
					/*if(toAdd.getNumber() > 0)
						toInsert.put("score", (tempScoreSource * toAdd.getNumber()));
					else*/
						toInsert.put("score", tempScoreSource);
					
					collectionUsers.insert(toInsert);
				//}
		}
		
		
	}
	
	public void addTags()
	{
		String SQL = "replace into tags(tag) values ";
		
		for(String s : tags)
			SQL += "(\"" + s.replace("\"", "\\\"") + "\"),";

		jdbcTemplate.update(SQL.substring(0, SQL.length() - 1));
	}
	
	public void addProfiles(String tag)
	{
		float average, sigma;
		DBCursor cursor, cursor2;
		DBCollection visits = mongoOperationDestination.getCollection("GJFO_visits");
		DBCollection users = mongoOperationDestination.getCollection("GJFO_users");
		DBCollection profiles = mongoOperationDestination.getCollection("GJFO_profiles");
		DBObject temp, temp2;
		int tempScore = 0, sumScore = 0;
		String tempId;
		List<String> usersId = new ArrayList<String>();
		
		int count = 0;
		
		BasicDBObject tagDBObject = new BasicDBObject("tag", tag);
		cursor = users.find(tagDBObject);
		
		while(cursor.hasNext())
		{
			temp = cursor.next();
			tempId = temp.get("userId").toString();
			
			if(!(usersId.contains(tempId)))
			{
				usersId.add(tempId);
				
				BasicDBObject usersDBObject = new BasicDBObject();
				usersDBObject.put("userId", tempId);
				usersDBObject.put("tag", tag);
				sumScore = 0;
				
				cursor2 = users.find(usersDBObject);
				
				if(cursor2.size() > 1)
				{
					System.out.println(count++);
					
					while(cursor2.hasNext())
					{
						temp2 = cursor2.next();
						tempScore = (int) Integer.parseInt(temp2.get("score").toString());
						sumScore += tempScore;
					}
					
					average = sumScore / cursor2.size();
					cursor2 = users.find(usersDBObject);
					sigma = 0;
					
					while(cursor2.hasNext())
					{
						temp2 = cursor2.next();
						tempScore = (int) Integer.parseInt(temp2.get("score").toString());
						sigma += (Math.pow((double) (tempScore - average), (double) 2)) / (double) (cursor2.size() - 1);
					}
					
					BasicDBObject profileDBObject = new BasicDBObject();
					profileDBObject.put("userId", tempId);
					profileDBObject.put("tag", tag);
					profileDBObject.put("average", average);
					profileDBObject.put("sigma", sigma);
					profiles.insert(profileDBObject);
				}
			}
		}
	}
}