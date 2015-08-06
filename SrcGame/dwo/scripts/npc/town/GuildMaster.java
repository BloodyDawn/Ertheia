package dwo.scripts.npc.town;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.datatables.xml.MultiSellData;
import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.instancemanager.clanhall.ClanHallSiegeManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2ClanMember;
import dwo.gameserver.model.player.formation.clan.SubPledge;
import dwo.gameserver.model.skills.L2SkillLearn;
import dwo.gameserver.model.skills.base.proptypes.AcquireSkillType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MagicSkillLaunched;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.acquire.ExAcquirableSkillListByClass;
import dwo.gameserver.util.Util;
import dwo.scripts.quests._10331_StartOfFate;
import dwo.scripts.quests._10360_CertificationOfFate;

import java.util.List;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 24.10.12
 * Time: 1:36
 */

public class GuildMaster extends Quest
{
	private static final int[] NPCs = {
		30026, 30031, 30037, 30066, 30070, 30109, 30115, 30120, 30129, 30141, 30154, 30174, 30175, 30176, 30187, 30191,
		30195, 30288, 30289, 30290, 30297, 30305, 30358, 30359, 30373, 30462, 30474, 30498, 30499, 30500, 30503, 30504,
		30505, 30508, 30511, 30512, 30513, 30520, 30525, 30565, 30594, 30595, 30676, 30677, 30681, 30685, 30687, 30689,
		30694, 30699, 30704, 30845, 30847, 30849, 30854, 30857, 30862, 30865, 30894, 30897, 30900, 30905, 30910, 30913,
		31269, 31272, 31276, 31279, 31285, 31288, 31314, 31317, 31321, 31324, 31326, 31328, 31331, 31334, 31336, 31755,
		31958, 31961, 31965, 31968, 31974, 31977, 31996, 32092, 32093, 32094, 32095, 32096, 32097, 32098, 32145, 32146,
		32147, 32150, 32153, 32154, 32157, 32158, 32160, 32171, 32191, 32193, 32196, 32199, 32202, 32205, 32206, 32209,
		32210, 32213, 32214, 32217, 32218, 32221, 32222, 32225, 32226, 32229, 32230, 32233, 32234, 33491
		// TODO sub_class_ellenia    33491
	};

	// Обмен сертификатов

	// НПЦ: Первая профы
	private static final int FRANCO = 32153;
	private static final int RIVIAN = 32147;
	private static final int DEVON = 32160;
	private static final int TOOK = 32150;
	private static final int MOKA = 32157;
	private static final int VALFAR = 32146;

	// НПЦ: Вторая профа
	private static final int MENDIO = 30504;
	private static final int RAYMOND = 30289;
	private static final int ELLIASIN = 30155;
	private static final int ESRANDEL = 30158;
	private static final int GERSHWIN = 32196;
	private static final int DRIKUS = 30505;
	private static final int RAINS = 30288;
	private static final int TOBIAS = 30297;

	public GuildMaster()
	{
		addAskId(NPCs, -111);
		addAskId(NPCs, -222);
		addAskId(NPCs, -223);
		addAskId(NPCs, -3);
		addAskId(NPCs, -4);
		addAskId(NPCs, -2);
		addAskId(NPCs, 717);
		addAskId(NPCs, 718);
		addPledgeLevelUpEvent(NPCs);
		addPledgeDismissEvent(NPCs);
		addPledgeReviveEvent(NPCs);
		addAcademyCreateEvent(NPCs);
		addCreateSubPledgeEvent(NPCs);
		addRenameSubPledgeEvent(NPCs);
		addUpdateSubPledgeMasterEvent(NPCs);
		addTransferPledgeMasterEvent(NPCs);
		addUpgradeSubpledgeCountEvent(NPCs);
	}

	public static void main(String[] args)
	{
		new GuildMaster();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		QuestState previous10331 = player.getQuestState(_10331_StartOfFate.class);
		QuestState previous10360 = player.getQuestState(_10360_CertificationOfFate.class);

		switch(ask)
		{
			case -223:
				int clanLevel = player.getClan().getLevel();
				switch(reply)
				{
					case 0: // Вооружить рыцарей
						if(clanLevel <= 8)
						{
							return "pl_need_plv_sub2.htm";
						}
						if(clanLevel == 9)
						{
							return "pl_sub2_upgrade1001.htm";
						}
						if(clanLevel == 10)
						{
							return "pl_sub2_upgrade2001.htm";
						}
						if(clanLevel == 11)
						{
							return "pl_sub2_upgrade2001.htm";
						}
					case 1: //  Увеличить гвардию
						if(clanLevel <= 10)
						{
							return "pl_need_plv_sub1.htm";
						}
						else if(clanLevel == 11)
						{
							return "pl_sub_upgrade201.htm";
						}
				}
			case -111:
				if(player.isClanLeader()) // TODO: || ( myself->HavePledgePower(talker,9) && talker.pledge_id != 0 ) )
				{
					if(reply < 1000)
					{
						if(reply < 100)
						{
							return "pl_err_rename_aca.htm";
						}
						else if(reply / 100 > 2)
						{
							return "pl_err_more_sub.htm";
						}
						else if(player.getClan().getSubPledge(reply) == null)
						{
							return "pl_err_more_sm.htm";
						}
						else if(player.getClan().getSubPledge(reply) != null)
						{
							int currentLeaderId = player.getClan().getSubPledge(reply).getLeaderId();
							String currentLeaderName = currentLeaderId > 0 ? CharNameTable.getInstance().getNameById(currentLeaderId) : "<fstring>1010642</fstring>";
							String content = HtmCache.getInstance().getHtm(player.getLang(), "default/pl_ch_submaster" + reply + ".htm");
							content = content.replace("<?" + reply + "submaster?>", currentLeaderName);
							return content;
						}
					}
					else if(reply < 10000)
					{
						if(reply / 1000 > 4)
						{
							return "pl_err_more_sub2.htm";
						}
						else if(player.getClan().getSubPledge(reply) == null)
						{
							return "pl_err_more_sm2.htm";
						}
						else if(player.getClan().getSubPledge(reply) != null)
						{
							int currentLeaderId = player.getClan().getSubPledge(reply).getLeaderId();
							String currentLeaderName = currentLeaderId > 0 ? CharNameTable.getInstance().getNameById(currentLeaderId) : "<fstring>1010642</fstring>";
							String content = HtmCache.getInstance().getHtm(player.getLang(), "default/pl_ch_submaster" + reply + ".htm");
							content = content.replace("<?" + reply + "submaster?>", currentLeaderName);
							return content;
						}
					}
				}
				else
				{
					return "pl_err_master.htm";
				}
				break;
			case -222:
				if(player.isClanLeader()) // TODO: || ( myself->HavePledgePower(talker,9)
				{
					if(reply == -1) // Изменить название Академии
					{
						if(player.getClan().getSubPledge(L2Clan.SUBUNIT_ACADEMY) != null)
						{
							return "pl_ch_rename_aca.htm";
						}
					}
					else if(reply < 1000)
					{
						if(reply / 100 > 2)
						{
							return "pl_err_more_sub.htm";
						}
						else if(player.getClan().getSubPledge(reply) == null)
						{
							return "pl_err_rename_sub.htm";
						}
						else if(player.getClan().getSubPledge(reply) != null)
						{
							return "pl_ch_rename" + reply + ".htm";
						}
					}
					else if(reply < 10000)
					{
						if(reply / 1000 > 4)
						{
							return "pl_err_more_sub2.htm";
						}
						else if(player.getClan().getSubPledge(reply) == null)
						{
							return "pl_err_rename_sub2.htm";
						}
						else if(player.getClan().getSubPledge(reply) != null)
						{
							return "pl_ch_rename" + reply + ".htm";
						}
					}
				}
				else
				{
					return "pl_err_master.htm";
				}
				break;
			case -4:
				switch(reply)
				{
					// Создать альянс
					case 0:
						return "al005.htm";
				}
			case -2:
				switch(reply)
				{
					// Узнать о 1-й смене профессии
					case 1:
						return "guard_class_change1001.htm";
					// Спросить о 2-й смене профессии
					case 2:
						return "guard_class_change2001.htm";
				}
			case -3:
				switch(reply)
				{
					case 100: // Клан
						return "pl001.htm";
					case 101: // Смена имени клана (DEPRECATED: ПЕРЕИМЕНОВЫВАТЬ КЛАН НЕЛЬЗЯ!)
						if(player.isClanLeader())
						{
							// TODO: myself->ShowChangePledgeNameUI(talker);
						}
						else
						{
							return "pl_err_master.htm";
						}
					case 0: // Создать Новый Клан
						if(player.getLevel() < 10)
						{
							return "pl002.htm";
						}
						else if(player.isClanLeader())
						{
							return "pl003.htm";
						}
						else
						{
							return player.getClan() != null ? "pl004.htm" : "pl005.htm";
						}
					case 1: // Повысить Уровень Клана
						return player.isClanLeader() ? "pl013.htm" : "pl014.htm";
					case 2: // Распустить Клан
						return player.isClanLeader() ? "pl007.htm" : "pl008.htm";
					case 3: // Восстановить Клан
						return player.isClanLeader() ? "pl010.htm" : "pl011.htm";
					case 4: // Получить Умения Клана
						if(player.isClanLeader())
						{
							List<L2SkillLearn> skills = SkillTreesData.getInstance().getAvailablePledgeSkills(player.getClan());
							ExAcquirableSkillListByClass asl = new ExAcquirableSkillListByClass(AcquireSkillType.Pledge);
							int counts = 0;

							for(L2SkillLearn s : skills)
							{
								asl.addSkill(s.getSkillId(), s.getSkillLevel(), s.getSkillLevel(), s.getLevelUpSp(), s.getSocialClass().ordinal());
								counts++;
							}
							if(counts == 0)
							{
								if(player.getClan().getLevel() < 5)
								{
									player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1).addNumber(5));
								}
								else
								{
									player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1).addNumber(player.getClan().getLevel() + 1));
								}
							}
							else
							{
								player.sendPacket(asl);
							}
							return null;
						}
						else
						{
							return "pl017.htm";
						}
					case 5: // Управлять Академией
						return "pl_aca_help.htm";
					case 6: // Управлять Стражей
						return "pl_sub_help.htm";
					case 7: // Создать гвардию
						if(player.getClan() == null)
						{
							return "pl_no_pledgeman.htm";
						}
						if(player.isClanLeader()) // TODO:  myself->HavePledgePower(talker,9) && talker.pledge_id != 0 )
						{
							if(player.getClan().getLevel() > 5)
							{
								if(player.getClan().getSubPledge(L2Clan.SUBUNIT_ROYAL1) != null)
								{
									return player.getClan().getSubPledge(L2Clan.SUBUNIT_ROYAL2) != null ? "pl_err_more_sub.htm" : "pl_create_sub200.htm";
								}
								else
								{
									return "pl_create_sub100.htm";
								}
							}
							else
							{
								return "pl_err_plv.htm";
							}
						}
						else
						{
							return "pl_err_master.htm";
						}
					case 8: // TODO Изменить капитана гвардии
						if(player.getClan() == null)
						{
							return "pl_no_pledgeman.htm";
						}
						else if(player.isClanLeader()) // TODO: ( myself->HavePledgePower(talker,9) && talker.pledge_id != 0 )
						{
							if(player.getClan().getLevel() > 5)
							{
								return player.getClan().getSubPledge(L2Clan.SUBUNIT_ROYAL1) != null ? "pl_submaster.htm" : "pl_err_more_sm.htm";
							}
							else
							{
								return "pl_err_plv.htm";
							}
						}
						else
						{
							return "pl_err_master.htm";
						}
					case 9: // Управлять Рыцарями
						return "pl_sub2_help.htm";
					case 10: // Создать Рыцарей
						if(player.getClan() == null)
						{
							return "pl_no_pledgeman.htm";
						}
						if(player.isClanLeader()) // TODO: || ( myself->HavePledgePower(talker,9) && talker.pledge_id != 0 )
						{
							if(player.getClan().getLevel() > 6)
							{
								if(player.getClan().getSubPledge(L2Clan.SUBUNIT_ROYAL1) != null)
								{
									if(player.getClan().getSubPledge(L2Clan.SUBUNIT_KNIGHT1) != null)
									{
										if(player.getClan().getSubPledge(L2Clan.SUBUNIT_KNIGHT2) != null)
										{
											if(player.getClan().getSubPledge(L2Clan.SUBUNIT_ROYAL2) != null)
											{
												if(player.getClan().getSubPledge(L2Clan.SUBUNIT_KNIGHT3) != null)
												{
													return player.getClan().getSubPledge(L2Clan.SUBUNIT_KNIGHT4) != null ? "pl_err_more_sub2.htm" : "pl_create_sub2002.htm";
												}
												else
												{
													return "pl_create_sub2001.htm";
												}
											}
											else
											{
												return "pl_need_high_lv_sub.htm";
											}
										}
										else
										{
											return "pl_create_sub1002.htm";
										}
									}
									else
									{
										return "pl_create_sub1001.htm";
									}
								}
								else
								{
									return "pl_need_high_lv_sub.htm";
								}
							}
							else
							{
								return "pl_err_plv.htm";
							}
						}
						break;
					case 11: // Поменять Главу Ордена Рыцарей
						if(player.getClan() == null)
						{
							return "pl_no_pledgeman.htm";
						}
						else if(player.isClanLeader()) // TODO: || ( myself->HavePledgePower(talker,9) && talker.pledge_id != 0 )
						{
							if(player.getClan().getLevel() > 6)
							{
								if(player.getClan().getSubPledge(L2Clan.SUBUNIT_ROYAL1) != null)
								{
									return player.getClan().getSubPledge(L2Clan.SUBUNIT_KNIGHT1) != null ? "pl_submaster2.htm" : "pl_err_more_sm2.htm";
								}
								else
								{
									return "pl_need_high_lv_sub.htm";
								}
							}
							else
							{
								return "pl_err_plv.htm";
							}
						}
						else
						{
							return "pl_err_master.htm";
						}
					case 12: // Создать академию
						if(player.isClanLeader())
						{
							return player.getClan().getLevel() > 4 ? "pl_create_aca.htm" : "pl_err_plv.htm";
						}
						else
						{
							return "pl_err_master.htm";
						}
					case 13: // Передать Полномочия Лидера Клана
						return "pl_master.htm";
					case 14: // Отправить запрос на передачу полномочий главы клана
						return player.isClanLeader() ? "pl_transfer_master.htm" : "pl_err_master.htm";
					case 15: // Отменить запрос на передачу полномочий главы клана
						return player.isClanLeader() ? "pl_cancel_master.htm" : "pl_err_master.htm";
					case 16: // Изменить название гвардии
						return player.isClanLeader() || player.getPledgeClass() >= 9 && player.getClan() != null ? "pl_rename.htm" : "pl_err_master.htm";
					case 17: // Поменять название Ордена Рыцарей
						return player.isClanLeader() || player.getPledgeClass() >= 9 && player.getClan() != null ? "pl_rename2.htm" : "pl_err_master.htm";
				}
				break;
			case 717:
				if(previous10331 != null && previous10331.isCompleted())
				{
					switch(npc.getNpcId())
					{
						case FRANCO:
							if(player.getRace() == Race.Human)
							{
								MultiSellData.getInstance().separateAndSend(reply, player, npc);
								return null;
							}
							else
							{
								return "highpriest_prana002.html";
							}
						case RIVIAN:
							if(player.getRace() == Race.Elf)
							{
								MultiSellData.getInstance().separateAndSend(reply, player, npc);
								return null;
							}
							else
							{
								return "grandmaster_rivian002.html";
							}
						case DEVON:
							if(player.getRace() == Race.DarkElf)
							{
								MultiSellData.getInstance().separateAndSend(reply, player, npc);
								return null;
							}
							else
							{
								return "grandmagister_devon002.html";
							}
						case TOOK:
							if(player.getRace() == Race.Orc)
							{
								MultiSellData.getInstance().separateAndSend(reply, player, npc);
								return null;
							}
							else
							{
								return "high_prefect_toonks002.html";
							}
						case MOKA:
							if(player.getRace() == Race.Dwarf)
							{
								MultiSellData.getInstance().separateAndSend(reply, player, npc);
								return null;
							}
							else
							{
								return "head_blacksmith_mokabred002.html";
							}
						case VALFAR:
							if(player.getRace() == Race.Kamael)
							{
								MultiSellData.getInstance().separateAndSend(reply, player, npc);
								return null;
							}
							else
							{
								return "grandmaster_valpar002.html";
							}
					}
				}
				else
				{
					return npc.getNpcId() == VALFAR ? npc.getServerName() + "002b.htm" : npc.getServerName() + "008a.htm";
				}
				break;
			case 718:
				if(previous10360 != null && previous10360.isCompleted())
				{
					switch(npc.getNpcId())
					{
						case MENDIO:
							if(player.getRace() == Race.Dwarf)
							{
								MultiSellData.getInstance().separateAndSend(reply, player, npc);
								return null;
							}
							else
							{
								return "head_blacksmith_mendio002.htm";
							}
						case RAYMOND:
							if(player.getRace() != Race.Human || !player.isMageClass())
							{
								return "bishop_raimund002.htm";
							}
							else
							{
								MultiSellData.getInstance().separateAndSend(reply, player, npc);
								return null;
							}
						case ELLIASIN:
							if(player.getRace() != Race.Elf || player.isMageClass())
							{
								return "elliasin003.htm";
							}
							else
							{
								MultiSellData.getInstance().separateAndSend(reply, player, npc);
								return null;
							}
						case ESRANDEL:
							if(player.getRace() != Race.Elf || !player.isMageClass())
							{
								return "eso003.htm";
							}
							else
							{
								MultiSellData.getInstance().separateAndSend(reply, player, npc);
								return null;
							}
						case GERSHWIN:
							if(player.getRace() == Race.Kamael)
							{
								MultiSellData.getInstance().separateAndSend(reply, player, npc);
								return null;
							}
							else
							{
								return "grandmaster_gershuin002.htm";
							}
						case DRIKUS:
							if(player.getRace() == Race.Orc)
							{
								MultiSellData.getInstance().separateAndSend(reply, player, npc);
								return null;
							}
							else
							{
								return "high_prefect_drikus002.htm";
							}
						case RAINS:
							if(player.getRace() != Race.Human || player.isMageClass())
							{
								return "master_rains002.htm";
							}
							else
							{
								MultiSellData.getInstance().separateAndSend(reply, player, npc);
								return null;
							}
						case TOBIAS:
							if(player.getRace() == Race.DarkElf)
							{
								MultiSellData.getInstance().separateAndSend(reply, player, npc);
								return null;
							}
							else
							{
								return "master_tobias002.htm";
							}
					}
				}
				else
				{
					if(npc.getNpcId() == GERSHWIN)
					{
						return npc.getServerName() + "002a.htm";
					}
					else
					{
						return npc.getNpcId() == ESRANDEL || npc.getNpcId() == ELLIASIN ? npc.getServerName() + "004a.htm" : npc.getServerName() + "012a.htm";
					}
				}
				break;
		}
		return null;
	}

	@Override
	public String onPledgeLevelUp(L2Npc npc, L2PcInstance player, int currentPledgeLevel)
	{
		if(player.getClan().levelUpClan(player))
		{
			player.setTarget(player);
			player.broadcastPacket(new MagicSkillUse(player, 5103, 1, 0, 0));
			player.broadcastPacket(new MagicSkillLaunched(player, 5103, 1));
		}
		else
		{
			return "pl016.htm";
		}
		return null;
	}

	@Override
	public String onPledgeDismiss(L2Npc npc, L2PcInstance player)
	{
		player.getClan().dissolveClan(player);
		return "pl009.htm";
	}

	@Override
	public String onPledgeRevive(L2Npc npc, L2PcInstance player)
	{
		L2Clan clan = player.getClan();
		clan.setDissolvingExpiryTime(0);
		clan.updateClanInDB();
		return "pl012.htm";
	}

	@Override
	public String onCreateAcademy(L2Npc npc, L2PcInstance player, int result)
	{
		switch(result)
		{
			case -2:
				return "pl_already_subname.htm";
			case 0:
				return "pl_err_aca.htm";
			case 1:
				return "pl_already_aca.htm";
			case 2:
				return "pl_create_ok_aca.htm";
		}
		return null;
	}

	@Override
	public String onCreateSubPledge(L2Npc npc, L2PcInstance player, String pledgeName, String pledgeMaster, int pledgeType)
	{
		// TODO: Перенести сюда проверку на фейм из clan.createSubPledge();
		L2Clan clan = player.getClan();

		// Общая проверка на валидность названия
		if(!Util.isAlphaNumeric(pledgeName) || !Util.isValidClanName(pledgeName) || pledgeName.length() < 2)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
			return "pl_err_subname.htm";
		}

		// Не разрешаем длинные имена
		if(pledgeName.length() > 16)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_TOO_LONG);
			return "pl_err_subname.htm";
		}

		// Проверяем чтобы у других кланов уже не было такого названия саб-юнита
		for(L2Clan tempClan : ClanTable.getInstance().getClans())
		{
			if(tempClan.getSubPledge(pledgeName) != null)
			{
				player.sendPacket(SystemMessageId.ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME);
				return "pl_already_subname.htm";
			}
		}

		// Проверяем на валидность имя игрока, которого хотим назначить главой саб-юнита
		if(clan.getClanMember(pledgeMaster) == null || clan.getClanMember(pledgeMaster).getPledgeType() != 0)
		{
			if(pledgeType >= L2Clan.SUBUNIT_KNIGHT1)
			{
				player.sendPacket(SystemMessageId.CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED);
				return "pl_err_man.htm";
			}
			else if(pledgeType >= L2Clan.SUBUNIT_ROYAL1)
			{
				player.sendPacket(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED);
				return "pl_err_man.htm";
			}
		}

		int leaderId = clan.getClanMember(pledgeMaster).getObjectId();

		if(!clan.createSubPledge(player, pledgeType, leaderId, pledgeName))
		{
			return null;
		}

		L2ClanMember leaderSubPledge = clan.getClanMember(pledgeMaster);
		L2PcInstance leaderPlayer = leaderSubPledge.getPlayerInstance();
		if(leaderPlayer != null)
		{
			leaderPlayer.setPledgeClass(leaderSubPledge.calculatePledgeClass(leaderPlayer));
			leaderPlayer.sendUserInfo();
		}

		if(pledgeType >= L2Clan.SUBUNIT_KNIGHT1)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_KNIGHTS_OF_S1_HAVE_BEEN_CREATED).addString(player.getClan().getName()));
			return "pl_create_ok_sub2.htm";
		}
		if(pledgeType >= L2Clan.SUBUNIT_ROYAL1)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_ROYAL_GUARD_OF_S1_HAVE_BEEN_CREATED).addString(player.getClan().getName()));
			return "pl_create_ok_sub1.htm";
		}

		return null;
	}

	@Override
	public String onRenameSubPledge(L2Npc npc, L2PcInstance player, String pledgeName, int pledgeType)
	{
		if(!player.isClanLeader())
		{
			return "pl_err_master.htm";
		}
		if(player.getClan().getDissolvingExpiryTime() > 0)
		{
			return "pl_err_rename_disband.htm";
		}

		L2Clan clan = player.getClan();
		SubPledge subPledge = player.getClan().getSubPledge(pledgeType);
		if(subPledge == null)
		{
			if(pledgeType == L2Clan.SUBUNIT_ACADEMY)
			{
				return "pl_err_rename_aca.htm";
			}
			else
			{
				return pledgeType < 1000 ? "pl_err_rename_sub.htm" : "pl_err_rename_sub2.htm";
			}
		}
		if(!Util.isAlphaNumeric(pledgeName) || pledgeName.length() < 2 || !Util.isValidClanName(pledgeName))
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
			return null;
		}
		if(pledgeName.length() > 16)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_TOO_LONG);
			return null;
		}

		for(L2Clan tempClan : ClanTable.getInstance().getClans())
		{
			if(tempClan.getSubPledge(pledgeName) != null)
			{
				player.sendPacket(SystemMessageId.ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME);
				return "pl_already_subname.htm";
			}
		}

		subPledge.setName(pledgeName);
		clan.updateSubPledgeInDB(subPledge.getTypeId());
		clan.broadcastClanStatus();

		if(pledgeType == L2Clan.SUBUNIT_ACADEMY)
		{
			return "pl_rename_ok_aca.htm";
		}
		else
		{
			return pledgeType < 1000 ? "pl_rename_ok_sub1.htm" : "pl_rename_ok_sub2.htm";
		}
	}

	@Override
	public String onUpdateSubPledgeMaster(L2Npc npc, L2PcInstance player, String masterName, int pledgeType)
	{
		if(!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return null;
		}
		if(masterName.length() > 16)
		{
			player.sendPacket(SystemMessageId.NAMING_CHARNAME_UP_TO_16CHARS);
			return null;
		}
		if(player.getName().equals(masterName))
		{
			player.sendPacket(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED);
			return null;
		}

		L2Clan clan = player.getClan();
		SubPledge subPledge = player.getClan().getSubPledge(pledgeType);

		if(subPledge == null || subPledge.getTypeId() == L2Clan.SUBUNIT_ACADEMY)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
			return null;
		}

		if(clan.getClanMember(masterName) == null || clan.getClanMember(masterName).getPledgeType() != 0)
		{
			if(subPledge.getTypeId() >= L2Clan.SUBUNIT_KNIGHT1)
			{
				player.sendPacket(SystemMessageId.CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED);
			}
			else if(subPledge.getTypeId() >= L2Clan.SUBUNIT_ROYAL1)
			{
				player.sendPacket(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED);
			}
			return null;
		}

		subPledge.setLeaderId(clan.getClanMember(masterName).getObjectId());
		clan.updateSubPledgeInDB(subPledge.getTypeId());

		L2ClanMember leaderSubPledge = clan.getClanMember(masterName);
		L2PcInstance leaderPlayer = leaderSubPledge.getPlayerInstance();
		if(leaderPlayer != null)
		{
			leaderPlayer.setPledgeClass(leaderSubPledge.calculatePledgeClass(leaderPlayer));
			leaderPlayer.sendUserInfo();
		}

		clan.broadcastClanStatus();
		clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_BEEN_SELECTED_AS_CAPTAIN_OF_S2).addString(masterName).addString(clan.getSubPledge(pledgeType).getName()));

		return "pl_create_ok_submaster.htm";
	}

	@Override
	public String onTransferPledgeMaster(L2Npc npc, L2PcInstance player, String masterName)
	{
		if(!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return "pl_err_master.htm";
		}
		if(player.getName().equalsIgnoreCase(masterName))
		{
			return "pl_err_master_transfer.htm";
		}
		if(player.isFlying())
		{
			player.sendMessage("Смена лидера не доступна во время полета.");
			return "pl_err_master_transfer.htm";
		}
		L2ClanMember member = player.getClan().getClanMember(masterName);
		if(member == null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DOES_NOT_EXIST).addString(masterName));
			return "pl_err_sm2.htm";
		}
		if(!member.isOnline())
		{
			player.sendPacket(SystemMessageId.INVITED_USER_NOT_ONLINE);
			return "pl_err_master_transfer.htm";
		}
		if(member.getPlayerInstance().isAcademyMember())
		{
			player.sendPacket(SystemMessageId.RIGHT_CANT_TRANSFERRED_TO_ACADEMY_MEMBER);
			return "pl_err_master_transfer.htm";
		}
		if(ClanHallSiegeManager.getInstance().isClanParticipating(player.getClan()))
		{
			return "pl_err_agit_owner.htm";
		}
		player.getClan().setNewLeader(member);
		return "pl_transfer_success.htm";
	}

	@Override
	public String onUpgradeSubpledgeMemberCount(L2Npc npc, L2PcInstance player, int pledgeType)
	{
		/*
		if( reply == 0 )
		{
			return "pl_upgrade_err_sub2.htm";
		}
		else if( reply == 1 )
		{
			return "pl_upgrade_ok_sub2.htm";
		}
		*/
		return "Нет данных о этой функции. Извините за неудобства.";
	}
}