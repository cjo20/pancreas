


public class Treatment {
	public enum TreatmentType
	{
		BOLUS,
		CARBS
	}


	TreatmentType type;
	int time;
	double amount;

	public Treatment()
	{
		type = TreatmentType.BOLUS;
		time = (int)(System.currentTimeMillis() / 1000);
		amount = 0; 
	}

	public Treatment(TreatmentType type, double amount)
	{
		this.type = type;
		this.amount = amount;
		this.time = (int)(System.currentTimeMillis() / 1000);
	}

	public Treatment(TreatmentType type, double amount, int time)
	{
		this.type = type;
		this.amount = amount;
		this.time = time;
	}

	public String toString()
	{
		java.util.Date time = new java.util.Date((long)this.time * 1000);
		return this.amount  + "U at " + time;
	}


}
