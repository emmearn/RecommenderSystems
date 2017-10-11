package com.xorovo.raccomandater;

import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mysql.jdbc.Driver;
import com.xorovo.userProfiler.mapper.BayesMapper;

/**
 * @author Marco Arnone
 */
public class Raccomandator
{
	private MongoOperations mongoOperationArticles;
	private DBObject tempArticle;
	private ApplicationContext context;
	private DBCursor cursor;
	private JdbcTemplate jdbcTemplate;
	private Driver mySqlDriver;
	private int idUser;
	
	public Raccomandator(List<Integer> idArticles, int idUser)
	{
		try {
			this.setDataSource();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.idUser = idUser;
		Iterator<Integer> iter = idArticles.iterator();
		
		while(iter.hasNext())
			this.calculateScore(iter.next());
	}
	
	public void setDataSource() throws UnknownHostException, SQLException
	{
		context = new ClassPathXmlApplicationContext("spring-config.xml");
		mongoOperationArticles = (MongoOperations)context.getBean("mongoTemplate1");
		
		mySqlDriver = new com.mysql.jdbc.Driver();
		DataSource dataSource = new SimpleDriverDataSource(mySqlDriver, "jdbc:mysql://localhost/sistem", "root", "xzgjwl3");
        jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	public float calculateScore(int idArticle)
	{
		String tempTagArticle;
		int tempScoreTag;
		float score = 0;
		List<Float> rate = new ArrayList<Float>();
		String SQL;
		BasicDBObject articleDBObject = new BasicDBObject("originalId", Integer.toString(idArticle));
		
		cursor = mongoOperationArticles.getCollection("GJFO_tags").find(articleDBObject);
		
		while(cursor.hasNext())
		{
			tempArticle = cursor.next();
			tempTagArticle = tempArticle.get("tag").toString();
			tempScoreTag = (int) (Float.parseFloat(tempArticle.get("score").toString()));
			SQL = "select * from bayes where (tag_name = \"" + tempTagArticle + "\") and (user_id = \"" + this.idUser + "\")";
			rate = jdbcTemplate.query(SQL, new BayesMapper());
			if(rate.size() > 0)
				score = rate.get(0) * tempScoreTag;
		}
		
		return score;
	}
}
