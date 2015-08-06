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
 * @author ANZO
 * Date: 23.04.12
 * Time: 4:03
 */

public class _00462_StuffedAncientHeroes extends Quest
{
	// Квестовые персонажи
	private static final int Типия = 32892;

	// Квестовые монстры
	private static final int[] Герои = {25760, 25761, 25762, 25763, 25764, 25765, 25766, 25767, 25768, 25769, 25770};

	// Квестовые предметы
	private static final int ДоказательствоГероя = 30386;

	public _00462_StuffedAncientHeroes()
	{
		addStartNpc(Типия);
		addTalkId(Типия);
		addKillId(Герои);
	}

	public static void main(String[] args)
	{
		new _00462_StuffedAncientHeroes();
	}

	@Override
	public int getQuestId()
	{
		return 462;
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
			case "32892-07.htm":
				st.startQuest();
				break;
			case "32892-10.htm":
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.giveItems(ДоказательствоГероя, 3);
				st.exitQuest(QuestType.DAILY);
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
			return null;
		}

		if(ArrayUtils.contains(Герои, npc.getNpcId()))
		{
			if(st.getCond() == 1 || st.getCond() == 2)
			{
				if(killer.getParty() != null)
				{
					QuestState pst;
					for(L2PcInstance partyMember : killer.getParty().getMembers())
					{
						pst = partyMember.getQuestState(getClass());
						if(pst != null && pst.isStarted())
						{
							int cond = pst.getCond();
							if(cond < 2)
							{
								pst.setCond(cond + 1);
							}
							pst.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							TIntIntHashMap moblist = new TIntIntHashMap();
							moblist.put(1033347, pst.getCond());
							partyMember.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
						}
					}
				}
				else
				{
					int cond = st.getCond();
					if(cond < 2)
					{
						st.setCond(cond + 1);
					}
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					TIntIntHashMap moblist = new TIntIntHashMap();
					moblist.put(1033347, st.getCond());
					killer.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		QuestState previous = player.getQuestState(_10317_OrbisWitch.class);

		if(npc.getNpcId() == Типия)
		{
			if(previous == null || !previous.isCompleted() || player.getLevel() < 95)
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "32892-03.htm";
			}
			switch(st.getState())
			{
				case COMPLETED:
					return "32892-02.htm";
				case CREATED:
					return "32892-01.htm";
				case STARTED:
					switch(st.getCond())
					{
						case 1:
							return "32892-08.htm";
						case 2:
							return "32892-09.htm";
						case 3:
							st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
							st.giveItems(ДоказательствоГероя, 6);
							st.exitQuest(QuestType.DAILY);
							return "32892-12.htm";
					}
					break;
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10317_OrbisWitch.class);
		return previous != null && previous.isCompleted() && player.getLevel() >= 95;

	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState pst = player.getQuestState(getClass());
		if(pst != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1033347, pst.getCond());
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}