package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.vehicle.ShuttleManager;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2ShuttleInstance;
import dwo.gameserver.model.items.base.type.L2WeaponType;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.shuttle.ExMTLInSuttle;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.shuttle.ExStopMoveInShuttle;
import dwo.gameserver.util.geometry.Point3D;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 03.08.11
 * Time: 22:32
 * (Send)MoveToLocationInShuttle X:%d Y:%d Z:%d OriginX:%d OriginY:%d OriginZ:%d
 */

public class MoveToLocationInShuttle extends L2GameClientPacket
{
	private int _shuttleId;
	private int _targetX;
	private int _targetY;
	private int _targetZ;
	private int _originX;
	private int _originY;
	private int _originZ;

	@Override
	protected void readImpl()
	{
		_shuttleId = readD();
		_targetX = readD();
		_targetY = readD();
		_targetZ = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(_targetX == _originX && _targetY == _originY && _targetZ == _originZ)
		{
			activeChar.sendPacket(new ExStopMoveInShuttle(activeChar, _shuttleId));
			return;
		}

		if(activeChar.isAttackingNow() && activeChar.getActiveWeaponItem() != null && activeChar.getActiveWeaponItem().getItemType() == L2WeaponType.BOW)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isSitting() || activeChar.isMovementDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!activeChar.isInShuttle())
		{
			activeChar.sendActionFailed();
			return;
		}

		L2ShuttleInstance shuttle;
		if(activeChar.isInShuttle())
		{
			shuttle = activeChar.getShuttle();
			if(shuttle.getId() != _shuttleId)
			{
				activeChar.sendActionFailed();
				return;
			}
		}
		else
		{
			shuttle = ShuttleManager.getInstance().getShuttle(_shuttleId);
			if(shuttle == null || !shuttle.isInsideRadius(activeChar, 300, true, false))
			{
				activeChar.sendActionFailed();
				return;
			}
			activeChar.setVehicle(shuttle);
		}
		Point3D pos = new Point3D(_targetX, _targetY, _targetZ);
		Point3D originPos = new Point3D(_originX, _originY, _originZ);
		activeChar.setInVehiclePosition(pos);
		activeChar.broadcastPacket(new ExMTLInSuttle(activeChar, pos, originPos));
		activeChar.getLocationController().setZ(shuttle.getZ());
		if(!activeChar.getPets().isEmpty())
		{
			for(L2Summon summon : activeChar.getPets())
			{
				summon.getLocationController().setZ(activeChar.getZ());
				summon.broadcastPacket(new ExMTLInSuttle(activeChar, summon, pos, originPos));
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:81 MoveToLocationInShuttle";
	}
}
