package dwo.scripts.custom;

import dwo.config.events.ConfigEvents;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;

/**
 * User: Bacek
 * Date: 24.06.13
 * Time: 11:28
 */
public class ItemOnGameTime extends Quest
{
	public ItemOnGameTime()
	{
		addEventId(HookType.ON_ENTER_WORLD);
	}

	public static void main(String[] args)
	{
		new ItemOnGameTime();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(!ConfigEvents.ENABLE_ITEM_ON_GAME_TIME_EVENT)
		{
			return null;
		}

		QuestState st = player.getQuestState(getClass());
		if(st != null && event.equals("reward"))
		{
			st.giveItems(ConfigEvents.ITEM_ON_GAME_TIME_GIVE_ITEM_ID, ConfigEvents.ITEM_ON_GAME_TIME_GIVE_COUNT);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return null;
	}

	@Override
	public void onEnterWorld(L2PcInstance player)
	{
		if(!ConfigEvents.ENABLE_ITEM_ON_GAME_TIME_EVENT)
		{
			return;
		}

		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		st.startRepeatingQuestTimer("reward", ConfigEvents.ITEM_ON_GAME_TIME_GIVE_TIME * 60000);
	}
}
