package dwo.gameserver.network.game.serverpackets.packet.info;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.effects.AbnormalEffect;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastSet;

/**
 * User: GenCloud
 * Date: 17.01.2015
 * Team: DWO
 */
public class ExUserInfoAbnormalVisualEffect extends L2GameServerPacket
{
    private L2PcInstance _activeChar;
    private FastSet<Integer> _abnormals;

    public ExUserInfoAbnormalVisualEffect(L2PcInstance cha)
    {
        _activeChar = cha;
        _abnormals = cha.getAbnormalEffects();
    }

    @Override
    protected void writeImpl()
    {
        writeD(_activeChar.getObjectId());
        writeD(_activeChar.getTransformationId());
        if(!_abnormals.isEmpty())
        {
            writeD(_abnormals.size());
            _abnormals.forEach(this::writeH);
        }
        else
        {
            writeD(AbnormalEffect.NULL.getMask());
        }
    }
}