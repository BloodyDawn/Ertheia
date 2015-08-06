package dwo.gameserver.handler.items;

import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.CrystalGrade;
import dwo.gameserver.model.items.base.type.L2WeaponType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.util.Broadcast;

public class FishShots implements IItemHandler
{
	private static final int[] SKILL_IDS = {
		2181, 2182, 2183, 2184, 2185, 2186
	};

	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if(!(playable instanceof L2PcInstance))
		{
			return false;
		}

		L2PcInstance activeChar = (L2PcInstance) playable;
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		L2Weapon weaponItem = activeChar.getActiveWeaponItem();

		if(weaponInst == null || weaponItem.getItemType() != L2WeaponType.FISHINGROD)
		{
			return false;
		}

		if(weaponInst.getChargedFishshot())
		// spirit shot is already active
		{
			return false;
		}

		int FishshotId = item.getItemId();
		CrystalGrade grade = weaponItem.getCrystalType();
		long count = item.getCount();

		if(grade == CrystalGrade.NONE && FishshotId != 6535 || grade == CrystalGrade.D && FishshotId != 6536 || grade == CrystalGrade.C && FishshotId != 6537 || grade == CrystalGrade.B && FishshotId != 6538 || grade == CrystalGrade.A && FishshotId != 6539 || FishshotId != 6540 && grade == CrystalGrade.S)
		{
			//1479 - This fishing shot is not fit for the fishing pole crystal.
			activeChar.sendPacket(SystemMessageId.WRONG_FISHINGSHOT_GRADE);
			return false;
		}

		if(count < 1)
		{
			return false;
		}

		weaponInst.setChargedFishshot(true);
		activeChar.destroyItemWithoutTrace(item.getObjectId(), 1, null, false);
		L2Object oldTarget = activeChar.getTarget();
		activeChar.setTarget(activeChar);

		Broadcast.toSelfAndKnownPlayers(activeChar, new MagicSkillUse(activeChar, SKILL_IDS[grade.ordinal()], 1, 0, 0));
		activeChar.setTarget(oldTarget);
		return true;
	}
}
