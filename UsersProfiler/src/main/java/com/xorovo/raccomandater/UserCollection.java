package com.xorovo.raccomandater;

/**
 * @author Marco Arnone
 */
public class UserCollection
{
	private int id;
	private String tag;
	private float score;
	
	public UserCollection(String name, float rate)
	{
		id = Integer.parseInt(name.substring(1));
		this.score = rate;
	}
	
	public UserCollection(String name, String tag, float rate)
	{
		id = Integer.parseInt(name.substring(1));
		this.tag = tag;
		this.score = rate;
	}
	
	public boolean isGreaterThan(UserCollection toCompare)
	{
		if(this.getScore() >= toCompare.getScore())
			return true;
		else
			return false;
	}
	
	public float getScore()
	{ return this.score; }
	
	public int getName() { return this.id; }
	
	public String getTag() { return this.tag; }
	
	public String toString()
	{
		return "Utente " + Integer.toString(this.id) + " ha rate = " + this.getScore();
	}
}
