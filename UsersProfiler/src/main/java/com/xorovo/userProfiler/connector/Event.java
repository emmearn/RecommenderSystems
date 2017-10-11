package com.xorovo.userProfiler.connector;

/**
 * @author Marco Arnone
 */
public class Event
{
	private int device;
	private int event;
	private int article;
	
	public Event(int event, int article, int device)
	{
		this.event = event;
		this.article = article;
		this.device = device;
	}
	
	public int getArticles() { return this.article; }
	public int getEvent() { return this.event; }
	public int getDevice() { return this.device; }
}
