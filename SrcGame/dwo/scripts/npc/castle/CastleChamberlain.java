package dwo.scripts.npc.castle;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.datatables.xml.ResidenceFunctionData;
import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.castle.CastleSide;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.model.world.residence.fort.FortState;
import dwo.gameserver.model.world.residence.function.FunctionType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.util.FStringUtil;
import dwo.gameserver.util.Util;
import org.apache.commons.lang3.ArrayUtils;

import java.text.SimpleDateFormat;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 15.01.13
 * Time: 1:03
 */

public class CastleChamberlain extends Quest
{
	private static final int[] NPCs = {35100, 35142, 35184, 35226, 35274, 35316, 35363, 35509, 35555};

	// Плащи
	private static final int CloakLight = 34925;
	private static final int CloakDark = 34926;
	private static final int CloakLightLeader = 34996;
	private static final int CloakDarkLeader = 34997;

	// Корона правителя
	private static final int Crown = 6841;
	private final int[] buff_1 = {284557313, 284622849, 284688385, 284819457, 284753921};
	// Wind Walk Lv.2, Decrease Weight Lv.3, Shield Lv.3, Mental Shield Lv.4, Might Lv.3
	private final int[] buff_2 = {284557314, 284622851, 284688387, 284819460, 284753923};
	// Wind Walk Lv.2, Decrease Weight Lv.3, Shield Lv.3, Mental Shield Lv.4, Might Lv.3, Horn Melody Lv.1, Drum Melody Lv.1
	private final int[] buff_3 = {284557314, 284622851, 284688387, 284819460, 284753923, 1007550465, 1007616001};
	/*
		 Wind Walk Lv.2, Decrease Weight Lv.3, Shield Lv.3, Mental Shield Lv.4, Might Lv.3, Horn Melody Lv.1, Drum Melody Lv.1
		 Blessed Body Lv.2, Magic Barrier Lv.1, Resist Shock Lv.1, Blessed Soul Lv.2, Pipe Organ Melody Lv.1, Guitar Melody Lv.1
	*/
	private final int[] buff_4 = {
		284557314, 284622851, 284688387, 284819460, 284753923, 1007550465, 1007616001, 284884994, 285016065, 285081601,
		284950530, 1007681537, 1007747073
	};
	/*
		Wind Walk Lv.2, Decrease Weight Lv.3, Shield Lv.3, Mental Shield Lv.4, Might Lv.3, Horn Melody Lv.1, Drum Melody Lv.1
		Blessed Body Lv.2, Magic Barrier Lv.1, Resist Shock Lv.1, Blessed Soul Lv.2, Pipe Organ Melody Lv.1, Guitar Melody Lv.1
		Concentration Lv.2, Berserker Spirit Lv.1, Blessed Shield Lv.2, Guidance Lv.1, Vampiric Rage Lv.1, Harp Melody Lv.1,
		Lute Melody Lv.1, Knight's Harmony Lv.1, Warrior's Harmony Lv.1, Wizard's Harmony Lv.1
	*/
	private final int[] buff_5 = {
		284557314, 284622851, 284688387, 284819460, 284753923, 1007550465, 1007616001, 284884994, 285016065, 285081601,
		284950530, 1007681537, 1007747073, 285147138, 285212673, 285278210, 285605889, 285343745, 1007812609,
		1007878145, 1007943681, 1008009217, 1008074753
	};
	/*
		Wind Walk Lv.2, Decrease Weight Lv.3, Shield Lv.3, Mental Shield Lv.4, Might Lv.3, Horn Melody Lv.2, Drum Melody Lv.2
		Blessed Body Lv.6, Magic Barrier Lv.2, Resist Shock Lv.4, Blessed Soul Lv.6, Pipe Organ Melody Lv.2, Guitar Melody Lv.1
		Concentration Lv.6, Berserker Spirit Lv.2, Blessed Shield Lv.6, Guidance Lv.3, Vampiric Rage Lv.4, Harp Melody Lv.1,
		Lute Melody Lv.1, Knight's Harmony Lv.1, Warrior's Harmony Lv.1, Wizard's Harmony Lv.1
	*/
	private final int[] buff_7 = {
		284557314, 284622851, 284688387, 284819460, 284753923, 1007550466, 1007616002, 284884998, 285016066, 285081604,
		284950534, 1007681538, 1007747073, 285147142, 285212674, 285278214, 285605891, 285343748, 1007812609,
		1007878145, 1007943681, 1008009217, 1008074753
	};
	/*
		Wind Walk Lv.2, Decrease Weight Lv.3, Shield Lv.3, Mental Shield Lv.4, Might Lv.3, Horn Melody Lv.2, Drum Melody Lv.2
		Blessed Body Lv.6, Magic Barrier Lv.2, Resist Shock Lv.4, Blessed Soul Lv.6, Pipe Organ Melody Lv.2, Guitar Melody Lv.2
		Concentration Lv.6, Berserker Spirit Lv.2, Blessed Shield Lv.6, Guidance Lv.3, Vampiric Rage Lv.4, Harp Melody Lv.2,
		Lute Melody Lv.2, Knight's Harmony Lv.1, Warrior's Harmony Lv.1, Wizard's Harmony Lv.1, Acumen Lv.1, Empower Lv.1
		Haste Lv.1,	Focus Lv.1, Death Whisper Lv.1

	*/
	private final int[] buff_8 = {
		284557314, 284622851, 284688387, 284819460, 284753923, 1007550466, 1007616002, 284884998, 285016066, 285081604,
		284950534, 1007681538, 1007747074, 285147142, 285212674, 285278214, 285605891, 285343748, 1007812610,
		1007878146, 1007943681, 1008009217, 1008074753, 285409281, 285474817, 285540353, 285671425, 285736961
	};
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	// Переменные из скриптов
	String fnHi = "chamberlain_saius001.htm";
	String fnViewReport = "chamberlain_saius002.htm";
	String fnViewTaxRate = "chamberlain_saius003.htm";
	String fnSetGate = "chamberlain_saius005.htm";
	String fnSetSiegeTime1 = "chamberlain_saius006.htm";
	String fnViewSiegeTime = "chamberlain_saius007.htm";
	String fnManageWarehouse = "chamberlain_saius008.htm";
	String fnNotMyLord = "chamberlain_saius009.htm";
	String fnAfterOpenGate = "chamberlain_saius011.htm";
	String fnAfterCloseGate = "chamberlain_saius012.htm";
	String fnAlreadySetSiege = "chamberlain_saius013.htm";
	String fnNotRegeistTime = "chamberlain_saius014.htm";
	String fnSetSiegeTime2 = "chamberlain_saius015.htm";
	String fnSetSiegeTime3 = "chamberlain_saius016.htm";
	String fnSetSiegeTime4 = "chamberlain_saius017.htm";
	String fnSetSiegeTime5 = "chamberlain_saius018.htm";
	String fnAfterSetSiegeTime = "chamberlain_saius019.htm";
	String fnSiegeViewReport = "chamberlain_saius020.htm";
	String fnSiegeStoppedFunction = "chamberlain_saius021.htm";
	String fnManorManage = "chamberlain_saius022.htm";
	String fnManageWarehouse2 = "chamberlain_saius023.htm";
	String fnNotEnoughMoney = "chamberlain_saius049.htm";
	String fnBanish = "chamberlain_saius050.htm";
	String fnAfterBanish = "chamberlain_saius051.htm";
	String fnSiegeDefendList = "chamberlain_saius052.htm";
	String fnDoorStrengthen = "chamberlain_saius053.htm";
	String fnDoorLevel = "chamberlain_saius054.htm";
	String fnDoorStrengthenConfirm = "chamberlain_saius055.htm";
	String fnCurrentDoorLevelHigher = "chamberlain_saius056.htm";
	String fnDoorHpLevelUp = "chamberlain_saius057.htm";
	String fnSetSlowZone = "chamberlain_saius058.htm";
	String fnSetDmgLevel = "chamberlain_saius059.htm";
	String fnDmgZoneConfirm = "chamberlain_saius060.htm";
	String fnCurrentDmgzoneLevelHigher = "chamberlain_saius061.htm";
	String fnDmgZoneLevelUp = "chamberlain_saius062.htm";
	String fnNoAuthority = "chamberlain_saius063.htm";
	String fnSellPage = "chamberlain_saius064.htm";
	String fnCastleList = "chamberlain_saius065.htm";
	String fnCrownBefor = "chamberlain_saius066.htm";
	String fnCrownAfter = "chamberlain_saius067.htm";
	String fnNoMoreCloaks = "chamberlain_saius071.htm";
	String fnGiveCloak = "chamberlain_saius072.htm";
	String fnManageVault = "castlemanagevault.htm";
	String fnNotEnoughBalance = "castlenotenoughbalance.htm";
	int DDoorPrice1_1 = 3000000;
	int DDoorPrice1_2 = 4000000;
	int DDoorPrice1_3 = 5000000;
	int DDoorPrice2_1 = 750000;
	int DDoorPrice2_2 = 900000;
	int DDoorPrice2_3 = 1000000;
	int DDoorPrice3_1 = 750000;
	int DDoorPrice3_2 = 900000;
	int DDoorPrice3_3 = 1000000;
	int DDoorPrice4_1 = 750000;
	int DDoorPrice4_2 = 900000;
	int DDoorPrice4_3 = 1000000;
	int WallPrice1_1 = 1600000;
	int WallPrice1_2 = 1800000;
	int WallPrice1_3 = 2000000;
	int dmgzoneprice1_1 = 3000000;
	int dmgzoneprice1_2 = 4000000;
	int dmgzoneprice1_3 = 5000000;
	int dmgzoneprice1_4 = 6000000;
	int fortress_fstr = 1300101;
	int fortress_status = 1300123;
	int dominion_id = 81;
	int decotype_hpregen = 1;
	int decotype_mpregen = 2;
	int decotype_cpregen = 3;
	int decotype_xprestore = 4;
	int decotype_teleport = 5;
	int decotype_broadcast = 6;
	int decotype_curtain = 7;
	int decotype_hanging = 8;
	int decotype_buff = 9;
	int decotype_outerflag = 10;
	int decotype_platform = 11;
	int decotype_item = 12;
	String fnManage = "chamberlain_CastletDecoManage.htm";
	String fnDecoReset = "castleresetdeco.htm";
	String fnAfterSetDeco = "castleaftersetdeco.htm";
	String fnAfterResetDeco = "castleafterresetdeco.htm";
	String fnDecoFunction = "castledecofunction.htm";
	String fnFuncDisabled = "castlefuncdisabled.htm";
	String fnAgitBuff = "castlebuff";
	String fnManageRegen = "chamberlain_CastleDeco_AR01.htm";
	String fnManageEtc = "chamberlain_CastleDeco_AE01.htm";
	String fnDecoAlreadySet = "castledecoalreadyset.htm";
	String fnAfterBuff = "castleafterbuff.htm";
	String fnNotEnoughMP = "castlenotenoughmp.htm";
	String fileSetName = "CastleDeco";
	int fstr_stage_lv = 9;
	int fstr_percent = 10;
	int fortress_dependancy = 1300133;

	public CastleChamberlain()
	{
		addFirstTalkId(NPCs);
		addTeleportRequestId(NPCs);
		addAskId(NPCs, 0);
		addAskId(NPCs, -201);
		addAskId(NPCs, -202);
		addAskId(NPCs, -203);
		addAskId(NPCs, -204);
		addAskId(NPCs, -205);
		addAskId(NPCs, -206);
		addAskId(NPCs, -207);
		addAskId(NPCs, -208);
		addAskId(NPCs, -209);
		addAskId(NPCs, -210);
		addAskId(NPCs, -219);
		addAskId(NPCs, -240);
		addAskId(NPCs, -241);
		addAskId(NPCs, -251);
		addAskId(NPCs, -252);
		addAskId(NPCs, -253);
		addAskId(NPCs, -255);
		addAskId(NPCs, -256);
		addAskId(NPCs, -257);
		addAskId(NPCs, -260);
		addAskId(NPCs, -270);
		addAskId(NPCs, -271);
		addAskId(NPCs, -22208);
	}

	public static void main(String[] args)
	{
		new CastleChamberlain();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		String content;
		if(!npc.isMyLord(player, false))
		{
			return fnNotMyLord;
		}

		if(ask == 0)
		{
			return npc.isMyLord(player, false) ? fnHi : fnNotMyLord;
		}
		if(ask == -201)
		{
			switch(reply)
			{
				case 1: // Получить доклад
					if(npc.isMyLord(player, false))
					{
						if(npc.getCastle().getZone().isSiegeActive())
						{
							return fnSiegeViewReport;
						}
						else if(npc.getCastle() != null)
						{
							L2Clan clan = ClanTable.getInstance().getClan(npc.getCastle().getOwnerId());
							content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnViewReport);
							content = content.replace("<?my_pledge_name?>", clan.getName());
							content = content.replace("<?my_owner_name?>", clan.getLeaderName());
							content = content.replace("<?feud_name?>", FStringUtil.makeFString(1001000 + npc.getCastle().getCastleId()));
							return content;
						}
					}
					else
					{
						return fnNoAuthority;
					}
					break;
				case 3: // Обеспечение безопасности замка
					if((player.getClanPrivileges() & L2Clan.CP_CS_TAXES) == L2Clan.CP_CS_TAXES)
					{
						content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnManageVault);
						content = content.replace("<?tax_income?>", Util.formatAdena(npc.getCastle().getTreasury()));
						content = content.replace("<?tax_income_reserved?>", ""); // TODO: Сколько налога пришло за период (нужно отдельное поле в базе, которое каждый день будет нулится в конце сдачи)
						content = content.replace("<?seed_income?>", ""); // TODO: Сколько семян было продано за период
						return content;
					}
					else
					{
						return fnNoAuthority;
					}
				case 4:  // Обеспечение функционирования замка
					return npc.getCastle().getZone().isSiegeActive() ? fnSiegeStoppedFunction : fnCastleList;
				case 5: // Просмотр информации об осаде
					if((player.getClanPrivileges() & L2Clan.CP_CS_MANAGE_SIEGE) == L2Clan.CP_CS_MANAGE_SIEGE)
					{
						npc.getCastle().getSiege().listRegisteredClans(player);
						return null;
					}
					else
					{
						return fnNoAuthority;
					}
				case 6: // Управление владениями
					return (player.getClanPrivileges() & L2Clan.CP_CS_MANOR_ADMIN) == L2Clan.CP_CS_MANOR_ADMIN ? "manor.htm" : fnNoAuthority;
				case 7: // Товары
					return (player.getClanPrivileges() & L2Clan.CP_CS_USE_FUNCTIONS) == L2Clan.CP_CS_USE_FUNCTIONS ? fnSellPage : fnNoAuthority;
				case 8: // Выгнать посторонних
					if((player.getClanPrivileges() & L2Clan.CP_CS_USE_FUNCTIONS) == L2Clan.CP_CS_USE_FUNCTIONS)
					{
						return npc.getCastle().getZone().isSiegeActive() ? fnSiegeStoppedFunction : fnBanish;
					}
					else
					{
						return fnNoAuthority;
					}
				case 9:  // Открыть/закрыть врата замка
					if((player.getClanPrivileges() & L2Clan.CP_CS_OPEN_DOOR) == L2Clan.CP_CS_OPEN_DOOR)
					{
						return npc.getCastle().getZone().isSiegeActive() ? fnSiegeStoppedFunction : fnSetGate;
					}
					else
					{
						return fnNoAuthority;
					}
				case 10: // Управление функциями осады
					if((player.getClanPrivileges() & L2Clan.CP_CS_MANAGE_SIEGE) == L2Clan.CP_CS_MANAGE_SIEGE)
					{
						boolean contractExists = false;
						for(Fort fort : FortManager.getInstance().getForts())
						{
							if(fort.getCastleId() == npc.getCastle().getCastleId() && fort.getFortState() == FortState.CONTRACTED)
							{
								contractExists = true;
							}
						}
						if(!contractExists)
						{
							return npc.getServerName() + "069.htm";
						}
						return npc.getCastle().getZone().isSiegeActive() ? fnSiegeStoppedFunction : fnSiegeDefendList;
					}
					else
					{
						return fnNoAuthority;
					}
				case 12: // Купить вещи
					if((player.getClanPrivileges() & L2Clan.CP_CS_USE_FUNCTIONS) == L2Clan.CP_CS_USE_FUNCTIONS)
					{
						npc.showBuyList(player, 0);
						return null;
					}
					else
					{
						return fnNoAuthority;
					}
				case 13: // Получить корону
					if(npc.getCastle().getZone().isSiegeActive())
					{
						return fnSiegeStoppedFunction;
					}
					if(npc.isMyLord(player, true))
					{
						if(player.getItemsCount(6841) > 0)
						{
							return fnCrownBefor;
						}
						else if(npc.getCastle() != null)
						{
							player.addItem(ProcessType.CASTLE, 6841, 1, npc, true);
							L2Clan clan = player.getClan();
							content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnCrownAfter);
							content = content.replace("<?my_owner_name?>", clan.getLeaderName());
							content = content.replace("<?feud_name?>", FStringUtil.makeFString(1001000, String.valueOf(npc.getCastle().getCastleId())));
							return content;
						}
					}
					else
					{
						return fnNoAuthority;
					}
				case 16: // Получить плащ
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
				case 151: // Лечение
					if((player.getClanPrivileges() & L2Clan.CP_CS_SET_FUNCTIONS) == L2Clan.CP_CS_SET_FUNCTIONS)
					{
						content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnManageRegen);
						Castle.CastleFunction hpFunc = npc.getCastle().getFunction(FunctionType.HP_REGEN);
						Castle.CastleFunction mpFunc = npc.getCastle().getFunction(FunctionType.MP_REGEN);
						Castle.CastleFunction expFunc = npc.getCastle().getFunction(FunctionType.XP_RESTORE);

						if(hpFunc == null)
						{
							content = content.replace("<?HPDepth?>", "");
							content = content.replace("<?HPCost?>", "");
							content = content.replace("<?HPExpire?>", FStringUtil.makeFString(4));
							content = content.replace("<?HPReset?>", "");
						}
						else
						{
							content = content.replace("<?HPDepth?>", FStringUtil.makeFString(fstr_percent, String.valueOf(hpFunc.getFunctionData().getPercent())));
							content = content.replace("<?HPCost?>", FStringUtil.makeFString(6, String.valueOf(hpFunc.getFunctionData().getFunctionCostData().getCost()), String.valueOf(hpFunc.getFunctionData().getFunctionCostData().getDays())));
							content = content.replace("<?HPExpire?>", FStringUtil.makeFString(5, dateFormat.format(hpFunc.getEndTime())));
							content = content.replace("<?HPReset?>", FStringUtil.makeFString(7, "[", String.valueOf(decotype_hpregen), "]"));
						}
						if(mpFunc == null)
						{
							content = content.replace("<?MPDepth?>", "");
							content = content.replace("<?MPCost?>", "");
							content = content.replace("<?MPExpire?>", FStringUtil.makeFString(4));
							content = content.replace("<?MPReset?>", "");
						}
						else
						{
							content = content.replace("<?MPDepth?>", FStringUtil.makeFString(fstr_percent, String.valueOf(mpFunc.getFunctionData().getPercent())));
							content = content.replace("<?MPCost?>", FStringUtil.makeFString(6, String.valueOf(mpFunc.getFunctionData().getFunctionCostData().getCost()), String.valueOf(mpFunc.getFunctionData().getFunctionCostData().getDays())));
							content = content.replace("<?MPExpire?>", FStringUtil.makeFString(5, dateFormat.format(mpFunc.getEndTime())));
							content = content.replace("<?MPReset?>", FStringUtil.makeFString(7, "[", String.valueOf(decotype_mpregen), "]"));
						}
						if(expFunc == null)
						{
							content = content.replace("<?XPDepth?>", "");
							content = content.replace("<?XPCost?>", "");
							content = content.replace("<?XPExpire?>", FStringUtil.makeFString(4));
							content = content.replace("<?XPReset?>", "");
						}
						else
						{
							content = content.replace("<?XPDepth?>", FStringUtil.makeFString(fstr_percent, String.valueOf(expFunc.getFunctionData().getPercent())));
							content = content.replace("<?XPCost?>", FStringUtil.makeFString(6, String.valueOf(expFunc.getFunctionData().getFunctionCostData().getCost()), String.valueOf(expFunc.getFunctionData().getFunctionCostData().getDays())));
							content = content.replace("<?XPExpire?>", FStringUtil.makeFString(5, dateFormat.format(expFunc.getEndTime())));
							content = content.replace("<?XPReset?>", FStringUtil.makeFString(7, "[", String.valueOf(decotype_xprestore), "]"));
						}

						return content;
					}
					else
					{
						return fnNoAuthority;
					}
				case 152: // Прочее
					if((player.getClanPrivileges() & L2Clan.CP_CS_SET_FUNCTIONS) == L2Clan.CP_CS_SET_FUNCTIONS)
					{
						content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnManageEtc);
						Castle.CastleFunction teleFunc = npc.getCastle().getFunction(FunctionType.TELEPORT);
						Castle.CastleFunction bcFunc = npc.getCastle().getFunction(FunctionType.BROADCAST);
						Castle.CastleFunction itemFunc = npc.getCastle().getFunction(FunctionType.ITEM_CREATE);
						Castle.CastleFunction buffFunc = npc.getCastle().getFunction(FunctionType.BUFF);
						if(teleFunc == null)
						{
							content = content.replace("<?TPDepth?>", "");
							content = content.replace("<?TPCost?>", "");
							content = content.replace("<?TPExpire?>", FStringUtil.makeFString(4));
							content = content.replace("<?TPReset?>", "");
						}
						else
						{
							content = content.replace("<?TPDepth?>", FStringUtil.makeFString(fstr_stage_lv, String.valueOf(teleFunc.getFunctionData().getLevel() - 10)));
							content = content.replace("<?TPCost?>", FStringUtil.makeFString(6, String.valueOf(teleFunc.getFunctionData().getFunctionCostData().getCost()), String.valueOf(teleFunc.getFunctionData().getFunctionCostData().getDays())));
							content = content.replace("<?TPExpire?>", FStringUtil.makeFString(5, dateFormat.format(teleFunc.getEndTime())));
							content = content.replace("<?TPReset?>", FStringUtil.makeFString(7, "[", String.valueOf(decotype_teleport), "]"));
						}
						if(bcFunc == null)
						{
							content = content.replace("<?BCDepth?>", "");
							content = content.replace("<?BCCost?>", "");
							content = content.replace("<?BCExpire?>", FStringUtil.makeFString(4));
							content = content.replace("<?BCReset?>", "");
						}
						else
						{
							content = content.replace("<?BCDepth?>", FStringUtil.makeFString(fstr_stage_lv, String.valueOf(bcFunc.getFunctionData().getLevel())));
							content = content.replace("<?BCCost?>", FStringUtil.makeFString(6, String.valueOf(bcFunc.getFunctionData().getFunctionCostData().getCost()), String.valueOf(bcFunc.getFunctionData().getFunctionCostData().getDays())));
							content = content.replace("<?BCExpire?>", FStringUtil.makeFString(5, dateFormat.format(bcFunc.getEndTime())));
							content = content.replace("<?BCReset?>", FStringUtil.makeFString(7, "[", String.valueOf(decotype_broadcast), "]"));
						}
						if(itemFunc == null)
						{
							content = content.replace("<?ICDepth?>", "");
							content = content.replace("<?ICCost?>", "");
							content = content.replace("<?ICExpire?>", FStringUtil.makeFString(4));
							content = content.replace("<?ICReset?>", "");
						}
						else
						{
							content = content.replace("<?ICDepth?>", FStringUtil.makeFString(fstr_stage_lv, String.valueOf(itemFunc.getFunctionData().getLevel())));
							content = content.replace("<?ICCost?>", FStringUtil.makeFString(6, String.valueOf(itemFunc.getFunctionData().getFunctionCostData().getCost()), String.valueOf(itemFunc.getFunctionData().getFunctionCostData().getDays())));
							content = content.replace("<?ICExpire?>", FStringUtil.makeFString(5, dateFormat.format(itemFunc.getEndTime())));
							content = content.replace("<?ICReset?>", FStringUtil.makeFString(7, "[", String.valueOf(decotype_item), "]"));
						}
						if(buffFunc == null)
						{
							content = content.replace("<?BFDepth?>", "");
							content = content.replace("<?BFCost?>", "");
							content = content.replace("<?BFExpire?>", FStringUtil.makeFString(4));
							content = content.replace("<?BFReset?>", "");
						}
						else
						{
							content = content.replace("<?BFDepth?>", FStringUtil.makeFString(fstr_stage_lv, String.valueOf(buffFunc.getFunctionData().getLevel() - 10)));
							content = content.replace("<?BFCost?>", FStringUtil.makeFString(6, String.valueOf(buffFunc.getFunctionData().getFunctionCostData().getCost()), String.valueOf(buffFunc.getFunctionData().getFunctionCostData().getDays())));
							content = content.replace("<?BFExpire?>", FStringUtil.makeFString(5, dateFormat.format(buffFunc.getEndTime())));
							content = content.replace("<?BFReset?>", FStringUtil.makeFString(7, "[", String.valueOf(decotype_buff), "]"));
						}
						return content;
					}
					else
					{
						return fnNoAuthority;
					}
				case 107:
					if((player.getClanPrivileges() & L2Clan.CP_CS_USE_FUNCTIONS) == L2Clan.CP_CS_USE_FUNCTIONS)
					{
						if(npc.getCastle().getFunction(FunctionType.BUFF) == null)
						{
							return fnFuncDisabled;
						}
						else
						{
							content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnAgitBuff + '_' + (npc.getCastle().getFunctionLevel(FunctionType.BUFF) - 10) + ".htm");
							content = content.replace("<?MPLeft?>", String.valueOf((int) npc.getCurrentMp()));
							return content;
						}
					}
					else
					{
						return fnNoAuthority;
					}
				case 105: // Дополнительные функции
					return (player.getClanPrivileges() & L2Clan.CP_CS_SET_FUNCTIONS) == L2Clan.CP_CS_SET_FUNCTIONS ? fnManage : fnNoAuthority;
				case 103: // Использование функций замка
					if((player.getClanPrivileges() & L2Clan.CP_CS_USE_FUNCTIONS) == L2Clan.CP_CS_USE_FUNCTIONS)
					{
						content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnDecoFunction);
						Castle.CastleFunction hpFunc = npc.getCastle().getFunction(FunctionType.HP_REGEN);
						Castle.CastleFunction mpFunc = npc.getCastle().getFunction(FunctionType.MP_REGEN);
						Castle.CastleFunction expFunc = npc.getCastle().getFunction(FunctionType.XP_RESTORE);
						content = hpFunc == null ? content.replace("<?HPDepth?>", String.valueOf(0)) : content.replace("<?HPDepth?>", String.valueOf(hpFunc.getFunctionData().getPercent()));
						content = mpFunc == null ? content.replace("<?MPDepth?>", String.valueOf(0)) : content.replace("<?MPDepth?>", String.valueOf(mpFunc.getFunctionData().getPercent()));
						content = expFunc == null ? content.replace("<?XPDepth?>", String.valueOf(0)) : content.replace("<?XPDepth?>", String.valueOf(expFunc.getFunctionData().getPercent()));
						return content;
					}
					else
					{
						return fnNoAuthority;
					}
				case 399: // Спросить о текущей обстановке в Крепости
					int[] parentForts = CastleManager.getInstance().getCastleForts(npc.getCastle().getCastleId());
					switch(parentForts.length)
					{
						case 2:
							content = HtmCache.getInstance().getHtm(player.getLang(), "default/chamberlain_neurath070.htm");
							break;
						case 3:
							content = HtmCache.getInstance().getHtm(player.getLang(), "default/chamberlain_alfred070.htm");
							break;
						case 4:
							content = HtmCache.getInstance().getHtm(player.getLang(), "default/chamberlain_saius070.htm");
							break;
						case 5:
							content = HtmCache.getInstance().getHtm(player.getLang(), "default/chamberlain_saul070.htm");
							break;
						default:
							content = HtmCache.getInstance().getHtm(player.getLang(), "default/chamberlain_saul070.htm");
					}
					for(int index = 0; index < parentForts.length; index++)
					{
						int fortId = parentForts[index];
						Fort fort = FortManager.getInstance().getFortById(fortId);
						int fortName = fortress_fstr + fortId - 101;
						int fortStatus = fortress_status + fort.getFortState().ordinal() - 1;
						int isBoundary = fortress_dependancy + fort.getFortType().ordinal();
						content = content.replace("<?name_fortress" + (index + 1) + "?>", FStringUtil.makeFString(fortName));
						content = content.replace("<?status_fortress" + (index + 1) + "?>", FStringUtil.makeFString(fortStatus));
						content = content.replace("<?boundary_fortress" + (index + 1) + "?>", FStringUtil.makeFString(isBoundary));
					}
					return content;
			}
		}
		else if(ask == -203)
		{
			if(npc.getCastle().getZone().isSiegeActive())
			{
				return fnSiegeStoppedFunction;
			}
			else if((player.getClanPrivileges() & L2Clan.CP_CS_OPEN_DOOR) == L2Clan.CP_CS_OPEN_DOOR)
			{
				switch(reply)
				{
					case 1:
						npc.openMyDoors("DDoorName1_1", "DDoorName1_2");
						return fnAfterOpenGate;
					case 101:
						npc.closeMyDoors("DDoorName1_1", "DDoorName1_2");
						return fnAfterCloseGate;
					case 2:
						npc.openMyDoors("DDoorName2_1", "DDoorName2_2");
						return fnAfterOpenGate;
					case 102:
						npc.closeMyDoors("DDoorName2_1", "DDoorName2_2");
						return fnAfterCloseGate;
					case 3:
						npc.openMyDoors("DDoorName3_1", "DDoorName3_2");
						return fnAfterOpenGate;
					case 103:
						npc.closeMyDoors("DDoorName3_1", "DDoorName3_2");
						return fnAfterCloseGate;
					case 4:
						npc.openMyDoors("DDoorName4_1", "DDoorName4_2");
						return fnAfterOpenGate;
					case 104:
						npc.closeMyDoors("DDoorName4_1", "DDoorName4_2");
						return fnAfterCloseGate;
					case 5:
						npc.openMyDoors("DDoorName5_1", "DDoorName5_2");
						return fnAfterOpenGate;
					case 105:
						npc.closeMyDoors("DDoorName5_1", "DDoorName5_2");
						return fnAfterCloseGate;
					case 6:
						npc.openMyDoors("DDoorName6_1", "DDoorName6_2");
						return fnAfterOpenGate;
					case 106:
						npc.closeMyDoors("DDoorName6_1", "DDoorName6_2");
						return fnAfterCloseGate;
					case 11:
						npc.openMyDoors("SDoorName1");
						return fnAfterOpenGate;
					case 111:
						npc.closeMyDoors("SDoorName1");
						return fnAfterCloseGate;
					case 12:
						npc.openMyDoors("SDoorName2");
						return fnAfterOpenGate;
					case 112:
						npc.closeMyDoors("SDoorName2");
						return fnAfterCloseGate;
					case 13:
						npc.openMyDoors("SDoorName3");
						return fnAfterOpenGate;
					case 113:
						npc.closeMyDoors("SDoorName3");
						return fnAfterCloseGate;
					case 14:
						npc.openMyDoors("SDoorName4");
						return fnAfterOpenGate;
					case 114:
						npc.closeMyDoors("SDoorName4");
						return fnAfterCloseGate;
					case 15:
						npc.openMyDoors("SDoorName5");
						return fnAfterOpenGate;
					case 115:
						npc.closeMyDoors("SDoorName5");
						return fnAfterCloseGate;
					case 99: // Открыть все врата замка
						npc.openMyDoors();
						return fnAfterOpenGate;
					case 199: // Закрыть все врата замка
						npc.closeMyDoors();
						return fnAfterCloseGate;
				}
			}
			else
			{
				return fnNoAuthority;
			}
		}
		else if(ask == -204)
		{
			if((player.getClanPrivileges() & L2Clan.CP_CS_MANAGE_SIEGE) == L2Clan.CP_CS_MANAGE_SIEGE)
			{
				switch(reply)
				{
					case 1:
						return fnDoorStrengthen;
					case 2:
						return fnSetSlowZone;
				}
			}
			else
			{
				return fnNoAuthority;
			}
		}
		else if(ask == -205)
		{
			player.getVariablesController().set("gate_num", reply);
			return fnDoorLevel;
		}
		else if(ask == -206)
		{
			int gateLevel = 0;
			switch(reply)
			{
				case 1:
					gateLevel = 200;
					break;
				case 2:
					gateLevel = 300;
					break;
				case 3:
					gateLevel = 500;
					break;
			}
			int i0 = player.getVariablesController().get("gate_num", Integer.class, 0);
			int i1 = gateLevel;
			int i2 = 0;
			if(i0 == 1 && i1 == 200)
			{
				i2 = DDoorPrice1_1;
			}
			if(i0 == 1 && i1 == 300)
			{
				i2 = DDoorPrice1_2;
			}
			if(i0 == 1 && i1 == 500)
			{
				i2 = DDoorPrice1_3;
			}
			if((i0 == 2 || i0 == 3) && i1 == 200)
			{
				i2 = DDoorPrice2_1;
			}
			if((i0 == 2 || i0 == 3) && i1 == 300)
			{
				i2 = DDoorPrice2_2;
			}
			if((i0 == 2 || i0 == 3) && i1 == 500)
			{
				i2 = DDoorPrice2_3;
			}
			if(i0 == 4 && i1 == 200)
			{
				i2 = DDoorPrice4_1;
			}
			if(i0 == 4 && i1 == 300)
			{
				i2 = DDoorPrice4_2;
			}
			if(i0 == 4 && i1 == 500)
			{
				i2 = DDoorPrice4_3;
			}
			if(i0 == 21 && i1 == 200)
			{
				i2 = WallPrice1_1;
			}
			if(i0 == 21 && i1 == 300)
			{
				i2 = WallPrice1_2;
			}
			if(i0 == 21 && i1 == 500)
			{
				i2 = WallPrice1_3;
			}
			if(i0 == 22 && i1 == 200)
			{
				i2 = WallPrice1_1;
			}
			if(i0 == 22 && i1 == 300)
			{
				i2 = WallPrice1_2;
			}
			if(i0 == 22 && i1 == 500)
			{
				i2 = WallPrice1_3;
			}
			player.getVariablesController().set("gate_price", i2);
			content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnDoorStrengthenConfirm);
			content = content.replace("<?gate_price?>", String.valueOf(i2));
			return content;
		}
		else if(ask == -207 && reply == 1)
		{
			if(npc.getCastle().getZone().isSiegeActive())
			{
				return fnSiegeStoppedFunction;
			}
			else if((player.getClanPrivileges() & L2Clan.CP_CS_MANAGE_SIEGE) == L2Clan.CP_CS_MANAGE_SIEGE)
			{
				Integer gate_price = player.getVariablesController().get("gate_price", Integer.class);
				Integer gate_level = player.getVariablesController().get("gate_level", Integer.class);
				Integer gate_num = player.getVariablesController().get("gate_num", Integer.class);

				player.getVariablesController().unset("gate_level"); // TODO : В npcAiVars
				player.getVariablesController().unset("gate_price");
				player.getVariablesController().unset("gate_num");

				if(npc.getCastle().getZone().isSiegeActive())
				{
					return fnSiegeStoppedFunction;
				}
				else
				{
					int level = 0;
					switch(gate_num)
					{
						case 1:
							level = DoorGeoEngine.getInstance().getDoor(npc.getTemplate().getDoorId("DDoorName1_1")).getHpLevel();
							break;
						case 2:
							level = DoorGeoEngine.getInstance().getDoor(npc.getTemplate().getDoorId("DDoorName2_1")).getHpLevel();
							break;
						case 3:
							level = DoorGeoEngine.getInstance().getDoor(npc.getTemplate().getDoorId("DDoorName3_1")).getHpLevel();
							break;
						case 4:
							level = DoorGeoEngine.getInstance().getDoor(npc.getTemplate().getDoorId("DDoorName4_1")).getHpLevel();
							break;
						case 21:
							level = DoorGeoEngine.getInstance().getDoor(npc.getTemplate().getDoorId("WallName1")).getHpLevel();
							break;
						case 22:
							level = DoorGeoEngine.getInstance().getDoor(npc.getTemplate().getDoorId("WallName3")).getHpLevel();
							break;
					}
					if(level >= gate_level)
					{
						content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnCurrentDoorLevelHigher);
						content = content.replace("<?doorlevel?>", String.valueOf(level));
						return content;
					}
					else if(player.getItemsCount(PcInventory.ADENA_ID) >= gate_price)
					{
						player.destroyItemByItemId(ProcessType.CASTLE, PcInventory.ADENA_ID, gate_num, npc, true);
						switch(gate_num)
						{
							case 1:
								npc.getCastle().setDoorHpLevel(npc.getTemplate().getDoorId("DDoorName1_1"), gate_level);
								npc.getCastle().setDoorHpLevel(npc.getTemplate().getDoorId("DDoorName1_2"), gate_level);
								break;
							case 2:
								npc.getCastle().setDoorHpLevel(npc.getTemplate().getDoorId("DDoorName2_1"), gate_level);
								npc.getCastle().setDoorHpLevel(npc.getTemplate().getDoorId("DDoorName2_2"), gate_level);
								break;
							case 3:
								npc.getCastle().setDoorHpLevel(npc.getTemplate().getDoorId("DDoorName3_1"), gate_level);
								npc.getCastle().setDoorHpLevel(npc.getTemplate().getDoorId("DDoorName3_2"), gate_level);
								break;
							case 4:
								npc.getCastle().setDoorHpLevel(npc.getTemplate().getDoorId("DDoorName4_1"), gate_level);
								npc.getCastle().setDoorHpLevel(npc.getTemplate().getDoorId("DDoorName4_2"), gate_level);
								break;
							case 21:
								npc.getCastle().setDoorHpLevel(npc.getTemplate().getDoorId("WallName1"), gate_level);
								npc.getCastle().setDoorHpLevel(npc.getTemplate().getDoorId("WallName2"), gate_level);
								break;
							case 22:
								npc.getCastle().setDoorHpLevel(npc.getTemplate().getDoorId("WallName3"), gate_level);
								break;
						}
						return fnDoorHpLevelUp;
					}
					else
					{
						return fnNotEnoughMoney;
					}
				}
			}
			else
			{
				return fnNoAuthority;
			}
		}
		else if(ask == -208)
		{
			if(reply == 1)
			{
				npc.setAiVar("dmgzone_num", 0);
			}
			else if(reply == 2)
			{
				npc.setAiVar("dmgzone_num", 1);
			}
			return fnSetDmgLevel;
		}
		else if(ask == -209)
		{
			npc.setAiVar("dmgzone_num", reply - 1);
			int i0 = 0;
			switch(reply)
			{
				case 0:
					i0 = dmgzoneprice1_1;
					break;
				case 1:
					i0 = dmgzoneprice1_2;
					break;
				case 2:
					i0 = dmgzoneprice1_3;
					break;
				case 3:
					i0 = dmgzoneprice1_4;
					break;
			}
			i0 = i0 * 100 / 100;
			npc.setAiVar("dmgzone_price", i0);
			content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnDmgZoneConfirm);
			content = content.replace("<?dmgzone_price?>", String.valueOf(i0));
			return content;
		}
		else if(ask == -210 && reply == 1)
		{
			int dmgZoneNum = npc.getAiVarInt("dmgzone_num");
			if(npc.getCastle().getZone().isSiegeActive())
			{
				return fnSiegeStoppedFunction;
			}

			if(npc.isMyLord(player, true) || (player.getClanPrivileges() & L2Clan.CP_CS_SET_FUNCTIONS) == L2Clan.CP_CS_SET_FUNCTIONS)
			{
				int i0 = npc.getAiVarInt("dmgzone_price");
				int i1 = npc.getAiVarInt("dmgzone_level");
				int i2 = npc.getAiVarInt("dmgzone_num");

				npc.unsetAiVar("dmgzone_price");
				npc.unsetAiVar("dmgzone_level");
				npc.unsetAiVar("dmgzone_num");

				int level = npc.getCastle().getTrapUpgradeLevel(dmgZoneNum);
				if(level >= 2 * i1)
				{
					content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnCurrentDmgzoneLevelHigher);
					content = content.replace("<?dmglevel?>", String.valueOf(level / 2));
					return content;
				}
				else if(player.getItemsCount(PcInventory.ADENA_ID) >= i0)
				{
					player.getInventory().destroyItemByItemId(ProcessType.CASTLE, PcInventory.ADENA_ID, i0, player, npc);
					npc.getCastle().setTrapUpgrade(i2, 2 * i1, true);
					return fnDmgZoneLevelUp;
				}
				else
				{
					return fnNotEnoughMoney;
				}
			}
			else
			{
				return fnNoAuthority;
			}
		}
		else if(ask == -219)
		{
			if((player.getClanPrivileges() & L2Clan.CP_CS_USE_FUNCTIONS) == L2Clan.CP_CS_USE_FUNCTIONS)
			{
				return npc.getCastle().getZone().isSiegeActive() ? fnSiegeStoppedFunction : fnAfterBanish;
			}
			else
			{
				return fnNoAuthority;
			}
		}
		else if(ask == -240) // Снять деньги из казны замка
		{
			if((player.getClanPrivileges() & L2Clan.CP_CS_TAXES) == L2Clan.CP_CS_TAXES)
			{
				if(reply == 0)
				{
					return fnHi;
				}
				else if(reply <= npc.getCastle().getTreasury())
				{
					npc.getCastle().addToTreasuryNoTax(-1 * reply);
					player.addAdena(ProcessType.CASTLE, reply, npc, true);
					return fnHi;
				}
				else
				{
					content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnNotEnoughBalance);
					content = content.replace("<?tax_income?>", Util.formatAdena(npc.getCastle().getTreasury()));
					content = content.replace("<?withdraw_amount?>", String.valueOf(reply));
					return content;
				}
			}
			else
			{
				return fnNoAuthority;
			}
		}
		else if(ask == -241) // Добавить деньги в казну замка
		{
			if((player.getClanPrivileges() & L2Clan.CP_CS_TAXES) == L2Clan.CP_CS_TAXES)
			{
				if(player.reduceAdena(ProcessType.CASTLE, reply, npc, true))
				{
					npc.getCastle().addToTreasuryNoTax(reply);
				}
				else
				{
					player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
				}
				return fnHi;
			}
			else
			{
				return fnNoAuthority;
			}
		}
		else if(ask == -270)
		{
			if((player.getClanPrivileges() & L2Clan.CP_CS_SET_FUNCTIONS) == L2Clan.CP_CS_SET_FUNCTIONS)
			{
				int i0 = reply / 1000;
				int i1 = reply - i0 * 1000;
				FunctionType decoType = FunctionType.values()[i0];
				if(decoType == null)
				{
					return fnNoAuthority;
				}

				if(i1 == 0)
				{
					content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnDecoReset);
					content = content.replace("<?AgitDecoSubmit?>", String.valueOf(reply));
					return content;
				}
				if(npc.getCastle().getFunctionLevel(decoType) == i1)
				{
					content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnDecoAlreadySet);
				}
				else
				{
					content = HtmCache.getInstance().getHtm(player.getLang(), "default/ol_mahum_" + fileSetName + "__" + i0 + ".htm");
					content = content.replace("<?AgitDecoCost?>", FStringUtil.makeFString(6, String.valueOf(ResidenceFunctionData.getInstance().getDeco(decoType, i1).getFunctionCostData().getCost()), String.valueOf(ResidenceFunctionData.getInstance().getDeco(decoType, i1).getFunctionCostData().getDays())));
					content = content.replace("<?AgitDecoSubmit?>", String.valueOf(reply));
				}

				switch(decoType)
				{
					case HP_REGEN:
						content = content.replace("<?AgitDecoEffect?>", FStringUtil.makeFString(fstr_percent, String.valueOf(ResidenceFunctionData.getInstance().getDeco(decoType, i1).getPercent())));
						break;
					case MP_REGEN:
						content = content.replace("<?AgitDecoEffect?>", FStringUtil.makeFString(fstr_percent, String.valueOf(ResidenceFunctionData.getInstance().getDeco(decoType, i1).getPercent())));
						break;
					case XP_RESTORE:
						content = content.replace("<?AgitDecoEffect?>", FStringUtil.makeFString(fstr_percent, String.valueOf(ResidenceFunctionData.getInstance().getDeco(decoType, i1).getPercent())));
						break;
					case CURTAIN:
						content = content.replace("<?AgitDecoEffect?>", FStringUtil.makeFString(fstr_percent, String.valueOf(i1 * 25)));
						break;
					default:
						content = content.replace("<?AgitDecoEffect?>", FStringUtil.makeFString(fstr_stage_lv, String.valueOf(i1 - 10)));
						break;
				}
				return content;
			}
			else
			{
				return fnNoAuthority;
			}
		}
		else if(ask == -271)
		{
			if((player.getClanPrivileges() & L2Clan.CP_CS_SET_FUNCTIONS) == L2Clan.CP_CS_SET_FUNCTIONS)
			{
				int i0 = reply / 1000;
				int i1 = reply - i0 * 1000;
				FunctionType decoType = FunctionType.values()[i0];
				if(i1 == 0)
				{
					npc.getCastle().removeFunction(decoType);
					return fnAfterResetDeco;
				}
				else if(player.getAdenaCount() >= ResidenceFunctionData.getInstance().getDeco(decoType, i1).getFunctionCostData().getCost())
				{
					player.reduceAdena(ProcessType.NPC, ResidenceFunctionData.getInstance().getDeco(decoType, i1).getFunctionCostData().getCost(), npc, true);
					npc.getCastle().updateFunctions(decoType, i1);
					return fnAfterSetDeco;
				}
				else
				{
					return fnNotEnoughMoney;
				}
			}
			else
			{
				return fnNoAuthority;
			}
		}
		else if(ask == -22208) // Баф
		{
			Castle.CastleFunction buffFunc = npc.getCastle().getFunction(FunctionType.BUFF);
			if(buffFunc != null)
			{
				L2Skill skill = SkillTable.getInstance().getInfo(reply);
				if(skill != null && skill.getMpConsume() < npc.getCurrentMp())
				{
					boolean check = false;
					switch(buffFunc.getFunctionData().getLevel() - 10)
					{
						case 1:
							if(ArrayUtils.contains(buff_1, reply))
							{
								check = true;
							}
							break;
						case 2:
							if(ArrayUtils.contains(buff_2, reply))
							{
								check = true;
							}
							break;
						case 3:
							if(ArrayUtils.contains(buff_3, reply))
							{
								check = true;
							}
							break;
						case 4:
							if(ArrayUtils.contains(buff_4, reply))
							{
								check = true;
							}
							break;
						case 5:
							if(ArrayUtils.contains(buff_5, reply))
							{
								check = true;
							}
							break;
						case 6:
						case 7:
							if(ArrayUtils.contains(buff_7, reply))
							{
								check = true;
							}
							break;
						case 8:
							if(ArrayUtils.contains(buff_8, reply))
							{
								check = true;
							}
							break;
					}

					if(check)
					{
						npc.setTarget(player);
						npc.doCast(skill);
					}
					content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnAfterBuff);
				}
				else
				{
					content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnNotEnoughMP);
				}
				content = content.replace("<?MPLeft?>", String.valueOf((int) npc.getCurrentMp()));
				return content;
			}
			else
			{
				return fnNoAuthority;
			}
		}
		return null;
	}

	@Override
	public String onTeleportRequest(L2Npc npc, L2PcInstance player)
	{
		if(npc.isMyLord(player, false))
		{
			if(npc.getCastle().getFunctionLevel(FunctionType.TELEPORT) == 11)
			{
				npc.showTeleportList(player, 1);
			}
			else if(npc.getCastle().getFunctionLevel(FunctionType.TELEPORT) == 12)
			{
				npc.showTeleportList(player, 2);
			}
			else
			{
				return fnFuncDisabled;
			}
		}
		else
		{
			return fnNoAuthority;
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.isMyLord(player, false) ? fnHi : fnNotMyLord;
	}
}