package dwo.scripts.npc.fort;

import dwo.config.Config;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.xml.BuyListData;
import dwo.gameserver.datatables.xml.ResidenceFunctionData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.L2TradeList;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.model.world.residence.function.FunctionType;
import dwo.gameserver.network.game.serverpackets.packet.tradelist.ExBuySellList;
import dwo.gameserver.util.FStringUtil;
import org.apache.commons.lang3.ArrayUtils;

import java.text.SimpleDateFormat;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 10.01.13
 * Time: 18:29
 */

public class FortManager extends Quest
{
	private static final int[] NPCs = {
		35658, 35689, 35727, 35758, 35796, 35827, 35858, 35896, 35927, 35965, 36003, 36034, 36072, 36110, 36141, 36172,
		36210, 36248, 36286, 36317, 36355
	};

	// Параметры с скриптов
	private static final String fnHi = "fortress_steward001.htm";
	private static final String fnHi2 = "fortress_steward002.htm";
	private static final String fnOptionList = "fortress_steward005.htm";
	private static final String fnNotMyLord = "fortress_steward002.htm";
	private static final String fnBanish = "fortress_steward004.htm";
	private static final String fnAfterOpenGate = "fortress_steward006.htm";
	private static final String fnAfterCloseGate = "fortress_steward007.htm";
	private static final String fnAfterBanish = "fortress_steward008.htm";
	private static final String fnNotEnoughAdena = "fortress_steward010.htm";
	private static final String fnNoAuthority = "fortress_steward017.htm";
	private static final String fnIsUnderSiege = "fortress_steward018.htm";
	private static final String fnManage = "ol_mahum_FortDecoManage.htm";
	private static final String fnManageRegen = "ol_mahum_FortDeco_AR01.htm";
	private static final String fnManageEtc = "ol_mahum_FortDeco_AE01.htm";
	private static final String fnDecoReset = "fortressresetdeco.htm";
	private static final String fnNotEnoughMP = "fortressnotenoughmp.htm";
	private static final String fnNeedCoolTime = "fortressneedcooltime.htm";
	private static final String fnFuncDisabled = "fortressfuncdisabled.htm";
	private static final String fnFailtoSetDeco = "fortressfailtosetdeco.htm";
	private static final String fnFailtoResetDeco = "fortressfailtoresetdeco.htm";
	private static final String fnDecoFunction = "fortressdecofunction.htm";
	private static final String fnDecoAlreadySet = "fortressdecoalreadyset.htm";
	private static final String fnAgitBuff = "fortressbuff";
	private static final String fnAfterSetDeco = "fortressaftersetdeco.htm";
	private static final String fnAfterResetDeco = "fortressafterresetdeco.htm";
	private static final String fnAfterBuff = "fortressafterbuff.htm";
	private static final String fnWarehouse = "fortresswarehouse_b.htm";
	private static final String fnFortressInfo = "fortress_steward022.htm";
	private static final String fnSetGate = "ol_mahum_steward_tamutak003.htm";
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
	// TODO запилить C B  грейды
	String fnManageRegenA = "agitdeco_ar01.htm";
	String fnManageRegenB = "agitdeco_br01.htm";
	String fnManageRegenC = "agitdeco_cr01.htm";
	int decotype_hpregen = 1;
	int decotype_mpregen = 2;
	int decotype_xprestore = 4;
	int decotype_teleport = 5;
	int decotype_broadcast = 6;
	int decotype_buff = 9;
	int decotype_item = 12;
	String fileSetName = "FortressDeco";
	int fstr_stage_lv = 9;
	int fstr_percent = 10;
	int fstr_own_time = 1300135;
	String fnFortressInfoIndep = "fortress_steward023.htm";
	int fstr_parent_castle = 1001000;
	int fstr_no_parent = 1300136;
	int fstr_contract_status = 1300123;

	public FortManager()
	{
		addFirstTalkId(NPCs);
		addTeleportRequestId(NPCs);
		addAskId(NPCs, -1); // Убираем вызов магазина через байпасс
		addAskId(NPCs, 0);
		addAskId(NPCs, -272);
		addAskId(NPCs, -201);
		addAskId(NPCs, -202);
		addAskId(NPCs, -203);
		addAskId(NPCs, -219);
		addAskId(NPCs, -270);
		addAskId(NPCs, -271);
		addAskId(NPCs, -208);
	}

	public static void main(String[] args)
	{
		new FortManager();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(!npc.isMyLord(player, false))
		{
			return fnNotMyLord;
		}
		if(ask == 0)
		{
			return fnHi;
		}
		if(ask == -272) // Получить отчет
		{
			if(reply == 0)
			{
				long milliseconds = npc.getFort().getTimeTillRebelArmy();
				int hours = (int) Math.floor(milliseconds / 3600000);
				int minutes = (int) ((milliseconds - hours * 3600000) / (1000 * 60)) % 60;

				int parentCastleId = npc.getFort().getCastleId();
				String content;
				if(parentCastleId > 0 && parentCastleId < 10)
				{
					int contractStatus = npc.getFort().getFortState().ordinal() - 1; // -1 для совместимости с офом
					long nextRewardRemainTime = npc.getFort().getTimeTillNextFortUpdate();
					content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnFortressInfo);
					content = content.replace("<?ParentCastle?>", FStringUtil.makeFString(fstr_parent_castle + parentCastleId));
					content = content.replace("<?ContractStatus?>", FStringUtil.makeFString(fstr_contract_status + contractStatus));
					content = content.replace("<?RentCost?>", String.valueOf(Config.FS_FEE_FOR_CASTLE << 1));
					content = content.replace("<?NextDueHour?>", String.valueOf(nextRewardRemainTime / 3600));
					content = content.replace("<?NextDueMin?>", String.valueOf(nextRewardRemainTime / 3600 / 60));
				}
				else
				{
					content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnFortressInfoIndep);
					content = content.replace("<?ParentCastle?>", FStringUtil.makeFString(fstr_no_parent));
				}
				content = content.replace("<?time_remained?>", FStringUtil.makeFString(fstr_own_time, String.valueOf(hours), String.valueOf(minutes)));
				return content;
			}
		}
		else if(ask == -201)
		{
			switch(reply)
			{
				case 0:
					return fnHi; // TODO: что брать?
				case 1: // Открыть/закрыть дверь
					if((player.getClanPrivileges() & L2Clan.CP_CS_OPEN_DOOR) == L2Clan.CP_CS_OPEN_DOOR)
					{
						return npc.getFort().getSiege().isInProgress() ? fnIsUnderSiege : fnSetGate;
					}
					else
					{
						return fnNoAuthority;
					}
				case 2: // Выгнать посторонних
					if((player.getClanPrivileges() & L2Clan.CP_CS_DISMISS) == L2Clan.CP_CS_DISMISS)
					{
						return npc.getFort().getSiege().isInProgress() ? fnIsUnderSiege : fnBanish;
					}
					else
					{
						return fnNoAuthority;
					}
				case 3: // Функции Форта
					if((player.getClanPrivileges() & L2Clan.CP_CS_SET_FUNCTIONS) == L2Clan.CP_CS_SET_FUNCTIONS)
					{
						String content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnDecoFunction);
						Fort.FortFunction hpFunc = npc.getFort().getFunction(FunctionType.HP_REGEN);
						Fort.FortFunction mpFunc = npc.getFort().getFunction(FunctionType.MP_REGEN);
						Fort.FortFunction expFunc = npc.getFort().getFunction(FunctionType.XP_RESTORE);
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
					return (player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) == L2Clan.CP_CL_VIEW_WAREHOUSE ? fnWarehouse : fnNoAuthority;
				case 5: // Управление Холлом Клана
					return (player.getClanPrivileges() & L2Clan.CP_CS_SET_FUNCTIONS) == L2Clan.CP_CS_SET_FUNCTIONS ? fnManage : fnNoAuthority;
				case 6:
					return fnWarehouse;
				case 7: // Чары
					if(npc.getFort().getFunction(FunctionType.BUFF) == null)
					{
						return fnFuncDisabled;
					}
					else
					{
						String content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnAgitBuff + '_' + (npc.getFort().getFunctionLevel(FunctionType.BUFF) - 10) + ".htm");
						content = content.replace("<?MPLeft?>", String.valueOf((int) npc.getCurrentMp()));
						return content;
					}
				case 12: // Создать предмет
				{
					Fort.FortFunction itemFunc = npc.getFort().getFunction(FunctionType.ITEM_CREATE);
					if(itemFunc == null)
					{
						return fnFuncDisabled;
					}
					L2TradeList tradeList = BuyListData.getInstance().getBuyList(npc.getNpcId(), itemFunc.getFunctionData().getLevel() - 11);
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
					if((player.getClanPrivileges() & L2Clan.CP_CS_SET_FUNCTIONS) == L2Clan.CP_CS_SET_FUNCTIONS)
					{
						String content;
						// TODO: Нихрена непонятно что с грейдами делать в форте
						content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnManageRegenA);
						/*if(npc.getFort().getGrade() == 3)
						{
							content = HtmCache.getInstance().getHtm(player.getLang(), "default/"+fnManageRegenA);
						}
						else if(npc.getFort().getGrade() == 2)
						{
							content = HtmCache.getInstance().getHtm(player.getLang(), "default/"+fnManageRegenB);
						}
						else
						{
							content = HtmCache.getInstance().getHtm(player.getLang(), "default/"+fnManageRegenC);
						}
                        */
						Fort.FortFunction hpFunc = npc.getFort().getFunction(FunctionType.HP_REGEN);
						Fort.FortFunction mpFunc = npc.getFort().getFunction(FunctionType.MP_REGEN);
						Fort.FortFunction expFunc = npc.getFort().getFunction(FunctionType.XP_RESTORE);
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
							content = content.replace("<?MPDepth?>", FStringUtil.makeFString(10, String.valueOf(mpFunc.getFunctionData().getPercent())));
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
							content = content.replace("<?XPDepth?>", FStringUtil.makeFString(10, String.valueOf(expFunc.getFunctionData().getPercent())));
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
					if((player.getClanPrivileges() & L2Clan.CP_CS_SET_FUNCTIONS) == L2Clan.CP_CS_SET_FUNCTIONS)
					{
						String content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + fnManageEtc);
						Fort.FortFunction teleFunc = npc.getFort().getFunction(FunctionType.TELEPORT);
						Fort.FortFunction bcFunc = npc.getFort().getFunction(FunctionType.BROADCAST);
						Fort.FortFunction itemFunc = npc.getFort().getFunction(FunctionType.ITEM_CREATE);
						Fort.FortFunction buffFunc = npc.getFort().getFunction(FunctionType.BUFF);
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
			}
		}
		else if(ask == -202)
		{
			if((player.getClanPrivileges() & L2Clan.CP_CS_OPEN_DOOR) == L2Clan.CP_CS_OPEN_DOOR)
			{
				switch(reply)
				{
					case 1:
						if(npc.getFort().getSiege().isInProgress())
						{
							return fnIsUnderSiege;
						}
						else
						{
							for(L2DoorInstance door : npc.getFort().getDoors())
							{
								door.openMe();
							}
							return fnAfterOpenGate;
						}
					case 2:
						if(npc.getFort().getSiege().isInProgress())
						{
							return fnIsUnderSiege;
						}
						else
						{
							for(L2DoorInstance door : npc.getFort().getDoors())
							{
								door.closeMe();
							}
							return fnAfterCloseGate;
						}
				}
			}
			else
			{
				return fnNoAuthority;
			}
		}
		else if(ask == -203) // Открыть или закрыть двери
		{
			if((player.getClanPrivileges() & L2Clan.CP_CS_OPEN_DOOR) == L2Clan.CP_CS_OPEN_DOOR)
			{
				switch(reply)
				{
					case 1:
						for(L2DoorInstance door : npc.getFort().getDoors())
						{
							door.openMe();
						}
						return fnAfterOpenGate;
					case 2:
						for(L2DoorInstance door : npc.getFort().getDoors())
						{
							door.closeMe();
						}
						return fnAfterCloseGate;
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
				if((player.getClanPrivileges() & L2Clan.CP_CS_DISMISS) == L2Clan.CP_CS_DISMISS)
				{
					npc.getFort().banishForeigners();
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
			if((player.getClanPrivileges() & L2Clan.CP_CS_SET_FUNCTIONS) == L2Clan.CP_CS_SET_FUNCTIONS)
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
				if(npc.getFort().getFunctionLevel(decoType) == i1)
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
		else if(ask == -271) // Согласиться (удаление\добавление функций)
		{
			if((player.getClanPrivileges() & L2Clan.CP_CS_SET_FUNCTIONS) == L2Clan.CP_CS_SET_FUNCTIONS)
			{
				int i0 = reply / 1000;
				int i1 = reply - i0 * 1000;
				FunctionType decoType = FunctionType.values()[i0];
				if(i1 == 0)
				{
					npc.getFort().removeFunction(decoType);
					return fnAfterResetDeco;
				}
				else if(player.getAdenaCount() >= ResidenceFunctionData.getInstance().getDeco(decoType, i1).getFunctionCostData().getCost())
				{
					player.reduceAdena(ProcessType.NPC, ResidenceFunctionData.getInstance().getDeco(decoType, i1).getFunctionCostData().getCost(), npc, true);
					npc.getFort().updateFunctions(decoType, i1);
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
			Fort.FortFunction buffFunc = npc.getFort().getFunction(FunctionType.BUFF);
			if(buffFunc != null)
			{
				String content;
				L2Skill skill = SkillTable.getInstance().getInfo(reply);
				if(skill != null && skill.getMpConsume() < npc.getCurrentMp())
				{
					boolean check = false;
					switch(buffFunc.getFunctionData().getLevel())
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
						default:
							return null;
					}

					if(check)
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
		if(npc.isMyLord(player, false))
		{
			if(npc.getFort().getFunctionLevel(FunctionType.TELEPORT) == 1)
			{
				npc.showTeleportList(player, 1);
			}
			else if(npc.getFort().getFunctionLevel(FunctionType.TELEPORT) == 2)
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
		if(npc.isMyLord(player, false))
		{
			return npc.getFort().getSiege().isInProgress() ? fnIsUnderSiege : fnHi;
		}
		else
		{
			return fnHi2;
		}
	}
}