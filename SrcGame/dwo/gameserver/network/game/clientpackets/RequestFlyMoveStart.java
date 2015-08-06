package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.jump.L2Jump;
import dwo.gameserver.model.world.zone.type.L2JumpZone;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import org.apache.log4j.Level;

public class RequestFlyMoveStart extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();

		if(player == null)
		{
			return;
		}

		if(player.hasBadReputation())
		{
			player.getActingPlayer().sendPacket(SystemMessage.getSystemMessage(3654));
			return;
		}

		L2JumpZone jumpZone = ZoneManager.getInstance().getZone(player, L2JumpZone.class);
		if(jumpZone != null)
		{
			player.setJumpId(jumpZone.getJumpId());
			player.setJumping(true);
			player.disableAllSkills();
			L2Jump jump = new L2Jump(player);
			jump.processJump(player, 0);
		}
		else
		{
			_log.log(Level.WARN, "Player " + player.getName() + " sending wrong RequestFlyMoveStart packet!");
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:B4 RequestFlyMoveStart";
	}
}
