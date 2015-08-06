package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00035_FindGlitteringJewelry extends Quest
{
	private static final int ROUGH_JEWEL = 7162;
	private static final int ORIHARUKON = 1893;
	private static final int ARMOR_PART = 34991;
	private static final int JEWEL_PART = 34992;
	private static final int JEWEL_BOX = 7077;

	public _00035_FindGlitteringJewelry()
	{
		addStartNpc(30091);
		addTalkId(30091, 30879);
		addKillId(20135);
		questItemIds = new int[]{ROUGH_JEWEL};
	}

	public static void main(String[] args)
	{
		new _00035_FindGlitteringJewelry();
	}

	@Override
	public int getQuestId()
	{
		return 35;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		int cond = st.getCond();
		if(event.equals("30091-1.htm") && cond == 0)
		{
			st.startQuest();
		}
		else if(event.equals("30879-1.htm") && cond == 1)
		{
			st.setCond(2);
		}
		else if(event.equals("30091-3.htm") && cond == 3)
		{
			st.takeItems(ROUGH_JEWEL, 10);
			st.setCond(4);
		}
		else if(event.equals("30091-5.htm") && cond == 4)
		{
			if(st.getQuestItemsCount(ORIHARUKON) >= 5 && st.getQuestItemsCount(ARMOR_PART) >= 900 && st.getQuestItemsCount(JEWEL_PART) >= 250)
			{
				st.takeItems(ORIHARUKON, 5);
				st.takeItems(ARMOR_PART, 900);
				st.takeItems(JEWEL_PART, 250);
				st.giveItems(JEWEL_BOX, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.unset("cond");
				st.exitQuest(QuestType.ONE_TIME);
			}
			else
			{
				return "У Вас нет нужных материалов!";
			}
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return onKill(npc, st);
		}

		if(st.isStarted())
		{
			long count = st.getQuestItemsCount(ROUGH_JEWEL);
			if(count < 10)
			{
				st.giveItems(ROUGH_JEWEL, 1);
				if(st.getQuestItemsCount(ROUGH_JEWEL) == 10)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(3);
				}
				else
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		return onKill(npc, st);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		int cond = st.getCond();

		if(st.isCompleted())
		{
			return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
		}
		if(npc.getNpcId() == 30091 && cond == 0 && !st.hasQuestItems(JEWEL_BOX))
		{
			QuestState fwear = player.getQuestState(_00037_PleaseMakeMeFormalWear.class);
			if(fwear != null)
			{
				if(fwear.getCond() == 6)
				{
					return "30091-0.htm";
				}
			}
			st.exitQuest(QuestType.REPEATABLE);
		}
		else if(npc.getNpcId() == 30879 && cond == 1)
		{
			return "30879-0.htm";
		}
		else if(st.isStarted())
		{
			if(npc.getNpcId() == 30091 && st.getQuestItemsCount(ROUGH_JEWEL) == 10)
			{
				return "30091-2.htm";
			}
			else if(npc.getNpcId() == 30091 && cond == 4 && st.getQuestItemsCount(ORIHARUKON) >= 5 && st.getQuestItemsCount(ARMOR_PART) >= 900 && st.getQuestItemsCount(JEWEL_PART) >= 250)
			{
				return "30091-4.htm";
			}
		}
		return null;
	}
}