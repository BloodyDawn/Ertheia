package dwo.scripts.npc;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 03.10.12
 * Time: 1:49
 */

public class WyvernManager extends Quest
{
	private static final int[] NPCs = {
		35101, 35143, 35185, 35227, 35275, 35317, 35510, 35556, 35364, 35419, 35638, 36457, 36458, 36459, 36460, 36461,
		36462, 36463, 36464, 36465, 36466, 36467, 36468, 36469, 36470, 36471, 36472, 36473, 36474, 36475, 36476, 36477
	};

	private static final int ride_wyvern_level = 55;
	private static final int ride_wyvern_fee = 25;

	private static final int[] STRIDERs = {12526, 12527, 12528, 16038, 16039, 16040, 16068, 13197};

	public WyvernManager()
	{
		addFirstTalkId(NPCs);
		addAskId(NPCs, -17);
		addAskId(NPCs, 0);
	}

	public static void main(String[] args)
	{
		new WyvernManager();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		// На главную
		if(ask == 0)
		{
			return onFirstTalk(npc, player);
		}

		String content;
		switch(reply)
		{
			case 1:
				if(getManagerType(npc) == ManagerType.CASTLE)
				{
					content = HtmCache.getInstance().getHtm(player.getLang(), "default/wkeeper003.htm");
					content = content.replace("<?ride_wyvern_level?>", String.valueOf(ride_wyvern_level)).replace("<?ride_wyvern_fee?>", String.valueOf(ride_wyvern_fee));
					return content;
				}
				else
				{
					content = HtmCache.getInstance().getHtm(player.getLang(), "default/wkeeper003_Agit.htm");
					content = content.replace("<?ride_wyvern_level?>", String.valueOf(ride_wyvern_level)).replace("<?ride_wyvern_fee?>", String.valueOf(ride_wyvern_fee));
					return content;
				}
			case 2:
				int mountNpcId = player.getMountNpcId();
				if(mountNpcId <= 0 || !ArrayUtils.contains(STRIDERs, mountNpcId))
				{
					player.sendPacket(SystemMessageId.YOU_MAY_ONLY_RIDE_WYVERN_WHILE_RIDING_STRIDER);
					return null;
				}

				if(player.getInventory().getCountOf(1460) >= ride_wyvern_fee)
				{
					if(player.getMountLevel() < ride_wyvern_level)
					{
						content = HtmCache.getInstance().getHtm(player.getLang(), "default/wkeeper005.htm");
						content = content.replace("<?ride_wyvern_level?>", String.valueOf(ride_wyvern_level)).replace("<?ride_wyvern_fee?>", String.valueOf(ride_wyvern_fee));
						return content;
					}
					else
					{
						if(player.mount(12621, 0, true))
						{
							player.destroyItemByItemId(ProcessType.NPC, 1460, 25, npc, true);
							player.addSkill(SkillTable.FrequentSkill.WYVERN_BREATH.getSkill());
							return "wkeeper004.htm";
						}
					}
				}
				else
				{
					if(getManagerType(npc) == ManagerType.CASTLE)
					{
						content = HtmCache.getInstance().getHtm(player.getLang(), "default/wkeeper006.htm");
						content = content.replace("<?ride_wyvern_fee?>", String.valueOf(ride_wyvern_fee));
						return content;
					}
					else
					{
						content = HtmCache.getInstance().getHtm(player.getLang(), "default/wkeeper006_Agit.htm");
						content = content.replace("<?ride_wyvern_fee?>", String.valueOf(ride_wyvern_fee));
						return content;
					}
				}
				break;
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(npc.isMyLord(player, true))
		{
			return getManagerType(npc) == ManagerType.CASTLE ? "wkeeper002.htm" : "wkeeper002_Agit.htm";
		}
		else
		{
			String content;
			content = getManagerType(npc) == ManagerType.CASTLE ? HtmCache.getInstance().getHtm(player.getLang(), "default/wkeeper001.htm") : HtmCache.getInstance().getHtm(player.getLang(), "default/wkeeper001_Agit.htm");
			content = content.replace("<?ride_wyvern_level?>", String.valueOf(ride_wyvern_level)).replace("<?ride_wyvern_fee?>", String.valueOf(ride_wyvern_fee));
			return content;
		}
	}

	/***
	 * @param npc меннеджер виверн
	 * @return {@code true} если в данный момент территория осаждается
	 */
	public boolean isInSiege(L2Npc npc)
	{
		if(npc.getConquerableHall() != null)
		{
			return npc.getConquerableHall().isInSiege();
		}
		if(npc.getFort() != null)
		{
			return npc.getFort().getSiege().isInProgress();
		}
		if(npc.getCastle() != null)
		{
			return npc.getCastle().getSiege().isInProgress();
		}
		return false;
	}

	/***
	 * @param npc проверяемый НПЦ
	 * @return {@link ManagerType} тип НПЦ
	 */
	private ManagerType getManagerType(L2Npc npc)
	{
		if(npc.getConquerableHall() != null)
		{
			return ManagerType.CLANHALL;
		}
		if(npc.getFort() != null)
		{
			return ManagerType.FORTRESS;
		}
		if(npc.getCastle() != null)
		{
			return ManagerType.CASTLE;
		}
		return null;
	}

	enum ManagerType
	{
		CASTLE,
		CLANHALL,
		FORTRESS
	}
}