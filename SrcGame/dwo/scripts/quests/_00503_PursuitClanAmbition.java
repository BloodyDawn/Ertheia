package dwo.scripts.quests;

import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2ClanMember;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;

import java.sql.ResultSet;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 21.06.13
 * Time: 0:19
 */

public class _00503_PursuitClanAmbition extends Quest
{
	// Квестовые персонажи
	private static final int MARTIEN = 30645;
	private static final int ATHREA = 30758;
	private static final int KALIS = 30759;
	private static final int GUSTAF = 30760;
	private static final int FRITZ = 30761;
	private static final int LUTZ = 30762;
	private static final int KURTZ = 30763;
	private static final int KUSTO = 30512;
	private static final int BALTHAZAR = 30764;
	private static final int RODEMAI = 30868;
	private static final int COFFER = 30765;
	private static final int CLEO = 30766;

	// Квестовые монстры
	private static final int THUNDER_WYRM_HOLD = 20282;
	private static final int THUNDER_WYRM = 20243;
	private static final int DRAKE = 20137;
	private static final int DRAKE_HOLD = 20285;
	private static final int BLITZ_WYRM = 27178;
	private static final int SPITE_SOUL_LEADER = 20974;
	private static final int GRAVE_GUARD = 20668;
	private static final int GRAVE_KEYMASTER = 27179;
	private static final int IMPERIAL_GRAVEKEEPER = 27181;

	// Квестовые предметы
	// Первая часть
	private static final short G_LET_MARTIEN = 3866;
	private static final short TH_WYRM_EGGS = 3842;
	private static final short DRAKE_EGGS = 3841;
	private static final short BL_WYRM_EGGS = 3840;
	private static final short MI_DRAKE_EGGS = 3839;
	private static final short BROOCH = 3843;
	private static final short BL_ANVIL_COIN = 3871;

	// Вторая часть
	private static final short G_LET_BALTHAZAR = 3867;
	private static final short RECIPE_SPITEFUL_SOUL_ENERGY = 14854;
	private static final short SPITEFUL_SOUL_ENERGY = 14855;
	private static final short SPITEFUL_SOUL_VENGEANCE = 14856;

	// Третья часть
	private static final short G_LET_RODEMAI = 3868;
	private static final short IMP_KEYS = 3847;
	private static final short SCEPTER_JUDGEMENT = 3869;

	// Пятая часть
	private static final short PROOF_ASPIRATION = 3870;

	public _00503_PursuitClanAmbition()
	{
		addStartNpc(GUSTAF);
		addTalkId(GUSTAF, MARTIEN, ATHREA, KALIS, FRITZ, LUTZ, KURTZ, KUSTO, BALTHAZAR, RODEMAI, COFFER, CLEO);
		addKillId(THUNDER_WYRM_HOLD, THUNDER_WYRM, DRAKE, DRAKE_HOLD, BLITZ_WYRM, SPITE_SOUL_LEADER, GRAVE_GUARD, GRAVE_KEYMASTER, IMPERIAL_GRAVEKEEPER);
		questItemIds = new int[]{
			3839, 3840, 3841, 3842, BROOCH, 3844, 3845, 3846, 3847, 3848, 3866, G_LET_BALTHAZAR, G_LET_RODEMAI,
			SCEPTER_JUDGEMENT, RECIPE_SPITEFUL_SOUL_ENERGY, SPITEFUL_SOUL_ENERGY, SPITEFUL_SOUL_VENGEANCE
		};
	}

	public static void main(String[] args)
	{
		new _00503_PursuitClanAmbition();
	}

	/***
	 * @param player игрок член клана
	 * @param variableName название искомой переменной
	 * @return значение указанной переменной в QuestState клан-лидера
	 */
	private String getClanLeaderVariable(L2PcInstance player, String variableName, boolean onlyIfOnline)
	{
		L2Clan clan = player.getClan();
		if(clan != null)
		{
			L2ClanMember clanLeaderMember = clan.getLeader();
			if(clanLeaderMember != null)
			{
				L2PcInstance clanLeader = clanLeaderMember.getPlayerInstance();
				// Если клан-лидер онлайн
				if(clanLeader != null)
				{
					QuestState st = clanLeader.getQuestState(getClass());
					if(st != null && st.isStarted())
					{
						return st.get(variableName);
					}
				}
				// Если клан-лидер офлайн
				else if(onlyIfOnline)
				{
					ThreadConnection con = null;
					FiltredPreparedStatement offline = null;
					ResultSet rs = null;
					try
					{
						con = L2DatabaseFactory.getInstance().getConnection();
						offline = con.prepareStatement("SELECT `value` FROM `character_quests` WHERE `charId`=? AND `var`=? AND `name`=?");
						offline.setInt(1, clanLeaderMember.getObjectId());
						offline.setString(2, variableName);
						offline.setString(3, getName());
						rs = offline.executeQuery();
						if(rs.next())
						{
							return rs.getString("value");
						}
					}
					catch(Exception e)
					{
						_log.log(Level.ERROR, "Error while getClanLeaderVariable() in quest 503", e);
						return null;
					}
					finally
					{
						DatabaseUtils.closeDatabaseCSR(con, offline, rs);
					}
				}
			}
		}
		return null;
	}

	private void setClanLeaderVariable(L2PcInstance player, String var, String value)
	{
		L2Clan clan = player.getClan();
		if(clan != null)
		{
			L2ClanMember clanLeaderMember = clan.getLeader();
			if(clanLeaderMember != null)
			{
				L2PcInstance clanLeader = clanLeaderMember.getPlayerInstance();
				// Если клан-лидер онлайн
				if(clanLeader != null)
				{
					QuestState st = clanLeader.getQuestState(getClass());
					if(st != null && st.isStarted())
					{
						st.set(var, value);
					}
				}
				// Если клан-лидер офлайн
				else
				{
					ThreadConnection con = null;
					FiltredPreparedStatement offline = null;
					try
					{
						con = L2DatabaseFactory.getInstance().getConnection();
						offline = con.prepareStatement("UPDATE `character_quests` SET `value`=? WHERE `charId`=? AND `var`=? AND `name`=?");
						offline.setString(1, value);
						offline.setInt(2, clanLeaderMember.getObjectId());
						offline.setString(3, var);
						offline.setString(4, getName());
						offline.executeUpdate();
					}
					catch(Exception e)
					{
						_log.log(Level.ERROR, "Error while setClanLeaderVariable() in quest 503", e);
					}
					finally
					{
						DatabaseUtils.closeDatabaseCS(con, offline);
					}
				}
			}
		}
	}

	/***
	 * Старт квеста для всех членов клана
	 * @param clanLeader инстанс клан-лидера
	 */
	private void startQuest(L2PcInstance clanLeader)
	{
		// Онлайн персонажи
		for(L2PcInstance clanMember : clanLeader.getClan().getOnlineMembers(clanLeader.getObjectId()))
		{

		}
	}

	/***
	 * Завершение квеста для всех членов клана
	 * @param clan инстанс клана
	 */
	private void exitQuest(L2Clan clan)
	{
		// Онлайн персонажи
		for(L2PcInstance clanMember : clan.getOnlineMembers(-1))
		{
			QuestState st = clanMember.getQuestState(getClass());
			if(st != null)
			{
				st.exitQuest(QuestType.REPEATABLE);
			}
		}

		// Оффлайн персонажи
		ThreadConnection con = null;
		FiltredPreparedStatement offline = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			offline = con.prepareStatement("DELETE FROM `character_quests` WHERE `name`=? AND `charId` IN (SELECT `charId` FROM `characters` WHERE `clanId`=? AND `online`=0)");
			offline.setString(1, getName());
			offline.setInt(2, clan.getClanId());
			offline.executeUpdate();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error while exitQuest() in quest 503", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, offline);
		}
	}

	@Override
	public int getQuestId()
	{
		return 503;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(event.equals("quest_accept"))
		{
			st.startQuest();
			st.setMemoState(1000);
			st.giveItem(G_LET_MARTIEN);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ACCEPT);
			return "sir_gustaf_athebaldt_q0503_08.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == GUSTAF)
		{
			switch(reply)
			{
				case 1:
					return "sir_gustaf_athebaldt_q0503_05.htm";
				case 2:
					return "sir_gustaf_athebaldt_q0503_06.htm";
				case 3:
					return "sir_gustaf_athebaldt_q0503_07.htm";
				case 4:
					st.giveItem(G_LET_BALTHAZAR);
					st.setMemoState(4000);
					st.setCond(4);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "sir_gustaf_athebaldt_q0503_12.htm";
				case 5:
					st.setMemoState(7000);
					st.giveItem(G_LET_RODEMAI);
					st.setCond(7);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "sir_gustaf_athebaldt_q0503_16.htm";
				case 6:
					if(getClanLeaderVariable(player, "<state>", false).equals("STARTED") && st.hasQuestItems(SCEPTER_JUDGEMENT))
					{
						st.giveItem(PROOF_ASPIRATION);
						st.takeItems(SCEPTER_JUDGEMENT, -1);
						st.addExpAndSp(0, 250000);
						exitQuest(player.getClan());
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						return "sir_gustaf_athebaldt_q0503_20.htm";
					}
					break;
				case 7:
					return "sir_gustaf_athebaldt_q0503_21.htm";
				case 8:
					st.setMemoState(10000);
					st.setCond(12);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "sir_gustaf_athebaldt_q0503_22.htm";
				case 9:
					if(getClanLeaderVariable(player, "<state>", false).equals("STARTED") && st.hasQuestItems(SCEPTER_JUDGEMENT))
					{
						st.giveItem(PROOF_ASPIRATION);
						st.takeItems(SCEPTER_JUDGEMENT, -1);
						st.addExpAndSp(0, 250000);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						exitQuest(player.getClan());
						return "sir_gustaf_athebaldt_q0503_23.htm";
					}
					break;
			}
		}
		else if(npc.getNpcId() == MARTIEN)
		{
			if(reply == 1)
			{
				st.takeItems(3866, -1);
				st.setMemoState(2000);
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "martian_q0503_03.htm";
			}
		}
		else if(npc.getNpcId() == FRITZ)
		{
			if(reply == 1)
			{
				if(st.getMemoState() == 2000 || st.getMemoState() == 2011 || st.getMemoState() == 2010 || st.getMemoState() == 2001)
				{
					st.addSpawn(27178, npc, 10000);
					st.addSpawn(27178, npc, 10000);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					st.giveItems(3840, 3);
					st.setMemoState(st.getMemoState() + 100);
					return "corpse_of_fritz_q0503_02.htm";
				}
				else if(st.getMemoState() == 2100 || st.getMemoState() == 2111 || st.getMemoState() == 2110 || st.getMemoState() == 2101)
				{
					st.addSpawn(27178, npc, 10000);
					st.addSpawn(27178, npc, 10000);
					return "corpse_of_fritz_q0503_03.htm";
				}
			}
		}
		else if(npc.getNpcId() == LUTZ)
		{
			if(reply == 1)
			{
				if(st.getMemoState() == 2000 || st.getMemoState() == 2101 || st.getMemoState() == 2001 || st.getMemoState() == 2100)
				{
					st.giveItems(3840, 3);
					st.giveItems(3839, 4);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					st.addSpawn(27178, npc, 10000);
					st.addSpawn(27178, npc, 10000);
					return "corpse_of_lutz_q0503_02.htm";
				}
				else if(st.getMemoState() == 2000 || st.getMemoState() == 2111 || st.getMemoState() == 2011 || st.getMemoState() == 2110)
				{
					st.addSpawn(27178, npc, 10000);
					st.addSpawn(27178, npc, 10000);
					return "corpse_of_lutz_q0503_03.htm";
				}
			}
		}
		else if(npc.getNpcId() == KURTZ)
		{
			if(reply == 1)
			{
				if(st.getMemoState() == 2000 || st.getMemoState() == 2110 || st.getMemoState() == 2010 || st.getMemoState() == 2100)
				{
					st.giveItem(BROOCH);
					st.giveItems(3839, 6);
					st.setMemoState(st.getMemoState() + 1);
					return "corpse_of_kurtz_q0503_02.htm";
				}
			}
		}
		else if(npc.getNpcId() == KUSTO)
		{
			if(reply == 1)
			{
				st.giveItem(BL_ANVIL_COIN);
				st.takeItems(BROOCH, -1);
				return "head_blacksmith_kusto_q0503_03.htm";
			}
		}
		else if(npc.getNpcId() == BALTHAZAR)
		{
			switch(reply)
			{
				case 1:
					st.takeItems(G_LET_BALTHAZAR, -1);
					st.setMemoState(5000);
					st.setCond(5);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "balthazar_q0503_03.htm";
				case 2:
					return "balthazar_q0503_05.htm";
				case 3:
					st.giveItem(14854);
					st.takeItems(G_LET_BALTHAZAR, -1);
					st.takeItems(BL_ANVIL_COIN, -1);
					st.setCond(5);
					st.setMemoState(5000);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "balthazar_q0503_03.htm";
			}
		}
		else if(npc.getNpcId() == RODEMAI)
		{
			switch(reply)
			{
				case 1:
					return "sir_eric_rodemai_q0503_03.htm";
				case 2:
					st.takeItems(G_LET_RODEMAI, -1);
					st.setCond(8);
					st.setMemoState(8000);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "sir_eric_rodemai_q0503_04.htm";
				case 3:
					st.setCond(11);
					st.setMemoState(9000);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "sir_eric_rodemai_q0503_10.htm";
			}
		}
		else if(npc.getNpcId() == COFFER)
		{
			switch(reply)
			{
				case 1:
					return "imperial_coffer_q0503_02.htm";
				case 2:
					st.giveItem(SCEPTER_JUDGEMENT);
					st.takeItems(3847, -1);
					st.setMemoState(8700);
					return "imperial_coffer_q0503_04.htm";
			}
		}
		else if(npc.getNpcId() == CLEO)
		{
			switch(reply)
			{
				case 1:
					return "witch_cleo_q0503_03.htm";
				case 2:
					st.setMemoState(8100);
					npc.broadcastPacket(new NS(npc, ChatType.NPC_ALL, NpcStringId.BLOOD_AND_HONOR));
					st.addSpawn(ATHREA, 160688, 21296, -3714, 60000);
					st.addSpawn(KALIS, 160688, 21296, -3714, 60000);
					st.setCond(9);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "witch_cleo_q0503_04.htm";
				case 3:
					if(st.isStarted() && player.getItemsCount(SCEPTER_JUDGEMENT) >= 1)
					{
						st.giveItem(PROOF_ASPIRATION);
						st.addExpAndSp(1, 250000);
						st.takeItems(SCEPTER_JUDGEMENT, player.getItemsCount(SCEPTER_JUDGEMENT));
						st.exitQuest(QuestType.REPEATABLE);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						return "witch_cleo_q0503_08.htm";
					}
					break;
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String leaderMemoState = getClanLeaderVariable(st.getPlayer(), "memostate", true);
		if(leaderMemoState != null)
		{
			L2PcInstance clanLeader = st.getPlayer().getClan().getLeader().getPlayerInstance();
			QuestState leaderSt = clanLeader.getQuestState(getClass());

			if(leaderSt != null && st.getPlayer().getDistanceSq(clanLeader) <= 1500)
			{
				switch(npcId)
				{
					case THUNDER_WYRM_HOLD:
					case THUNDER_WYRM:
						if(leaderSt.getMemoState() < 3000 && leaderSt.getMemoState() >= 2000 && leaderSt.getQuestItemsCount(TH_WYRM_EGGS) < 10)
						{
							if(Rnd.get(2) == 0)
							{
								leaderSt.giveItem(TH_WYRM_EGGS);
								if(leaderSt.getQuestItemsCount(TH_WYRM_EGGS) >= 10)
								{
									leaderSt.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
								}
								else
								{
									leaderSt.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
								}
							}
						}
						return null;
					case DRAKE:
					case DRAKE_HOLD:
						if(leaderSt.getMemoState() < 3000 && leaderSt.getMemoState() >= 2000)
						{
							if(Rnd.getChance(10))
							{
								if(st.getQuestItemsCount(MI_DRAKE_EGGS) < 10)
								{
									leaderSt.giveItem(MI_DRAKE_EGGS);
									if(st.getQuestItemsCount(MI_DRAKE_EGGS) >= 10)
									{
										leaderSt.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
									}
									else
									{
										leaderSt.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
									}
								}
							}
							if(Rnd.getChance(50))
							{
								if(st.getQuestItemsCount(DRAKE_EGGS) < 10)
								{
									leaderSt.giveItem(DRAKE_EGGS);
									if(st.getQuestItemsCount(DRAKE_EGGS) >= 10)
									{
										leaderSt.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
									}
									else
									{
										leaderSt.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
									}
								}
							}
						}
						return null;
					case BLITZ_WYRM:
						if(leaderSt.getMemoState() < 3000 && leaderSt.getMemoState() >= 2000 && st.getQuestItemsCount(BL_WYRM_EGGS) < 10)
						{
							leaderSt.giveItem(BL_WYRM_EGGS);
							if(st.getQuestItemsCount(BL_WYRM_EGGS) >= 10)
							{
								leaderSt.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							}
							else
							{
								leaderSt.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
							}
						}
						return null;
					case SPITE_SOUL_LEADER:
						if(leaderSt.getMemoState() == 5000)
						{
							int i0 = Rnd.get(100);
							if(i0 < 10)
							{
								if(leaderSt.getQuestItemsCount(SPITEFUL_SOUL_ENERGY) < 10)
								{
									leaderSt.giveItem(SPITEFUL_SOUL_ENERGY);
								}
							}
							else if(i0 < 60)
							{
								leaderSt.giveItem(SPITEFUL_SOUL_VENGEANCE);
							}
						}
						return null;
					case GRAVE_GUARD:
						if(leaderSt.getMemoState() < 8511 && leaderSt.getMemoState() >= 8500)
						{
							leaderSt.setMemoState(leaderSt.getMemoState() + 1);
							if(leaderSt.getMemoState() >= 8505 && Rnd.getChance(50) || leaderSt.getMemoState() >= 8510)
							{
								leaderSt.setMemoState(8500);
								leaderSt.addSpawn(GRAVE_KEYMASTER, 120000);
							}
						}
						return null;
					case GRAVE_KEYMASTER:
						if(leaderSt.getMemoState() >= 8500 && leaderSt.getQuestItemsCount(IMP_KEYS) < 6)
						{
							leaderSt.giveItem(IMP_KEYS);
							if(leaderSt.getQuestItemsCount(IMP_KEYS) >= 6)
							{
								leaderSt.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							}
							else
							{
								leaderSt.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
							}
						}
						return null;
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == GUSTAF)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.isClanLeader())
					{
						L2Clan clan = player.getClan();
						if(clan != null)
						{
							if(clan.getLevel() < 4)
							{
								return "sir_gustaf_athebaldt_q0503_01.htm";
							}
							else if(clan.getLevel() >= 5)
							{
								return "sir_gustaf_athebaldt_q0503_02.htm";
							}
							else if(clan.getLevel() == 4 && st.hasQuestItems(PROOF_ASPIRATION))
							{
								return st.hasQuestItems(PROOF_ASPIRATION) ? "sir_gustaf_athebaldt_q0503_03.htm" : "sir_gustaf_athebaldt_q0503_04.htm";
							}
						}
					}
					else
					{
						return "sir_gustaf_athebaldt_q0503_04t.htm";
					}
				case STARTED:
					String clanLeaderState = getClanLeaderVariable(player, "<state>", false);
					int clanLeaderMemoState = Integer.parseInt(getClanLeaderVariable(player, "memoState", false));
					if("STARTED".equals(clanLeaderState))
					{
						switch(clanLeaderMemoState)
						{
							case 1000:
								return "sir_gustaf_athebaldt_q0503_09.htm";
							case 2000:
								return "sir_gustaf_athebaldt_q0503_10.htm";
							case 3000:
								return player.isClanLeader() ? "sir_gustaf_athebaldt_q0503_11.htm" : "sir_gustaf_athebaldt_q0503_11t.htm";
							case 4000:
								return "sir_gustaf_athebaldt_q0503_13.htm";
							case 5000:
								return "sir_gustaf_athebaldt_q0503_14.htm";
							case 6000:
								return player.isClanLeader() ? "sir_gustaf_athebaldt_q0503_15.htm" : "sir_gustaf_athebaldt_q0503_15t.htm";
							case 7000:
								return "sir_gustaf_athebaldt_q0503_17.htm";
							case 9000:
								if(!player.isClanLeader())
								{
									return "sir_gustaf_athebaldt_q0503_19t.htm";
								}
							case 10000:
								return player.isClanLeader() ? "sir_gustaf_athebaldt_q0503_24.htm" : "sir_gustaf_athebaldt_q0503_24t.htm";
							default:
								if(clanLeaderMemoState >= 8000 && clanLeaderMemoState < 8700)
								{
									return "sir_gustaf_athebaldt_q0503_18.htm";
								}
								else if(clanLeaderMemoState >= 8700 && clanLeaderMemoState < 10000)
								{
									if(player.isClanLeader())
									{
										return "sir_gustaf_athebaldt_q0503_19.htm";
									}
								}
						}
					}
			}
		}
		else if(npc.getNpcId() == MARTIEN)
		{
			// TODO: Возможно может говорить в >= 2000 только КЛ
			String clanLeaderState = getClanLeaderVariable(player, "<state>", false);
			int clanLeaderMemoState = Integer.parseInt(getClanLeaderVariable(player, "memoState", false));

			if("STARTED".equals(clanLeaderState))
			{
				if(clanLeaderMemoState == 1000)
				{
					return !player.isClanLeader() ? "martian_q0503_01.htm" : "martian_q0503_02.htm";
				}
				else if(clanLeaderMemoState < 3000 && clanLeaderMemoState >= 2000)
				{
					if(player.getItemsCount(3839) < 10 || player.getItemsCount(3840) < 10 || player.getItemsCount(3842) < 10 || player.getItemsCount(3841) < 10)
					{
						return "martian_q0503_04.htm";
					}
					else
					{
						st.takeItems(3839, -1);
						st.takeItems(3840, -1);
						st.takeItems(3841, -1);
						st.takeItems(3842, -1);
						st.setCond(3);
						st.setMemoState(3000);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "martian_q0503_05.htm";
					}
				}
				else if(clanLeaderMemoState == 3000)
				{
					return "martian_q0503_07.htm";
				}
				else if(clanLeaderMemoState > 3000)
				{
					return "martian_q0503_08.htm";
				}
			}
		}
		else if(npc.getNpcId() == ATHREA)
		{
			String clanLeaderState = getClanLeaderVariable(player, "<state>", false);
			if("STARTED".equals(clanLeaderState))
			{
				return "witch_athrea_q0503_01.htm";
			}
		}
		else if(npc.getNpcId() == KALIS)
		{
			String clanLeaderState = getClanLeaderVariable(player, "<state>", false);
			if("STARTED".equals(clanLeaderState))
			{
				return "witch_kalis_q0503_01.htm";
			}
		}
		else if(npc.getNpcId() == FRITZ)
		{
			if(st.isStarted())
			{
				if(st.getMemoState() < 3000 && st.getMemoState() >= 2000)
				{
					return "corpse_of_fritz_q0503_01.htm";
				}
			}
		}
		else if(npc.getNpcId() == LUTZ)
		{
			if(st.isStarted())
			{
				if(st.getMemoState() < 3000 && st.getMemoState() >= 2000)
				{
					return "corpse_of_lutz_q0503_01.htm";
				}
			}
		}
		else if(npc.getNpcId() == KURTZ)
		{
			if(st.isStarted())
			{
				if(st.getMemoState() < 3000 && (st.getMemoState() == 2000 || st.getMemoState() == 2110 || st.getMemoState() == 2010 || st.getMemoState() == 2100))
				{
					return "corpse_of_kurtz_q0503_01.htm";
				}
				else if(st.getMemoState() == 2001 || st.getMemoState() == 2111 || st.getMemoState() == 2011 || st.getMemoState() == 2101)
				{
					return "corpse_of_kurtz_q0503_03.htm";
				}
			}
		}
		else if(npc.getNpcId() == KUSTO)
		{
			String clanLeaderState = getClanLeaderVariable(player, "<state>", false);
			if(clanLeaderState != null && clanLeaderState.equals("STARTED") && !player.isClanLeader())
			{
				return "head_blacksmith_kusto_q0503_01a.htm";
			}
			else if(st.isStarted())
			{
				if(!st.hasQuestItems(BROOCH) && !st.hasQuestItems(BL_ANVIL_COIN))
				{
					return "head_blacksmith_kusto_q0503_01.htm";
				}
				else if(st.hasQuestItems(BROOCH))
				{
					return "head_blacksmith_kusto_q0503_02.htm";
				}
				else if(st.hasQuestItems(BL_ANVIL_COIN) && !st.hasQuestItems(BROOCH))
				{
					return "head_blacksmith_kusto_q0503_04.htm";
				}
			}
		}
		else if(npc.getNpcId() == BALTHAZAR)
		{
			if(!player.isClanLeader())
			{
				String clanLeaderState = getClanLeaderVariable(player, "<state>", false);
				int clanLeaderMemoState = Integer.parseInt(getClanLeaderVariable(player, "memoState", false));
				if("STARTED".equals(clanLeaderState))
				{
					if(clanLeaderMemoState == 4000)
					{
						return "balthazar_q0503_01.htm";
					}
				}
			}
			else if(st.isStarted())
			{
				if(st.getMemoState() == 4000)
				{
					return !st.hasQuestItems(BL_ANVIL_COIN) ? "balthazar_q0503_02.htm" : "balthazar_q0503_04.htm";
				}
				else if(st.getMemoState() == 5000)
				{
					if(player.getItemsCount(14855) < 10)
					{
						return "balthazar_q0503_07a.htm";
					}
					else if((player.getItemsCount(3846) < 10 || player.getItemsCount(3844) < 10) && player.getItemsCount(14855) < 1)
					{
						return "balthazar_q0503_07a.htm";
					}
					else if(player.getItemsCount(14855) >= 10)
					{
						st.takeItems(3846, -1);
						st.takeItems(3844, -1);
						st.takeItems(14855, -1);
						st.setCond(6);
						st.setMemoState(6000);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "balthazar_q0503_08a.htm";
					}
					else if(player.getItemsCount(3846) >= 10 && player.getItemsCount(3844) >= 10)
					{
						st.takeItems(3846, -1);
						st.takeItems(3844, -1);
						st.takeItems(14855, -1);
						st.setCond(6);
						st.setMemoState(6000);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "balthazar_q0503_08a.htm";
					}
				}
				else if(st.getMemoState() >= 6000)
				{
					return "balthazar_q0503_09.htm";
				}
			}
		}
		else if(npc.getNpcId() == RODEMAI)
		{
			if(!player.isClanLeader())
			{
				String clanLeaderState = getClanLeaderVariable(player, "<state>", false);
				int clanLeaderMemoState = Integer.parseInt(getClanLeaderVariable(player, "memoState", false));
				if("STARTED".equals(clanLeaderState))
				{
					if(clanLeaderMemoState == 7000)
					{
						return "sir_eric_rodemai_q0503_01.htm";
					}
					else if(clanLeaderMemoState == 8100)
					{
						return "sir_eric_rodemai_q0503_07.htm";
					}
				}
			}
			else if(st.isStarted())
			{
				if(st.getMemoState() == 7000)
				{
					return "sir_eric_rodemai_q0503_02.htm";
				}
				else if(st.getMemoState() == 8000)
				{
					return "sir_eric_rodemai_q0503_05.htm";
				}
				else if(st.getMemoState() == 8100)
				{
					st.setCond(10);
					st.setMemoState(8500);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "sir_eric_rodemai_q0503_06.htm";
				}
				else if(st.getMemoState() < 8511 && st.getMemoState() >= 8500)
				{
					return "sir_eric_rodemai_q0503_08.htm";
				}
				else if(st.getMemoState() == 8700)
				{
					return "sir_eric_rodemai_q0503_09.htm";
				}
				else if(st.getMemoState() >= 9000)
				{
					return "sir_eric_rodemai_q0503_11.htm";
				}
			}
		}
		else if(npc.getNpcId() == COFFER)
		{
			if(!player.isClanLeader())
			{
				String clanLeaderState = getClanLeaderVariable(player, "<state>", false);
				int clanLeaderMemoState = Integer.parseInt(getClanLeaderVariable(player, "memoState", false));
				if("STARTED".equals(clanLeaderState))
				{
					if(clanLeaderMemoState >= 8500 && clanLeaderMemoState < 8700 && player.getItemsCount(3847) >= 6)
					{
						return "imperial_coffer_q0503_01.htm";
					}
				}
			}
			else if(st.isStarted())
			{
				if(st.getMemoState() >= 8500 && st.getMemoState() < 8700 && st.getQuestItemsCount(3847) >= 6)
				{
					return "imperial_coffer_q0503_03.htm";
				}
				else if(st.getMemoState() >= 8700)
				{
					return "imperial_coffer_q0503_05.htm";
				}
			}
		}
		return null;
	}
}