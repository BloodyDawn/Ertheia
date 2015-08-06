package dwo.scripts.quests;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import gnu.trove.map.hash.TIntIntHashMap;

public class _00647_InfluxOfMachines extends Quest
{
	private static final TIntIntHashMap DropList = new TIntIntHashMap();

	private static final int[] Recepies = {6881, 6883, 6885, 6887, 6891, 6893, 6895, 6897, 6899, 7580};

	public _00647_InfluxOfMachines()
	{
		DropList.put(22801, 35);
		DropList.put(22802, 35);
		DropList.put(22803, 35);
		DropList.put(22804, 25);
		DropList.put(22805, 25);
		DropList.put(22806, 25);
		DropList.put(22807, 25);
		DropList.put(22809, 60);
		DropList.put(22810, 40);
		DropList.put(22811, 80);
		DropList.put(22812, 35);
		addKillId(DropList.keys());
		addTalkId(32069);
		addStartNpc(32069);
		questItemIds = new int[]{15521};
	}

	public static void main(String[] args)
	{
		new _00647_InfluxOfMachines();
	}

	@Override
	public int getQuestId()
	{
		return 647;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}
		if(event.equals("32069-02.htm"))
		{
			st.startQuest();
		}
		else if(event.equals("32069-05.htm"))
		{
			st.giveItems(Recepies[Rnd.get(Recepies.length)], 1);
			st.takeItems(15521, -1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.REPEATABLE);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st;
		L2Party party;
		party = player.getParty();
		if(party != null)
		{
			for(L2PcInstance partyMember : party.getMembers())
			{
				if(Rnd.getChance(DropList.get(npc.getNpcId())))
				{
					st = partyMember.getQuestState(getClass());
					if(st != null && st.getState() == STARTED && st.getCond() == 1)
					{
						st.giveItems(15521, (long) Config.RATE_QUEST_DROP);
						if(st.getQuestItemsCount(15521) >= 500)
						{
							st.setCond(2);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
						else
						{
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
				}
			}
		}
		else
		{
			st = player.getQuestState(getClass());
			if(st != null && st.getState() == STARTED && st.getCond() == 1)
			{
				if(Rnd.getChance(DropList.get(npc.getNpcId())))
				{
					st.giveItems(15521, (long) Config.RATE_QUEST_DROP);
					if(st.getQuestItemsCount(15521) >= 500)
					{
						st.setCond(2);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					else
					{
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return htmltext;
		}
		if(st.getCond() == 0)
		{
			htmltext = player.getLevel() >= 70 ? "32069-01.htm" : "32069-01a.htm";
		}
		else if(st.getCond() == 1)
		{
			htmltext = "32069-03.htm";
		}
		else if(st.getCond() == 2)
		{
			htmltext = "32069-04.htm";
		}
		return htmltext;
	}
}