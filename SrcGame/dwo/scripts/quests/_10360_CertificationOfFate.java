package dwo.scripts.quests;

import dwo.gameserver.datatables.xml.MultiSellData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.player.base.ClassLevel;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowUsm;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.06.13
 * Time: 13:03
 */

public class _10360_CertificationOfFate extends Quest
{
	// Квестовые персонажи
	private static final int MENDIO = 30504;
	private static final int RAYMOND = 30289;
	private static final int ELLIASIN = 30155;
	private static final int ESRANDEL = 30158;
	private static final int GERSHWIN = 32196;
	private static final int DRIKUS = 30505;
	private static final int RAINS = 30288;
	private static final int TOBIAS = 30297;

	private static final int RENFAD = 33524;

	private static final int JOEL = 33516;
	private static final int SCHUAZEN = 33517;
	private static final int SELON = 33518;

	// Квестовые монстры
	private static final int REG_KANILOV = 27459;
	private static final int REG_POSLOF = 27460;
	private static final int SAKUM = 27453;

	// Квестовые предметы
	private static final int SHINE_STONE = 17587;

	public _10360_CertificationOfFate()
	{
		addStartNpc(MENDIO, RAYMOND, ELLIASIN, ESRANDEL, GERSHWIN, DRIKUS, RAINS, TOBIAS);
		addTalkId(MENDIO, RAYMOND, ELLIASIN, ESRANDEL, GERSHWIN, DRIKUS, RAINS, TOBIAS, RENFAD, JOEL, SCHUAZEN, SELON);
		addKillId(REG_KANILOV, REG_POSLOF, SAKUM);
		questItemIds = new int[]{SHINE_STONE};
	}

	public static void main(String[] args)
	{
		new _10360_CertificationOfFate();
	}

	/***
	 * Смена класса персонажу на указанный
	 * @param st QuestState состояние квеста
	 * @param classId ClassId класса
	 */
	private void changeClassAndFinish(QuestState st, L2Npc npc, ClassId classId)
	{
		if(st != null && st.isStarted() && st.getCond() > 7)
		{
			L2PcInstance player = st.getPlayer();
			if(classId.getParent() == player.getClassId())
			{
				player.setClassId(classId.getId());
				player.setBaseClassId(player.getActiveClassId());
				player.broadcastUserInfo();
				st.exitQuest(QuestType.ONE_TIME);

				st.giveAdena(110000, true);
				st.addExpAndSp(2700000, 250000);
				st.takeItems(SHINE_STONE, -1);
				st.giveItems(17822, 40); // Proof of Justice
				st.giveItems(32777, 1); // Goddess of Destruction Guide - Level 40-85
				MultiSellData.getInstance().separateAndSend(718, player, npc);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			}
		}
	}

	@Override
	public int getQuestId()
	{
		return 10360;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			L2PcInstance player = qs.getPlayer();
			qs.startQuest();
			switch(player.getRace())
			{
				case Human:
					return player.isMageClass() ? "bishop_raimund_q10360_06.htm" : "master_rains_q10360_06.htm";
				case Elf:
					return player.isMageClass() ? "eso_q10360_06.htm" : "elliasin_q10360_06.htm";
				case DarkElf:
					return "master_tobias_q10360_06.htm";
				case Orc:
					return "high_prefect_drikus_q10360_06.htm";
				case Dwarf:
					return "head_blacksmith_mendio_q10360_06.htm";
				case Kamael:
					return "grandmaster_gershuin_q10360_06.htm";
			}
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		switch(npc.getNpcId())
		{
			case RENFAD:
				switch(reply)
				{
					case 1:
						return "gludio_renfad_q10360_02.htm";
					case 2:
						st.setCond(2);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "gludio_renfad_q10360_03.htm";
				}
			case JOEL:
				switch(reply)
				{
					case 1:
						return "gludio_joel_q10360_02.htm";
					case 2:
						st.setCond(4);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "gludio_joel_q10360_02.htm";
				}
			case SCHUAZEN:
				switch(reply)
				{
					case 1:
						return "gludio_shuazen_q10360_02.htm";
					case 2:
						st.setCond(6);
						player.showUsmVideo(ExShowUsm.Q003);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "gludio_shuazen_q10360_03.htm";
				}
			case SELON:
				switch(reply)
				{
					case 1:
						return "gludio_selon_q10360_02.htm";
					case 2:
						return "gludio_selon_q10360_03.htm";
					case 3:
						return "gludio_selon_q10360_04.htm";
					case 4:
						switch(player.getRace())
						{
							case Human:
								if(player.isMageClass())
								{
									st.setCond(9);
									st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
									return "gludio_selon_q10360_07.htm";
								}
								else
								{
									st.setCond(8);
									st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
									return "gludio_selon_q10360_06.htm";
								}
							case Elf:
								if(player.isMageClass())
								{
									st.setCond(11);
									st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
									return "gludio_selon_q10360_09.htm";
								}
								else
								{
									st.setCond(10);
									st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
									return "gludio_selon_q10360_08.htm";
								}
							case DarkElf:
								st.setCond(12);
								st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
								return "gludio_selon_q10360_10.htm";
							case Orc:
								st.setCond(13);
								st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
								return "gludio_selon_q10360_11.htm";
							case Dwarf:
								st.setCond(14);
								st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
								return "gludio_selon_q10360_12.htm";
							case Kamael:
								st.setCond(15);
								st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
								return "gludio_selon_q10360_13.htm";
						}
				}
			case MENDIO:
				if(player.getRace() == Race.Dwarf)
				{
					switch(reply)
					{
						case 1:
							return "head_blacksmith_mendio_q10360_05.htm";
						case 10:
							player.teleToLocation(-24795, 188754, -3960);
							return null;
						case 2:
							changeClassAndFinish(st, npc, ClassId.bountyHunter);
							return "head_blacksmith_mendio_q10360_11.htm";
						case 3:
							changeClassAndFinish(st, npc, ClassId.warsmith);
							return "head_blacksmith_mendio_q10360_12.htm";
					}
				}
				else
				{
					return "head_blacksmith_mendio_q10360_08.htm";
				}
			case RAYMOND:
				if(player.getRace() == Race.Human)
				{
					switch(reply)
					{
						case 1:
							return "bishop_raimund_q10360_05.htm";
						case 10:
							player.teleToLocation(-24795, 188754, -3960);
							return null;
						case 2:
							changeClassAndFinish(st, npc, ClassId.sorceror);
							return "bishop_raimund_q10360_11.htm";
						case 3:
							changeClassAndFinish(st, npc, ClassId.necromancer);
							return "bishop_raimund_q10360_12.htm";
						case 4:
							changeClassAndFinish(st, npc, ClassId.warlock);
							return "bishop_raimund_q10360_13.htm";
						case 5:
							changeClassAndFinish(st, npc, ClassId.bishop);
							return "bishop_raimund_q10360_14.htm";
						case 6:
							changeClassAndFinish(st, npc, ClassId.prophet);
							return "bishop_raimund_q10360_15.htm";
					}
				}
				else
				{
					return "bishop_raimund_q10360_08.htm";
				}
			case ELLIASIN:
				if(player.getRace() == Race.Elf)
				{
					switch(reply)
					{
						case 1:
							return "elliasin_q10360_05.htm";
						case 10:
							player.teleToLocation(-24795, 188754, -3960);
							return null;
						case 2:
							changeClassAndFinish(st, npc, ClassId.templeKnight);
							return "elliasin_q10360_11.htm";
						case 3:
							changeClassAndFinish(st, npc, ClassId.swordSinger);
							return "elliasin_q10360_12.htm";
						case 4:
							changeClassAndFinish(st, npc, ClassId.plainsWalker);
							return "elliasin_q10360_13.htm";
						case 5:
							changeClassAndFinish(st, npc, ClassId.silverRanger);
							return "elliasin_q10360_14.htm";
					}
				}
				else
				{
					return "elliasin_q10360_08.htm";
				}
			case ESRANDEL:
				if(player.getRace() == Race.Elf)
				{
					switch(reply)
					{
						case 1:
							return "eso_q10360_05.htm";
						case 10:
							player.teleToLocation(-24795, 188754, -3960);
							return null;
						case 2:
							changeClassAndFinish(st, npc, ClassId.spellsinger);
							return "eso_q10360_11.htm";
						case 3:
							changeClassAndFinish(st, npc, ClassId.elementalSummoner);
							return "eso_q10360_12.htm";
						case 4:
							changeClassAndFinish(st, npc, ClassId.elder);
							return "eso_q10360_13.htm";
					}
				}
				else
				{
					return "eso_q10360_08.htm";
				}
			case GERSHWIN:
				if(player.getRace() == Race.Kamael)
				{
					switch(reply)
					{
						case 1:
							return "grandmaster_gershuin_q10360_05.htm";
						case 10:
							player.teleToLocation(-24795, 188754, -3960);
							return null;
						case 2:
							changeClassAndFinish(st, npc, ClassId.berserker);
							return "grandmaster_gershuin_q10360_11.htm";
						case 3:
							changeClassAndFinish(st, npc, ClassId.maleSoulbreaker);
							return "grandmaster_gershuin_q10360_12.htm";
						case 4:
							changeClassAndFinish(st, npc, ClassId.arbalester);
							return "grandmaster_gershuin_q10360_13.htm";
						case 5:
							changeClassAndFinish(st, npc, ClassId.femaleSoldier);
							return "grandmaster_gershuin_q10360_14.htm";
					}
				}
				else
				{
					return "grandmaster_gershuin_q10360_08.htm";
				}
			case DRIKUS:
				if(player.getRace() == Race.Orc)
				{
					switch(reply)
					{
						case 1:
							return "high_prefect_drikus_q10360_05.htm";
						case 10:
							player.teleToLocation(-24795, 188754, -3960);
							return null;
						case 2:
							changeClassAndFinish(st, npc, ClassId.destroyer);
							return "high_prefect_drikus_q10360_12.htm";
						case 3:
							changeClassAndFinish(st, npc, ClassId.tyrant);
							return "high_prefect_drikus_q10360_13.htm";
						case 4:
							changeClassAndFinish(st, npc, ClassId.overlord);
							return "high_prefect_drikus_q10360_14.htm";
						case 5:
							changeClassAndFinish(st, npc, ClassId.warcryer);
							return "high_prefect_drikus_q10360_15.htm";
					}
				}
				else
				{
					return "high_prefect_drikus_q10360_08.htm";
				}
			case RAINS:
				if(player.getRace() == Race.Human)
				{
					switch(reply)
					{
						case 1:
							return "master_rains_q10360_05.htm";
						case 10:
							player.teleToLocation(-24795, 188754, -3960);
							return null;
						case 2:
							changeClassAndFinish(st, npc, ClassId.gladiator);
							return "master_rains_q10360_12.htm";
						case 3:
							changeClassAndFinish(st, npc, ClassId.warlord);
							return "master_rains_q10360_13.htm";
						case 4:
							changeClassAndFinish(st, npc, ClassId.paladin);
							return "master_rains_q10360_14.htm";
						case 5:
							changeClassAndFinish(st, npc, ClassId.darkAvenger);
							return "master_rains_q10360_15.htm";
						case 6:
							changeClassAndFinish(st, npc, ClassId.treasureHunter);
							return "master_rains_q10360_16.htm";
						case 7:
							changeClassAndFinish(st, npc, ClassId.hawkeye);
							return "master_rains_q10360_17.htm";
					}
				}
				else
				{
					return "master_rains_q10360_08.htm";
				}
			case TOBIAS:
				if(player.getRace() == Race.DarkElf)
				{
					switch(reply)
					{
						case 1:
							return "master_tobias_q10360_05.htm";
						case 10:
							player.teleToLocation(-24795, 188754, -3960);
							return null;
						case 2:
							changeClassAndFinish(st, npc, ClassId.shillienKnight);
							return "master_tobias_q10360_13.htm";
						case 3:
							changeClassAndFinish(st, npc, ClassId.bladedancer);
							return "master_tobias_q10360_14.htm";
						case 4:
							changeClassAndFinish(st, npc, ClassId.abyssWalker);
							return "master_tobias_q10360_15.htm";
						case 5:
							changeClassAndFinish(st, npc, ClassId.phantomRanger);
							return "master_tobias_q10360_16.htm";
						case 6:
							changeClassAndFinish(st, npc, ClassId.spellhowler);
							return "master_tobias_q10360_17.htm";
						case 7:
							changeClassAndFinish(st, npc, ClassId.phantomSummoner);
							return "master_tobias_q10360_18.htm";
						case 8:
							changeClassAndFinish(st, npc, ClassId.shillenElder);
							return "master_tobias_q10360_19.htm";
					}
				}
				else
				{
					return "master_rains_q10360_08.htm";
				}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return super.onKill(npc, player, isPet);
		}
		if(st.getCond() == 2 && npc.getNpcId() == REG_KANILOV)
		{
			st.setCond(3);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		else if(st.getCond() == 4 && npc.getNpcId() == REG_POSLOF)
		{
			st.setCond(5);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		else if(st.getCond() == 6 && npc.getNpcId() == SAKUM)
		{
			st.setCond(7);
			st.giveItem(SHINE_STONE);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}

		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == MENDIO)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 38 && player.getClassId().level() == ClassLevel.FIRST.ordinal())
					{
						if(player.getRace() == Race.Dwarf)
						{
							return "head_blacksmith_mendio_q10360_01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "head_blacksmith_mendio_q10360_03.htm";
						}
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "head_blacksmith_mendio_q10360_02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "head_blacksmith_mendio_q10360_07.htm";
					}
					if(st.getCond() > 7)
					{
						if(player.getRace() == Race.Dwarf)
						{
							if(player.getClassId() == ClassId.scavenger)
							{
								return "head_blacksmith_mendio_q10360_09.htm";
							}
							else if(player.getClassId() == ClassId.artisan)
							{
								return "head_blacksmith_mendio_q10360_10.htm";
							}
						}
						else
						{
							return "head_blacksmith_mendio_q10360_08.htm";
						}
					}
					break;
				case COMPLETED:
					return "head_blacksmith_mendio_q10360_04.htm";
			}
		}
		else if(npc.getNpcId() == RAYMOND)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 38 && player.getClassId().level() == ClassLevel.FIRST.ordinal())
					{
						if(player.getRace() == Race.Human && player.isMageClass())
						{
							return "bishop_raimund_q10360_01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "bishop_raimund_q10360_03.htm";
						}
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "bishop_raimund_q10360_02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "bishop_raimund_q10360_07.htm";
					}
					if(st.getCond() > 7)
					{
						if(player.getRace() == Race.Human && player.isMageClass())
						{
							if(player.getClassId() == ClassId.wizard)
							{
								return "bishop_raimund_q10360_09.htm";
							}
							else if(player.getClassId() == ClassId.cleric)
							{
								return "bishop_raimund_q10360_10.htm";
							}
						}
						else
						{
							return "bishop_raimund_q10360_08.htm";
						}
					}
					break;
				case COMPLETED:
					return "bishop_raimund_q10360_04.htm";
			}
		}
		else if(npc.getNpcId() == ELLIASIN)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 38 && player.getClassId().level() == ClassLevel.FIRST.ordinal())
					{
						if(player.getRace() == Race.Elf && !player.isMageClass())
						{
							return "elliasin_q10360_01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "elliasin_q10360_03.htm";
						}
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "elliasin_q10360_02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "elliasin_q10360_07.htm";
					}
					if(st.getCond() > 7)
					{
						if(player.getRace() == Race.Elf && !player.isMageClass())
						{
							if(player.getClassId() == ClassId.elvenKnight)
							{
								return "elliasin_q10360_09.htm";
							}
							else if(player.getClassId() == ClassId.elvenScout)
							{
								return "elliasin_q10360_10.htm";
							}
						}
						else
						{
							return "elliasin_q10360_08.htm";
						}
					}
					break;
				case COMPLETED:
					return "elliasin_q10360_04.htm";
			}
		}
		else if(npc.getNpcId() == ESRANDEL)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 38 && player.getClassId().level() == ClassLevel.FIRST.ordinal())
					{
						if(player.getRace() == Race.Elf && player.isMageClass())
						{
							return "eso_q10360_01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "eso_q10360_03.htm";
						}
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "eso_q10360_02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "eso_q10360_07.htm";
					}
					if(st.getCond() > 7)
					{
						if(player.getRace() == Race.Elf && player.isMageClass())
						{
							if(player.getClassId() == ClassId.elvenWizard)
							{
								return "eso_q10360_09.htm";
							}
							else if(player.getClassId() == ClassId.oracle)
							{
								return "eso_q10360_10.htm";
							}
						}
						else
						{
							return "eso_q10360_08.htm";
						}
					}
					break;
				case COMPLETED:
					return "eso_q10360_04.htm";
			}
		}
		else if(npc.getNpcId() == GERSHWIN)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 38 && player.getClassId().level() == ClassLevel.FIRST.ordinal())
					{
						if(player.getRace() == Race.Kamael)
						{
							return "grandmaster_gershuin_q10360_01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "grandmaster_gershuin_q10360_03.htm";
						}
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "grandmaster_gershuin_q10360_02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "grandmaster_gershuin_q10360_07.htm";
					}
					if(st.getCond() > 7)
					{
						if(player.getRace() == Race.Kamael)
						{
							if(player.getClassId() == ClassId.trooper)
							{
								return "grandmaster_gershuin_q10360_09.htm";
							}
							else if(player.getClassId() == ClassId.warder)
							{
								return "grandmaster_gershuin_q10360_10.htm";
							}
						}
						else
						{
							return "grandmaster_gershuin_q10360_08.htm";
						}
					}
					break;
				case COMPLETED:
					return "grandmaster_gershuin_q10360_04.htm";
			}
		}
		else if(npc.getNpcId() == DRIKUS)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 38 && player.getClassId().level() == ClassLevel.FIRST.ordinal())
					{
						if(player.getRace() == Race.Orc)
						{
							return "high_prefect_drikus_q10360_01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "high_prefect_drikus_q10360_03.htm";
						}
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "high_prefect_drikus_q10360_02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "high_prefect_drikus_q10360_07.htm";
					}
					if(st.getCond() > 7)
					{
						if(player.getRace() == Race.Orc)
						{
							if(player.getClassId() == ClassId.orcRaider)
							{
								return "high_prefect_drikus_q10360_09.htm";
							}
							else if(player.getClassId() == ClassId.orcMonk)
							{
								return "high_prefect_drikus_q10360_10.htm";
							}
							else if(player.getClassId() == ClassId.orcShaman)
							{
								return "high_prefect_drikus_q10360_11.htm";
							}
						}
						else
						{
							return "high_prefect_drikus_q10360_08.htm";
						}
					}
					break;
				case COMPLETED:
					return "high_prefect_drikus_q10360_04.htm";
			}
		}
		else if(npc.getNpcId() == RAINS)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 38 && player.getClassId().level() == ClassLevel.FIRST.ordinal())
					{
						return player.getRace() == Race.Human && !player.isMageClass() ? "master_rains_q10360_01.htm" : "master_rains_q10360_03.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "master_rains_q10360_02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "master_rains_q10360_07.htm";
					}
					if(st.getCond() > 7)
					{
						if(player.getRace() == Race.Human && !player.isMageClass())
						{
							if(player.getClassId() == ClassId.warrior)
							{
								return "master_rains_q10360_09.htm";
							}
							else if(player.getClassId() == ClassId.knight)
							{
								return "master_rains_q10360_10.htm";
							}
							else if(player.getClassId() == ClassId.rogue)
							{
								return "master_rains_q10360_11.htm";
							}
						}
						else
						{
							return "master_rains_q10360_08.htm";
						}
					}
					break;
				case COMPLETED:
					return "master_rains_q10360_04.htm";
			}
		}
		else if(npc.getNpcId() == TOBIAS)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 38 && player.getClassId().level() == ClassLevel.FIRST.ordinal())
					{
						return player.getRace() == Race.DarkElf ? "master_tobias_q10360_01.htm" : "master_tobias_q10360_03.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "master_tobias_q10360_02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "master_tobias_q10360_07.htm";
					}
					if(st.getCond() > 7)
					{
						if(player.getRace() == Race.DarkElf)
						{
							if(player.getClassId() == ClassId.palusKnight)
							{
								return "master_tobias_q10360_09.htm";
							}
							else if(player.getClassId() == ClassId.assassin)
							{
								return "master_tobias_q10360_10.htm";
							}
							else if(player.getClassId() == ClassId.darkWizard)
							{
								return "master_tobias_q10360_11.htm";
							}
							else if(player.getClassId() == ClassId.shillienOracle)
							{
								return "master_tobias_q10360_12.htm";
							}
						}
						else
						{
							return "master_tobias_q10360_08.htm";
						}
					}
					break;
				case COMPLETED:
					return "master_tobias_q10360_04.htm";
			}
		}
		else if(npc.getNpcId() == RENFAD)
		{
			if(st.getCond() == 1)
			{
				return "gludio_renfad_q10360_01.htm";
			}
			else if(st.getCond() == 2)
			{
				return "gludio_renfad_q10360_04.htm";
			}
		}
		else if(npc.getNpcId() == JOEL)
		{
			if(st.getCond() == 2)
			{
				return "gludio_joel_q10360_05.htm";
			}
			else if(st.getCond() == 3)
			{
				return "gludio_joel_q10360_01.htm";
			}
			else if(st.getCond() == 4)
			{
				return "gludio_joel_q10360_04.htm";
			}
		}
		else if(npc.getNpcId() == SCHUAZEN)
		{
			if(st.getCond() == 4)
			{
				return "gludio_shuazen_q10360_05.htm";
			}
			else if(st.getCond() == 5)
			{
				return "gludio_shuazen_q10360_01.htm";
			}
			else if(st.getCond() == 6)
			{
				return "gludio_shuazen_q10360_04.htm";
			}
		}
		else if(npc.getNpcId() == SELON)
		{
			if(st.getCond() == 6)
			{
				return "gludio_selon_q10360_05.htm";
			}
			else if(st.getCond() == 7)
			{
				return "gludio_selon_q10360_01.htm";
			}
			else if(st.getCond() > 7)
			{
				switch(player.getRace())
				{
					case Human:
						return player.isMageClass() ? "gludio_selon_q10360_07.htm" : "gludio_selon_q10360_06.htm";
					case Elf:
						return player.isMageClass() ? "gludio_selon_q10360_09.htm" : "gludio_selon_q10360_08.htm";
					case DarkElf:
						return "gludio_selon_q10360_10.htm";
					case Orc:
						return "gludio_selon_q10360_11.htm";
					case Dwarf:
						return "gludio_selon_q10360_12.htm";
					case Kamael:
						return "gludio_selon_q10360_13.htm";
				}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 38 && player.getClassId().level() == ClassLevel.FIRST.ordinal();

	}
} 