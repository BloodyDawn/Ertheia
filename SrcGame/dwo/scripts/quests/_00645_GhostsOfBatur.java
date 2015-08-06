package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import org.apache.commons.lang3.ArrayUtils;

public class _00645_GhostsOfBatur extends Quest
{
	private static final int KARUDA = 32017;
	private static final int CURSED_BURIAL = 14861;
	private static final int[] MOBS = {22703, 22704, 22705};

	public _00645_GhostsOfBatur()
	{

		addStartNpc(KARUDA);
		addTalkId(KARUDA);
		addKillId(MOBS);

		questItemIds = new int[]{CURSED_BURIAL};
	}

	public static void main(String[] args)
	{
		new _00645_GhostsOfBatur();
	}

	@Override
	public int getQuestId()
	{
		return 645;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}
		switch(event)
		{
			case "32017-03.htm":
				if(player.getLevel() < 80)
				{
					st.exitQuest(QuestType.REPEATABLE);
					return "32017-02.htm";
				}
				st.startQuest();
				break;
			case "Reward":
				return "32017-06.htm";
			case "RDB":
				if(st.getQuestItemsCount(CURSED_BURIAL) >= 500)
				{
					st.takeItems(CURSED_BURIAL, 500);
					st.rewardItems(9968, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.REPEATABLE);
					return "32017-07.htm";
				}
				else
				{
					return "32017-04.htm";
				}
			case "RDP":
				if(st.getQuestItemsCount(CURSED_BURIAL) >= 500)
				{
					st.takeItems(CURSED_BURIAL, 500);
					st.rewardItems(9969, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.REPEATABLE);
					return "32017-07.htm";
				}
				else
				{
					return "32017-04.htm";
				}
			case "RDW":
				if(st.getQuestItemsCount(CURSED_BURIAL) >= 500)
				{
					st.takeItems(CURSED_BURIAL, 500);
					st.rewardItems(9970, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.REPEATABLE);
					return "32017-07.htm";
				}
				else
				{
					return "32017-04.htm";
				}
			case "RDK":
				if(st.getQuestItemsCount(CURSED_BURIAL) >= 500)
				{
					st.takeItems(CURSED_BURIAL, 500);
					st.rewardItems(9971, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.REPEATABLE);
					return "32017-07.htm";
				}
				else
				{
					return "32017-04.htm";
				}
			case "RDH":
				if(st.getQuestItemsCount(CURSED_BURIAL) >= 500)
				{
					st.takeItems(CURSED_BURIAL, 500);
					st.rewardItems(9972, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.REPEATABLE);
					return "32017-07.htm";
				}
				else
				{
					return "32017-04.htm";
				}
			case "RDC":
				if(st.getQuestItemsCount(CURSED_BURIAL) >= 500)
				{
					st.takeItems(CURSED_BURIAL, 500);
					st.rewardItems(9973, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.REPEATABLE);
					return "32017-07.htm";
				}
				else
				{
					return "32017-04.htm";
				}
			case "RDM":
				if(st.getQuestItemsCount(CURSED_BURIAL) >= 500)
				{
					st.takeItems(CURSED_BURIAL, 500);
					st.rewardItems(9974, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.REPEATABLE);
					return "32017-07.htm";
				}
				else
				{
					return "32017-04.htm";
				}
			case "RDN":
				if(st.getQuestItemsCount(CURSED_BURIAL) >= 500)
				{
					st.takeItems(CURSED_BURIAL, 500);
					st.rewardItems(9975, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.REPEATABLE);
					return "32017-07.htm";
				}
				else
				{
					return "32017-04.htm";
				}
			case "LEO":
				if(st.getQuestItemsCount(CURSED_BURIAL) >= 500)
				{
					st.takeItems(CURSED_BURIAL, 500);
					st.rewardItems(9628, 62);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.REPEATABLE);
					return "32017-07.htm";
				}
				else
				{
					return "32017-04.htm";
				}
			case "ADA":
				if(st.getQuestItemsCount(CURSED_BURIAL) >= 500)
				{
					st.takeItems(CURSED_BURIAL, 500);
					st.rewardItems(9629, 33);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.REPEATABLE);
					return "32017-07.htm";
				}
				else
				{
					return "32017-04.htm";
				}
			case "ORI":
				if(st.getQuestItemsCount(CURSED_BURIAL) >= 500)
				{
					st.takeItems(CURSED_BURIAL, 500);
					st.rewardItems(9630, 41);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.REPEATABLE);
					return "32017-07.htm";
				}
				else
				{
					return "32017-04.htm";
				}
		}

		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null || st.getCond() != 1)
		{
			return null;
		}

		int npcId = npc.getNpcId();
		if(ArrayUtils.contains(MOBS, npcId))
		{
			if(st.getRandom(100) <= 20)
			{
				st.giveItems(CURSED_BURIAL, 1);

				if(st.getQuestItemsCount(CURSED_BURIAL) >= 500)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(2);
				}
				else
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		switch(st.getState())
		{
			case COMPLETED:
				return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
			case CREATED:
				return "32017-01.htm";
			case STARTED:
				switch(st.getCond())
				{
					case 0:
						return "32017-01.htm";
					case 1:
						return "32017-04.htm";
					case 2:
						return st.getQuestItemsCount(CURSED_BURIAL) >= 500 ? "32017-05.htm" : "32017-01.htm";
				}
				break;
		}
		return null;
	}
}