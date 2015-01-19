import java.util.ArrayList;

public class InsulinModel {

	double insulinContrib;
	double activityContrib;

	public InsulinModel(double insulinContrib, double activityContrib)
	{
		this.insulinContrib = insulinContrib;
		this.activityContrib = activityContrib;
	}

    static InsulinModel decay_IOB(Treatment treatment, int currentTime)
	{
		int peak = 75;
		int dia = 3;
		int sens = 40;
		int deliveryTime = treatment.time;
		double deliveryAmount = treatment.amount;
		double minAgo = (currentTime - deliveryTime) / 60.0f;
		double iobContrib = 0.0f;
		double activityContrib = 0.0f;
	
		if (minAgo < 0)
		{
			iobContrib = 0.0f;
			activityContrib = 0.0f;
		}
		else if (minAgo < peak) {
			double x = (minAgo / 5) + 1;
		
			iobContrib = deliveryAmount * (1 - 0.001852f*x*x + 0.001852f*x );
			activityContrib = sens * deliveryAmount * (2.0 / dia / 60.0/ peak) * minAgo;
		}
		else if (minAgo < (dia * 60))
		{
			double x = (minAgo - 75) / 5;
			iobContrib = deliveryAmount * (0.001323f*x*x - 0.054233f*x + 0.55556f);
			activityContrib = sens * deliveryAmount*(2.0/dia/60.0-(minAgo - peak)*2/dia/60.0/(60.0*dia-peak));
		}
		return new InsulinModel(iobContrib, activityContrib);
	}


	static InsulinModel calcContrib(ArrayList<Treatment> treatments, int currentTime)
	{
		int i = 0; 
		InsulinModel totalContrib = new InsulinModel(0, 0);
		for (Treatment treatment : treatments)
		{
			InsulinModel m = decay_IOB(treatment, currentTime);

			totalContrib.insulinContrib += m.insulinContrib;
			totalContrib.activityContrib += m.activityContrib;
		}
		
		return totalContrib;
	}
}
