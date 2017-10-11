
package com.xorovo.userProfiler;

import com.xorovo.exception.DBEmptyException;
import com.xorovo.userProfiler.connector.Event;
import com.xorovo.userProfiler.connector.IDBItem;
import com.xorovo.userProfiler.connector.IDBTracking;
import com.xorovo.userProfiler.connector.SimpleProfileMongoDB;
import com.xorovo.userProfiler.connector.SimpleTrackingSQLDB;

import java.util.ArrayList;
import java.util.List;

/**
 * This class retrieves events from DB Tracking and creates users.
 * @author Marco Arnone
 */
public class Profiler
{
	private List<Event> event = new ArrayList<Event>();
	
	private IDBTracking dbTracking = new SimpleTrackingSQLDB();
	private IDBItem dbProfiler = new SimpleProfileMongoDB();
	
	public Profiler()
	{
		try {
			this.usersInitializer();
		} catch (DBEmptyException e) {
			e.printStackTrace();
		}
	}
	
	public void usersInitializer() throws DBEmptyException
	{
		/*event = dbTracking.getEvent();
		
		for(int i = 0; i < event.size(); i++)
			dbProfiler.addEvent(event.get(i));
		
		dbProfiler.addTags();*/
		
		dbProfiler.addProfiles("scienze");
	}
}
