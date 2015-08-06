package dwo.scripts.dynamic_quests;

import dwo.gameserver.datatables.xml.DynamicQuestsData;
import dwo.gameserver.model.world.quest.dynamicquest.DynamicQuest;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 12.07.12
 * Time: 14:54
 */
public class _00005_ShilenAdeptsDestruction extends DynamicQuest
{
	public _00005_ShilenAdeptsDestruction(int id)
	{
		super(DynamicQuestsData.getInstance().getQuest(id));
	}

	public static void main(String[] args)
	{
		new _00005_ShilenAdeptsDestruction(501);
	}
}
