package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExQuestNpcLogList;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 13.02.12
 * Time: 6:13
 */

public class _00493_TheEliminationOfTheDangerousElement extends Quest
{
	// Квестовые персонажи
	private static final int АгентДжорджио = 33515;

	// Квестовые монстры
	private static final int ВоскресшееСоздание = 23147;
	private static final int БезумноеСоздание = 23148;
	private static final int ЖадноеСоздание = 23149;
	private static final int СозданиеАда = 23150;
	private static final int ПосланникШиллен = 23151;

	public _00493_TheEliminationOfTheDangerousElement()
	{
		addStartNpc(АгентДжорджио);
		addTalkId(АгентДжорджио);
		addKillId(ВоскресшееСоздание, БезумноеСоздание, ЖадноеСоздание, СозданиеАда, ПосланникШиллен);
	}

	public static void main(String[] args)
	{
		new _00493_TheEliminationOfTheDangerousElement();
	}

	@Override
	public int getQuestId()
	{
		return 493;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(npc.getNpcId() == АгентДжорджио)
		{
			if(event.equalsIgnoreCase("33515-05.htm"))
			{
				st.startQuest();
			}
			else if(event.equalsIgnoreCase("33515-08.htm"))
			{
				st.unset("one");
				st.unset("two");
				st.unset("three");
				st.unset("four");
				st.unset("five");
				st.addExpAndSp(560000000, 16000000);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.DAILY);
			}
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}

		if(st.getCond() == 1)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();

			int ONE = st.getInt("_1");
			int TWO = st.getInt("_2");
			int THREE = st.getInt("_3");
			int FOUR = st.getInt("_4");
			int FIVE = st.getInt("_5");

			if(npc.getNpcId() == ВоскресшееСоздание && ONE < 20)
			{
				ONE++;
				st.set("_1", String.valueOf(ONE));
			}
			else if(npc.getNpcId() == БезумноеСоздание && TWO < 20)
			{
				TWO++;
				st.set("_2", String.valueOf(TWO));
			}
			else if(npc.getNpcId() == ЖадноеСоздание && THREE < 20)
			{
				THREE++;
				st.set("_3", String.valueOf(THREE));
			}
			else if(npc.getNpcId() == СозданиеАда && FOUR < 20)
			{
				FOUR++;
				st.set("_4", String.valueOf(FOUR));
			}
			else if(npc.getNpcId() == ПосланникШиллен && FIVE < 20)
			{
				FIVE++;
				st.set("_5", String.valueOf(FIVE));
			}
			moblist.put(1023147, ONE);
			moblist.put(1023148, TWO);
			moblist.put(1023149, THREE);
			moblist.put(1023150, FOUR);
			moblist.put(1023151, FIVE);
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
			if(ONE >= 20 && TWO >= 20 && THREE >= 20 && FOUR >= 20 && FIVE >= 20)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npc.getNpcId() == АгентДжорджио)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() < 95)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "33515-00.htm";
					}
					else
					{
						return "33515-01.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "33515-06.htm";
					}
					if(st.getCond() == 2)
					{
						return "33515-07.htm";
					}
					break;
				case COMPLETED:
					return "33515-09.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 95;

	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1023147, st.getInt("_1"));
			moblist.put(1023148, st.getInt("_2"));
			moblist.put(1023149, st.getInt("_3"));
			moblist.put(1023150, st.getInt("_4"));
			moblist.put(1023151, st.getInt("_5"));
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}