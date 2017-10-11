package test;

public class UserBayesed
{
	private int id;
	private double rate;
	
	public UserBayesed(int id, double rate)
	{
		this.id = id;
		this.rate = rate;
	}
	
	public boolean isGreaterThan(UserBayesed toCompare)
	{
		if(this.getRate() >= toCompare.getRate())
			return true;
		else
			return false;
	}
	
	public double getRate() { return this.rate; }
	public float getId() { return this.id; }
	
	public String toString()
	{
		return "Utente " + Integer.toString(this.id) + " ha rate = " + Double.toString(this.getRate());
	}
}
