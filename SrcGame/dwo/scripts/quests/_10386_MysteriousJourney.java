package dwo.scripts.quests;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 28.01.13
 * Time: 16:47
 * TODO Диалоги
 */

public class _10386_MysteriousJourney extends Quest
{

	public static void main(String[] args)
	{
		new _10386_MysteriousJourney();
	}

	@Override
	public int getQuestId()
	{
		return 10386;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 93;
	}
}