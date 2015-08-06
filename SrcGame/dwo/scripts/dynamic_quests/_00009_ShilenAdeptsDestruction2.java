package dwo.scripts.dynamic_quests;

import dwo.gameserver.datatables.xml.DynamicQuestsData;
import dwo.gameserver.model.world.quest.dynamicquest.DynamicQuest;

public class _00009_ShilenAdeptsDestruction2 extends DynamicQuest
{
	public _00009_ShilenAdeptsDestruction2(int id)
	{
		super(DynamicQuestsData.getInstance().getQuest(id));
	}

	public static void main(String[] args)
	{
		new _00009_ShilenAdeptsDestruction2(901);
	}
}
