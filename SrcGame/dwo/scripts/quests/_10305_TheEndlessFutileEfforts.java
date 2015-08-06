package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExQuestNpcLogList;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 15.02.12
 * Time: 7:45
 */

public class _10305_TheEndlessFutileEfforts extends Quest
{
	// Квестовые персонажи
	private static final int Мимирид = 32895;

	// Квестовые монстры
	private static final int[] Коконы = {32919, 32920};

	public _10305_TheEndlessFutileEfforts()
	{
		addStartNpc(Мимирид);
		addTalkId(Мимирид);
		addKillId(Коконы);
	}

	public static void main(String[] args)
	{
		new _10305_TheEndlessFutileEfforts();
	}

	@Override
	public int getQuestId()
	{
		return 10305;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}

		if(event.equalsIgnoreCase("32895-05.htm"))
		{
			st.startQuest();
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return super.onKill(npc, killer, isPet);
		}

		if(ArrayUtils.contains(Коконы, npc.getNpcId()))
		{
			TIntIntHashMap moblist = new TIntIntHashMap();

			int ONE = st.getInt("_1");
			ONE++;
			st.set("_1", String.valueOf(ONE));
			moblist.put(1032919, ONE);
			killer.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));

			if(ONE >= 5)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		QuestState previous = player.getQuestState(_10302_TheShadowOfAnxiety.class);

		if(npc.getNpcId() == Мимирид)
		{
			if(previous == null || !previous.isCompleted() || player.getLevel() < 88)
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "32895-00.htm";
			}
			switch(st.getState())
			{
				case CREATED:
					return "32895-01.htm";
				case STARTED:
					switch(st.getCond())
					{
						case 1:
							return "32895-06.htm";
						case 2:
							st.addExpAndSp(34971975, 15142200);
							st.giveAdena(1007735, true);
							st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
							st.exitQuest(QuestType.ONE_TIME);
							return "32895-07.htm";
					}
					break;
				case COMPLETED:
					return "32895-08.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10302_TheShadowOfAnxiety.class);
		return previous != null && previous.isCompleted() && player.getLevel() >= 88;

	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1032919, st.getInt("_1"));
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}