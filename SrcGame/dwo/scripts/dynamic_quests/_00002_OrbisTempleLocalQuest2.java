package dwo.scripts.dynamic_quests;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class _00002_OrbisTempleLocalQuest2 extends AbstractOrbisTempleLocalQuest
{
	private _00002_OrbisTempleLocalQuest2(int id)
	{
		super(id);
	}

	public static void main(String[] args)
	{
		_instance = new _00002_OrbisTempleLocalQuest2(202);
	}

	@Override
	public void onCampainDone(boolean succeed)
	{
		AbstractOrbisTempleLocalQuest.getInstance().spawnRaidOnNextSchedule();
	}
}
