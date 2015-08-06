package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.ClassLevel;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExQuestNpcLogList;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 15.03.12
 * Time: 3:21
 */

public class _10375_TheApostlesOfDreams extends Quest
{
	// Квестовые персонажи
	private static final int Zenya = 32140;

	// Квестовые монстры
	private static final int SuccubusOfDeath = 23191;
	private static final int SuccubusOfDarkness = 23192;
	private static final int SuccubusOfLunacy = 23197;
	private static final int SuccubusOfSilence = 23198;

	public _10375_TheApostlesOfDreams()
	{
		addStartNpc(Zenya);
		addTalkId(Zenya);
		addKillId(SuccubusOfDeath, SuccubusOfDarkness, SuccubusOfLunacy, SuccubusOfSilence);
	}

	public static void main(String[] args)
	{
		new _10375_TheApostlesOfDreams();
	}

	@Override
	public int getQuestId()
	{
		return 10375;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "subelder_zenya_q10375_06.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == Zenya)
		{
			if(reply == 1)
			{
				if(st.getCond() <= 1)
				{
					return "subelder_zenya_q10375_02.htm";
				}
				else
				{
					st.setCond(3);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "subelder_zenya_q10375_09.htm";
				}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return super.onKill(npc, killer, isPet);
		}

		if(st.getCond() == 1)
		{
			int _1 = st.getInt("_1");
			int _2 = st.getInt("_2");

			TIntIntHashMap mobList = new TIntIntHashMap();

			if(npc.getNpcId() == SuccubusOfDeath && _1 < 5)
			{
				_1++;
				st.set("_1", String.valueOf(_1));
			}
			else if(npc.getNpcId() == SuccubusOfDarkness && _2 < 5)
			{
				_2++;
				st.set("_2", String.valueOf(_2));
			}
			if(_1 + _2 >= 10)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			mobList.put(1023191, _1);
			mobList.put(1023192, _2);
			killer.sendPacket(new ExQuestNpcLogList(getQuestId(), mobList));
		}
		else if(st.getCond() == 3)
		{
			int _3 = st.getInt("_3");
			int _4 = st.getInt("_4");

			TIntIntHashMap mobList = new TIntIntHashMap();

			if(npc.getNpcId() == SuccubusOfLunacy && _3 < 5)
			{
				_3++;
				st.set("_3", String.valueOf(_3));
			}
			else if(npc.getNpcId() == SuccubusOfSilence && _4 < 5)
			{
				_4++;
				st.set("_4", String.valueOf(_4));
			}

			if(_3 + _4 >= 10)
			{
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			mobList.put(1023197, _3);
			mobList.put(1023198, _4);
			killer.sendPacket(new ExQuestNpcLogList(getQuestId(), mobList));
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == Zenya)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "subelder_zenya_q10375_05.htm";
				case CREATED:
					if(player.getLevel() >= 80)
					{
						QuestState prevst = player.getQuestState(_10374_SourceOfNightmares.class);
						if(!player.isAwakened() && player.getClassId().level() == ClassLevel.THIRD.ordinal() && prevst != null && prevst.isCompleted())
						{
							return "subelder_zenya_q10375_01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "subelder_zenya_q10375_03.htm";
						}
					}
					else
					{
						return "subelder_zenya_q10375_04.htm";
					}
				case STARTED:
					switch(st.getCond())
					{
						case 1:
							return "subelder_zenya_q10375_07.htm";
						case 2:
							return "subelder_zenya_q10375_08.htm";
						case 3:
							return "subelder_zenya_q10375_10.htm";
						case 4:
							st.giveAdena(498700, true);
							st.addExpAndSp(24782300, 28102300);
							st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
							st.exitQuest(QuestType.ONE_TIME);
							return "subelder_zenya_q10375_11.htm";
					}
					break;
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState prevst = player.getQuestState(_10374_SourceOfNightmares.class);
		return prevst != null && prevst.isCompleted() && player.getLevel() >= 80 && !player.isAwakened() && player.getClassId().level() == ClassLevel.THIRD.ordinal();

	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			if(st.getCond() == 2)
			{
				moblist.put(1023191, st.getInt("_1"));
				moblist.put(1023192, st.getInt("_2"));
			}
			else if(st.getCond() == 3)
			{
				moblist.put(1023197, st.getInt("_3"));
				moblist.put(1023198, st.getInt("_4"));
			}
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}