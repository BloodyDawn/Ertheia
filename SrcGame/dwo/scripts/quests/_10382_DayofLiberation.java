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
 * Date: 31.08.12
 * Time: 10:31
 */

public class _10382_DayofLiberation extends Quest
{
	// Квестовые персонажи
	private static final int Сизрак = 33669;

	// Квестовые монстры
	private static final int Таути = 29236;

	// Квестовые предметы
	private static final int БраслетТаути = 35293;

	public _10382_DayofLiberation()
	{
		addStartNpc(Сизрак);
		addTalkId(Сизрак);
	}

	public static void main(String[] args)
	{
		new _10382_DayofLiberation();
	}

	private void giveItem(QuestState pst)
	{
		if(pst != null && pst.getCond() == 1)
		{
			sendNpcLogList(pst.getPlayer());
			pst.setCond(2);
			pst.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
	}

	@Override
	public int getQuestId()
	{
		return 10382;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "sofa_sizraku_q10382_03.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Сизрак)
		{
			if(reply == 1)
			{
				if(cond == 1)
				{
					return "sofa_sizraku_q10382_02.htm";
				}
				else if(cond == 2)
				{
					return "sofa_sizraku_q10382_09.htm";
				}
			}
			else if(reply == 2 && cond == 2)
			{
				st.addExpAndSp(951127800, 435041400);
				st.giveAdena(3256740, true);
				st.giveItem(БраслетТаути);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "sofa_sizraku_q10382_10.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == Таути)
		{
			if(player.getParty() == null)
			{
				giveItem(player.getQuestState(getClass()));
			}
			else
			{
				if(player.getParty().isInCommandChannel())
				{
					for(L2PcInstance member : player.getParty().getCommandChannel().getMembersInRadius(player, 900))
					{
						giveItem(member.getQuestState(getClass()));
					}
				}
				else
				{
					for(L2PcInstance member : player.getParty().getMembersInRadius(player, 900))
					{
						giveItem(member.getQuestState(getClass()));
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Сизрак)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "sofa_sizraku_q10382_06.htm";
				case CREATED:
					QuestState pst = st.getPlayer().getQuestState(_10381_TotheSeedofHellfire.class);
					if(pst == null || !pst.isCompleted())
					{
						return "sofa_sizraku_q10382_05.htm";
					}
					else
					{
						return st.getPlayer().getLevel() < 97 ? "sofa_sizraku_q10382_04.htm" : "sofa_sizraku_q10382_01.htm";
					}
				case STARTED:
					if(cond == 1)
					{
						return "sofa_sizraku_q10382_07.htm";
					}
					else if(cond == 2)
					{
						return "sofa_sizraku_q10382_08.htm";
					}
			}
		}

		return getNoQuestMsg(st.getPlayer());
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState pst = player.getQuestState(_10381_TotheSeedofHellfire.class);
		return player.getLevel() >= 97 && pst != null && pst.isCompleted();
	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1029236, st.getCond() - 1);
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}