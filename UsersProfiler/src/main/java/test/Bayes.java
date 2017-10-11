package test;

import java.sql.SQLException;
import java.util.ArrayList;
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

public class Bayes
{
	private MongoOperations mongoOperations;
	private ApplicationContext context;
	private JdbcTemplate jdbcTemplate;
	private Driver mySqlDriver;
	
	public Bayes(String tagSearched)
	{
		try {
			this.setDataSource();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.generatesList(tagSearched);
	}
	
	public void setDataSource() throws SQLException
	{
		context = new ClassPathXmlApplicationContext("spring-config.xml");
		mongoOperations = (MongoOperations)context.getBean("mongoTemplate2");
		mySqlDriver = new com.mysql.jdbc.Driver();
		DataSource dataSource = new SimpleDriverDataSource(mySqlDriver, "jdbc:mysql://localhost/sistem", "root", "xzgjwl3");
        jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	public void generatesList(String tagSearched)
	{
		DBCursor cursor, cursor2;
		DBObject temp;
		BasicDBObject searched = new BasicDBObject();
		int sumVisits = 0, id;
		double gauss, bayes, pU, pT;
		List<String> tags = new ArrayList<String>();
		List<UserBayesed> users = new ArrayList<UserBayesed>();
		
		String SQL = "select * from tags";
		tags = jdbcTemplate.query(SQL, new TagMapper());
		cursor = mongoOperations.getCollection("GJFO_visits").find();
		
		while(cursor.hasNext())
		{
			temp = cursor.next();
			sumVisits += Integer.parseInt(temp.get("visits").toString());
		}
		
		searched.put("tag", tagSearched);
		cursor = mongoOperations.getCollection("GJFO_NaiveBayes").find();
		
		while(cursor.hasNext())
		{
			temp = cursor.next();
			id = Integer.parseInt(temp.get("userId").toString());
			gauss = Float.parseFloat(temp.get("GaussianVariable").toString());
			
			BasicDBObject userSearched = new BasicDBObject();
			userSearched.put("user", id);
			cursor2 = mongoOperations.getCollection("GJFO_visits").find(userSearched);
			
			while(cursor2.hasNext())
			{
				pU = Double.parseDouble(cursor2.next().get("visits").toString())/ (double) sumVisits;
				pT = 1.0 / (double) tags.size();
				bayes = (gauss * pU) / (pT);
				users.add(new UserBayesed(id, bayes));
				System.out.println(sumVisits);
			}
		}
		
		for(UserBayesed u : users)
			System.out.println(u);
	}
	
	public static void main(String[] args)
	{
		new Bayes("scienze");
	}
}
