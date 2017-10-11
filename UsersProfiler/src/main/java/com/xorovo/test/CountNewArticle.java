package com.xorovo.test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import com.mysql.jdbc.Driver;

public class CountNewArticle
{
	private Driver mySqlDriver;
	private JdbcTemplate jdbcTemplate;
	private String SQL;
	
	public CountNewArticle()
	{
		jdbcTemplate = this.setDataSource("jdbc:mysql://vp-pro1.xorovo.com/viewerplus_tracking", "vp", "z5x6c7v8b9");
	}
	
	public JdbcTemplate setDataSource(String url, String username, String password)
    {
            try {
				mySqlDriver = new com.mysql.jdbc.Driver();
			} catch (SQLException e) {
				e.printStackTrace();
			}
            DataSource dataSource = new SimpleDriverDataSource(mySqlDriver, url, username,  password);
            return new JdbcTemplate(dataSource);
    }
	
	public List<Integer> getEvent(int min, int max)
	{
		SQL = "select distinct(article) from " +
			"(select e.id as counter, e.device_id, e.page from event e where page is not null and e.ended = 1 and e.duration >= 20 and e.time >= '2013-01-01 00:00:00' and e.id >= " + min + " and e.id <= " + max + ") ev " +
			"left join " +
			"(select ap.article_id as article, ap.page_id as page from article_page ap where ap.article_id is not null group by ap.article_id order by ap.page_id) a " +
			"on ev.page = a.page where a.article is not null group by a.article, ev.device_id;";
		return jdbcTemplate.query(SQL, new ArticleMapper());
	}
	
	public static void main(String[] args)
	{
		CountNewArticle counter = new CountNewArticle();
		List<Integer> oldArticle = counter.getEvent(0, 10000000);
		List<Integer> newArticle = counter.getEvent(10000000, 12000000);
		List<Integer> notPresentArticle = new ArrayList<Integer>();
		
		for(Integer a : newArticle)
			if(!(oldArticle.contains(a)))
			{
				notPresentArticle.add(a);
				System.out.println(a);
			}
	}
}
