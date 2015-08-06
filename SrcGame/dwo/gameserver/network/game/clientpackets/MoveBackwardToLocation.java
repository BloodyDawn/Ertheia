package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.StopMove;
import dwo.gameserver.util.Util;

public class MoveBackwardToLocation extends L2GameClientPacket
{
	// cdddddd
	private int _targetX;
	private int _targetY;
	private int _targetZ;
	private int _clientX;
	private int _clientY;
	private int _clientZ;
	private int _moveMovement;

	@Override
	protected void readImpl()
	{
		_targetX = readD();
		_targetY = readD();
		_targetZ = readD();
		_clientX = readD();
		_clientY = readD();
		_clientZ = readD();

		_moveMovement = _buf.remaining() >= 4 ? readD() : -2;
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null || activeChar.isAfraid())
		{
			return;
		}

		if(Config.PLAYER_MOVEMENT_BLOCK_TIME > 0 && activeChar.getNotMoveUntil() > System.currentTimeMillis())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_MOVE_WHILE_SPEAKING_TO_AN_NPC);
			activeChar.sendActionFailed();
			return;
		}

		if(_moveMovement == -2)
		{
			// Если не было прислано типа движения, то считаем за бота
			Util.handleIllegalPlayerAction(activeChar, "Warning!! Character " + activeChar.getName() + " was detected as a bot.", Config.DEFAULT_PUNISH);
		}
		else if(_moveMovement == 0 && !Config.GEODATA_ENABLED)
		{
			// Передвижение на стрелках без геодаты запрещено
			activeChar.sendActionFailed();
		}
		else
		{
			if(_targetX == _clientX && _targetY == _clientY && _targetZ == _clientZ)
			{
				activeChar.sendPacket(new StopMove(activeChar.getObjectId(), _clientX, _clientY, _clientZ, activeChar.getHeading()));
				return;
			}

			// Correcting targetZ from floor level to head level (?)
			// Client is giving floor level as targetZ but that floor level doesn't
			// match our current geodata and teleport coords as good as head level!
			// L2J uses floor, not head level as char coordinates. This is some
			// sort of incompatibility fix.
			// Validate position packets sends head level.
			_targetZ += activeChar.getTemplate().getCollisionHeight(activeChar);

			double dx = _targetX - activeChar.getX();
			double dy = _targetY - activeChar.getY();

			// Can't move if character is confused, or trying to move a huge distance
			if(activeChar.isOutOfControl() || dx * dx + dy * dy > 98010000) // 9900*9900
			{
				activeChar.sendActionFailed();
				return;
			}

			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(_targetX, _targetY, _targetZ));
			activeChar.updatePartyPosition(false);
		}
	}

	@Override
	public String getType()
	{
		return "[C] 01 MoveBackwardToLoc";
	}
}
