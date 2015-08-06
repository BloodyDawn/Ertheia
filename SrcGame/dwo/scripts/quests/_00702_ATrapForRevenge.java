package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _00702_ATrapForRevenge extends Quest
{
	// NPC
	private static final int Plenos = 32563;
	private static final int Lekon = 32557;
	private static final int Tenius = 32555;
	private static final int[] Monsters = {22612, 22613, 25632, 22610, 22611, 25631, 25626};

	// Items
	private static final int DrakeFlesh = 13877;
	private static final int RottenBlood = 13878;
	private static final int BaitForDrakes = 13879;
	private static final int VariantDrakeWingHorns = 13880;
	private static final int ExtractedRedStarStone = 14009;

	public _00702_ATrapForRevenge()
	{
		addStartNpc(Plenos);
		addTalkId(Plenos, Lekon, Tenius);
		addKillId(Monsters);
	}

	public static void main(String[] args)
	{
		new _00702_ATrapForRevenge();
	}

	@Override
	public int getQuestId()
	{
		return 702;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		switch(event)
		{
			case "32563-04.htm":
				st.startQuest();
				break;
			case "32563-07.html":
				return st.hasQuestItems(DrakeFlesh) ? "32563-08.html" : "32563-07.html";
			case "32563-09.html":
				st.giveAdena(st.getQuestItemsCount(DrakeFlesh) * 100, true);
				st.takeItems(DrakeFlesh, st.getQuestItemsCount(DrakeFlesh));
				break;
			case "32563-11.html":
				if(st.hasQuestItems(VariantDrakeWingHorns))
				{
					st.giveAdena(st.getQuestItemsCount(VariantDrakeWingHorns) * 200000, true);
					st.takeItems(VariantDrakeWingHorns, st.getQuestItemsCount(VariantDrakeWingHorns));
					return "32563-12.html";
				}
				else
				{
					return "32563-11.html";
				}
			case "32563-14.html":
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.exitQuest(QuestType.REPEATABLE);
				break;
			case "32557-03.html":
				if(!st.hasQuestItems(RottenBlood) && st.getQuestItemsCount(ExtractedRedStarStone) < 100)
				{
					return "32557-03.html";
				}
				if(st.hasQuestItems(RottenBlood) && st.getQuestItemsCount(ExtractedRedStarStone) < 100)
				{
					return "32557-04.html";
				}
				if(!st.hasQuestItems(RottenBlood) && st.getQuestItemsCount(ExtractedRedStarStone) >= 100)
				{
					return "32557-05.html";
				}
				if(st.hasQuestItems(RottenBlood) && st.getQuestItemsCount(ExtractedRedStarStone) >= 100)
				{
					st.giveItems(BaitForDrakes, 1);
					st.takeItems(RottenBlood, 1);
					st.takeItems(ExtractedRedStarStone, 100);
					return "32557-06.html";
				}
				break;
			case "32555-03.html":
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32555-05.html":
				st.exitQuest(QuestType.REPEATABLE);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32555-06.html":
				return st.getQuestItemsCount(DrakeFlesh) < 100 ? "32555-06.html" : "32555-07.html";
			case "32555-08.html":
				st.giveItems(RottenBlood, 1);
				st.takeItems(DrakeFlesh, 100);
				break;
			case "32555-10.html":
				return st.hasQuestItems(VariantDrakeWingHorns) ? "32555-11.html" : "32555-10.html";
			case "32555-15.html":
				int i0 = Rnd.get(1000);
				int i1 = Rnd.get(1000);

				if(i0 >= 500 && i1 >= 600)
				{
					st.giveAdena(Rnd.get(49917) + 125000, true);
					if(i1 < 720)
					{
						st.giveItems(9628, Rnd.get(3) + 1);
						st.giveItems(9629, Rnd.get(3) + 1);
					}
					else if(i1 < 840)
					{
						st.giveItems(9629, Rnd.get(3) + 1);
						st.giveItems(9630, Rnd.get(3) + 1);
					}
					else if(i1 < 960)
					{
						st.giveItems(9628, Rnd.get(3) + 1);
						st.giveItems(9630, Rnd.get(3) + 1);
					}
					else if(i1 < 1000)
					{
						st.giveItems(9628, Rnd.get(3) + 1);
						st.giveItems(9629, Rnd.get(3) + 1);
						st.giveItems(9630, Rnd.get(3) + 1);
					}
					return "32555-15.html";
				}
				if(i0 >= 500 && i1 < 600)
				{
					st.giveAdena(Rnd.get(49917) + 125000, true);
					if(i1 < 210)
					{
					}
					else if(i1 < 340)
					{
						st.giveItems(9628, Rnd.get(3) + 1);
					}
					else if(i1 < 470)
					{
						st.giveItems(9629, Rnd.get(3) + 1);
					}
					else if(i1 < 600)
					{
						st.giveItems(9630, Rnd.get(3) + 1);
					}

					return "32555-16.html";
				}
				if(i0 < 500 && i1 >= 600)
				{
					st.giveAdena(Rnd.get(49917) + 25000, true);
					if(i1 < 720)
					{
						st.giveItems(9628, Rnd.get(3) + 1);
						st.giveItems(9629, Rnd.get(3) + 1);
					}
					else if(i1 < 840)
					{
						st.giveItems(9629, Rnd.get(3) + 1);
						st.giveItems(9630, Rnd.get(3) + 1);
					}
					else if(i1 < 960)
					{
						st.giveItems(9628, Rnd.get(3) + 1);
						st.giveItems(9630, Rnd.get(3) + 1);
					}
					else if(i1 < 1000)
					{
						st.giveItems(9628, Rnd.get(3) + 1);
						st.giveItems(9629, Rnd.get(3) + 1);
						st.giveItems(9630, Rnd.get(3) + 1);
					}
					return "32555-17.html";
				}
				if(i0 < 500 && i1 < 600)
				{
					st.giveAdena(Rnd.get(49917) + 25000, true);
					if(i1 < 210)
					{
					}
					else if(i1 < 340)
					{
						st.giveItems(9628, Rnd.get(3) + 1);
					}
					else if(i1 < 470)
					{
						st.giveItems(9629, Rnd.get(3) + 1);
					}
					else if(i1 < 600)
					{
						st.giveItems(9630, Rnd.get(3) + 1);
					}

					return "32555-18.html";
				}
				st.takeItems(VariantDrakeWingHorns, 1);
				break;
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, "2");
		if(partyMember == null)
		{
			return null;
		}
		QuestState st = partyMember.getQuestState(getClass());
		int chance = Rnd.get(1000);

		switch(npc.getNpcId())
		{
			case 22612:
				if(chance < 413)
				{
					st.giveItems(DrakeFlesh, 2);
				}
				else
				{
					st.giveItems(DrakeFlesh, 1);
				}
				break;
			case 22613:
				if(chance < 440)
				{
					st.giveItems(DrakeFlesh, 2);
				}
				else
				{
					st.giveItems(DrakeFlesh, 1);
				}
				break;
			case 25632:
				if(chance < 996)
				{
					st.giveItems(DrakeFlesh, 1);
				}
				break;
			case 22610:
				if(chance < 485)
				{
					st.giveItems(DrakeFlesh, 2);
				}
				else
				{
					st.giveItems(DrakeFlesh, 1);
				}
				break;
			case 22611:
				if(chance < 451)
				{
					st.giveItems(DrakeFlesh, 2);
				}
				else
				{
					st.giveItems(DrakeFlesh, 1);
				}
				break;
			case 25631:
				if(chance < 485)
				{
					st.giveItems(DrakeFlesh, 2);
				}
				else
				{
					st.giveItems(DrakeFlesh, 1);
				}
				break;
			case 25626:
				if(chance < 708)
				{
					st.giveItems(VariantDrakeWingHorns, Rnd.get(2) + 1);
				}
				else if(chance < 978)
				{
					st.giveItems(VariantDrakeWingHorns, Rnd.get(3) + 3);
				}
				else if(chance < 994)
				{
					st.giveItems(VariantDrakeWingHorns, Rnd.get(4) + 6);
				}
				else if(chance < 998)
				{
					st.giveItems(VariantDrakeWingHorns, Rnd.get(4) + 10);
				}
				else if(chance < 1000)
				{
					st.giveItems(VariantDrakeWingHorns, Rnd.get(5) + 14);
				}
				break;
		}
		st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		QuestState prev = player.getQuestState(_10273_GoodDayToFly.class);

		if(npc.getNpcId() == Plenos)
		{
			switch(st.getState())
			{
				case CREATED:
					return prev != null && prev.getState() == COMPLETED && player.getLevel() >= 78 ? "32563-01.htm" : "32563-02.htm";
				case STARTED:
					return st.getCond() == 1 ? "32563-05.html" : "32563-06.html";
			}
		}
		if(st.getState() == STARTED)
		{
			if(npc.getNpcId() == Lekon)
			{
				switch(st.getCond())
				{
					case 1:
						return "32557-01.html";
					case 2:
						return "32557-02.html";
				}
			}
			else if(npc.getNpcId() == Tenius)
			{
				switch(st.getCond())
				{
					case 1:
						return "32555-01.html";
					case 2:
						return "32555-04.html";
				}
			}
		}
		return null;
	}
}
