package dwo.gameserver.model.player.jump;

import dwo.gameserver.datatables.xml.CharJumpRoutesTable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.JumpHolder;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExFlyMove;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExFlyMoveBroadcast;
import dwo.gameserver.util.Broadcast;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 21.06.11
 * Time: 13:39
 * TODO: JavaDocs me! :)
 */

public class L2Jump
{
	private static final Logger _log = LogManager.getLogger(L2Jump.class);
	protected final L2Character _actor;
	private List<L2JumpNode> _route;
	private List<JumpHolder> _jumpRoutes;
	private int number;
	private int destinationX;
	private int destinationY;
	private int destinationZ;

	public L2Jump(L2Character cha)
	{
		_actor = cha;
	}

	public void processJump(L2PcInstance player, int jumpPointId)
	{
		_route = CharJumpRoutesTable.getInstance().getJumpId(player.getJumpId());
		if(_route != null)
		{
			// Конец пути обозначен типом прыжка с ID = -1
			if(jumpPointId == -1)
			{
				player.setJumping(false);
				player.enableAllSkills();
			}
			else
			{
				try
				{
					int charObjectId = player.getObjectId();
					L2JumpType jumpType = _route.get(jumpPointId).getType();
					_jumpRoutes = _route.get(jumpPointId).getJump();
					if(_jumpRoutes.size() < 2)
					{
						for(JumpHolder up : _jumpRoutes)
						{
							destinationX = up.getLoc().getX();
							destinationY = up.getLoc().getY();
							destinationZ = up.getLoc().getZ();
							number = up.getNum();
						}
					}

					player.sendPacket(new ExFlyMove(charObjectId, jumpType, player.getJumpId(), _jumpRoutes));
					if(_jumpRoutes.size() < 2)
					{
						// TODO нужно задать АИ перемещения
						player.setXYZ(destinationX, destinationY, destinationZ);
						ExFlyMoveBroadcast flyMovePacket = new ExFlyMoveBroadcast(player, jumpType, destinationX, destinationY, destinationZ, player.getJumpId(), number);
						Broadcast.toKnownPlayersInRadius(_actor, flyMovePacket, 8000);
					}
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, "processJump JumpId:" + player.getJumpId() + " number: " + number, e);
				}
			}
		}
		else
		{
			_log.log(Level.WARN, getClass().getSimpleName() + ": Missing jump route point data! L2Jump: Character JumpID = " + player.getJumpId() + " Process point ID = " + jumpPointId);
		}
	}
}