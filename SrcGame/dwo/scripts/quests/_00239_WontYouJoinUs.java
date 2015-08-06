package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00239_WontYouJoinUs extends Quest
{
	// NPCs
	private static final int Athenia = 32643;

	// Monsters
	private static final int WasteLandfillMachines = 18805;
	private static final int Suppressor = 22656;
	private static final int Exterminator = 22657;

	//ITEMS
	private static final int DestroyedMachinePiece = 14869;
	private static final int EnchantedGolemFragment = 14870;
	private static final int CerificateOfSupport = 14866;

	public _00239_WontYouJoinUs()
	{
		addStartNpc(Athenia);
		addTalkId(Athenia);
		addKillId(WasteLandfillMachines, Suppressor, Exterminator);
		questItemIds = new int[]{DestroyedMachinePiece, EnchantedGolemFragment};
	}

	public static void main(String[] args)
	{
		new _00239_WontYouJoinUs();
	}

	@Override
	public int getQuestId()
	{
		return 239;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}
		if(event.equals("32643-2.htm"))
		{
			st.startQuest();
		}
		if(event.equals("32643-4.htm"))
		{
			st.setCond(3);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMemberState(player, STARTED);
		if(partyMember == null)
		{
			return null;
		}
		QuestState st = partyMember.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}
		int cond = st.getCond();
		if(npc.getNpcId() == WasteLandfillMachines)
		{
			if(cond == 1)
			{
				int count = 1;
				int chance = 5;
				while(chance > 1000)
				{
					chance -= 1000;
					if(chance < 5)
					{
						chance = 5;
					}
					count++;
				}
				if(st.getRandom(1000) <= chance)
				{
					st.giveItems(DestroyedMachinePiece, count);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					if(st.getQuestItemsCount(DestroyedMachinePiece) == 10)
					{
						st.setCond(2);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
				}
			}
		}
		else if(npc.getNpcId() == Suppressor || npc.getNpcId() == Exterminator)
		{
			if(cond == 3)
			{
				int count = 1;
				int chance = 5;
				while(chance > 1000)
				{
					chance -= 1000;
					if(chance < 5)
					{
						chance = 5;
					}
					count++;
				}
				if(st.getRandom(1000) <= chance)
				{
					st.giveItems(EnchantedGolemFragment, count);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					if(st.getQuestItemsCount(EnchantedGolemFragment) == 20)
					{
						st.setCond(4);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.getState() == COMPLETED)
		{
			return "32643-6.htm";
		}
		if(npc.getNpcId() == Athenia)
		{
			int cond = st.getCond();
			if(cond == 0)
			{
				if(!st.hasQuestItems(CerificateOfSupport))
				{
					return "32643-7.htm";
				}
				if(player.getLevel() >= 82)
				{
					return "32643-0.htm";
				}
				else
				{
					st.exitQuest(QuestType.REPEATABLE);
					return "32643-0a.htm";
				}
			}
			else if(cond == 1)
			{
				return "32643-2.htm";
			}
			else if(cond == 2)
			{
				st.takeItems(DestroyedMachinePiece, 10);
				return "32643-3.htm";
			}
			else if(cond == 3)
			{
				return "32643-4.htm";
			}
			else if(cond == 4)
			{
				st.giveItems(PcInventory.ADENA_ID, 283346);
				st.takeItems(CerificateOfSupport, 1);
				st.takeItems(EnchantedGolemFragment, 20);
				st.addExpAndSp(1319736, 103553);
				st.unset("cond");
				st.exitQuest(QuestType.ONE_TIME);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return "32643-5.htm";
			}
		}
		return null;
	}
}