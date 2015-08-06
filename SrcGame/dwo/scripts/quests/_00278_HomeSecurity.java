package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _00278_HomeSecurity extends Quest
{
	// NPC
	private static final int Tunatun = 31537;
	private static final int[] Monster = {18905, 18906, 18907};

	// Item
	private static final int SelMahumMane = 15531;

	public _00278_HomeSecurity()
	{
		addStartNpc(Tunatun);
		addTalkId(Tunatun);
		addKillId(Monster);
		questItemIds = new int[]{SelMahumMane};
	}

	public static void main(String[] args)
	{
		new _00278_HomeSecurity();
	}

	@Override
	public int getQuestId()
	{
		return 278;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}

		if(event.equalsIgnoreCase("31537-02.htm"))
		{
			return player.getLevel() >= 82 ? "31537-02.htm" : "31537-03.html";
		}
		if(event.equalsIgnoreCase("31537-04.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("31537-07.html"))
		{
			int i0 = Rnd.get(100);

			if(i0 < 10)
			{
				st.giveItems(960, 1);
			}
			else if(i0 < 19)
			{
				st.giveItems(960, 2);
			}
			else if(i0 < 27)
			{
				st.giveItems(960, 3);
			}
			else if(i0 < 34)
			{
				st.giveItems(960, 4);
			}
			else if(i0 < 40)
			{
				st.giveItems(960, 5);
			}
			else if(i0 < 45)
			{
				st.giveItems(960, 6);
			}
			else if(i0 < 49)
			{
				st.giveItems(960, 7);
			}
			else if(i0 < 52)
			{
				st.giveItems(960, 8);
			}
			else if(i0 < 54)
			{
				st.giveItems(960, 9);
			}
			else if(i0 < 55)
			{
				st.giveItems(960, 10);
			}
			else if(i0 < 75)
			{
				st.giveItems(9553, 1);
			}
			else if(i0 < 90)
			{
				st.giveItems(9553, 2);
			}
			else
			{
				st.giveItems(959, 1);
			}

			st.takeItems(SelMahumMane, -1);
			st.unset("cond");
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			st.exitQuest(QuestType.REPEATABLE);
			return "31537-07.html";
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, "1");
		if(partyMember == null)
		{
			return null;
		}
		QuestState st = partyMember.getQuestState(getClass());

		int chance;
		int i1;
		if(st.getCond() == 1)
		{
			switch(npc.getNpcId())
			{
				case 18907: // Beast Devourer
				case 18906: // Farm Bandit
					chance = Rnd.get(1000);
					if(chance < 85)
					{
						st.giveItems(SelMahumMane, 1);
						if(st.getQuestItemsCount(SelMahumMane) >= 300)
						{
							st.setCond(2);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
						else
						{
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
					break;
				case 18905: // Farm Ravager (Crazy)
					chance = Rnd.get(1000);
					if(chance < 486)
					{
						i1 = Rnd.get(6) + 1;
						if(i1 + st.getQuestItemsCount(SelMahumMane) >= 300)
						{
							st.setCond(2);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							st.giveItems(SelMahumMane, 300 - st.getQuestItemsCount(SelMahumMane));
						}
						else
						{
							st.giveItems(SelMahumMane, i1);
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
					else
					{
						i1 = Rnd.get(5) + 1;
						if(i1 + st.getQuestItemsCount(SelMahumMane) >= 300)
						{
							st.setCond(2);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							st.giveItems(SelMahumMane, 300 - st.getQuestItemsCount(SelMahumMane));
						}
						else
						{
							st.giveItems(SelMahumMane, i1);
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
					break;
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		switch(st.getState())
		{
			case CREATED:
				return "31537-01.htm";
			case STARTED:
				if(st.getCond() == 1 || st.getQuestItemsCount(SelMahumMane) < 300)
				{
					return "31537-06.html";
				}
				if(st.getCond() == 2 && st.getQuestItemsCount(SelMahumMane) >= 300)
				{
					return "31537-05.html";
				}
				break;
		}
		return null;
	}
}