package com.xorovo.userProfiler.connector;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import com.mysql.jdbc.Driver;
import com.xorovo.userProfiler.mapper.EventMapper;
import com.xorovo.userProfiler.mapper.CounterMapper;
import com.xorovo.userProfiler.mapper.IDEventMapper;

/**
 * @author Marco Arnone
 */
public class SimpleTrackingSQLDB implements IDBTracking
{
	private Driver mySqlDriver;
	private JdbcTemplate jdbcTemplate, jdbcTemplate2;
	private String SQL;
	private int eventCounter;
	
	public SimpleTrackingSQLDB()
	{
		jdbcTemplate = this.setDataSource("jdbc:mysql://vp-pro1.xorovo.com/viewerplus_tracking", "vp", "z5x6c7v8b9");
		jdbcTemplate2 = this.setDataSource("jdbc:mysql://localhost/sistem", "root", "xzgjwl3");
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
	
	public List<Event> getEvent()
	{
		//this.setEventCounter();
		/*SQL = "select count(ev.counter), a.article, ev.device_id from " +
			"(select e.id as counter, e.device_id, e.page from event e where page is not null and e.ended = 1 and e.duration >= 20 and e.time >= '2013-01-01 00:00:00' and e.id >= " + this.eventCounter + " and e.id <= 10000000) ev " +
			"left join " +
			"(select ap.article_id as article, ap.page_id as page from article_page ap where ap.article_id is not null group by ap.article_id order by ap.page_id) a " +
			"on ev.page = a.page where a.article is not null group by a.article, ev.device_id;";*/
		
		SQL = "select e.id, artp.article_id, e.device_id " +
				"from event e " +
				"left join (select ap.article_id as article_id, ap.page_id as page_id " +
				"from article_page ap " +
				"where ap.article_id is not null group by ap.article_id order by ap.page_id) artp " +
				"on e.page = artp.page_id " +
				"left join article a on a.id = artp.article_id " +
				"left join issue i on a.issue_id = i.id " +
				"where page is not null and e.ended = 1 and e.duration >= 20 and i.magazine_id = 'GJFO' and e.time >= '2013-01-01 00:00:00' limit 0,100000;";
		return jdbcTemplate.query(SQL, new EventMapper());
	}
	
	public void updateCounter()
	{
		SQL = "select max(id) from event where id <= 10000000";
		this.eventCounter = jdbcTemplate.query(SQL, new IDEventMapper()).get(0);
		SQL = "update eventCounter set counter = " + this.eventCounter + " where DBname = 'Focus'";
		jdbcTemplate2.update(SQL);
	}
	
	public void setEventCounter()
	{
		SQL = "select counter from eventCounter where DBname = 'Focus'";
		this.eventCounter = jdbcTemplate2.query(SQL, new CounterMapper()).get(0);
	}
}

