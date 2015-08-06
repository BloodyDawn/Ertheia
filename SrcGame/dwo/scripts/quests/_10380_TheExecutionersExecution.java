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
 * Time: 14:26
 */

public class _10380_TheExecutionersExecution extends Quest
{
	// Квестовые персонажи
	private static final int Эндриго = 30632;

	// Квестовые монстры
	private static final int СмертоносныйГильотин = 25892;

	// Квестовые предметы
	private static final int ФутболкаСлавы = 35291;

	public _10380_TheExecutionersExecution()
	{
		addStartNpc(Эндриго);
		addTalkId(Эндриго);
		addKillId(СмертоносныйГильотин);
	}

	public static void main(String[] args)
	{
		new _10380_TheExecutionersExecution();
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
		return 10380;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "warden_endrigo_q10380_06.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Эндриго)
		{
			if(reply == 1)
			{
				return "warden_endrigo_q10380_04.htm";
			}
			else if(reply == 2)
			{
				return "warden_endrigo_q10380_05.htm";
			}
			else if(reply == 10 && cond == 2)
			{
				return "warden_endrigo_q10380_09.htm";
			}
			else if(reply == 11 && cond == 2)
			{
				st.addExpAndSp(0, 458117910);
				st.giveItem(ФутболкаСлавы);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "warden_endrigo_q10380_10.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == СмертоносныйГильотин)
		{
			if(player.getParty() == null)
			{
				giveItem(player.getQuestState(getClass()));
			}
			else
			{
				for(L2PcInstance member : player.getParty().getMembersInRadius(player, 900))
				{
					giveItem(member.getQuestState(getClass()));
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

		if(npcId == Эндриго)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "warden_endrigo_q10380_03.htm";
				case CREATED:
					QuestState pst = st.getPlayer().getQuestState(_10379_AnUninvitedGuest.class);
					return pst == null || !pst.isCompleted() || st.getPlayer().getLevel() < 95 ? "warden_endrigo_q10380_02.htm" : "warden_endrigo_q10380_01.htm";
				case STARTED:
					if(cond == 1)
					{
						return "warden_endrigo_q10380_07.htm";
					}
					else if(cond == 2)
					{
						return "warden_endrigo_q10380_08.htm";
					}
			}
		}

		return getNoQuestMsg(st.getPlayer());
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState pst = player.getQuestState(_10379_AnUninvitedGuest.class);
		return player.getLevel() >= 95 && pst != null && pst.isCompleted();
	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1025885, st.getCond() - 1);
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}