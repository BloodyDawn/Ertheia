package dwo.scripts.npc.castle;

import dwo.gameserver.datatables.xml.MultiSellData;
import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.L2SkillLearn;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.proptypes.AcquireSkillType;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.residence.castle.CastleSide;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.acquire.ExAcquirableSkillListByClass;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 30.09.12
 * Time: 23:46
 */

public class CastleCourtMagician extends Quest
{
	private static final int[] NPCs = {35648, 35649, 35650, 35651, 35652, 35653, 35654, 35655, 35656};

	// Плащи
	private static final int CloakLight = 34925;
	private static final int CloakDark = 34926;
	private static final int CloakLightLeader = 34996;
	private static final int CloakDarkLeader = 34997;

	private static final String fnNoMoreCloaks = "chamberlain_saius071.htm";
	private static final String fnGiveCloak = "chamberlain_saius072.htm";
	private static final String fnHi2 = "fortress_wizard002.htm";
	private static final String fnHi7 = "fortress_wizard007.htm";
	private static final String fnHi8 = "fortress_wizard008.htm";
	private static final String fnHi9 = "fortress_wizard009.htm";

	public CastleCourtMagician()
	{
		addFirstTalkId(NPCs);
		addTeleportRequestId(NPCs);
		addAskId(NPCs, -301);
		addAskId(NPCs, -401);
	}

	/**
	 * @param clanLeader инстанс клан-лидера
	 * @param player инстанс игрока, запрашивающего телепорт
	 * @return {@code true} если в данный момент телепортация к кланлидеру возможна
	 */
	private static boolean validateGateCondition(L2PcInstance clanLeader, L2PcInstance player)
	{
		if(clanLeader.isAlikeDead())
		{
			return false;
		}
		if(clanLeader.isInStoreMode())
		{
			return false;
		}
		if(clanLeader.isRooted() || clanLeader.isInCombat())
		{
			return false;
		}
		if(clanLeader.getOlympiadController().isParticipating())
		{
			return false;
		}
		if(clanLeader.getObserverController().isObserving())
		{
			return false;
		}
		if(clanLeader.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
		{
			return false;
		}
		if(clanLeader.getInstanceId() > 0)
		{
			return false;
		}
		if(!EventManager.onEscapeUse(player))
		{
			return false;
		}
		return EventManager.onEscapeUse(clanLeader);
	}

	public static void main(String[] args)
	{
		new CastleCourtMagician();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ArrayUtils.contains(NPCs, npc.getNpcId()))
		{
			// Посылаем нафиг чужих
			if(player.getClan() != null && npc.getCastle().getCastleId() != player.getClan().getCastleId())
			{
				return "court_magician002.htm";
			}

			if(ask == -301)
			{
				switch(reply)
				{
					case 1:
						MultiSellData.getInstance().separateAndSend(606, player, npc);
						break;
					case 2:
						MultiSellData.getInstance().separateAndSend(610, player, npc);
						break;
					case 3:
						MultiSellData.getInstance().separateAndSend(609, player, npc);
						break;
					case 4:
						return fnHi2;
					case 5:
						int item;
						int i0 = Rnd.get(100);
						if(i0 < 5)
						{
							int i1 = Rnd.get(100);
							if(i1 < 5)
							{
								item = 9931; // Red Talisman of Meditation
							}
							else if(i1 <= 50)
							{
								item = 9932; // Blue Talisman - Divine Protection
							}
							else
							{
								item = i1 <= 75 ? 10416 : 10417;
							}
						}
						else if(i0 <= 15)
						{
							switch(Rnd.get(5))
							{
								case 1: // Red Talisman of Minimum Clarity
									item = 9917;
									break;
								case 2: // Red Talisman of Maximum Clarity
									item = 9918;
									break;
								case 3: // Red Talisman of Mental Regeneration
									item = 9928;
									break;
								case 4: // Blue Talisman of Protection
									item = 9929;
									break;
								default: // Blue Talisman of Invisibility
									item = 9920;

							}
						}
						else if(i0 <= 30)
						{
							switch(Rnd.get(8))
							{
								case 1: // Blue Talisman of Defense
									item = 9916;
									break;
								case 2: // Blue Talisman of Defense
									item = 9916;
									break;
								case 3: // Blue Talisman of Healing
									item = 9924;
									break;
								case 4: // Red Talisman of Recovery
									item = 9925;
									break;
								case 5: // Blue Talisman of Defense
									item = 9926;
									break;
								case 6: // Blue Talisman of Magic Defense
									item = 9927;
									break;
								case 7: // Red Talisman - Life Force
									item = 10518;
									break;
								default: // Blue Talisman - Greater Healing
									item = 10424;
							}
						}
						else
						{
							switch(Rnd.get(46))
							{
								case 0:
									item = 9914;
									break;
								case 1:
									item = 9915;
									break;
								case 2:
									item = 9920;
									break;
								case 3:
									item = 9920;
									break;
								case 4:
									item = 9921;
									break;
								case 5:
									item = 9922;
									break;
								case 6:
									item = 9933;
									break;
								case 7:
									item = 9934;
									break;
								case 8:
									item = 9935;
									break;
								case 9:
									item = 9936;
									break;
								case 10:
									item = 9937;
									break;
								case 11:
									item = 9938;
									break;
								case 12:
									item = 9939;
									break;
								case 13:
									item = 9940;
									break;
								case 14:
									item = 9941;
									break;
								case 15:
									item = 9942;
									break;
								case 16:
									item = 9943;
									break;
								case 17:
									item = 9944;
									break;
								case 18:
									item = 9945;
									break;
								case 19:
									item = 9946;
									break;
								case 20:
									item = 9947;
									break;
								case 21:
									item = 9948;
									break;
								case 22:
									item = 9949;
									break;
								case 23:
									item = 9950;
									break;
								case 24:
									item = 9965;
									break;
								case 25:
									item = 9952;
									break;
								case 26:
									item = 9953;
									break;
								case 27:
									item = 9954;
									break;
								case 28:
									item = 9955;
									break;
								case 29:
									item = 9956;
									break;
								case 30:
									item = 9957;
									break;
								case 31:
									item = 9958;
									break;
								case 32:
									item = 9959;
									break;
								case 33:
									item = 9960;
									break;
								case 34:
									item = 9961;
									break;
								case 35:
									item = 9962;
									break;
								case 36:
									item = 9963;
									break;
								case 37:
									item = 9964;
									break;
								case 38:
									item = 10418;
									break;
								case 39:
									item = 10420;
									break;
								case 40:
									item = 10519;
									break;
								case 41:
									item = 10422;
									break;
								case 42:
									item = 10423;
									break;
								case 43:
									item = 10419;
									break;
								default:
									item = 10421;
							}
						}
						return player.exchangeItemsById(ProcessType.CASTLE, npc, 9912, 10, item, 1, true) ? "fortress_wizard003.htm" : "fortress_wizard006.htm";
					case 6:
						if(player.isClanLeader())
						{
							List<L2SkillLearn> skills = SkillTreesData.getInstance().getAvailableSubPledgeSkills(player.getClan());
							ExAcquirableSkillListByClass asl = new ExAcquirableSkillListByClass(AcquireSkillType.SubPledge);
							int count = 0;

							for(L2SkillLearn s : skills)
							{
								if(SkillTable.getInstance().getInfo(s.getSkillId(), s.getSkillLevel()) != null)
								{
									asl.addSkill(s.getSkillId(), s.getSkillLevel(), s.getSkillLevel(), s.getLevelUpSp(), 0);
									++count;
								}
							}

							if(count > 0)
							{
								player.sendPacket(asl);
							}
							else
							{
								player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
							}
						}
						else
						{
							return "fortress_wizard004.htm";
						}
						break;
					case 7:
						MultiSellData.getInstance().separateAndSend(627, player, npc);
						break;
					case 10:
						return player.isAwakened() ? fnHi7 : fnHi9;
					case 11: // Выполнить обмен на обычный Талисман
						return fnHi8;
					case 12: // Выполнить обмен на Талисман по классам
						switch(Util.getGeneralIdForAwaken(player.getClassId().getId()))
						{
							case 139:
								if(player.getLevel() >= 85 && player.getLevel() < 90)
								{
									MultiSellData.getInstance().separateAndSend(797, player, npc);
									return null;
								}
								if(player.getLevel() >= 90 && player.getLevel() < 95)
								{
									MultiSellData.getInstance().separateAndSend(805, player, npc);
									return null;
								}
								if(player.getLevel() >= 95 && player.getLevel() < 99)
								{
									MultiSellData.getInstance().separateAndSend(813, player, npc);
									return null;
								}
								if(player.getLevel() >= 99)
								{
									MultiSellData.getInstance().separateAndSend(821, player, npc);
									return null;
								}
							case 140:
								if(player.getLevel() >= 85 && player.getLevel() < 90)
								{
									MultiSellData.getInstance().separateAndSend(798, player, npc);
									return null;
								}
								if(player.getLevel() >= 90 && player.getLevel() < 95)
								{
									MultiSellData.getInstance().separateAndSend(806, player, npc);
									return null;
								}
								if(player.getLevel() >= 95 && player.getLevel() < 99)
								{
									MultiSellData.getInstance().separateAndSend(814, player, npc);
									return null;
								}
								if(player.getLevel() >= 99)
								{
									MultiSellData.getInstance().separateAndSend(822, player, npc);
									return null;
								}
							case 141:
								if(player.getLevel() >= 85 && player.getLevel() < 90)
								{
									MultiSellData.getInstance().separateAndSend(799, player, npc);
									return null;
								}
								if(player.getLevel() >= 90 && player.getLevel() < 95)
								{
									MultiSellData.getInstance().separateAndSend(807, player, npc);
									return null;
								}
								if(player.getLevel() >= 95 && player.getLevel() < 99)
								{
									MultiSellData.getInstance().separateAndSend(815, player, npc);
									return null;
								}
								if(player.getLevel() >= 99)
								{
									MultiSellData.getInstance().separateAndSend(823, player, npc);
									return null;
								}
							case 142:
								if(player.getLevel() >= 85 && player.getLevel() < 90)
								{
									MultiSellData.getInstance().separateAndSend(800, player, npc);
									return null;
								}
								if(player.getLevel() >= 90 && player.getLevel() < 95)
								{
									MultiSellData.getInstance().separateAndSend(808, player, npc);
									return null;
								}
								if(player.getLevel() >= 95 && player.getLevel() < 99)
								{
									MultiSellData.getInstance().separateAndSend(816, player, npc);
									return null;
								}
								if(player.getLevel() >= 99)
								{
									MultiSellData.getInstance().separateAndSend(824, player, npc);
									return null;
								}
							case 143:
								if(player.getLevel() >= 85 && player.getLevel() < 90)
								{
									MultiSellData.getInstance().separateAndSend(801, player, npc);
									return null;
								}
								if(player.getLevel() >= 90 && player.getLevel() < 95)
								{
									MultiSellData.getInstance().separateAndSend(809, player, npc);
									return null;
								}
								if(player.getLevel() >= 95 && player.getLevel() < 99)
								{
									MultiSellData.getInstance().separateAndSend(817, player, npc);
									return null;
								}
								if(player.getLevel() >= 99)
								{
									MultiSellData.getInstance().separateAndSend(825, player, npc);
									return null;
								}
							case 144:
								if(player.getLevel() >= 85 && player.getLevel() < 90)
								{
									MultiSellData.getInstance().separateAndSend(802, player, npc);
									return null;
								}
								if(player.getLevel() >= 90 && player.getLevel() < 95)
								{
									MultiSellData.getInstance().separateAndSend(810, player, npc);
									return null;
								}
								if(player.getLevel() >= 95 && player.getLevel() < 99)
								{
									MultiSellData.getInstance().separateAndSend(818, player, npc);
									return null;
								}
								if(player.getLevel() >= 99)
								{
									MultiSellData.getInstance().separateAndSend(826, player, npc);
									return null;
								}
							case 145:
								if(player.getLevel() >= 85 && player.getLevel() < 90)
								{
									MultiSellData.getInstance().separateAndSend(803, player, npc);
									return null;
								}
								if(player.getLevel() >= 90 && player.getLevel() < 95)
								{
									MultiSellData.getInstance().separateAndSend(811, player, npc);
									return null;
								}
								if(player.getLevel() >= 95 && player.getLevel() < 99)
								{
									MultiSellData.getInstance().separateAndSend(819, player, npc);
									return null;
								}
								if(player.getLevel() >= 99)
								{
									MultiSellData.getInstance().separateAndSend(827, player, npc);
									return null;
								}
							case 146:
								if(player.getLevel() >= 85 && player.getLevel() < 90)
								{
									MultiSellData.getInstance().separateAndSend(804, player, npc);
									return null;
								}
								else if(player.getLevel() >= 90 && player.getLevel() < 95)
								{
									MultiSellData.getInstance().separateAndSend(812, player, npc);
									return null;
								}
								else if(player.getLevel() >= 95 && player.getLevel() < 99)
								{
									MultiSellData.getInstance().separateAndSend(820, player, npc);
									return null;
								}
								else if(player.getLevel() >= 99)
								{
									MultiSellData.getInstance().separateAndSend(828, player, npc);
									return null;
								}
						}
						break;
					case 13: // Активный
						if(player.getLevel() >= 85 && player.getLevel() < 90)
						{
							MultiSellData.getInstance().separateAndSend(793, player, npc);
							return null;
						}
						if(player.getLevel() >= 90 && player.getLevel() < 95)
						{
							MultiSellData.getInstance().separateAndSend(794, player, npc);
							return null;
						}
						if(player.getLevel() >= 95 && player.getLevel() < 99)
						{
							MultiSellData.getInstance().separateAndSend(795, player, npc);
							return null;
						}
						if(player.getLevel() >= 99)
						{
							MultiSellData.getInstance().separateAndSend(796, player, npc);
							return null;
						}
						break;
					case 14: // Пассивный
						if(player.getLevel() >= 85 && player.getLevel() < 90)
						{
							MultiSellData.getInstance().separateAndSend(789, player, npc);
							return null;
						}
						if(player.getLevel() >= 90 && player.getLevel() < 95)
						{
							MultiSellData.getInstance().separateAndSend(790, player, npc);
							return null;
						}
						if(player.getLevel() >= 95 && player.getLevel() < 99)
						{
							MultiSellData.getInstance().separateAndSend(791, player, npc);
							return null;
						}
						if(player.getLevel() >= 99)
						{
							MultiSellData.getInstance().separateAndSend(792, player, npc);
							return null;
						}
						break;
				}
			}
			else if(ask == -401)
			{
				if(reply == 1)
				{
					if(npc.isMyLord(player, true))
					{
						int itemId = npc.getCastle().getCastleSide() == CastleSide.LIGHT ? CloakLightLeader : CloakDarkLeader;
						if(player.getItemsCount(itemId) > 0)
						{
							return fnNoMoreCloaks;
						}
						else
						{
							player.addItem(ProcessType.CASTLE, itemId, 1, npc, true);
							return fnGiveCloak;
						}
					}
					else if(npc.isMyLord(player, false))
					{
						int itemId = npc.getCastle().getCastleSide() == CastleSide.LIGHT ? CloakLight : CloakDark;
						// Плащ может получить только маркиз и выше.
						if(player.getItemsCount(itemId) > 0 || player.getPledgeClass() < 5)
						{
							return fnNoMoreCloaks;
						}
						else
						{
							player.addItem(ProcessType.CASTLE, itemId, 1, npc, true);
							return fnGiveCloak;
						}
					}
					else
					{
						return null;
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onTeleportRequest(L2Npc npc, L2PcInstance player)
	{
		if(player.getClan() != null)
		{
			L2PcInstance clanLeader = player.getClan().getLeader().getPlayerInstance();
			if(clanLeader == null)
			{
				return null;
			}
			if(clanLeader.getFirstEffect(L2EffectType.CLAN_GATE) != null)
			{
				if(!validateGateCondition(clanLeader, player))
				{
					return null;
				}

				player.teleToLocation(clanLeader.getX(), clanLeader.getY(), clanLeader.getZ(), false);
				return null;
			}
			return "court_magician003.htm";
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(player.getClan() != null)
		{
			if(npc.getCastle().getOwnerId() != player.getClanId())
			{
				return "court_magician002.htm";
			}
		}
		else if(npc.getCastle().getZone().isSiegeActive())
		{
			return "court_magician002.htm";
		}
		return "court_magician001.htm";
	}
}