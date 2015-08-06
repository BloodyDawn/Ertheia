package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.01.13
 * Time: 11:38
 */

public class _10389_TheVoiceofAuthority extends Quest
{
	// Квестовые персонажи
	private static final int Razen = 33803;

	// Квестовые монстры
	private static final int[] Mobs = {
		22139, 22140, 22141, 22147, 22149, 22145, 22154, 22161, 22169, 22172, 22190, 22195, 22144, 22143, 22148, 22150,
		22158, 22162, 22164, 22166, 22170, 22142, 22155, 22159, 22163, 22167, 22171, 19409, 19410
	};

	// Награда
	private static final int PaganSign = 8067;

	public _10389_TheVoiceofAuthority()
	{
		addStartNpc(Razen);
		addTalkId(Razen);
		addKillId(Mobs);
	}

	public static void main(String[] args)
	{
		new _10389_TheVoiceofAuthority();
	}

	private void giveItem(QuestState st, L2Npc npc)
	{
		if(st != null && st.getCond() == 1)
		{
			if(Rnd.getChance(15) && ArrayUtils.contains(Mobs, npc.getNpcId()) && st.getInt("mobCounter") < 30) // TODO: Новый тип уведомления (538951)
			{
				st.set("mobCounter", String.valueOf(st.getInt("mobCounter") + 1));
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			if(st.getInt("mobCounter") >= 30)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
	}

	@Override
	public int getQuestId()
	{
		return 10389;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "razen_q10389_06.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int cond = st.getCond();

		if(npc.getNpcId() == Razen)
		{
			if(reply == 1)
			{
				return "razen_q10389_04.htm";
			}
			else if(reply == 2)
			{
				return "razen_q10389_05.htm";
			}
			else if(reply == 10 && cond == 2)
			{
				st.addExpAndSp(592767000, 59276700);
				st.giveAdena(1302720, true);
				st.giveItem(PaganSign);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "razen_q10389_10.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(player.getParty() == null)
		{
			giveItem(st, npc);
		}
		else
		{
			for(L2PcInstance member : player.getParty().getMembersInRadius(player, 900))
			{
				QuestState pst = member.getQuestState(getClass());
				giveItem(pst, npc);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Razen)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "razen_q10389_03.htm";
				case CREATED:
					QuestState pst = st.getPlayer().getQuestState(_10388_ConspiracyBehindDoors.class);
					return st.getPlayer().getLevel() < 97 || pst == null || !pst.isCompleted() ? "razen_q10389_02.htm" : "razen_q10389_01.htm";
				case STARTED:
					if(cond == 1)
					{
						return "razen_q10389_07.htm";
					}
					else if(cond == 2)
					{
						return "razen_q10389_08.htm";
					}
			}
		}

		return getNoQuestMsg(st.getPlayer());
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState pst = player.getQuestState(_10388_ConspiracyBehindDoors.class);
		return player.getLevel() >= 97 && pst != null && pst.isCompleted();
	}
}
