package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 04.06.13
 * Time: 14:21
 */

public class _00237_WindsOfChange extends Quest
{
	private static final int Flauen = 30899;
	private static final int Iason = 30969;
	private static final int Roman = 30897;
	private static final int Morelyn = 30925;
	private static final int Helvetica = 32641;
	private static final int Athenia = 32643;

	private static final int FlauensLetter = 14862;
	private static final int LetterToHelvetica = 14863;
	private static final int LetterToAthenia = 14864;
	private static final int VicinityOfTheFieldOfSilenceResearchCenter = 14865;
	private static final int CertificateOfSupport = 14866;

	public _00237_WindsOfChange()
	{
		addStartNpc(Flauen);
		addTalkId(Iason, Roman, Morelyn, Helvetica, Athenia);
		questItemIds = new int[]{FlauensLetter, LetterToHelvetica, LetterToAthenia};
	}

	public static void main(String[] args)
	{
		new _00237_WindsOfChange();
	}

	@Override
	public int getQuestId()
	{
		return 237;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			qs.giveItem(FlauensLetter);
			return "gatekeeper_flauen_q0237_08.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == Flauen)
		{
			switch(reply)
			{
				case 1:
					return "gatekeeper_flauen_q0237_04.htm";
				case 2:
					return "gatekeeper_flauen_q0237_05.htm";
				case 3:
					return "gatekeeper_flauen_q0237_06.htm";
				case 4:
					return "gatekeeper_flauen_q0237_07.htm";
			}
		}
		else if(npc.getNpcId() == Iason)
		{
			if(st.isStarted())
			{
				switch(reply)
				{
					case 1:
						return "iason_haine_q0237_04.htm";
					case 2:
						st.setCond(2);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "iason_haine_q0237_08.htm";
					case 3:
						return "iason_haine_q0237_11.htm";
					case 5:
						return "iason_haine_q0237_14.htm";
					case 6:
						if(st.hasQuestItems(FlauensLetter))
						{
							st.takeItems(FlauensLetter, -1);
							return "iason_haine_q0237_02.htm";
						}
						else
						{
							return "iason_haine_q0237_02a.htm";
						}
					case 11:
						return "iason_haine_q0237_05.htm";
					case 12:
						return "iason_haine_q0237_06.htm";
					case 13:
						return "iason_haine_q0237_07.htm";
					case 31:
						return "iason_haine_q0237_12.htm";
					case 32:
						st.setCond(5);
						st.giveItem(LetterToHelvetica);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "iason_haine_q0237_15.htm";
					case 41:
						return "iason_haine_q0237_13.htm";
					case 42:
						st.setCond(6);
						st.giveItem(LetterToAthenia);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "iason_haine_q0237_16.htm";
				}
			}
		}
		else if(npc.getNpcId() == Roman)
		{
			if(st.isStarted() && st.getCond() == 2)
			{
				if(reply == 1)
				{
					return "head_blacksmith_roman_q0237_02.htm";
				}
				else if(reply == 2)
				{
					st.setCond(3);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "head_blacksmith_roman_q0237_03.htm";
				}
			}
		}
		else if(npc.getNpcId() == Morelyn)
		{
			if(st.isStarted() && st.getCond() == 3)
			{
				if(reply == 1)
				{
					return "highpriestess_morelyn_q0237_02.htm";
				}
				else if(reply == 2)
				{
					st.setCond(4);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "highpriestess_morelyn_q0237_03.htm";
				}
			}
		}
		else if(npc.getNpcId() == Helvetica)
		{
			if(st.isStarted())
			{
				if(reply == 1)
				{
					if(st.getCond() == 5)
					{
						if(st.hasQuestItems(LetterToHelvetica))
						{
							st.giveItem(VicinityOfTheFieldOfSilenceResearchCenter);
							st.giveAdena(499880, true);
							st.addExpAndSp(2427030, 2786680);
							st.takeItems(LetterToHelvetica, -1);
							st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
							st.exitQuest(QuestType.ONE_TIME);
							return "gakka_q0237_02.htm";
						}
						else
						{
							return "gakka_q0237_03.htm";
						}
					}
					else if(st.getCond() == 6)
					{
						return "gakka_q0237_04.htm";
					}
				}
			}
		}
		else if(npc.getNpcId() == Athenia)
		{
			if(st.isStarted())
			{
				if(reply == 1)
				{
					if(st.getCond() == 6)
					{
						if(st.hasQuestItems(LetterToAthenia))
						{
							st.giveItem(CertificateOfSupport);
							st.giveAdena(499880, true);
							st.addExpAndSp(2427030, 2786680);
							st.takeItems(LetterToAthenia, -1);
							st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
							st.exitQuest(QuestType.ONE_TIME);
							return "minehr_q0237_02.htm";
						}
						else
						{
							return "minehr_q0237_03.htm";
						}
					}
					else if(st.getCond() == 5)
					{
						return "minehr_q0237_04.htm";
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		if(npc.getNpcId() == Flauen)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "gatekeeper_flauen_q0237_02.htm";
				case CREATED:
					return st.getPlayer().getLevel() < 82 ? "gatekeeper_flauen_q0237_03.htm" : "gatekeeper_flauen_q0237_01.htm";
				case STARTED:
					switch(st.getCond())
					{
						case 1:
						case 4:
							return "gatekeeper_flauen_q0237_09.htm";
						case 2:
							return "gatekeeper_flauen_q0237_10.htm";
						case 3:
							return "gatekeeper_flauen_q0237_11.htm";
						case 5:
							return "gatekeeper_flauen_q0237_12.htm";
					}
			}
		}
		else if(npc.getNpcId() == Iason)
		{
			if(st.isStarted())
			{
				switch(st.getCond())
				{
					case 1:
						return "iason_haine_q0237_01.htm";
					case 2:
					case 3:
						return "iason_haine_q0237_09.htm";
					case 4:
						return "iason_haine_q0237_10.htm";
					case 5:
					case 6:
						return "iason_haine_q0237_17.htm";
				}
			}
		}
		else if(npc.getNpcId() == Roman)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 2)
				{
					return "head_blacksmith_roman_q0237_01.htm";
				}
				else if(st.getCond() == 3)
				{
					return "head_blacksmith_roman_q0237_04.htm";
				}
			}
		}
		else if(npc.getNpcId() == Morelyn)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 3)
				{
					return "highpriestess_morelyn_q0237_01.htm";
				}
				else if(st.getCond() == 4)
				{
					return "highpriestess_morelyn_q0237_04.htm";
				}
			}
		}
		else if(npc.getNpcId() == Helvetica)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 5)
				{
					return "gakka_q0237_01.htm";
				}
				if(st.hasQuestItems(CertificateOfSupport) || st.hasQuestItems(VicinityOfTheFieldOfSilenceResearchCenter))
				{
					return st.hasQuestItems(VicinityOfTheFieldOfSilenceResearchCenter) ? "gakka_q0237_05.htm" : "gakka_q0237_06.htm";
				}
			}
		}
		else if(npc.getNpcId() == Athenia)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 6)
				{
					return "minehr_q0237_01.htm";
				}
				if(st.hasQuestItems(CertificateOfSupport) || st.hasQuestItems(VicinityOfTheFieldOfSilenceResearchCenter))
				{
					return st.hasQuestItems(CertificateOfSupport) ? "minehr_q0237_05.htm" : "minehr_q0237_06.htm";
				}
			}
		}
		return null;
	}
}