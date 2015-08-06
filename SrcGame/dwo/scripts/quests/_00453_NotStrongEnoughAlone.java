package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExQuestNpcLogList;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.lang3.ArrayUtils;

public class _00453_NotStrongEnoughAlone extends Quest
{
	// Квестовые персонажи
	private static final int Klemis = 32734;

	// Квестовые монстры
	private static final int[] Monsters1 = {
		22746, 22747, 22748, 22749, 22750, 22751, 22752, 22753
	};
	private static final int[] Monsters2 = {
		22754, 22755, 22756, 22757, 22758, 22759
	};
	private static final int[] Monsters3 = {
		22760, 22761, 22762, 22763, 22764, 22765
	};

	// Квестовые награды
	private static final int[][] Reward = {
		{
			15815, 15816, 15817, 15818, 15819, 15820, 15821, 15822, 15823, 15824, 15825
		}, {
		15634, 15635, 15636, 15637, 15638, 15639, 15640, 15641, 15642, 15643, 15644
	}
	};

	public _00453_NotStrongEnoughAlone()
	{

		addStartNpc(Klemis);
		addTalkId(Klemis);
		addKillId(Monsters1);
		addKillId(Monsters2);
		addKillId(Monsters3);
	}

	public static void main(String[] args)
	{
		new _00453_NotStrongEnoughAlone();
	}

	private void increaseKill(L2PcInstance player, L2Npc npc)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return;
		}

		int npcId = npc.getNpcId();

		int _1 = st.getInt("_1");
		int _2 = st.getInt("_2");
		int _3 = st.getInt("_3");
		int _4 = st.getInt("_4");

		if(Util.checkIfInRange(1500, npc, player, false))
		{
			if(ArrayUtils.contains(Monsters1, npcId) && st.getCond() == 2)
			{
				TIntIntHashMap moblist = new TIntIntHashMap();

				if(npcId == Monsters1[4] || npcId == Monsters1[0])
				{
					if(_1 < 15)
					{
						_1++;
						st.set("_1", String.valueOf(_1));
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}
				else if(npcId == Monsters1[5] || npcId == Monsters1[1])
				{
					if(_2 < 15)
					{
						_2++;
						st.set("_2", String.valueOf(_2));
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}
				else if(npcId == Monsters1[6] || npcId == Monsters1[2])
				{
					if(_3 < 15)
					{
						_3++;
						st.set("_3", String.valueOf(_3));
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}
				else if(npcId == Monsters1[7] || npcId == Monsters1[3])
				{
					if(_4 < 15)
					{
						_4++;
						st.set("_4", String.valueOf(_4));
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}

				moblist.put(1022746, _1);
				moblist.put(1022747, _2);
				moblist.put(1022748, _3);
				moblist.put(1022749, _4);
				player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));

				if(_1 + _2 + _3 + _4 >= 60)
				{
					st.setCond(5);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
			else if(ArrayUtils.contains(Monsters2, npcId) && st.getCond() == 3)
			{
				TIntIntHashMap moblist = new TIntIntHashMap();

				if(npcId == Monsters2[0] || npcId == Monsters2[3])
				{
					if(_1 < 20)
					{
						_1++;
						st.set("_1", String.valueOf(_1));
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}
				else if(npcId == Monsters2[1] || npcId == Monsters2[4])
				{
					if(_2 < 20)
					{
						_2++;
						st.set("_2", String.valueOf(_2));
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}
				else if(npcId == Monsters2[2] || npcId == Monsters2[5])
				{
					if(_3 < 20)
					{
						_3++;
						st.set("_3", String.valueOf(_3));
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}

				moblist.put(1022754, _1);
				moblist.put(1022755, _2);
				moblist.put(1022756, _3);
				player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));

				if(_1 + _2 + _3 >= 60)
				{
					st.setCond(5);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
			else if(ArrayUtils.contains(Monsters3, npcId) && st.getCond() == 4)
			{
				TIntIntHashMap moblist = new TIntIntHashMap();

				if(npcId == Monsters3[0] || npcId == Monsters3[3])
				{
					if(_1 < 20)
					{
						_1++;
						st.set("_1", String.valueOf(_1));
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}
				else if(npcId == Monsters3[1] || npcId == Monsters3[4])
				{
					if(_2 < 20)
					{
						_2++;
						st.set("_2", String.valueOf(_2));
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}
				else if(npcId == Monsters3[2] || npcId == Monsters3[5])
				{
					if(_3 < 20)
					{
						_3++;
						st.set("_3", String.valueOf(_3));
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}

				moblist.put(1022754, _1);
				moblist.put(1022755, _2);
				moblist.put(1022756, _3);
				player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));

				if(_1 + _2 + _3 >= 60)
				{
					st.setCond(5);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
		}
	}

	@Override
	public int getQuestId()
	{
		return 453;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(event.equalsIgnoreCase("32734-06.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("32734-07.html"))
		{
			st.setCond(2);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		else if(event.equalsIgnoreCase("32734-08.html"))
		{
			st.setCond(3);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		else if(event.equalsIgnoreCase("32734-09.html"))
		{
			st.setCond(4);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(player.getParty() != null)
		{
			for(L2PcInstance member : player.getParty().getMembers())
			{
				increaseKill(member, npc);
			}
		}
		else
		{
			increaseKill(player, npc);
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		QuestState prev = player.getQuestState(_10282_ToTheSeedOfAnnihilation.class);

		switch(st.getState())
		{
			case CREATED:
				if(player.getLevel() >= 84 && prev != null && prev.isCompleted())
				{
					return "32734-01.htm";
				}
				else
				{
					st.exitQuest(QuestType.REPEATABLE);
					return "32734-03.html";
				}
			case STARTED:
				switch(st.getCond())
				{
					case 1:
						return "32734-10.html";
					case 2:
						return "32734-11.html";
					case 3:
						return "32734-12.html";

					case 4:
						return "32734-13.html";
					case 5:
						if(Rnd.getChance(50))
						{
							st.giveItems(Reward[0][Rnd.get(Reward[0].length)], 1);
						}
						else
						{
							st.giveItems(Reward[1][Rnd.get(Reward[1].length)], 1);
						}
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.DAILY);
						return "32734-14.html";
				}
				break;
			case COMPLETED:
				if(st.isNowAvailable())
				{
					if(player.getLevel() >= 84 && prev != null && prev.isCompleted())
					{
						st.setState(CREATED);
						return "32734-01.htm";
					}
					else
					{
						return "32734-03.html";
					}
				}
				else
				{
					return "32734-02.htm";
				}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState prev = player.getQuestState(_10282_ToTheSeedOfAnnihilation.class);
		return prev != null && prev.isCompleted() && player.getLevel() >= 84;

	}
}
