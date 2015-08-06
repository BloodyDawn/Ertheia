package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExQuestNpcLogList;
import dwo.gameserver.util.Rnd;
import gnu.trove.map.hash.TIntIntHashMap;

public class _10333_DisappearedSakum extends Quest
{
	//npc
	private static final int BATHIS = 30332;
	private static final int VENT = 33176;
	private static final int SHUNAIN = 33508;
	// items
	private static final int SUSPICIOUS_MARKS = 17583;
	// mobs
	private static final int LANGK_LIZARDMAN = 20030;
	private static final int VUKU_ORC_FIGHTER = 20017;
	private static final int POISONOUS_SPIDER = 23094;
	private static final int VENOMOUS_SPIDER = 20038;
	private static final int POISON_PREDATOR = 20050;

	public _10333_DisappearedSakum()
	{
		addStartNpc(BATHIS);
		addTalkId(BATHIS, VENT, SHUNAIN);
		addKillId(LANGK_LIZARDMAN);
		addKillId(VUKU_ORC_FIGHTER);
		addKillId(POISONOUS_SPIDER, VENOMOUS_SPIDER, POISON_PREDATOR);
		questItemIds = new int[]{SUSPICIOUS_MARKS};
	}

	public static void main(String[] args)
	{
		new _10333_DisappearedSakum();
	}

	@Override
	public int getQuestId()
	{
		return 10333;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "captain_bathia_q10333_09.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == BATHIS)
		{
			if(reply == 1)
			{
				return "captain_bathia_q10333_04.htm";
			}
			else if(reply == 2)
			{
				return "captain_bathia_q10333_05.htm";
			}
			else if(reply == 3)
			{
				return "captain_bathia_q10333_06.htm";
			}
			else if(reply == 4)
			{
				return "captain_bathia_q10333_08.htm";
			}
		}
		else if(npc.getNpcId() == VENT)
		{
			if(reply == 1)
			{
				return "gludio_windmill_q10333_02.htm";
			}
			else if(reply == 2 && st.getCond() == 1)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "gludio_windmill_q10333_03.htm";
			}
		}
		else if(npc.getNpcId() == SHUNAIN)
		{
			if(reply == 1)
			{
				return "gludio_shunine_q10333_03.htm";
			}
			else if(reply == 2 && st.getCond() == 3)
			{
				st.unset("_1");
				st.unset("_2");
				st.giveAdena(80000, true);
				st.takeItems(SUSPICIOUS_MARKS, -1);
				st.addExpAndSp(130000, 43);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "gludio_shunine_q10333_04.htm";
			}
		}
		return null;
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
		if(st.getCond() == 2)
		{
			int ONE = st.getInt("_1");
			int TWO = st.getInt("_2");
			if(npc.getNpcId() == POISONOUS_SPIDER || npc.getNpcId() == VENOMOUS_SPIDER || npc.getNpcId() == POISON_PREDATOR)
			{
				if(Rnd.getChance(25))
				{
					if(st.getQuestItemsCount(SUSPICIOUS_MARKS) < 5)
					{
						st.giveItems(SUSPICIOUS_MARKS, 1);
					}
				}
			}
			else if(npc.getNpcId() == LANGK_LIZARDMAN && ONE < 7)
			{
				ONE++;
				st.set("_1", String.valueOf(ONE));

			}
			else if(npc.getNpcId() == VUKU_ORC_FIGHTER && TWO < 5)
			{
				TWO++;
				st.set("_2", String.valueOf(TWO));
			}
			moblist.put(1020030, ONE);
			moblist.put(1020017, TWO);
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
			if(ONE >= 7 && TWO >= 5 && st.getQuestItemsCount(SUSPICIOUS_MARKS) >= 5)
			{
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == BATHIS)
		{
			switch(st.getState())
			{
				case CREATED:
					if(canBeStarted(player))
					{
						return "captain_bathia_q10333_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "captain_bathia_q10333_02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "captain_bathia_q10333_10.htm";
					}
					if(st.getCond() == 2)
					{
						return "captain_bathia_q10333_11.htm";
					}
					if(st.getCond() == 3)
					{
						return "captain_bathia_q10333_12.htm";
					}
					break;
				case COMPLETED:
					return "captain_bathia_q10333_03.htm";
			}
		}
		else if(npc.getNpcId() == VENT)
		{
			if(st.getCond() == 1)
			{
				return "gludio_windmill_q10333_01.htm";
			}
			else if(st.getCond() == 2)
			{
				return "gludio_windmill_q10333_04.htm";
			}
			else if(st.getCond() == 3)
			{
				return "gludio_windmill_q10333_05.htm";
			}
			else if(st.isCompleted())
			{
				return "gludio_windmill_q10333_06.htm";
			}
		}
		else if(npc.getNpcId() == SHUNAIN)
		{
			if(st.getCond() == 3)
			{
				return "gludio_shunine_q10333_02.htm";
			}
			else
			{
				return st.isCompleted() ? "gludio_shunine_q10333_05.htm" : "gludio_shunine_q10333_01.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 18 && player.getLevel() <= 40;
	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1020030, st.getInt("_1"));
			moblist.put(1020017, st.getInt("_2"));
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
} 