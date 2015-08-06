package dwo.scripts.quests;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 19.04.13
 * Time: 16:31
 */

public class _00463_IMustBeaGenius extends Quest
{
	private static final int _gutenhagen = 32069;
	private static final int _corpse_log = 15510;
	private static final int _collection = 15511;
	private static final int[] _mobs = {22801, 22802, 22804, 22805, 22807, 22808, 22809, 22810, 22811, 22812};

	public _00463_IMustBeaGenius()
	{
		addStartNpc(_gutenhagen);
		addTalkId(_gutenhagen);
		addKillId(_mobs);
		questItemIds = new int[]{_collection};
	}

	public static void main(String[] args)
	{
		new _00463_IMustBeaGenius();
	}

	@Override
	public int getQuestId()
	{
		return 463;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept"))
		{
			int i0 = Rnd.get(51) + 500;
			int i1 = Rnd.get(4);
			qs.startQuest();
			qs.setMemoState(1);
			qs.setMemoStateEx(1, i0);
			qs.setMemoStateEx(2, i1);
			String content = HtmCache.getInstance().getHtm(qs.getPlayer().getLang(), "default/collecter_gutenhagen_q0463_05.htm");
			content = content.replace("<?number?>", String.valueOf(i0));
			return content;
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int i0 = st.getMemoStateEx(1);
		int i8 = Rnd.get(10);
		int i6;
		if(reply == 1)
		{
			return "collecter_gutenhagen_q0463_04.htm";
		}
		if(reply == 2)
		{
			if(st.getMemoState() == 1 && player.getItemsCount(15510) != i0)
			{
				String content = HtmCache.getInstance().getHtm(player.getLang(), "default/collecter_gutenhagen_q0463_07.htm");
				content = content.replace("<?number?>", String.valueOf(i0));
				return content;
			}
		}
		else if(reply == 3)
		{
			if(st.getMemoState() == 3)
			{
				st.takeItems(_collection, -1);
				st.exitQuest(QuestType.DAILY);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				if(i8 == 0)
				{
					player.addExpAndSp(198725, 15892);
					i6 = 1;
				}
				else if(i8 >= 1 && i8 < 5)
				{
					player.addExpAndSp(278216, 22249);
					i6 = 1;
				}
				else if(i8 >= 5 && i8 < 10)
				{
					player.addExpAndSp(317961, 25427);
					i6 = 1;
				}
				else if(i8 >= 10 && i8 < 25)
				{
					player.addExpAndSp(357706, 28606);
					i6 = 2;
				}
				else if(i8 >= 25 && i8 < 40)
				{
					player.addExpAndSp(397451, 31784);
					i6 = 2;
				}
				else if(i8 >= 40 && i8 < 60)
				{
					player.addExpAndSp(596176, 47677);
					i6 = 2;
				}
				else if(i8 >= 60 && i8 < 72)
				{
					player.addExpAndSp(715411, 57212);
					i6 = 3;
				}
				else if(i8 >= 72 && i8 < 81)
				{
					player.addExpAndSp(794901, 63569);
					i6 = 3;
				}
				else if(i8 >= 81 && i8 < 89)
				{
					player.addExpAndSp(914137, 73104);
					i6 = 3;
				}
				else
				{
					player.addExpAndSp(1192352, 95353);
					i6 = 4;
				}

				switch(i6)
				{
					case 1:
						return "collecter_gutenhagen_q0463_09.htm";
					case 2:
						return "collecter_gutenhagen_q0463_10.htm";
					case 3:
						return "collecter_gutenhagen_q0463_11.htm";
					case 4:
						return "collecter_gutenhagen_q0463_14.htm";
					default:
						return "collecter_gutenhagen_q0463_15.htm";
				}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}

		if(st.getState() == STARTED && st.getCond() == 1 && ArrayUtils.contains(_mobs, npc.getNpcId()))
		{
			int i0;
			i0 = st.getMemoStateEx(2) == 0 ? Rnd.get(100) + 1 : 5;

			if(player.getItemsCount(_corpse_log) + i0 == st.getMemoStateEx(1))
			{
				st.takeItems(_corpse_log, -1);
				st.giveItems(_collection, 1);
				st.setMemoState(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else
			{
				st.giveItems(_corpse_log, i0);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.ATT_ATTACK_S1_RO_ROGUE_S2).addStringParameter(player.getName()).addStringParameter(String.valueOf(i0)));
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npc.getNpcId() == _gutenhagen)
		{
			switch(st.getState())
			{
				case CREATED:
					return player.getLevel() >= 70 ? "collecter_gutenhagen_q0463_01.htm" : "collecter_gutenhagen_q0463_02.htm";
				case STARTED:
					if(st.getMemoState() != 2 && player.getItemsCount(15511) != 1 && st.getMemoState() != 3)
					{
						return "collecter_gutenhagen_q0463_06.htm";
					}
					if(st.getMemoState() == 2 && player.getItemsCount(15511) == 1)
					{
						st.takeItems(15510, -1);
						st.takeItems(15511, -1);
						st.setMemoState(3);
						return "collecter_gutenhagen_q0463_08.htm";
					}
					if(st.getMemoState() == 3)
					{
						return "collecter_gutenhagen_q0463_08a.htm";
					}
					break;
				case COMPLETED:
					return "collecter_gutenhagen_q0463_03.htm";
			}
		}
		return null;
	}
}