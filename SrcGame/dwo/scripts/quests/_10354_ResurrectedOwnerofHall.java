package dwo.scripts.quests;

import dwo.gameserver.cache.HtmCache;
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
 * User: Bacek
 * Date: 25.08.12
 * Time: 21:41
 */

public class _10354_ResurrectedOwnerofHall extends Quest
{
	// Квестовые персонажи
	private static final int Типия = 32892;

	// Квестовые монстры
	private static final int Октавис = 29212;

	// Квестовые предметы
	private static final int БутыльсДушойОктависа = 34884;

	public _10354_ResurrectedOwnerofHall()
	{
		addStartNpc(Типия);
		addTalkId(Типия);
		addKillId(Октавис);
	}

	public static void main(String[] args)
	{
		new _10354_ResurrectedOwnerofHall();
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
		return 10354;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		QuestState pst = qs.getPlayer().getQuestState(_10351_OwnerOfHall.class);
		if(!qs.isCompleted() && event.equals("quest_accept") && qs.getPlayer().getLevel() >= 95 && pst != null && pst.isCompleted())
		{
			qs.startQuest();
			return "orbis_typia_q10354_07.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Типия)
		{
			switch(reply)
			{
				case 1:
					return "orbis_typia_q10354_05.htm";
				case 2:
					return "orbis_typia_q10354_06.htm";
				case 11:
					if(cond == 2)
					{
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.addExpAndSp(897850000, 416175000);
						st.giveAdena(23655000, true);
						st.giveItem(БутыльсДушойОктависа);
						st.exitQuest(QuestType.ONE_TIME);
						String content = HtmCache.getInstance().getHtmQuest(player.getLang(), "quests/10354_ResurrectedOwnerofHall/orbis_typia_q10354_10.htm");
						content = content.replace("<?name?>", player.getName());
						return content;
					}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == Октавис)
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

		QuestState previous = st.getPlayer().getQuestState(_10351_OwnerOfHall.class);

		if(npcId == Типия)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "orbis_typia_q10354_03.htm";
				case CREATED:
					if(st.getPlayer().getLevel() < 95)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "orbis_typia_q10354_02.htm";
					}
					else if(previous == null || !previous.isCompleted())
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "orbis_typia_q10354_04.htm";
					}
					else
					{
						return "orbis_typia_q10354_01.htm";
					}
				case STARTED:
					if(cond == 1)
					{
						return "orbis_typia_q10354_08.htm";
					}
					else if(cond == 2)
					{
						return "orbis_typia_q10354_09.htm";
					}
			}
		}
		return getNoQuestMsg(st.getPlayer());
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10351_OwnerOfHall.class);
		return previous != null && previous.isCompleted() && player.getLevel() >= 95;
	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1029194, st.getCond() - 1);
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}