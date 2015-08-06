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
 * User: ANZO
 * Date: 01.03.12
 * Time: 7:18
 *
 * TODO: Оф диалоги!
 */

public class _10374_SourceOfNightmares extends Quest
{
	// Квестовые персонажи
	private static final int Andrew = 31292;
	private static final int Agnes = 31588;
	private static final int Zenya = 32140;

	// Квестовые монстры
	private static final int PhantomSoldier = 23186;
	private static final int PhantomWarrior = 23187;
	private static final int PhantomArcher = 23188;
	private static final int PhantomShaman = 23189;
	private static final int PhantomMartyr = 23190;

	public _10374_SourceOfNightmares()
	{
		addStartNpc(Andrew);
		addTalkId(Andrew, Agnes, Zenya);
		addKillId(PhantomSoldier, PhantomWarrior, PhantomArcher, PhantomShaman, PhantomMartyr);
	}

	public static void main(String[] args)
	{
		new _10374_SourceOfNightmares();
	}

	@Override
	public int getQuestId()
	{
		return 10374;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		switch(event)
		{
			case "31292-07.htm":
				st.startQuest();
				break;
			case "31588-02.htm":
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32140-02.htm":
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
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

		if(st.getCond() == 3)
		{
			int _1 = st.getInt("_1");
			int _2 = st.getInt("_2");
			int _3 = st.getInt("_3");
			int _4 = st.getInt("_4");
			int _5 = st.getInt("_5");

			TIntIntHashMap moblist = new TIntIntHashMap();

			if(npc.getNpcId() == PhantomSoldier && _1 < 15)
			{
				_1++;
				st.set("_1", String.valueOf(_1));
			}
			else if(npc.getNpcId() == PhantomWarrior && _2 < 10)
			{
				_2++;
				st.set("_2", String.valueOf(_2));
			}
			else if(npc.getNpcId() == PhantomArcher && _3 < 5)
			{
				_3++;
				st.set("_3", String.valueOf(_3));
			}
			else if(npc.getNpcId() == PhantomShaman && _4 < 5)
			{
				_4++;
				st.set("_4", String.valueOf(_4));
			}
			else if(npc.getNpcId() == PhantomMartyr && _5 < 5)
			{
				_5++;
				st.set("_5", String.valueOf(_5));
			}
			if(_1 + _2 + _3 + _4 + _5 >= 40)
			{
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			moblist.put(1023186, _1);
			moblist.put(1023187, _2);
			moblist.put(1023188, _3);
			moblist.put(1023189, _4);
			moblist.put(1023190, _5);
			killer.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == Andrew)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "31292-06.htm";
				case CREATED:
					if(player.getLevel() >= 80)
					{
						if(!player.isAwakened() && player.getClassId().level() == ClassLevel.THIRD.ordinal())
						{
							return "31292-01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "31292-04.htm";
						}
					}
					else
					{
						return "31292-05.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "31292-08.htm";
					}
					break;
			}
		}
		else if(npc.getNpcId() == Agnes)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 1)
				{
					return "31588-01.htm";
				}
				else if(st.getCond() == 2)
				{
					return "31588-03.htm";
				}
			}
		}
		else if(npc.getNpcId() == Zenya)
		{
			if(st.isStarted())
			{
				switch(st.getCond())
				{
					case 2:
						return "32140-01.htm";
					case 3:
						return "32140-03.htm";
					case 4:
						st.addExpAndSp(23747100, 27618200);
						st.giveAdena(500560, true);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.ONE_TIME);
						return "32140-04.htm";
				}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 80 && !player.isAwakened() && player.getClassId().level() == ClassLevel.THIRD.ordinal();

	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1023186, st.getInt("_1"));
			moblist.put(1023187, st.getInt("_2"));
			moblist.put(1023188, st.getInt("_3"));
			moblist.put(1023189, st.getInt("_4"));
			moblist.put(1023190, st.getInt("_5"));
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}