package dwo.gameserver.network.game.serverpackets.packet.divide;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * User: GenCloud
 * Date: 19.01.2015
 * Team: La2Era Team
 */
public class ExDivideAdenaDone extends L2GameServerPacket
{
    private final long _adenaStartSum;
    private final long _adenaFor1player;
    private final int _isPartyLeader;
    private final int _isCommandChannelLeader;
    private final int _playersCount;
    private final L2PcInstance _pL;

    public ExDivideAdenaDone(L2PcInstance player , long adenaBank)
    {
        _adenaStartSum = adenaBank;
        _isPartyLeader = player.isPartyLeader() ? 1:0;
        _isCommandChannelLeader = player.isCommandChannelLeader() ? 1:0;
        if(!player.getParty().isInCommandChannel())
        {
            _playersCount = player.getParty().getMemberCount();
            _pL = player.getParty().getCommandChannel().getLeader();
        }
        else
        {
            _playersCount = player.getParty().getCommandChannel().getMemberCount();
            _pL = player.getParty().getLeader();
        }
        _adenaFor1player = adenaBank / _playersCount;

    }

    @Override
    protected void writeImpl()
    {
        writeC(_isPartyLeader); //ok
        writeC(_isCommandChannelLeader); // hz or cc active hz TODO unk
        writeD(_playersCount); //ok
        writeQ(_adenaFor1player); // по скольку получил каждый игрок
        writeQ(_adenaStartSum); //start sum
        writeS(_pL.getName()); //Name Leader
    }
}
