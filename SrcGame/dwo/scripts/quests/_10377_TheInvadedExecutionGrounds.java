package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 31.08.12
 * Time: 14:29
 * TODO: Нужны диалоги надгробий (сейчас нужно убивать РБ)
 */

public class _10377_TheInvadedExecutionGrounds extends Quest
{
	// Квестовые персонажи
	private static final int Сильвейн = 30070;
	private static final int Хитсран = 30074;
	private static final int Родерик = 30631;
	private static final int Эндриго = 30632;

	// Квестовые монстры
	private static final int Хоупан = 25886;
	private static final int Крук = 25887;
	private static final int Смотритель = 25888;

	// Квестовые предметы
	private static final int СвитокТелепорта = 35292;
	private static final int ПриказХарлана = 34972;
	private static final int ОтчетЭндриго = 34973;

	public _10377_TheInvadedExecutionGrounds()
	{
		addStartNpc(Сильвейн);
		addTalkId(Сильвейн, Хитсран, Родерик, Эндриго);
		addKillId(Хоупан, Крук, Смотритель);
		questItemIds = new int[]{ПриказХарлана, ОтчетЭндриго};
	}

	public static void main(String[] args)
	{
		new _10377_TheInvadedExecutionGrounds();
	}

	@Override
	public int getQuestId()
	{
		return 10377;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "sylvain_q10377_06.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Сильвейн)
		{
			if(reply == 1)
			{
				return "sylvain_q10377_04.htm";
			}
			else if(reply == 2)
			{
				return "sylvain_q10377_05.htm";
			}
		}
		else if(npcId == Хитсран)
		{
			if(reply == 1 && cond == 1)
			{
				return "hitsran_q10377_02.htm";
			}
			else if(reply == 2 && cond == 1)
			{
				st.setCond(2);
				st.giveItem(ПриказХарлана);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "hitsran_q10377_03.htm";
			}
		}
		else if(npcId == Родерик)
		{
			if(reply == 1 && cond == 2)
			{
				st.takeItems(ПриказХарлана, -1);
				return "warden_roderik_q10377_02.htm";
			}
			else if(reply == 2 && cond == 2)
			{
				st.giveItem(ОтчетЭндриго);
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "warden_roderik_q10377_03.htm";
			}
		}
		else if(npcId == Эндриго)
		{
			if(reply == 1 && cond == 6)
			{
				st.addExpAndSp(756106110, 338608890);
				st.giveAdena(2970560, true);
				st.giveItems(СвитокТелепорта, 2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "warden_endrigo_q10377_02.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		L2Party party = st.getPlayer().getParty();
		if(party != null)
		{
			for(L2PcInstance partyMember : party.getMembersInRadius(st.getPlayer(), 900))
			{
				QuestState pst = partyMember.getQuestState(getClass());
				if(pst != null && pst.isStarted() && pst.getCond() > 2)
				{
					switch(npc.getNpcId())
					{
						case Хоупан:
							if(pst.getCond() == 3)
							{
								pst.setCond(4);
								pst.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							}
							break;
						case Крук:
							if(pst.getCond() == 4)
							{
								pst.setCond(5);
								pst.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							}
							break;
						case Смотритель:
							if(pst.getCond() == 5)
							{
								pst.setCond(6);
								pst.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							}
							break;
					}
				}
			}
		}
		else
		{
			if(st.getCond() > 2)
			{
				switch(npc.getNpcId())
				{
					case Хоупан:
						if(st.getCond() == 3)
						{
							st.setCond(4);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
						break;
					case Крук:
						if(st.getCond() == 4)
						{
							st.setCond(5);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
						break;
					case Смотритель:
						if(st.getCond() == 5)
						{
							st.setCond(6);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
						break;
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

		if(npcId == Сильвейн)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "sylvain_q10377_03.htm";
				case CREATED:
					return st.getPlayer().getLevel() < 95 ? "sylvain_q10377_02.htm" : "sylvain_q10377_01.htm";
				case STARTED:
					return "sylvain_q10377_07.htm";
			}
		}
		else if(npcId == Хитсран)
		{
			if(st.isStarted())
			{
				if(cond == 1)
				{
					return "hitsran_q10377_01.htm";
				}
				else if(cond == 2)
				{
					return "hitsran_q10377_04.htm";
				}
			}
		}
		else if(npcId == Родерик)
		{
			if(st.isStarted())
			{
				if(cond == 2)
				{
					return "warden_roderik_q10377_01.htm";
				}
				else if(cond == 3)
				{
					return "warden_roderik_q10377_04.htm";
				}
				else if(cond == 6)
				{
					return "warden_roderik_q10377_05.htm";
				}
			}
		}
		else if(npcId == Эндриго)
		{
			if(st.isStarted())
			{
				if(cond == 6)
				{
					st.takeItems(ОтчетЭндриго, -1);
					return "warden_endrigo_q10377_01.htm";
				}
			}
		}
		return getNoQuestMsg(st.getPlayer());
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 95;
	}
}