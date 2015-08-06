package dwo.scripts.quests;

import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestTimer;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

public class _00501_ProofOfClanAlliance extends Quest
{
	// Квестовые персонажи
	private static final int SIR_KRISTOF_RODEMAI = 30756;
	private static final int STATUE_OF_OFFERING = 30757;
	private static final int WITCH_ATHREA = 30758;
	private static final int WITCH_KALIS = 30759;

	// Квестовые предметы
	private static final int HERB_OF_HARIT = 3832;
	private static final int HERB_OF_VANOR = 3833;
	private static final int HERB_OF_OEL_MAHUM = 3834;
	private static final int BLOOD_OF_EVA = 3835;
	private static final int SYMBOL_OF_LOYALTY = 3837;
	private static final int PROOF_OF_ALLIANCE = 3874;
	private static final int VOUCHER_OF_FAITH = 3873;
	private static final int ANTIDOTE_RECIPE = 3872;
	private static final int POTION_OF_RECOVERY = 3889;

	private static final int[] CHESTS = {
		27173, 27174, 27175, 27176, 27177, 27178
	};

	private static final int[] MOBS = {
		20685, 20644, 20576
	};

	public _00501_ProofOfClanAlliance()
	{
		addStartNpc(SIR_KRISTOF_RODEMAI, STATUE_OF_OFFERING, WITCH_ATHREA);
		addTalkId(SIR_KRISTOF_RODEMAI, STATUE_OF_OFFERING, WITCH_KALIS, WITCH_ATHREA);
		addKillId(MOBS);
		addKillId(CHESTS);
		addEventId(HookType.ON_DELETEME);
		addEventId(HookType.ON_DIE);
		questItemIds = new int[]{
			HERB_OF_HARIT, HERB_OF_VANOR, HERB_OF_OEL_MAHUM, BLOOD_OF_EVA, SYMBOL_OF_LOYALTY, VOUCHER_OF_FAITH,
			ANTIDOTE_RECIPE, POTION_OF_RECOVERY
		};
	}

	public static void main(String[] args)
	{
		new _00501_ProofOfClanAlliance();
	}

	private QuestState getLeaderSt(L2PcInstance player)
	{
		QuestState leaderSt = null;

		L2Clan clan = player.getClan();

		if(clan != null)
		{
			L2PcInstance cLeader = clan.getLeader().getPlayerInstance();
			if(cLeader != null)
			{
				leaderSt = cLeader.getQuestState(getClass());
			}
		}
		return leaderSt;
	}

	@Override
	public int getQuestId()
	{
		return 501;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("chest_timer"))
		{
			L2Npc chestOwner = npc.getOwner().getNpcInstance();
			if(chestOwner != null)
			{
				switch(npc.getNpcId())
				{
					case 27173:
						chestOwner.unsetAiVar("chest0");
						break;
					case 27174:
						chestOwner.unsetAiVar("chest1");
						break;
					case 27175:
						chestOwner.unsetAiVar("chest2");
						break;
					case 27176:
						chestOwner.unsetAiVar("chest3");
						break;
					case 27177:
						chestOwner.unsetAiVar("chest4");
						break;
				}
			}
			return null;
		}

		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(event.equals("quest_accept"))
		{
			st.startQuest();
			st.setMemoState(1);
			return "sir_kristof_rodemai_q0501_07.htm";
		}
		if(event.equalsIgnoreCase("poison_timer"))
		{
			st.exitQuest(QuestType.REPEATABLE);
			return null;
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == SIR_KRISTOF_RODEMAI)
		{
			if(reply == 1)
			{
				return "sir_kristof_rodemai_q0501_06.htm";
			}
			else if(reply == 2)
			{
				return "sir_kristof_rodemai_q0501_08.htm";
			}
		}
		else if(npc.getNpcId() == STATUE_OF_OFFERING)
		{
			if(reply == 1)
			{
				if(Rnd.get(10) > 5)
				{
					QuestState leaderSt = getLeaderSt(player);
					int loyalCount = leaderSt.getInt("2501");
					if(leaderSt != null && !st.hasQuestItems(SYMBOL_OF_LOYALTY) && loyalCount < 3)
					{
						st.giveItem(SYMBOL_OF_LOYALTY);
						leaderSt.set("2501", String.valueOf(loyalCount + 1));
					}
					return "statue_of_offering_q0501_04.htm";
				}
				else
				{
					L2Skill castSkill = SkillTable.getInstance().getInfo(267583489);
					npc.setTarget(player);
					npc.doCast(castSkill);
					return "statue_of_offering_q0501_03.htm";
				}
			}
			else if(reply == 2)
			{
				return "statue_of_offering_q0501_05.htm";
			}
		}
		else if(npc.getNpcId() == WITCH_KALIS)
		{
			switch(reply)
			{
				case 1:
					return "witch_kalis_q0501_02.htm";
				case 2:
					st.setMemoState(2);
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "witch_kalis_q0501_03.htm";
				case 3:
					return "witch_kalis_q0501_04.htm";
				case 4:
					st.takeItems(SYMBOL_OF_LOYALTY, 1);
					st.takeItems(SYMBOL_OF_LOYALTY, 1);
					st.takeItems(SYMBOL_OF_LOYALTY, 1);
					st.giveItems(ANTIDOTE_RECIPE, 1);
					L2Skill castSkill = SkillTable.getInstance().getInfo(267517953);
					castSkill.getEffects(npc, player);
					st.setMemoState(3);
					st.setCond(3);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.startQuestTimer("poison_timer", 3600000);
					return "witch_kalis_q0501_07.htm";
			}
		}
		else if(npc.getNpcId() == WITCH_ATHREA)
		{
			QuestState leaderSt = getLeaderSt(player);
			if(leaderSt != null)
			{
				if(reply == 1)
				{
					return "witch_athrea_q0501_02.htm";
				}
				else if(reply == 2)
				{
					if(npc.getAiVar("chest0") == null && npc.getAiVar("chest1") == null && npc.getAiVar("chest2") == null && npc.getAiVar("chest3") == null && npc.getAiVar("chest4") == null)
					{
						leaderSt.setMemoState(4);
						leaderSt.set("flag", String.valueOf(0));
						npc.setAiVar("flag_boss", String.valueOf(0));
						int i1 = 0;
						int i2 = 0;
						int i3 = 0;
						int i4 = 0;
						while(i1 < 16)
						{
							switch(i1)
							{
								case 0:
									i2 = 102273;
									i3 = 103433;
									i4 = -3512;
									break;
								case 1:
									i2 = 102190;
									i3 = 103379;
									i4 = -3524;
									break;
								case 2:
									i2 = 102107;
									i3 = 103325;
									i4 = -3533;
									break;
								case 3:
									i2 = 102024;
									i3 = 103271;
									i4 = -3500;
									break;
								case 4:
									i2 = 102327;
									i3 = 103350;
									i4 = -3511;
									break;
								case 5:
									i2 = 102244;
									i3 = 103296;
									i4 = -3518;
									break;
								case 6:
									i2 = 102161;
									i3 = 103242;
									i4 = -3529;
									break;
								case 7:
									i2 = 102078;
									i3 = 103188;
									i4 = -3500;
									break;
								case 8:
									i2 = 102381;
									i3 = 103267;
									i4 = -3538;
									break;
								case 9:
									i2 = 102298;
									i3 = 103213;
									i4 = -3532;
									break;
								case 10:
									i2 = 102215;
									i3 = 103159;
									i4 = -3520;
									break;
								case 11:
									i2 = 102132;
									i3 = 103105;
									i4 = -3513;
									break;
								case 12:
									i2 = 102435;
									i3 = 103184;
									i4 = -3515;
									break;
								case 13:
									i2 = 102352;
									i3 = 103130;
									i4 = -3522;
									break;
								case 14:
									i2 = 102269;
									i3 = 103076;
									i4 = -3533;
									break;
								case 15:
									i2 = 102186;
									i3 = 103022;
									i4 = -3541;
									break;
							}
							int i0 = Rnd.get(5);
							switch(i0)
							{
								case 0:
									L2Npc chest0 = addSpawn(27173, i2, i3, i4, 0, false, 300000);
									chest0.setOwner(npc);
									npc.setAiVar("chest0", chest0);
									startQuestTimer("chest_timer", 290000, chest0, player, false);
									break;
								case 1:
									L2Npc chest1 = addSpawn(27174, i2, i3, i4, 0, false, 300000);
									chest1.setOwner(npc);
									npc.setAiVar("chest1", chest1);
									startQuestTimer("chest_timer", 290000, chest1, player, false);
									break;
								case 2:
									L2Npc chest2 = addSpawn(27175, i2, i3, i4, 0, false, 300000);
									chest2.setOwner(npc);
									npc.setAiVar("chest2", chest2);
									startQuestTimer("chest_timer", 290000, chest2, player, false);
									break;
								case 3:
									L2Npc chest3 = addSpawn(27176, i2, i3, i4, 0, false, 300000);
									chest3.setOwner(npc);
									npc.setAiVar("chest3", chest3);
									startQuestTimer("chest_timer", 290000, chest3, player, false);
									break;
								case 4:
									L2Npc chest4 = addSpawn(27177, i2, i3, i4, 0, false, 300000);
									chest4.setOwner(npc);
									npc.setAiVar("chest4", chest4);
									startQuestTimer("chest_timer", 290000, chest4, player, false);
									break;
							}
							i1 += 1;
						}
						return "witch_athrea_q0501_03.htm";
					}
					else
					{
						return "witch_athrea_q0501_03a.htm";
					}
				}
				else if(reply == 3)
				{
					return "witch_athrea_q0501_04.htm";
				}
				else if(reply == 4)
				{
					if(player.getAdenaCount() >= 10000)
					{
						if(npc.getAiVar("chest0") == null && npc.getAiVar("chest1") == null && npc.getAiVar("chest2") == null && npc.getAiVar("chest3") == null && npc.getAiVar("chest4") == null)
						{
							st.takeAdena(10000);
						}
						return "witch_athrea_q0501_07.htm";
					}
					else
					{
						return "witch_athrea_q0501_06.htm";
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState leaderSt = getLeaderSt(player);

		if(leaderSt == null || !leaderSt.isStarted())
		{
			return null;
		}

		int npcId = npc.getNpcId();

		if(ArrayUtils.contains(CHESTS, npcId))
		{
			if(leaderSt.getMemoState() == 4)
			{
				L2Npc chestOwner = npc.getOwner().getNpcInstance();

				if(chestOwner != null)
				{
					switch(npc.getNpcId())
					{
						case 27173:
							chestOwner.unsetAiVar("chest0");
							break;
						case 27174:
							chestOwner.unsetAiVar("chest1");
							break;
						case 27175:
							chestOwner.unsetAiVar("chest2");
							break;
						case 27176:
							chestOwner.unsetAiVar("chest3");
							break;
						case 27177:
							chestOwner.unsetAiVar("chest4");
							break;
					}

					int leaderFlag = leaderSt.getInt("flag");
					int bossFlag = chestOwner.getAiVarInt("flag_boss");
					if(leaderFlag == 3 && bossFlag == 15 || leaderFlag == 2 && bossFlag == 14 || leaderFlag == 1 && bossFlag == 13 || leaderFlag == 0 && bossFlag == 12)
					{
						leaderSt.set("flag", String.valueOf(leaderFlag + 1));
						npc.broadcastPacket(new NS(npc, ChatType.NPC_ALL, NpcStringId.getNpcStringId(50110)));
					}
					else if(leaderFlag < 4)
					{
						if(Rnd.get(4) == 0)
						{
							leaderSt.set("flag", String.valueOf(leaderFlag + 1));
							npc.broadcastPacket(new NS(npc, ChatType.NPC_ALL, NpcStringId.getNpcStringId(50110)));
						}
					}
					chestOwner.setAiVar("flag_boss", String.valueOf(bossFlag + 1));
				}
			}
		}
		else if(ArrayUtils.contains(MOBS, npcId))
		{
			if(leaderSt.getMemoState() >= 3 && leaderSt.getMemoState() < 6)
			{
				QuestState st = player.getQuestState(getClass());
				if(st == null)
				{
					st = newQuestState(player);
				}

				if(Rnd.getChance(10))
				{
					switch(npcId)
					{
						case 20685:
							st.giveItem(HERB_OF_VANOR);
							break;
						case 20644:
							st.giveItem(HERB_OF_HARIT);
							break;
						case 20576:
							st.giveItem(HERB_OF_OEL_MAHUM);
							break;
					}
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == SIR_KRISTOF_RODEMAI)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.isClanLeader())
					{
						L2Clan clan = player.getClan();
						if(clan != null)
						{
							if(clan.getLevel() < 3)
							{
								return "sir_kristof_rodemai_q0501_01.htm";
							}
							else if(clan.getLevel() == 3)
							{
								return player.getItemsCount(PROOF_OF_ALLIANCE) == 1 ? "sir_kristof_rodemai_q0501_03.htm" : "sir_kristof_rodemai_q0501_04.htm";
							}
							else
							{
								return "sir_kristof_rodemai_q0501_02.htm";
							}
						}
					}
					else
					{
						return "sir_kristof_rodemai_q0501_05.htm";
					}
				case STARTED:
					if(st.getMemoState() == 6 && st.hasQuestItems(VOUCHER_OF_FAITH))
					{
						st.takeItems(VOUCHER_OF_FAITH, -1);
						st.giveItem(PROOF_OF_ALLIANCE);
						player.addExpAndSp(0, 12000);
						st.exitQuest(QuestType.REPEATABLE);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						return "sir_kristof_rodemai_q0501_09.htm";
					}
					else if(st.getMemoState() >= 1 && !st.hasQuestItems(VOUCHER_OF_FAITH))
					{
						return "sir_kristof_rodemai_q0501_10.htm";
					}
			}
		}
		else if(npc.getNpcId() == STATUE_OF_OFFERING)
		{
			QuestState leaderSt = getLeaderSt(player);
			if(leaderSt != null && leaderSt.isStarted() && leaderSt.getMemoState() == 2)
			{
				if(player.isClanLeader())
				{
					return "statue_of_offering_q0501_01a.htm";
				}
				else
				{
					if(player.getLevel() >= 40)
					{
						return leaderSt.getInt("2501") < 3 ? "statue_of_offering_q0501_01.htm" : "statue_of_offering_q0501_01b.htm";
					}
					else
					{
						return "statue_of_offering_q0501_02.htm";
					}
				}
			}
			else
			{
				return "statue_of_offering_q0501_06.htm";
			}
		}
		else if(npc.getNpcId() == WITCH_KALIS)
		{
			if(st.isStarted())
			{
				if(st.getMemoState() == 1 && !st.hasQuestItems(SYMBOL_OF_LOYALTY))
				{
					return "witch_kalis_q0501_01.htm";
				}
				else if(st.getMemoState() == 2 && player.getItemsCount(SYMBOL_OF_LOYALTY) < 3)
				{
					return "witch_kalis_q0501_05.htm";
				}
				else if(player.getItemsCount(SYMBOL_OF_LOYALTY) >= 3 && player.getFirstEffect(4082) == null)
				{
					return "witch_kalis_q0501_06.htm";
				}
				else if(st.getMemoState() == 5 && player.getItemsCount(3835) == 1 && player.getItemsCount(3833) >= 1 && player.getItemsCount(3832) >= 1 && player.getItemsCount(3834) >= 1 && player.getFirstEffect(4082) != null)
				{
					st.giveItems(VOUCHER_OF_FAITH, 1);
					st.giveItem(POTION_OF_RECOVERY);
					st.takeItems(3835, -1);
					st.takeItems(ANTIDOTE_RECIPE, -1);
					st.takeItems(3834, -1);
					st.takeItems(3832, -1);
					st.takeItems(3833, -1);
					st.setMemoState(6);
					st.setCond(4);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "witch_kalis_q0501_08.htm";
				}
				else if((st.getMemoState() == 3 || st.getMemoState() == 4 || st.getMemoState() == 5) && player.getFirstEffect(4082) == null)
				{
					st.setMemoState(1);
					st.setCond(1);
					st.unset("2501");
					st.takeItems(ANTIDOTE_RECIPE, -1);
					return "witch_kalis_q0501_09.htm";
				}
				else if(st.getMemoState() < 6 && st.getMemoState() >= 3 && (player.getItemsCount(3835) == 0 || player.getItemsCount(3833) == 0 || player.getItemsCount(3832) == 0 || player.getItemsCount(3834) == 0) && player.getFirstEffect(4082) != null)
				{
					return "witch_kalis_q0501_10.htm";
				}
				else if(st.getMemoState() == 6)
				{
					return "witch_kalis_q0501_11.htm";
				}
			}
			else
			{
				QuestState leaderSt = getLeaderSt(player);
				if((leaderSt == null || leaderSt.isStarted()) && !player.isClanLeader())
				{
					return "witch_kalis_q0501_12.htm";
				}
			}
		}
		else if(npc.getNpcId() == WITCH_ATHREA)
		{
			QuestState leaderSt = getLeaderSt(player);

			if(leaderSt != null)
			{
				if(leaderSt.getMemoState() == 3 && leaderSt.hasQuestItems(ANTIDOTE_RECIPE) && !leaderSt.hasQuestItems(3835))
				{
					leaderSt.set("flag", String.valueOf(0));
					return "witch_athrea_q0501_01.htm";
				}
				else if(leaderSt.getMemoState() == 4)
				{
					if(leaderSt.getInt("flag") < 4)
					{
						return "witch_athrea_q0501_05.htm";
					}
					else
					{
						st.giveItem(BLOOD_OF_EVA);
						leaderSt.setMemoState(5);
						return "witch_athrea_q0501_08.htm";
					}
				}
				else if(leaderSt.getMemoState() == 5)
				{
					return "witch_athrea_q0501_09.htm";
				}
			}
		}
		return null;
	}

	@Override
	public void onDie(L2PcInstance player, L2Character killer)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null && st.isStarted() && st.getCond() == 3)
		{
			QuestTimer poisonTimer = st.getQuestTimer("poison_timer");
			if(poisonTimer != null)
			{
				poisonTimer.cancelAndRemove();
			}
			st.setMemoState(1);
			st.setCond(1);
			st.unset("2501");
			st.takeItems(ANTIDOTE_RECIPE, -1);
		}
	}

	@Override
	public void onDeleteMe(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null && st.isStarted() && st.getCond() == 3)
		{
			st.setMemoState(1);
			st.setCond(1);
			st.unset("2501");
			st.takeItems(ANTIDOTE_RECIPE, -1);
		}
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.isClanLeader() && player.getClan().getLevel() == 3;
	}
}