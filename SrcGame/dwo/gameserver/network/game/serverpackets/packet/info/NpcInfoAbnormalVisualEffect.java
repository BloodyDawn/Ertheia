package dwo.gameserver.network.game.serverpackets.packet.info;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.Set;

/**
 * User: GenCloud
 * Date: 20.01.2015
 * Team: La2Era Team
 */
public class NpcInfoAbnormalVisualEffect extends L2GameServerPacket
{
  private final L2Npc _npc;

  public NpcInfoAbnormalVisualEffect( L2Npc npc )
  {
    _npc = npc;
  }

  @Override
  protected void writeImpl()
  {
    writeD( _npc.getObjectId() );
    if( _npc instanceof L2MonsterInstance )
      writeD( ((L2MonsterInstance) _npc).getTransformationId() );
    else
      writeD( 0x00 );

    Set<Integer> _abnormals = _npc.getAbnormalEffects();
    writeD( _abnormals.size() );
    _abnormals.forEach( this::writeH );
  }
}
