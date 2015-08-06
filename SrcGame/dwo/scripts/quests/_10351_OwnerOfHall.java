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
 * @author ANZO
 * Date: 24.04.12
 * Time: 23:26
 */

public class _10351_OwnerOfHall extends Quest
{
	// Квестовые персонажи
	private static final int Типия = 32892;

	// Квестовые монстры
	private static final int Октавис = 29194;

	// Квестовые предметы
	private static final int БраслетОктависа = 19461;

	public _10351_OwnerOfHall()
	{
		addStartNpc(Типия);
		addTalkId(Типия);
		addKillId(Октавис);
	}

	public static void main(String[] args)
	{
		new _10351_OwnerOfHall();
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
		return 10351;
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
			case "32892-08.htm":
				st.startQuest();
				break;
			case "32892-11.htm":
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.addExpAndSp(897850000, 416175000);
				st.giveAdena(23655000, true);
				st.giveItem(БраслетОктависа);
				st.exitQuest(QuestType.ONE_TIME);
				break;
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(player == null)
		{
			return super.onKill(npc, player, isPet);
		}

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
					for(L2PcInstance member : player.getParty().getCommandChannel().getMembers())
					{
						giveItem(member.getQuestState(getClass()));
					}

				}
				else
				{
					for(L2PcInstance member : player.getParty().getMembers())
					{
						giveItem(member.getQuestState(getClass()));
					}
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		QuestState previous = player.getQuestState(_10318_DecayingDarkness.class);

		if(npc.getNpcId() == Типия)
		{
			if(player.getLevel() < 95)
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "32892-02.htm";
			}
			if(previous == null || !previous.isCompleted())
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "32892-04.htm";
			}
			switch(st.getState())
			{
				case COMPLETED:
					return "32892-03.htm";
				case CREATED:
					return "32892-01.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						return "32892-09.htm";
					}
					if(st.getCond() == 2)
					{
						return "32892-10.htm";
					}
					break;
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10318_DecayingDarkness.class);
		return previous != null && previous.isCompleted() && player.getLevel() >= 95;

	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1029212, st.getCond() - 1);
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}