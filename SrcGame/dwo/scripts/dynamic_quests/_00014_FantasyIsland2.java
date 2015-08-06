package dwo.scripts.dynamic_quests;

import dwo.gameserver.datatables.xml.DynamicQuestsData;
import dwo.gameserver.model.world.quest.dynamicquest.DynamicQuest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 11.07.12
 * Time: 18:36
 */

public class _00014_FantasyIsland2 extends DynamicQuest
{
	public _00014_FantasyIsland2(int id)
	{
		super(DynamicQuestsData.getInstance().getQuest(id));
	}

	public static void main(String[] args)
	{
		new _00014_FantasyIsland2(1401);
	}
}