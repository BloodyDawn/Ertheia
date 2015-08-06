package dwo.scripts.dynamic_quests;

import dwo.gameserver.datatables.xml.DynamicQuestsData;
import dwo.gameserver.model.world.quest.dynamicquest.DynamicQuest;

public class _00001_DemonsOfDarknessGontaDestruction extends DynamicQuest
{
	public _00001_DemonsOfDarknessGontaDestruction(int id)
	{
		super(DynamicQuestsData.getInstance().getQuest(id));
	}

	public static void main(String[] args)
	{
		new _00001_DemonsOfDarknessGontaDestruction(101);
	}
}
