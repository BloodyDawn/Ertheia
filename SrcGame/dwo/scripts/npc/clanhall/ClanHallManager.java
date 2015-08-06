package dwo.scripts.npc.clanhall;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.xml.BuyListData;
import dwo.gameserver.datatables.xml.ResidenceFunctionData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.L2TradeList;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.residence.clanhall.ClanHall;
import dwo.gameserver.model.world.residence.clanhall.type.AuctionableHall;
import dwo.gameserver.model.world.residence.function.FunctionType;
import dwo.gameserver.network.game.serverpackets.AgitDecoInfo;
import dwo.gameserver.network.game.serverpackets.packet.tradelist.ExBuySellList;
import dwo.gameserver.util.FStringUtil;
import org.apache.commons.lang3.ArrayUtils;

import java.text.SimpleDateFormat;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 05.01.13
 * Time: 15:29
 */

public class ClanHallManager extends Quest
{
	// Управляющий Холлом Клана
	private final int[] NPCs = {
		35384, 35386, 35388, 35390, 35392, 35394, 35396, 35398, 35400, 35403, 35405, 35407, 35439, 35441, 35443, 35445,
		35447, 35449, 35451, 35453, 35455, 35457, 35459, 35461, 35463, 35465, 35467, 35566, 35568, 35570, 35572, 35574,
		35576, 35578, 35580, 35582, 35584, 35586, 35383, 35421, 35438, 35640
	};

	// TODO телепорты 35383, 35421, 35438, 35640

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
	// Диалоги с скриптов
	String fnHi = "black001.htm";
	String fnNotMyLord = "black002.htm";
	String fnGetOut = "black003.htm";                   // не используется
	String fnOptionList = "black005.htm";               // не используется
	String fnAlreadyHaveOption = "black009.htm";        // не используется
	String fnNeedLowGrade = "black011.htm";             // не используется
	String fnAfterOptionAdd = "black012.htm";           // не используется
	String fnAddHPRegen1 = "black013.htm";              // не используется
	String fnAddHPRegen2 = "black014.htm";              // не используется
	String fnAddMPRegen1 = "black015.htm";              // не используется
	String fnAddMPRegen2 = "black016.htm";              // не используется
	String fnAddTeleporter1 = "black017.htm";           // не используется
	String fnAddTeleporter2 = "black020.htm";           // не используется
	String fnMyPledge = "black019.htm";                 // не используется
	String fnTeleportLevelZero = "black021.htm";        // не используется
	String fnWarehouse = "agitwarehouse.htm";
	String fnFunction = "agitfunction.htm";             // не используется
	String fnManage = "agitdecomanage.htm";
	// TODO запилить C B  грейды
	String fnManageRegenA = "agitdeco_ar01.htm";
	String fnManageRegenB = "agitdeco_br01.htm";
	String fnManageRegenC = "agitdeco_cr01.htm";
	String fnManageEtc = "agitdeco_ae01.htm";
	String fnManageDeco = "agitdeco_ad01.htm";
	String fnBanish = "agitbanish.htm";
	String fnAfterBanish = "agitafterbanish.htm";
	String fnDoor = "agitdoor.htm";
	String fnAfterDoorOpen = "agitafterdooropen.htm";
	String fnAfterDoorClose = "agitafterdoorclose.htm";
	String fnDecoFunction = "agitdecofunction.htm";
	String fnAfterSetDeco = "agitaftersetdeco.htm";
	String fnAfterResetDeco = "agitafterresetdeco.htm";
	String fnFailtoSetDeco = "agitfailtosetdeco.htm";
	String fnFailtoResetDeco = "agitfailtoresetdeco.htm";
	String fnDecoAlreadySet = "agitdecoalreadyset.htm";
	String fnDecoReset = "agitresetdeco.htm";
	String fnAgitBuff = "AgitBuff";
	String fnAfterBuff = "agitafterbuff.htm";
	String fnAfterBuyItem = "agitafterbuyitem.htm";      // не используется
	String fnNoAuthority = "noauthority.htm";
	String fnNotEnoughAdena = "agitnotenoughadena.htm";
	String fnUnableToSell = "agitunabletosell.htm";       // не используется
	String fnFuncDisabled = "agitfuncdisabled.htm";
	String fnNotEnoughMP = "agitnotenoughmp.htm";
	String fnNeedCoolTime = "agitneedcooltime.htm";
	String fnCostFail = "agitcostfail.htm";
	// Переменные из скриптов
	int decotype_hpregen = 1;
	int decotype_mpregen = 2;
	int decotype_xprestore = 4;
	int decotype_teleport = 5;
	int decotype_broadcast = 6;
	int decotype_curtain = 7;
	int decotype_hanging = 8;
	int decotype_buff = 9;
	int decotype_outerflag = 10;
	int decotype_platform = 11;
	int decotype_item = 12;
	int fstr_stage_lv = 9;
	int fstr_percent = 10;

	public ClanHallManager()
	{
		addFirstTalkId(NPCs);
		addTeleportRequestId(NPCs);
		addAskId(NPCs, -1); // Убираем вызов магазина через байпасс
		addAskId(NPCs, 0);
		addAskId(NPCs, -201);
		addAskId(NPCs, -203);
		addAskId(NPCs, -208);
		addAskId(NPCs, -219);
		addAskId(NPCs, -270);
		addAskId(NPCs, -271);
	}

	public static void main(String[] args)
	{
		new ClanHallManager();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(player.getClan() == null || player.getClan() != null && player.getClan().getClanhallId() != npc.getClanHall().getId())
		{
			return fnNotMyLord;
		}

		if(ask == 0)
		{
			return fnHi;
		}
		if(ask == -201)
		{
			switch(reply)
			{
				case 0:
					return fnHi;
				case 1: // Открыть/закрыть дверь
					return (player.getClanPrivileges() & L2Clan.CP_CH_OPEN_DOOR) == L2Clan.CP_CH_OPEN_DOOR ? fnDoor : fnNoAuthority;
				case 2: // Выгнать посторонних
					return (player.getClanPrivileges() & L2Clan.CP_CH_DISMISS) == L2Clan.CP_CH_DISMISS ? fnBanish : fnNoAuthority;
				case 3: // Функции Холла Клана
					if((player.getClanPrivileges() & L2Clan.CP_CH_SET_FUNCTIONS) == L2Clan.CP_CH_SET_FUNCTIONS)
					{
						String content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnDecoFunction);
						ClanHall.ClanHallFunction hpFunc = npc.getClanHall().getFunction(FunctionType.HP_REGEN);
						ClanHall.ClanHallFunction mpFunc = npc.getClanHall().getFunction(FunctionType.MP_REGEN);
						ClanHall.ClanHallFunction expFunc = npc.getClanHall().getFunction(FunctionType.XP_RESTORE);
						content = hpFunc == null ? content.replace("<?HPDepth?>", String.valueOf(0)) : content.replace("<?HPDepth?>", String.valueOf(hpFunc.getFunctionData().getPercent()));
						content = mpFunc == null ? content.replace("<?MPDepth?>", String.valueOf(0)) : content.replace("<?MPDepth?>", String.valueOf(mpFunc.getFunctionData().getPercent()));
						content = expFunc == null ? content.replace("<?XPDepth?>", String.valueOf(0)) : content.replace("<?XPDepth?>", String.valueOf(expFunc.getFunctionData().getPercent()));
						return content;
					}
					else
					{
						return fnNoAuthority;
					}
				case 4: // Хранилище Холла Клана
					if((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) == L2Clan.CP_CL_VIEW_WAREHOUSE)
					{
						String content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnWarehouse);
						content = content.replace("<?agit_lease?>", String.valueOf(npc.getClanHall().getLease()));
						content = content.replace("<?pay_time?>", String.valueOf(dateFormat.format(npc.getClanHall().getPaidUntil())));
						return content;
					}
					else
					{
						return fnNoAuthority;
					}
				case 5: // Управление Холлом Клана
					return (player.getClanPrivileges() & L2Clan.CP_CH_SET_FUNCTIONS) == L2Clan.CP_CH_SET_FUNCTIONS ? fnManage : fnNoAuthority;
				case 6:
					return fnWarehouse;
				case 7: // Чары
					if(npc.getClanHall().getFunction(FunctionType.BUFF) == null)
					{
						return fnFuncDisabled;
					}
					else
					{
						String content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnAgitBuff + '_' + npc.getClanHall().getFunctionLevel(FunctionType.BUFF) + ".htm");
						content = content.replace("<?MPLeft?>", String.valueOf((int) npc.getCurrentMp()));
						return content;
					}
				case 12: // Создать предмет
				{
					ClanHall.ClanHallFunction itemFunc = npc.getClanHall().getFunction(FunctionType.ITEM_CREATE);
					if(itemFunc == null)
					{
						return fnFuncDisabled;
					}
					L2TradeList tradeList = BuyListData.getInstance().getBuyList(npc.getNpcId(), itemFunc.getFunctionData().getLevel() - 1);
					if(tradeList != null)
					{
						double buyTotalTaxRate = npc.getAdenaTotalTaxRate(ProcessType.BUY);
						double sellTotalTaxRate = npc.getAdenaTotalTaxRate(ProcessType.SELL);
						player.tempInventoryDisable();
						player.sendPacket(new ExBuySellList(player, tradeList, ProcessType.BUY, buyTotalTaxRate, false, player.getAdenaCount()));
						player.sendPacket(new ExBuySellList(player, tradeList, ProcessType.SELL, sellTotalTaxRate, false, player.getAdenaCount()));
						player.sendActionFailed();
					}
					break;
				}
				case 51:
					if((player.getClanPrivileges() & L2Clan.CP_CH_SET_FUNCTIONS) == L2Clan.CP_CH_SET_FUNCTIONS)
					{
						String content;

						if(npc.getClanHall().getGrade() == 3)
						{
							content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnManageRegenA);
						}
						else
						{
							content = npc.getClanHall().getGrade() == 2 ? HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnManageRegenB) : HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnManageRegenC);
						}

						ClanHall.ClanHallFunction hpFunc = npc.getClanHall().getFunction(FunctionType.HP_REGEN);
						ClanHall.ClanHallFunction mpFunc = npc.getClanHall().getFunction(FunctionType.MP_REGEN);
						ClanHall.ClanHallFunction expFunc = npc.getClanHall().getFunction(FunctionType.XP_RESTORE);
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
				case 52:
					if((player.getClanPrivileges() & L2Clan.CP_CH_SET_FUNCTIONS) == L2Clan.CP_CH_SET_FUNCTIONS)
					{
						String content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnManageEtc);
						ClanHall.ClanHallFunction teleFunc = npc.getClanHall().getFunction(FunctionType.TELEPORT);
						ClanHall.ClanHallFunction bcFunc = npc.getClanHall().getFunction(FunctionType.BROADCAST);
						ClanHall.ClanHallFunction itemFunc = npc.getClanHall().getFunction(FunctionType.ITEM_CREATE);
						ClanHall.ClanHallFunction buffFunc = npc.getClanHall().getFunction(FunctionType.BUFF);
						if(teleFunc == null)
						{
							content = content.replace("<?TPDepth?>", "");
							content = content.replace("<?TPCost?>", "");
							content = content.replace("<?TPExpire?>", FStringUtil.makeFString(4));
							content = content.replace("<?TPReset?>", "");
						}
						else
						{
							content = content.replace("<?TPDepth?>", FStringUtil.makeFString(fstr_stage_lv, String.valueOf(teleFunc.getFunctionData().getLevel())));
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
							content = content.replace("<?BFDepth?>", FStringUtil.makeFString(fstr_stage_lv, String.valueOf(buffFunc.getFunctionData().getLevel())));
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
				case 53:
					if((player.getClanPrivileges() & L2Clan.CP_CH_SET_FUNCTIONS) == L2Clan.CP_CH_SET_FUNCTIONS)
					{
						String content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnManageDeco);
						ClanHall.ClanHallFunction curtFunc = npc.getClanHall().getFunction(FunctionType.CURTAIN);
						ClanHall.ClanHallFunction platformFunc = npc.getClanHall().getFunction(FunctionType.PLATFORM);
						if(curtFunc == null)
						{
							content = content.replace("<?7_Depth?>", "");
							content = content.replace("<?7_Cost?>", "");
							content = content.replace("<?7_Expire?>", FStringUtil.makeFString(4));
							content = content.replace("<?7_Reset?>", "");
						}
						else
						{
							content = content.replace("<?7_Depth?>", FStringUtil.makeFString(fstr_stage_lv, String.valueOf(curtFunc.getFunctionData().getLevel())));
							content = content.replace("<?7_Cost?>", FStringUtil.makeFString(6, String.valueOf(curtFunc.getFunctionData().getFunctionCostData().getCost()), String.valueOf(curtFunc.getFunctionData().getFunctionCostData().getDays())));
							content = content.replace("<?7_Expire?>", FStringUtil.makeFString(5, dateFormat.format(curtFunc.getEndTime())));
							content = content.replace("<?7_Reset?>", FStringUtil.makeFString(7, "[", String.valueOf(decotype_curtain), "]"));
						}
						if(platformFunc == null)
						{
							content = content.replace("<?11_Depth?>", "");
							content = content.replace("<?11_Cost?>", "");
							content = content.replace("<?11_Expire?>", FStringUtil.makeFString(4));
							content = content.replace("<?11_Reset?>", "");
						}
						else
						{
							content = content.replace("<?11_Depth?>", FStringUtil.makeFString(fstr_stage_lv, String.valueOf(platformFunc.getFunctionData().getLevel())));
							content = content.replace("<?11_Cost?>", FStringUtil.makeFString(6, String.valueOf(platformFunc.getFunctionData().getFunctionCostData().getCost()), String.valueOf(platformFunc.getFunctionData().getFunctionCostData().getDays())));
							content = content.replace("<?11_Expire?>", FStringUtil.makeFString(5, dateFormat.format(platformFunc.getEndTime())));
							content = content.replace("<?11_Reset?>", FStringUtil.makeFString(7, "[", String.valueOf(decotype_platform), "]"));
						}
						return content;
					}
					else
					{
						return fnNoAuthority;
					}
			}
		}
		else if(ask == -203) // Открыть или закрыть двери
		{
			if((player.getClanPrivileges() & L2Clan.CP_CH_OPEN_DOOR) == L2Clan.CP_CH_OPEN_DOOR)
			{
				switch(reply)
				{
					case 1:
						npc.getClanHall().openCloseDoors(true);
						return fnAfterDoorOpen;
					case 2:
						npc.getClanHall().openCloseDoors(false);
						return fnAfterDoorClose;
				}
			}
			else
			{
				return fnNoAuthority;
			}
		}
		else if(ask == -219) // Выгнать посторонних
		{
			if(reply == 1)
			{
				if((player.getClanPrivileges() & L2Clan.CP_CH_DISMISS) == L2Clan.CP_CH_DISMISS)
				{
					npc.getClanHall().banishForeigners();
					return fnAfterBanish;
				}
				else
				{
					return fnNoAuthority;
				}
			}
		}
		else if(ask == -270)
		{
			if((player.getClanPrivileges() & L2Clan.CP_CH_SET_FUNCTIONS) == L2Clan.CP_CH_SET_FUNCTIONS)
			{
				String content;
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
				if(npc.getClanHall().getFunctionLevel(decoType) == i1)
				{
					content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnDecoAlreadySet);
				}
				else
				{
					content = HtmCache.getInstance().getHtm(player.getLang(), "default/agitdeco__" + i0 + ".htm");
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
		else if(ask == -271) // Согласиться (удаление\добавление функций)
		{
			if((player.getClanPrivileges() & L2Clan.CP_CH_SET_FUNCTIONS) == L2Clan.CP_CH_SET_FUNCTIONS)
			{
				int i0 = reply / 1000;
				int i1 = reply - i0 * 1000;
				FunctionType decoType = FunctionType.values()[i0];
				if(i1 == 0)
				{
					npc.getClanHall().removeFunction(decoType);
					npc.broadcastPacket(new AgitDecoInfo((AuctionableHall) npc.getClanHall()));
					return fnAfterResetDeco;
				}
				else if(player.getAdenaCount() >= ResidenceFunctionData.getInstance().getDeco(decoType, i1).getFunctionCostData().getCost())
				{
					player.reduceAdena(ProcessType.NPC, ResidenceFunctionData.getInstance().getDeco(decoType, i1).getFunctionCostData().getCost(), npc, true);
					npc.getClanHall().updateFunctions(decoType, i1);
					npc.broadcastPacket(new AgitDecoInfo((AuctionableHall) npc.getClanHall()));
					return fnAfterSetDeco;
				}
				else
				{
					return fnNotEnoughAdena;
				}
			}
			else
			{
				return fnNoAuthority;
			}
		}
		else if(ask == -208) // Баф
		{
			ClanHall.ClanHallFunction buffFunc = npc.getClanHall().getFunction(FunctionType.BUFF);
			if(buffFunc != null)
			{
				String content;
				L2Skill skill = SkillTable.getInstance().getInfo(reply);
				if(skill != null && skill.getMpConsume() < npc.getCurrentMp())
				{
					boolean validCheck = false;
					switch(buffFunc.getFunctionData().getLevel())
					{
						case 1:
							if(ArrayUtils.contains(buff_1, reply))
							{
								validCheck = true;
							}
							break;
						case 2:
							if(ArrayUtils.contains(buff_2, reply))
							{
								validCheck = true;
							}
							break;
						case 3:
							if(ArrayUtils.contains(buff_3, reply))
							{
								validCheck = true;
							}
							break;
						case 4:
							if(ArrayUtils.contains(buff_4, reply))
							{
								validCheck = true;
							}
							break;
						case 5:
							if(ArrayUtils.contains(buff_5, reply))
							{
								validCheck = true;
							}
							break;
						case 6:
						case 7:
							if(ArrayUtils.contains(buff_7, reply))
							{
								validCheck = true;
							}
							break;
						case 8:
							if(ArrayUtils.contains(buff_8, reply))
							{
								validCheck = true;
							}
							break;
						default:
							return null;
					}

					if(validCheck)
					{
						npc.setTarget(player);
						npc.doCast(skill);
					}
					content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnAgitBuff + '_' + buffFunc.getFunctionData().getLevel() + ".htm");
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
		if(player.getClan() != null && player.getClan().getClanhallId() == npc.getClanHall().getId())
		{
			if(npc.getClanHall().getFunctionLevel(FunctionType.TELEPORT) == 1)
			{
				npc.showTeleportList(player, 1);
			}
			else if(npc.getClanHall().getFunctionLevel(FunctionType.TELEPORT) == 2)
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
		if(player.getClan() != null && player.getClan().getClanhallId() == npc.getClanHall().getId())
		{
			int daysToFree = npc.getClanHall().getDaysToFree();
			if(daysToFree > 1)
			{
				return fnHi;
			}
			else
			{
				String content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnCostFail);
				content = content.replace("<?CostFailDayLeft?>", String.valueOf(daysToFree));
				return content;
			}
		}
		else
		{
			return fnNotMyLord;
		}
	}
}