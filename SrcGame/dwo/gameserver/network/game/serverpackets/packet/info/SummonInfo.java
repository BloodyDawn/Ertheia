package dwo.gameserver.network.game.serverpackets.packet.info;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.masktypes.NpcInfoType;
import dwo.gameserver.network.game.serverpackets.AbstractMaskPacket;
import javolution.util.FastSet;

/**
 * User: GenCloud
 * Date: 21.01.2015
 * Team: La2Era Team
 */
public class SummonInfo extends AbstractMaskPacket<NpcInfoType>
{
    private final L2Summon _summon;
    private final L2PcInstance _attacker;
    private final int _val;
    private final byte[] _masks = new byte[]
            {
                    (byte) 0x00,
                    (byte) 0x0C,
                    (byte) 0x0C,
                    (byte) 0x00,
                    (byte) 0x00
            };

    private int _initSize = 0;
    private int _blockSize = 0;

    private int _clanCrest = 0;
    private int _clanLargeCrest = 0;
    private int _allyCrest = 0;
    private int _allyId = 0;
    private int _clanId = 0;
    private int _statusMask = 0;
    private final String _title;
    private final FastSet<Integer> _abnormalVisualEffects;

    public SummonInfo(L2Summon summon, L2PcInstance attacker, int val)
    {
        _summon = summon;
        _attacker = attacker;
        _title = (summon.getOwner() != null) && summon.getOwner().isOnline() ? summon.getOwner().getName() : "";
        _val = val;
        _abnormalVisualEffects = summon.getAbnormalEffects();

        if (summon.getTemplate().getIdTemplate() != summon.getTemplate().getNpcId())
        {
            _masks[2] |= 0x10;
            addComponentType(NpcInfoType.NAME);
        }

        addComponentType(NpcInfoType.ATTACKABLE, NpcInfoType.UNKNOWN1, NpcInfoType.TITLE, NpcInfoType.ID, NpcInfoType.POSITION, NpcInfoType.ALIVE, NpcInfoType.RUNNING);

        if (summon.getHeading() > 0)
        {
            addComponentType(NpcInfoType.HEADING);
        }

        if ((summon.getStat().getPAtkSpd() > 0) || (summon.getStat().getMAtkSpd() > 0))
        {
            addComponentType(NpcInfoType.ATK_CAST_SPEED);
        }

        if (summon.getRunSpeed() > 0)
        {
            addComponentType(NpcInfoType.SPEED_MULTIPLIER);
        }

        if ((summon.getWeapon() > 0) || (summon.getArmor() > 0))
        {
            addComponentType(NpcInfoType.EQUIPPED);
        }

        if (summon.getOwner().getTeam() != 0)
        {
            addComponentType(NpcInfoType.TEAM);
        }

        if (summon.isInsideZone(L2Character.ZONE_WATER) || summon.isFlying())
        {
            addComponentType(NpcInfoType.SWIM_OR_FLY);
        }

        if (summon.isFlying())
        {
            addComponentType(NpcInfoType.FLYING);
        }

        if (summon.getMaxHp() > 0)
        {
            addComponentType(NpcInfoType.MAX_HP);
        }

        if (summon.getMaxMp() > 0)
        {
            addComponentType(NpcInfoType.MAX_MP);
        }

        if (summon.getCurrentHp() <= summon.getMaxHp())
        {
            addComponentType(NpcInfoType.CURRENT_HP);
        }

        if (summon.getCurrentMp() <= summon.getMaxMp())
        {
            addComponentType(NpcInfoType.CURRENT_MP);
        }

        if (!_abnormalVisualEffects.isEmpty())
        {
            addComponentType(NpcInfoType.ABNORMALS);
        }

        if (summon.getTemplate().getEnchantEffect() > 0)
        {
            addComponentType(NpcInfoType.ENCHANT);
        }

        if (summon.isTransformed())
        {
            addComponentType(NpcInfoType.TRANSFORMATION);
        }

        if (summon.getOwner().getClan() != null)
        {
            _clanId = summon.getOwner().getClanId();
            _clanCrest = summon.getOwner().getClanCrestId();
            _clanLargeCrest = summon.getOwner().getClanCrestLargeId();
            _allyCrest = summon.getOwner().getAllyId();
            _allyId = summon.getOwner().getAllyCrestId();

            addComponentType(NpcInfoType.CLAN);
        }

        addComponentType(NpcInfoType.UNKNOWN8);

        if (summon.isInCombat())
        {
            _statusMask |= 0x01;
        }
        if (summon.isDead())
        {
            _statusMask |= 0x02;
        }
        if (summon.isTargetable())
        {
            _statusMask |= 0x04;
        }

        _statusMask |= 0x08;

        if (_statusMask != 0)
        {
            addComponentType(NpcInfoType.VISUAL_STATE);
        }
    }

    @Override
    protected byte[] getMasks()
    {
        return _masks;
    }

    @Override
    protected void onNewMaskAdded(NpcInfoType component)
    {
        calcBlockSize(_summon, component);
    }

    private void calcBlockSize(L2Summon summon, NpcInfoType type)
    {
        switch (type)
        {
            case ATTACKABLE:
            case UNKNOWN1:
            {
                _initSize += type.getBlockLength();
                break;
            }
            case TITLE:
            {
                _initSize += type.getBlockLength() + (_title.length() * 2);
                break;
            }
            case NAME:
            {
                _blockSize += type.getBlockLength() + (summon.getName().length() * 2);
                break;
            }
            default:
            {
                _blockSize += type.getBlockLength();
                break;
            }
        }
    }

    @Override
    protected void writeImpl()
    {
        writeC(0x8B);
        writeD(_summon.getObjectId());
        writeC(_val); // 0=teleported 1=default 2=summoned
        writeH(37);
        writeB(_masks);

        writeC(_initSize);

        if (containsMask(NpcInfoType.ATTACKABLE))
        {
            writeC(_summon.isAutoAttackable(_attacker) ? 0x01 : 0x00);
        }
        if (containsMask(NpcInfoType.UNKNOWN1))
        {
            writeD(0x00); // unknown
        }
        if (containsMask(NpcInfoType.TITLE))
        {
            writeS(_title);
        }

        writeH(_blockSize);
        if (containsMask(NpcInfoType.ID))
        {
            writeD(_summon.getTemplate().getIdTemplate() + 1000000);
        }
        if (containsMask(NpcInfoType.POSITION))
        {
            writeD(_summon.getX());
            writeD(_summon.getY());
            writeD(_summon.getZ());
        }
        if (containsMask(NpcInfoType.HEADING))
        {
            writeD(_summon.getHeading());
        }
        if (containsMask(NpcInfoType.UNKNOWN2))
        {
            writeD(0x00); // Unknown
        }
        if (containsMask(NpcInfoType.ATK_CAST_SPEED))
        {
            writeD(_summon.getPAtkSpd());
            writeD(_summon.getMAtkSpd());
        }
        if (containsMask(NpcInfoType.SPEED_MULTIPLIER))
        {
            _buf.putFloat(_summon.getStat().getMovementSpeedMultiplier());
            _buf.putFloat(_summon.getStat().getAttackSpeedMultiplier());
        }
        if (containsMask(NpcInfoType.EQUIPPED))
        {
            writeD(_summon.getWeapon());
            writeD(_summon.getArmor()); // Armor id?
            writeD(0x00);
        }
        if (containsMask(NpcInfoType.ALIVE))
        {
            writeC(_summon.isDead() ? 0x00 : 0x01);
        }
        if (containsMask(NpcInfoType.RUNNING))
        {
            writeC(_summon.isRunning() ? 0x01 : 0x00);
        }
        if (containsMask(NpcInfoType.SWIM_OR_FLY))
        {
            writeC(_summon.isInsideZone(L2Character.ZONE_WATER) ? 0x01 : _summon.isFlying() ? 0x02 : 0x00);
        }
        if (containsMask(NpcInfoType.TEAM))
        {
            writeC(_summon.getOwner() != null ? _summon.getOwner().getTeam() : 0x00);
        }
        if (containsMask(NpcInfoType.ENCHANT))
        {
            writeD(_summon.getTemplate().getEnchantEffect());
        }
        if (containsMask(NpcInfoType.FLYING))
        {
            writeD(_summon.isFlying() ? 0x01 : 0x00);
        }
        if (containsMask(NpcInfoType.CLONE))
        {
            writeD(0x00);
        }
        if (containsMask(NpcInfoType.UNKNOWN8))
        {
            writeD(0x00);
        }
        if (containsMask(NpcInfoType.DISPLAY_EFFECT))
        {
            writeD(0x00);
        }
        if (containsMask(NpcInfoType.TRANSFORMATION))
        {
            writeD(_summon.isTransformed() ? 0x01 : 0x00);
        }
        if (containsMask(NpcInfoType.CURRENT_HP))
        {
            writeD((int) _summon.getCurrentHp());
        }
        if (containsMask(NpcInfoType.CURRENT_MP))
        {
            writeD((int) _summon.getCurrentMp());
        }
        if (containsMask(NpcInfoType.MAX_HP))
        {
            writeD(_summon.getMaxHp());
        }
        if (containsMask(NpcInfoType.MAX_MP))
        {
            writeD(_summon.getMaxMp());
        }
        if (containsMask(NpcInfoType.CLONE2))
        {
            writeC(0x00);
        }
        if (containsMask(NpcInfoType.OCTAVIS_RAID))
        {
            writeD(0x00);
            writeD(0x00);
        }
        if (containsMask(NpcInfoType.NAME))
        {
            writeS(_summon.getName());
        }
        if (containsMask(NpcInfoType.NAME_NPCSTRINGID))
        {
            writeD(-1);
        }
        if (containsMask(NpcInfoType.TITLE_NPCSTRINGID))
        {
            writeD(-1);
        }
        if (containsMask(NpcInfoType.PVP_FLAG))
        {
            writeC(_summon.getOwner() != null ? _summon.getOwner().getPvPFlagController().getStateValue() : 0);
        }
        if (containsMask(NpcInfoType.NAME_COLOR))
        {
            writeD(0x00);
        }
        if (containsMask(NpcInfoType.CLAN))
        {
            writeD(_clanId);
            writeD(_clanCrest);
            writeD(_clanLargeCrest);
            writeD(_allyId);
            writeD(_allyCrest);
        }

        if (containsMask(NpcInfoType.VISUAL_STATE))
        {
            writeC(_statusMask);
        }

        if (containsMask(NpcInfoType.ABNORMALS))
        {
            writeH(_abnormalVisualEffects.size());
            _abnormalVisualEffects.forEach(this::writeH);
        }
    }
}