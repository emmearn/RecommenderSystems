package com.xorovo.raccomandater;

/**
 * @author Marco Arnone
 */
public class UserData
{
	private String id;
	private float tagScore;
	private float sumTagsScore;
	
	public UserData(String id, int tagScore, int sumTagsScore)
	{
		this.id = id;
		this.tagScore = tagScore;
		this.sumTagsScore = sumTagsScore;
	}
	
	public String getId()
	{ return this.id; }
	
	public float getTagScore()
	{ return this.tagScore; }
	
	public float getSumTagsScore()
	{ return this.sumTagsScore; }
}
