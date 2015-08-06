package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExQuestNpcLogList;
import gnu.trove.map.hash.TIntIntHashMap;

public class _10358_DividedSakumPoslof extends Quest
{
	//npc
	private static final int LEF = 33510;
	private static final int ADVENTURE_GUILDSMAN = 31795;
	// items
	private static final int SAKUMS_SKETCH_B = 17585;
	// mobs
	private static final int ZOMBIE_WARRIOR = 20458;
	private static final int VEELAN_BUGBEARS = 20402;
	private static final int POSLOF = 27452;

	public _10358_DividedSakumPoslof()
	{
		addStartNpc(LEF);
		addTalkId(LEF, ADVENTURE_GUILDSMAN);
		addKillId(ZOMBIE_WARRIOR, VEELAN_BUGBEARS, POSLOF);
		questItemIds = new int[]{SAKUMS_SKETCH_B};
	}

	public static void main(String[] args)
	{
		new _10358_DividedSakumPoslof();
	}

	@Override
	public int getQuestId()
	{
		return 10358;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		if(event.equalsIgnoreCase("33510-03.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("31795-03.html"))
		{
			st.unset("one");
			st.unset("two");
			st.giveAdena(105000, true);
			st.takeItems(SAKUMS_SKETCH_B, -1);
			st.addExpAndSp(550000, 150000);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.ONE_TIME);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		TIntIntHashMap moblist = new TIntIntHashMap();

		QuestState st = player.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return super.onKill(npc, player, isPet);
		}
		int ONE = st.getInt("_1");
		int TWO = st.getInt("_2");

		if(st.getCond() == 1)
		{
			if(npc.getNpcId() == ZOMBIE_WARRIOR && ONE < 20)
			{
				ONE++;
				st.set("_1", String.valueOf(ONE));
			}
			else if(npc.getNpcId() == VEELAN_BUGBEARS && TWO < 23)
			{
				TWO++;
				st.set("_2", String.valueOf(TWO));
			}

			moblist.put(1020458, ONE);
			moblist.put(1020402, TWO);
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));

			if(ONE >= 20 && TWO >= 23)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		else if(st.getCond() == 3)
		{
			if(npc.getNpcId() == POSLOF)
			{
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		QuestState previous = player.getQuestState(_10337_SakumsImpact.class);

		if(npc.getNpcId() == LEF)
		{
			switch(st.getState())
			{
				case CREATED:
					if(previous != null && previous.isCompleted() && player.getLevel() >= 32 && player.getLevel() < 40)
					{
						return "33510-01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "33510-06.html";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "33510-04.html";
					}
					if(st.getCond() == 2)
					{
						st.giveItems(SAKUMS_SKETCH_B, 1);
						st.setCond(3);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "33510-05.html";
					}
					if(st.getCond() == 3)
					{
						return "33510-05.html";
					}
					break;
				case COMPLETED:
					return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
			}
		}
		else if(npc.getNpcId() == ADVENTURE_GUILDSMAN)
		{
			if(st.getCond() == 4)
			{
				return "31795-01.html";
			}
			else if(st.isCompleted())
			{
				return "31795-04.html";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10337_SakumsImpact.class);
		return previous != null && previous.isCompleted() && player.getLevel() >= 32 && player.getLevel() < 40;

	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1020458, st.getInt("_1"));
			moblist.put(1020402, st.getInt("_2"));
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
} 