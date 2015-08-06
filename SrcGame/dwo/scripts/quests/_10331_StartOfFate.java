package dwo.scripts.quests;

import dwo.gameserver.datatables.xml.MultiSellData;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.TutorialShowHtml;
import dwo.scripts.instances.LabyrinthOfBelis;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.07.11
 * Time: 1:11
 */

public class _10331_StartOfFate extends Quest
{
	// Квестовые персонажи
	private static final int PRANA = 32153;
	private static final int RIVIAN = 32147;
	private static final int DEVON = 32160;
	private static final int TOOK = 32150;
	private static final int MOKA = 32157;
	private static final int VALFAR = 32146;
	private static final int LAKCIS = 32977;
	private static final int SEBION = 32978;
	private static final int PANTHEON = 32972;

	// Квестовые предметы
	private static final int SARIL_NECKLACE = 17580;
	private static final int BELIS_MARK = 17615;

	public _10331_StartOfFate()
	{
		addStartNpc(PRANA, MOKA, RIVIAN, DEVON, TOOK, VALFAR);
		addTalkId(MOKA, PRANA, RIVIAN, DEVON, TOOK, VALFAR, LAKCIS, SEBION, PANTHEON);
		questItemIds = new int[]{SARIL_NECKLACE, BELIS_MARK};
	}

	public static void main(String[] args)
	{
		new _10331_StartOfFate();
	}

	/***
	 * Смена класса персонажу на указанный
	 * @param st QuestState состояние квеста
	 * @param classId ClassId класса
	 */
	private void changeClassAndFinish(QuestState st, L2Npc npc, ClassId classId)
	{
		if(st != null && st.isStarted() && st.getCond() > 5)
		{
			L2PcInstance player = st.getPlayer();
			if(classId.getParent() == player.getClassId())
			{
				player.setClassId(classId.getId());
				player.setBaseClassId(player.getActiveClassId());
				player.broadcastUserInfo();
				st.exitQuest(QuestType.ONE_TIME);

				st.giveAdena(80000, true);
				st.addExpAndSp(200000, 48);
				player.sendPacket(new TutorialShowHtml(TutorialShowHtml.CLIENT_SIDE, "..\\L2text\\QT_009_enchant_01.htm"));
				st.giveItems(17821, 40); // Proof of Courage
				MultiSellData.getInstance().separateAndSend(717, player, npc);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			}
		}
	}

	@Override
	public int getQuestId()
	{
		return 10331;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			switch(qs.getPlayer().getRace())
			{
				case Human:
					return "highpriest_prana_q10331_06.htm";
				case Elf:
					return "grandmaster_rivian_q10331_06.htm";
				case DarkElf:
					return "grandmagister_devon_q10331_06.htm";
				case Orc:
					return "high_prefect_toonks_q10331_06.htm";
				case Dwarf:
					return "head_blacksmith_mokabred_q10331_06.htm";
				case Kamael:
					return "grandmaster_valpar_q10331_06.htm";
			}
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		switch(npc.getNpcId())
		{
			case PRANA:
				if(player.getRace() != Race.Human)
				{
					return "highpriest_prana_q10331_03.htm";
				}

				switch(reply)
				{
					case 1:
						return "highpriest_prana_q10331_05.htm";
					case 2: // Воитель
						changeClassAndFinish(st, npc, ClassId.warrior);
						return "highpriest_prana_q10331_11.htm";
					case 3: // Рыцарь
						changeClassAndFinish(st, npc, ClassId.knight);
						return "highpriest_prana_q10331_12.htm";
					case 4: // Разбойник
						changeClassAndFinish(st, npc, ClassId.rogue);
						return "highpriest_prana_q10331_13.htm";
					case 5: // Волшебник
						changeClassAndFinish(st, npc, ClassId.wizard);
						return "highpriest_prana_q10331_14.htm";
					case 6: // Клерик
						changeClassAndFinish(st, npc, ClassId.cleric);
						return "highpriest_prana_q10331_15.htm";
				}
				return null;
			case RIVIAN:
				if(player.getRace() != Race.Elf)
				{
					return "grandmaster_rivian_q10331_03.htm";
				}

				switch(reply)
				{
					case 1:
						return "grandmaster_rivian_q10331_05.htm";
					case 2: // Светлый Рыцарь
						changeClassAndFinish(st, npc, ClassId.elvenKnight);
						return "grandmaster_rivian_q10331_11.htm";
					case 3: // Разведчик
						changeClassAndFinish(st, npc, ClassId.elvenScout);
						return "grandmaster_rivian_q10331_12.htm";
					case 4: // Светлый Маг
						changeClassAndFinish(st, npc, ClassId.elvenWizard);
						return "grandmaster_rivian_q10331_13.htm";
					case 5: // Оракул Евы
						changeClassAndFinish(st, npc, ClassId.oracle);
						return "grandmaster_rivian_q10331_14.htm";
				}
				return null;
			case DEVON:
				if(player.getRace() != Race.DarkElf)
				{
					return "grandmagister_devon_q10331_03.htm";
				}

				switch(reply)
				{
					case 1:
						return "grandmagister_devon_q10331_05.htm";
					case 2: // Темный Рыцарь
						changeClassAndFinish(st, npc, ClassId.palusKnight);
						return "grandmagister_devon_q10331_11.htm";
					case 3: // Ассасин
						changeClassAndFinish(st, npc, ClassId.assassin);
						return "grandmagister_devon_q10331_12.htm";
					case 4: // Темный Маг
						changeClassAndFinish(st, npc, ClassId.darkWizard);
						return "grandmagister_devon_q10331_13.htm";
					case 5: // Оракул Шилен
						changeClassAndFinish(st, npc, ClassId.shillienOracle);
						return "grandmagister_devon_q10331_14.htm";
				}
				return null;
			case TOOK:
				if(player.getRace() != Race.Orc)
				{
					return "high_prefect_toonks_q10331_03.htm";
				}

				switch(reply)
				{
					case 1:
						return "high_prefect_toonks_q10331_05.htm";
					case 2: // Налетчик
						changeClassAndFinish(st, npc, ClassId.orcRaider);
						return "high_prefect_toonks_q10331_11.htm";
					case 3: // Монах
						changeClassAndFinish(st, npc, ClassId.orcMonk);
						return "high_prefect_toonks_q10331_12.htm";
					case 4: // Шаманом
						changeClassAndFinish(st, npc, ClassId.orcShaman);
						return "high_prefect_toonks_q10331_13.htm";
				}
				return null;
			case MOKA:
				if(player.getRace() != Race.Dwarf)
				{
					return "head_blacksmith_mokabred_q10331_03.htm";
				}

				switch(reply)
				{
					case 1:
						return "head_blacksmith_mokabred_q10331_05.htm";
					case 2: // Собиратель
						changeClassAndFinish(st, npc, ClassId.scavenger);
						return "head_blacksmith_mokabred_q10331_11.htm";
					case 3: // Ремесленник
						changeClassAndFinish(st, npc, ClassId.artisan);
						return "head_blacksmith_mokabred_q10331_12.htm";
				}
				return null;
			case VALFAR:
				if(player.getRace() != Race.Kamael)
				{
					return "grandmaster_valpar_q10331_03.htm";
				}

				switch(reply)
				{
					case 1:
						return "grandmaster_valpar_q10331_05.htm";
					case 2: // Солдат
						changeClassAndFinish(st, npc, ClassId.trooper);
						return "grandmaster_valpar_q10331_11.htm";
					case 3: // Надзиратель
						changeClassAndFinish(st, npc, ClassId.warder);
						return "grandmaster_valpar_q10331_12.htm";
				}
				return null;
			case LAKCIS:
				switch(reply)
				{
					case 1:
						st.setCond(2);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "si_illusion_larcis_q10331_02.htm";
					case 10:
						player.teleToLocation(-111774, 231933, -3160);
						return null;
				}
				return null;
			case SEBION:
				switch(reply)
				{
					case 1:
						return "si_illusion_sebion_q10331_02.htm";
					case 2:
						st.setCond(3);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "si_illusion_sebion_q10331_03.htm";
					case 3:
						QuestManager.getInstance().getQuest(LabyrinthOfBelis.class).notifyEvent("enterInstance", npc, player);
						return null;
				}
			case PANTHEON:
				if(reply == 1)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.takeItems(SARIL_NECKLACE, -1);
					switch(player.getRace())
					{
						case Human:
							st.setCond(6);
							return "si_illusion_pantheon_q10331_02.htm";
						case Elf:
							st.setCond(7);
							return "si_illusion_pantheon_q10331_04.htm";
						case DarkElf:
							st.setCond(8);
							return "si_illusion_pantheon_q10331_06.htm";
						case Orc:
							st.setCond(9);
							return "si_illusion_pantheon_q10331_08.htm";
						case Dwarf:
							st.setCond(10);
							return "si_illusion_pantheon_q10331_10.htm";
						case Kamael:
							st.setCond(11);
							return "si_illusion_pantheon_q10331_12.htm";
					}
				}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		switch(npc.getNpcId())
		{
			case PRANA:
				switch(st.getState())
				{
					case CREATED:
						if(player.getRace() != Race.Human)
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "highpriest_prana_q10331_03.htm";
						}
						if(player.getLevel() < 18)
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "highpriest_prana_q10331_02.htm";
						}
						if(player.getClassId() == ClassId.fighter || player.getClassId() == ClassId.mage)
						{
							return "highpriest_prana_q10331_01.htm";
						}
						break;
					case STARTED:
						if(st.getCond() == 1)
						{
							return "highpriest_prana_q10331_07.htm";
						}
						if(st.getCond() == 6 && player.getRace() != Race.Human)
						{
							return "highpriest_prana_q10331_08.htm";
						}
						if(st.getCond() == 6 && player.getClassId() == ClassId.fighter)
						{
							return "highpriest_prana_q10331_09.htm";
						}
						if(st.getCond() == 6 && player.getClassId() == ClassId.mage)
						{
							return "highpriest_prana_q10331_10.htm";
						}
						break;
					case COMPLETED:
						return "highpriest_prana_q10331_04.htm";
				}
				break;
			case RIVIAN:
				switch(st.getState())
				{
					case CREATED:
						if(player.getRace() != Race.Elf)
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "grandmaster_rivian_q10331_03.htm";
						}
						if(player.getLevel() < 18)
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "grandmaster_rivian_q10331_02.htm";
						}
						if(player.getClassId() == ClassId.elvenFighter || player.getClassId() == ClassId.elvenMage)
						{
							return "grandmaster_rivian_q10331_01.htm";
						}
						break;
					case STARTED:
						if(st.getCond() == 1)
						{
							return "grandmaster_rivian_q10331_07.htm";
						}
						if(st.getCond() == 7 && player.getRace() != Race.Elf)
						{
							return "grandmaster_rivian_q10331_08.htm";
						}
						if(st.getCond() == 7 && player.getClassId() == ClassId.elvenMage)
						{
							return "grandmaster_rivian_q10331_10.htm";
						}
						if(st.getCond() == 7 && player.getClassId() == ClassId.elvenFighter)
						{
							return "grandmaster_rivian_q10331_09.htm";
						}
						break;
					case COMPLETED:
						return "grandmaster_rivian_q10331_04.htm";
				}
				break;
			case DEVON:
				switch(st.getState())
				{
					case CREATED:
						if(player.getRace() != Race.DarkElf)
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "grandmagister_devon_q10331_03.htm";
						}
						if(player.getLevel() < 18)
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "grandmagister_devon_q10331_02.htm";
						}
						if(player.getClassId() == ClassId.darkFighter || player.getClassId() == ClassId.darkMage)
						{
							return "grandmagister_devon_q10331_01.htm";
						}
						break;
					case STARTED:
						if(st.getCond() == 1)
						{
							return "grandmagister_devon_q10331_07.htm";
						}
						if(st.getCond() == 8 && player.getRace() != Race.DarkElf)
						{
							return "grandmagister_devon_q10331_08.htm";
						}
						if(st.getCond() == 8 && player.getClassId() == ClassId.darkMage)
						{
							return "grandmagister_devon_q10331_10.htm";
						}
						if(st.getCond() == 8 && player.getClassId() == ClassId.darkFighter)
						{
							return "grandmagister_devon_q10331_09.htm";
						}
						break;
					case COMPLETED:
						return "grandmagister_devon_q10331_04.htm";
				}
				break;
			case TOOK:
				switch(st.getState())
				{
					case CREATED:
						if(player.getRace() != Race.Orc)
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "high_prefect_toonks_q10331_03.htm";
						}
						if(player.getLevel() < 18)
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "high_prefect_toonks_q10331_02.htm";
						}
						if(player.getClassId() == ClassId.orcFighter || player.getClassId() == ClassId.orcMage)
						{
							return "high_prefect_toonks_q10331_01.htm";
						}
						break;
					case STARTED:
						if(st.getCond() == 1)
						{
							return "high_prefect_toonks_q10331_07.htm";
						}
						if(st.getCond() == 9 && player.getRace() != Race.Orc)
						{
							return "high_prefect_toonks_q10331_08.htm";
						}
						if(st.getCond() == 9 && player.getClassId() == ClassId.orcMage)
						{
							return "high_prefect_toonks_q10331_10.htm";
						}
						if(st.getCond() == 9 && player.getClassId() == ClassId.orcFighter)
						{
							return "high_prefect_toonks_q10331_09.htm";
						}
						break;
					case COMPLETED:
						return "high_prefect_toonks_q10331_04.htm";
				}
			case MOKA:
				switch(st.getState())
				{
					case CREATED:
						if(player.getRace() != Race.Dwarf)
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "head_blacksmith_mokabred_q10331_03.htm";
						}
						if(player.getLevel() < 18)
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "head_blacksmith_mokabred_q10331_02.htm";
						}
						if(player.getClassId() == ClassId.dwarvenFighter)
						{
							return "head_blacksmith_mokabred_q10331_01.htm";
						}
						break;
					case STARTED:
						if(st.getCond() == 1)
						{
							return "head_blacksmith_mokabred_q10331_07.htm";
						}
						if(st.getCond() == 10 && player.getRace() != Race.Dwarf)
						{
							return "head_blacksmith_mokabred_q10331_08.htm";
						}
						if(st.getCond() == 10 && player.getClassId() == ClassId.dwarvenFighter)
						{
							return "head_blacksmith_mokabred_q10331_09.htm";
						}
						break;
					case COMPLETED:
						return "head_blacksmith_mokabred_q10331_04.htm";
				}
				break;
			case VALFAR:
				switch(st.getState())
				{
					case CREATED:
						if(player.getRace() != Race.Kamael)
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "grandmaster_valpar_q10331_03.htm";
						}
						if(player.getLevel() < 18)
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "grandmaster_valpar_q10331_02.htm";
						}
						if(player.getClassId() == ClassId.maleSoldier || player.getClassId() == ClassId.femaleSoldier)
						{
							return "grandmaster_valpar_q10331_01.htm";
						}
						break;
					case STARTED:
						if(st.getCond() == 1)
						{
							return "grandmaster_valpar_q10331_07.htm";
						}
						if(st.getCond() == 11 && player.getRace() != Race.Kamael)
						{
							return "grandmaster_valpar_q10331_08.htm";
						}
						if(st.getCond() == 11 && player.getClassId() == ClassId.maleSoldier)
						{
							return "grandmaster_valpar_q10331_09.htm";
						}
						if(st.getCond() == 11 && player.getClassId() == ClassId.femaleSoldier)
						{
							return "grandmaster_valpar_q10331_10.htm";
						}
						break;
					case COMPLETED:
						return "grandmaster_valpar_q10331_04.htm";
				}
				break;
			case LAKCIS:
				switch(st.getCond())
				{
					case 1:
						return "si_illusion_larcis_q10331_01.htm";
					case 2:
						return "si_illusion_larcis_q10331_03.htm";
					default:
						return "si_illusion_larcis_q10331_06.htm";
				}
			case SEBION:
				switch(st.getCond())
				{
					case 2:
						return "si_illusion_sebion_q10331_01.htm";
					case 3:
						return "si_illusion_sebion_q10331_04.htm";
					case 4:
						st.setCond(5);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "si_illusion_sebion_q10331_06.htm";
					case 5:
						return "si_illusion_sebion_q10331_07.htm";
				}
				break;
			case PANTHEON:
				switch(st.getCond())
				{
					case 5:
						return "si_illusion_pantheon_q10331_01.htm";
					case 6:
						return "si_illusion_pantheon_q10331_03.htm";
					case 7:
						return "si_illusion_pantheon_q10331_05.htm";
					case 8:
						return "si_illusion_pantheon_q10331_07.htm";
					case 9:
						return "si_illusion_pantheon_q10331_09.htm";
					case 10:
						return "si_illusion_pantheon_q10331_11.htm";
					case 11:
						return "si_illusion_pantheon_q10331_13.htm";
				}
				break;
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 18;
	}
}