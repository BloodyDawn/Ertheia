/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.network.game.serverpackets.packet.party;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.masktypes.PartySmallWindowUpdateType;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class PartySmallWindowUpdate extends L2GameServerPacket
{
  private L2PcInstance _member;
  private int _flags = 0;

  public PartySmallWindowUpdate( L2PcInstance member )
  {
    this( member, true );
  }

  public PartySmallWindowUpdate( L2PcInstance member, boolean addAllFlags )
  {
    _member = member;
    if( addAllFlags )
    {
      for( PartySmallWindowUpdateType type : PartySmallWindowUpdateType.values() )
      {
        addUpdateType( type );
      }
    }
  }

  public void addUpdateType( PartySmallWindowUpdateType type )
  {
    _flags |= type.getMask();
  }

  @Override
  protected void writeImpl()
  {
    writeD(_member.getObjectId());
    writeH(_flags);
    if (containsMask(_flags, PartySmallWindowUpdateType.CURRENT_CP))
    {
      writeD((int) _member.getCurrentCp()); // c4
    }
    if (containsMask(_flags, PartySmallWindowUpdateType.MAX_CP))
    {
      writeD(_member.getMaxCp()); // c4
    }
    if (containsMask(_flags, PartySmallWindowUpdateType.CURRENT_HP))
    {
      writeD((int) _member.getCurrentHp());
    }
    if (containsMask(_flags, PartySmallWindowUpdateType.MAX_HP))
    {
      writeD(_member.getMaxHp());
    }
    if (containsMask(_flags, PartySmallWindowUpdateType.CURRENT_MP))
    {
      writeD((int) _member.getCurrentMp());
    }
    if (containsMask(_flags, PartySmallWindowUpdateType.MAX_MP))
    {
      writeD(_member.getMaxMp());
    }
    if (containsMask(_flags, PartySmallWindowUpdateType.LEVEL))
    {
      writeC(_member.getLevel());
    }
    if (containsMask(_flags, PartySmallWindowUpdateType.CLASS_ID))
    {
      writeH(_member.getClassId().getId());
    }
    if (containsMask(_flags, PartySmallWindowUpdateType.VITALITY_POINTS))
    {
      writeD(_member.getVitalityDataForCurrentClassIndex().getVitalityPoints());
    }
  }
}