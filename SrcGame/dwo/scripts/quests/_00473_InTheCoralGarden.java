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
 * Time: 12:05
 */

public class _00473_InTheCoralGarden extends Quest
{
	// Квестовые персонажи
	private static final int Фиорен = 33044;

	// Квестовые монстры
	private static final int Михаель = 25799;

	// Квестовые предметы
	private static final int ДоказательствоАда = 30387;

	public _00473_InTheCoralGarden()
	{
		addStartNpc(Фиорен);
		addTalkId(Фиорен);
		addKillId(Михаель);
	}

	public static void main(String[] args)
	{
		new _00473_InTheCoralGarden();
	}

	@Override
	public int getQuestId()
	{
		return 473;
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
			case "33044-04.htm":
				st.startQuest();
				break;
			case "33044-07.htm":
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.giveItems(ДоказательствоАда, 10);
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

		if(npc.getNpcId() == Михаель && st.getCond() == 1)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1025799, 1);
			if(killer.getParty() != null)
			{
				QuestState pst;
				for(L2PcInstance partyMember : killer.getParty().getMembers())
				{
					pst = partyMember.getQuestState(getClass());
					if(pst != null && pst.isStarted())
					{
						pst.setCond(2);
						pst.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						partyMember.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
					}
				}
			}
			else
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				killer.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
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

		if(npc.getNpcId() == Фиорен)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "33044-08.htm";
				case CREATED:
					if(player.getLevel() < 97)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "33044-02.htm";
					}
					else
					{
						return "33044-01.htm";
					}
				case STARTED:
					return st.getCond() == 1 ? "33044-05.htm" : "33044-06.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 97;

	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1025799, st.getCond() - 1);
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}