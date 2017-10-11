package com.xorovo.raccomandater;

import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mysql.jdbc.Driver;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

/**
 * @author Marco Arnone
 */
public class Classificator
{
	private MongoOperations mongoOperation;
	private ApplicationContext context;
	private DBCursor cursor, cursor2;
	private DBCollection collectionResult;
	private JdbcTemplate jdbcTemplate;
	private Driver mySqlDriver;

	public Classificator(float minimumScore)
	{
		List<UserCollection> result = new ArrayList<UserCollection>();
		try {
			this.setDataSource();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		result = this.searchTagBayesOverall(minimumScore);
		
		for(int i = 0; i < result.size(); i++)
			jdbcTemplate.update("INSERT INTO sistem.bayes_partial VALUES (\"" + result.get(i).getTag().replace("\"", "\\\"") + "\", \"" + result.get(i).getName() + "\", \"" + result.get(i).getScore() + "\");");
	}
	
	/**
	 * This method takes care of connecting the two DB: Item DB.
	 * @throws UnknownHostException
	 * @throws SQLException 
	 */
	public void setDataSource() throws UnknownHostException, SQLException
	{
		context = new ClassPathXmlApplicationContext("spring-config.xml");
		mongoOperation = (MongoOperations)context.getBean("mongoTemplate2");
		
		mySqlDriver = new com.mysql.jdbc.Driver();
		DataSource dataSource = new SimpleDriverDataSource(mySqlDriver, "jdbc:mysql://localhost/sistem", "root", "xzgjwl3");
        jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	public List<UserCollection> searchTag(String tagSearched, float minimumScore)
	{
		DBObject temp;
		String nameUser;
		int tempScore;
		List<UserCollection> users = new ArrayList<UserCollection>();
		Set<String> collectionNames = mongoOperation.getCollectionNames();
		Iterator<String> iter = collectionNames.iterator();
		
		while(iter.hasNext())
		{
			BasicDBObject tagDBObject = new BasicDBObject("tag", tagSearched);
			nameUser = iter.next();
			collectionResult = mongoOperation.getCollection(nameUser);
			cursor = collectionResult.find(tagDBObject);
			
			while(cursor.hasNext())
			{
				temp = cursor.next();
				tempScore = (int) (Float.parseFloat(temp.get("score").toString()));
				if(tempScore > minimumScore)
					users.add(new UserCollection(nameUser, tempScore));
			}
		}
		
		return this.sort(users);
	}
	
	public List<UserCollection> searchTagB(String tagSearched, float minimumScore)
	{
		int userCounter = 0;
		int tagsScoreSum = 0;
		int scoreTagSearched = 0;
		Set<String> tags = new HashSet<String>();
		DBObject temp;
		String nameUser;
		String tempTag;
		int tempScore;
		boolean tagFound;
		float rate;
		List<UserCollection> users = new ArrayList<UserCollection>();
		List<UserData> usersData = new ArrayList<UserData>();
		Set<String> collectionNames = mongoOperation.getCollectionNames();
		Iterator<String> iter = collectionNames.iterator();
		
		while(iter.hasNext())
		{
			userCounter++;
			nameUser = iter.next();
			collectionResult = mongoOperation.getCollection(nameUser);
			cursor = collectionResult.find();
			tagFound = false;
			
			while(cursor.hasNext())
			{
				temp = cursor.next();
				if(temp.containsField("tag"))
				{
					tempTag = temp.get("tag").toString();
					if(!(tags.contains(tempTag)))
							tags.add(tempTag);
					tempScore = (int) (Float.parseFloat(temp.get("score").toString()));
					tagsScoreSum += tempScore;
					if(tempTag.equalsIgnoreCase(tagSearched))
					{
						scoreTagSearched = tempScore;
						tagFound = true;
					}
				}
			}
			
			if(nameUser.contains("U") && (tagFound == true))
				usersData.add(new UserData(nameUser, scoreTagSearched, tagsScoreSum));
			
			tagsScoreSum = 0;
			scoreTagSearched = 0;
		}
		
		userCounter--; //dovuto alla forzata inizializzazione a 0 che ha sfasato di 1 il conteggio
		
		for(int i = 0; i < usersData.size(); i++)
			if(usersData.get(i).getTagScore() != 0)
			{
				rate = this.bayes(usersData.get(i).getTagScore(), usersData.get(i).getSumTagsScore(), userCounter, tags.size());
				if(rate > minimumScore)
					users.add(new UserCollection(usersData.get(i).getId(), rate));
			}

		return this.sort(users);
	}
	
	public List<UserCollection> searchTagBayes(String tagSearched, float minimumScore)
	{
		Set<String> usersName = mongoOperation.getCollectionNames();
		String userName;
		boolean tagFound;
		DBObject temp;
		String tempTag;
		Set<String> tags = new HashSet<String>();
		int tempScore;
		int tagsScoreSum = 0;
		int scoreTagSearched = 0;
		List<UserData> usersData = new ArrayList<UserData>();
		List<UserCollection> users = new ArrayList<UserCollection>();
		float rate;
		
		Iterator<String> iter = usersName.iterator();
		
		while(iter.hasNext())
		{
			userName = iter.next();
			BasicDBObject tagDBObject = new BasicDBObject("tag", tagSearched);
			
			if(!(userName.startsWith("U")))
				usersName.remove(userName);
			else
			{
				collectionResult = mongoOperation.getCollection(userName);
				cursor = collectionResult.find(tagDBObject);
				
				while(cursor.hasNext())
				{
					cursor.next();
					cursor2 = collectionResult.find();
					tagFound = false;
					
					while(cursor2.hasNext())
					{
						temp = cursor2.next();
						tempTag = temp.get("tag").toString();
						if(!(tags.contains(tempTag)))
								tags.add(tempTag);
						tempScore = (int) (Float.parseFloat(temp.get("score").toString()));
						tagsScoreSum += tempScore;
						if(tempTag.equalsIgnoreCase(tagSearched))
						{
							scoreTagSearched = tempScore;
							tagFound = true;
						}
					}
					
					if(tagFound == true)
						usersData.add(new UserData(userName, scoreTagSearched, tagsScoreSum));
					
					tagsScoreSum = 0;
					scoreTagSearched = 0;
				}
			}
		}
		
		for(int i = 0; i < usersData.size(); i++)
			if(usersData.get(i).getTagScore() != 0)
			{
				rate = this.bayes(usersData.get(i).getTagScore(), usersData.get(i).getSumTagsScore(), usersName.size(), tags.size());
				if(rate > minimumScore)
					users.add(new UserCollection(usersData.get(i).getId(), rate));
			}

		return this.sort(users);
	}
	
	public List<UserCollection> searchTagBayesOverall(float minimumScore)
	{
		Set<String> usersName = mongoOperation.getCollectionNames();
		String userName;
		DBObject temp;
		String tempTag;
		Set<String> tags = new HashSet<String>();
		int tempScore;
		int tagsScoreSum = 0;
		List<dataUsers> usersData = new ArrayList<dataUsers>();
		List<UserCollection> users = new ArrayList<UserCollection>();
		float rate;
		
		Iterator<String> iter = usersName.iterator();
		
		while(iter.hasNext())
		{
			userName = iter.next();
			List<TagWeighed> tagsWeighed = new ArrayList<TagWeighed>();
			
			if(!(userName.startsWith("U")))
				usersName.remove(userName);
			else
			{
				collectionResult = mongoOperation.getCollection(userName);
				cursor2 = collectionResult.find();
				
				while(cursor2.hasNext())
				{
					temp = cursor2.next();
					tempTag = temp.get("tag").toString();
					if(!(tags.contains(tempTag)))
							tags.add(tempTag);
					tempScore = (int) (Float.parseFloat(temp.get("score").toString()));
					tagsScoreSum += tempScore;
					tagsWeighed.add(new TagWeighed(tempTag, tempScore));
				}
					
				for(int i = 0; i < tagsWeighed.size(); i++)
					usersData.add(new dataUsers(userName, tagsWeighed.get(i).getTag(), tagsWeighed.get(i).getScore(), tagsScoreSum));
				
				tagsScoreSum = 0;
			}
		}
		
		for(int i = 0; i < usersData.size(); i++)
			if(usersData.get(i).getTagScore() != 0)
			{
				rate = this.bayes(usersData.get(i).getTagScore(), usersData.get(i).getSumTagsScore(), usersName.size(), tags.size());
				if(rate > minimumScore)
					users.add(new UserCollection(usersData.get(i).getId(), usersData.get(i).getTagName(), rate));
			}

		return users;
	}
	
	public List<UserCollection> searchTagBayesOverall2(float minimumScore)
	{
		Set<String> usersName = mongoOperation.getCollectionNames();
		String userName;
		DBObject temp;
		String tempTag;
		Set<String> tags = new HashSet<String>();
		int tempScore;
		int tagsScoreSum = 0;
		List<dataUsers> usersData = new ArrayList<dataUsers>();
		List<UserCollection> users = new ArrayList<UserCollection>();
		float rate;
		
		Iterator<String> iter = usersName.iterator();
		
		while(iter.hasNext())
		{
			userName = iter.next();
			List<TagWeighed> tagsWeighed = new ArrayList<TagWeighed>();
			
			if(!(userName.startsWith("U")))
				usersName.remove(userName);
			else
			{
				collectionResult = mongoOperation.getCollection(userName);
				cursor2 = collectionResult.find();
				
				while(cursor2.hasNext())
				{
					temp = cursor2.next();
					tempTag = temp.get("tag").toString();
					if(!(tags.contains(tempTag)))
							tags.add(tempTag);
					tempScore = (int) (Float.parseFloat(temp.get("score").toString()));
					tagsScoreSum += tempScore;
					tagsWeighed.add(new TagWeighed(tempTag, tempScore));
				}
					
				for(int i = 0; i < tagsWeighed.size(); i++)
					usersData.add(new dataUsers(userName, tagsWeighed.get(i).getTag(), tagsWeighed.get(i).getScore(), tagsScoreSum));
				
				tagsScoreSum = 0;
			}
		}
		
		for(int i = 0; i < usersData.size(); i++)
			if(usersData.get(i).getTagScore() != 0)
			{
				rate = this.bayes(usersData.get(i).getTagScore(), usersData.get(i).getSumTagsScore(), usersName.size(), tags.size());
				if(rate > minimumScore)
					users.add(new UserCollection(usersData.get(i).getId(), usersData.get(i).getTagName(), rate));
			}

		return users;
	}
	
	public List<UserCollection> sort(List<UserCollection> toOrdered)
	{
		int indexBiggest = 0;
		List<UserCollection> ordered = new ArrayList<UserCollection>();
		
		while(toOrdered.size() > 0)
		{
			for(int i = 1; i < toOrdered.size(); i++)
				if(toOrdered.get(i).isGreaterThan(toOrdered.get(indexBiggest)))
					indexBiggest = i;
			
			ordered.add(toOrdered.get(indexBiggest));
			toOrdered.remove(indexBiggest);
			indexBiggest = 0; //reset
		}
		
		return ordered;
	}


	public float bayes(float tagScore, float sumTagsScore, float userCounter, float tagCounter)
	{
		return (float) (((1.0 / userCounter) * (tagScore / sumTagsScore)) / (1.0 / tagCounter));
	}
	
	public static void main(String[] args)
	{
		//new Raccomandater("scienza", 0f);
		Classificator prova = new Classificator(0f);
	}
}
