package dwo.scripts.ai.zone;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 20.11.12
 * Time: 13:03
 */

public class VarkaKetraSupport extends Quest
{
	// Кетра
	private static final int KADUN = 31370; // Hierarch
	private static final int WAHKAN = 31371; // Messenger
	private static final int ASEFA = 31372; // Soul Guide
	private static final int ATAN = 31373; // Grocer
	private static final int JAFF = 31374; // Warehouse Keeper
	private static final int JUMARA = 31375; // Trader
	private static final int KURFA = 31376; // Gate Keeper
	private static final int[] KetraNPCS = {
		KADUN, WAHKAN, ASEFA, ATAN, JAFF, JUMARA, KURFA
	};
	// Варка
	private static final int ASHAS = 31377; //Hierarch
	private static final int NARAN = 31378; //Messenger
	private static final int UDAN = 31379; //Buffer
	private static final int DIYABU = 31380; //Grocer
	private static final int HAGOS = 31381; //Warehouse Keeper
	private static final int SHIKON = 31382; //Trader
	private static final int TERANU = 31383; //Teleporter
	private static final int[] VarkaNPCS = {
		ASHAS, NARAN, UDAN, DIYABU, HAGOS, SHIKON, TERANU
	};

	// Кетра
	private static final int HORN = 7186;

	// Варка
	private static final int SEED = 7187;

	private static final TIntObjectHashMap<BuffsData> BUFF = new TIntObjectHashMap<>();

	public VarkaKetraSupport()
	{
		addAskId(KURFA, -30);
		addAskId(ASEFA, -34);
		addAskId(TERANU, -30);
		addAskId(UDAN, -34);
		addFirstTalkId(KetraNPCS);
		addFirstTalkId(VarkaNPCS);

		// Заполяем таблицу бафов
		BUFF.put(1, new BuffsData(4359, 2)); // Focus: Requires 2 Buffalo Horns
		BUFF.put(2, new BuffsData(4360, 2)); // Death Whisper: Requires 2 Buffalo Horns
		BUFF.put(3, new BuffsData(4345, 3)); // Might: Requires 3 Buffalo Horns
		BUFF.put(4, new BuffsData(4355, 3)); // Acumen: Requires 3 Buffalo Horns
		BUFF.put(5, new BuffsData(4352, 3)); // Berserker: Requires 3 Buffalo Horns
		BUFF.put(6, new BuffsData(4354, 3)); // Vampiric Rage: Requires 3 Buffalo Horns
		BUFF.put(7, new BuffsData(4356, 6)); // Empower: Requires 6 Buffalo Horns
		BUFF.put(8, new BuffsData(4357, 6)); // Haste: Requires 6 Buffalo Horns
	}

	public static void main(String[] args)
	{
		new VarkaKetraSupport();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -30)
		{
			npc.showTeleportList(player, reply);
		}
		else if(ask == -34)
		{
			int tradeItemId = ArrayUtils.contains(KetraNPCS, npc.getNpcId()) ? HORN : SEED;
			long tradeItemCount = player.getItemsCount(tradeItemId);
			BuffsData buff = BUFF.get(reply);
			if(tradeItemCount >= buff.getCost())
			{
				player.destroyItemByItemId(ProcessType.NPC, tradeItemId, buff.getCost(), npc, true);
				npc.setTarget(player);
				npc.doCast(buff.getSkill());
				npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
				return npc.getNpcId() == ASEFA ? "shaman_asefa008.htm" : "shaman_udan008.htm";
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();
		int allianceLevel = player.getAllianceWithVarkaKetra();
		long tradeItemsCount = ArrayUtils.contains(KetraNPCS, npcId) ? player.getItemsCount(HORN) : player.getItemsCount(SEED);

		// Кетра
		if(npcId == KADUN)
		{
			return allianceLevel > 0 ? "elder_kadun_zu_ketra001.htm" : "elder_kadun_zu_ketra009.htm";
		}
		if(npcId == WAHKAN)
		{
			return allianceLevel > 0 ? "herald_wakan001.htm" : "herald_wakan009.htm";
		}
		if(npcId == ASEFA)
		{
			if(allianceLevel < 1)
			{
				return "shaman_asefa009.htm";
			}
			else if(allianceLevel < 3 && allianceLevel > 0)
			{
				return "shaman_asefa001.htm";
			}
			else if(allianceLevel > 2)
			{
				return tradeItemsCount != 0 ? "shaman_asefa008.htm" : "shaman_asefa002.htm";
			}
		}
		else if(npcId == ATAN)
		{
			if(player.hasBadReputation())
			{
				return "trader_atan009.htm";
			}
			else if(allianceLevel <= 0)
			{
				return "trader_atan009.htm";
			}
			else
			{
				return allianceLevel == 1 || allianceLevel == 2 ? "trader_atan001.htm" : "trader_atan005.htm";
			}
		}
		else if(npcId == JAFF)
		{
			if(allianceLevel <= 0)
			{
				return "keeper_jaf009.htm";
			}
			else if(allianceLevel == 1)
			{
				return "keeper_jaf001.htm";
			}
			else if(player.getWarehouse().getSize() == 0)
			{
				return "keeper_jaf005.htm";
			}
			else if(allianceLevel > 1)
			{
				return player.hasBadReputation() ? "keeper_jaf003.htm" : "keeper_jaf008.htm";
			}
		}
		else if(npcId == JUMARA)
		{
			if(allianceLevel == 2)
			{
				return "merchant_jumara001.htm";
			}
			else if(allianceLevel == 3 || allianceLevel == 4)
			{
				return "merchant_jumara008.htm";
			}
			else
			{
				return allianceLevel == 5 ? "merchant_jumara007.htm" : "merchant_jumara009.htm";
			}
		}
		else if(npcId == KURFA)
		{
			if(allianceLevel <= 0)
			{
				return "gatekeeper_kurfa009.htm";
			}
			else if(allianceLevel > 0 && allianceLevel < 4)
			{
				return "gatekeeper_kurfa001.htm";
			}
			else if(allianceLevel == 4)
			{
				return "gatekeeper_kurfa007.htm";
			}
			else if(allianceLevel == 5)
			{
				return "gatekeeper_kurfa008.htm";
			}
		}
		// Варка
		else if(npcId == ASHAS)
		{
			return allianceLevel < 0 ? "elder_ashas_barka_durai001.htm" : "elder_ashas_barka_durai009.htm";
		}
		else if(npcId == NARAN)
		{
			return allianceLevel < 0 ? "herald_naran001.htm" : "herald_naran009.htm";
		}
		else if(npcId == UDAN)
		{
			if(allianceLevel > -1)
			{
				return "shaman_udan009.htm";
			}
			else if(allianceLevel > -3 && allianceLevel > 0)
			{
				return "shaman_udan001.htm";
			}
			else if(allianceLevel < -2)
			{
				return tradeItemsCount != 0 ? "shaman_udan0081.htm" : "shaman_udan002.htm";
			}
		}
		else if(npcId == DIYABU)
		{
			if(player.hasBadReputation())
			{
				return "trader_diyabu009.htm";
			}
			else if(allianceLevel >= 0)
			{
				return "trader_diyabu009.htm";
			}
			else
			{
				return allianceLevel == -1 || allianceLevel == -2 ? "trader_diyabu001.htm" : "trader_diyabu005.htm";
			}
		}
		else if(npcId == HAGOS)
		{
			if(allianceLevel >= 0)
			{
				return "keeper_hagos009.htm";
			}
			else if(allianceLevel == -1)
			{
				return "keeper_hagos001.htm";
			}
			else if(player.getWarehouse().getSize() == 0)
			{
				return "keeper_hagos005.htm";
			}
			else if(allianceLevel == -2 || allianceLevel == -3)
			{
				return "keeper_hagos003.htm";
			}
			else if(allianceLevel == -4)
			{
				return "keeper_hagos008.htm";
			}
		}
		else if(npcId == SHIKON)
		{
			if(allianceLevel == -2)
			{
				return "merchant_shikon001.htm";
			}
			else if(allianceLevel == -3 || allianceLevel == -4)
			{
				return "merchant_shikon008.htm";
			}
			else
			{
				return allianceLevel == -5 ? "merchant_shikon007.htm" : "merchant_shikon009.htm";
			}
		}
		else if(npcId == TERANU)
		{
			if(allianceLevel >= 0)
			{
				return "gatekeeper_teranu009.htm";
			}
			else if(allianceLevel < 0 && allianceLevel > -4)
			{
				return "gatekeeper_teranu001.htm";
			}
			else
			{
				return allianceLevel == -4 ? "gatekeeper_teranu007.htm" : "gatekeeper_teranu008.htm";
			}
		}
		return null;
	}

	private class BuffsData
	{
		private int _skill;
		private int _cost;

		public BuffsData(int skill, int cost)
		{
			_skill = skill;
			_cost = cost;
		}

		public L2Skill getSkill()
		{
			return SkillTable.getInstance().getInfo(_skill, 1);
		}

		public int getCost()
		{
			return _cost;
		}
	}
}