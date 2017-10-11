package com.xorovo.raccomandater;

/**
 * @author Marco Arnone
 */
public class dataUsers
{
	private String id;
	private String tagName;
	private float tagScore;
	private float sumTagsScore;
	
	public dataUsers(String id, String name, float tagScore, int sumTagsScore)
	{
		this.id = id;
		this.tagName = name;
		this.tagScore = tagScore;
		this.sumTagsScore = sumTagsScore;
	}
	
	public String getId()
	{ return this.id; }
	
	public String getTagName()
	{ return this.tagName; }
	
	public float getTagScore()
	{ return this.tagScore; }
	
	public float getSumTagsScore()
	{ return this.sumTagsScore; }
}
