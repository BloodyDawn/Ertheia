package dwo.gameserver.model.world.quest.dynamicquest;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.network.game.serverpackets.PlaySound;

/**
 * L2GOD Team
 * User: Yorie, ANZO
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class DynamicQuestState extends QuestState
{
	public DynamicQuestState(Quest quest, L2PcInstance player, QuestStateType state)
	{
		super(quest, player, state);
	}

	@Override
	public Object setState(QuestStateType state)
	{
		return _state = state;
	}

	@Override
	public Object startQuest()
	{
		setState(QuestStateType.STARTED);
		_player.sendPacket(new PlaySound("ItemSound.quest_accept"));
		return _state;
	}
}