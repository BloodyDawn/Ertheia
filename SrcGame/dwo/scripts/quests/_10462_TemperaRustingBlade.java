package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 10.06.13
 * Time: 22:20
 */

public class _10462_TemperaRustingBlade extends Quest
{
	// Квестовые персонажи
	private static final int FLUTTER = 30677;

	// Квестовые предметы
	private static final int PRACTICE_WEAPON = 36717;
	private static final int PRACTICE_LIFE_STONE = 36718;
	private static final int PRACTICE_LIFE_GEMSTONE = 36719;

	// Квестовые награды
	private static final int LIFESTONE = 8723;
	private static final int GEMSTONE = 2130;

	public _10462_TemperaRustingBlade()
	{
		addStartNpc(FLUTTER);
		addTalkId(FLUTTER);
		questItemIds = new int[]{PRACTICE_WEAPON, PRACTICE_LIFE_STONE, PRACTICE_LIFE_GEMSTONE};
	}

	public static void main(String[] args)
	{
		new _10462_TemperaRustingBlade();
	}

	@Override
	public int getQuestId()
	{
		return 10462;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept") && !st.isCompleted())
		{
			st.startQuest();
			st.giveItem(PRACTICE_WEAPON);
			st.giveItem(PRACTICE_LIFE_STONE);
			st.giveItems(PRACTICE_LIFE_GEMSTONE, 20);
			return "head_blacksmith_flutter_q10462_06.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == FLUTTER)
		{
			switch(reply)
			{
				case 1:
					return "head_blacksmith_flutter_q10462_03.htm";
				case 2:
					return "head_blacksmith_flutter_q10462_04.htm";
				case 3:
					return "head_blacksmith_flutter_q10462_05.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		if(npc.getNpcId() == FLUTTER)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
				case CREATED:
					if(st.getPlayer().getLevel() >= 46 && st.getPlayer().getLevel() <= 52)
					{
						return "head_blacksmith_flutter_q10462_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "head_blacksmith_flutter_q10462_02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						L2ItemInstance augumentedWeapon = st.getPlayer().getInventory().getItemByItemId(PRACTICE_WEAPON);
						if(augumentedWeapon != null && augumentedWeapon.isAugmented())
						{
							st.addExpAndSp(504210, 5042);
							st.giveItem(LIFESTONE);
							st.giveItems(GEMSTONE, 20);
							st.exitQuest(QuestType.ONE_TIME);
							st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
							return "head_blacksmith_flutter_q10462_08.htm";
						}
						else
						{
							return "head_blacksmith_flutter_q10462_07.htm";
						}
					}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 46 && player.getLevel() <= 52;
	}
}