package com.xorovo.raccomandater;

public class TagWeighed
{
	private String tag;
	private float score;
	
	public TagWeighed(String name, float rate)
	{
		tag = name;
		this.score = rate;
	}
	
	public float getScore()
	{ return this.score; }
	
	public String getTag()
	{ return this.tag; }
}
