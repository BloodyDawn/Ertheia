package dwo.xmlrpcserver.model;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.11
 * Time: xx:xx
 */

public class ServerRates
{
	private final double xp;
	private final double sp;
	private final double questDrop;
	private final double questReward;
	private final double drop;
	private final double spoil;
	private final double raid;

	public ServerRates(double xp, double sp, double questDrop, double questReward, double drop, double spoil, double raid)
	{
		this.xp = xp;
		this.sp = sp;
		this.questDrop = questDrop;
		this.questReward = questReward;
		this.drop = drop;
		this.spoil = spoil;
		this.raid = raid;
	}
}
