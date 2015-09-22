package dwo.gameserver.network.game.serverpackets.packet.info;

import dwo.config.Config;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.instancemanager.TownManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Trap;
import dwo.gameserver.model.actor.instance.L2GuardInstance;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.masktypes.NpcInfoType;
import dwo.gameserver.network.game.serverpackets.AbstractMaskPacket;

/**
 * La2Era Team
 * User: GenCloud
 * Date: 02.10.12
 * Time: 12:10
 */
public class NpcInfo extends AbstractMaskPacket<NpcInfoType>
{
    private L2Npc _npc;
    private L2Trap _trap;
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

    public NpcInfo(L2Trap trap)
    {
        _trap = trap;
        common(_trap);
    }

    public NpcInfo(L2Npc npc)
    {
        _npc = npc;

//        if (npc.getTemplate().getIdTemplate() != npc.getTemplate().getNpcId())
//        {
//            _masks[2] |= 0x10;
//        }

        addComponentType(NpcInfoType.ATTACKABLE, NpcInfoType.UNKNOWN1, NpcInfoType.TITLE, NpcInfoType.ID, NpcInfoType.POSITION, NpcInfoType.ALIVE, NpcInfoType.RUNNING);

        if (npc.getHeading() > 0)
        {
            addComponentType(NpcInfoType.HEADING);
        }

        if ((npc.getStat().getPAtkSpd() > 0) || (npc.getStat().getMAtkSpd() > 0))
        {
            addComponentType(NpcInfoType.ATK_CAST_SPEED);
        }

        if (npc.getRunSpeed() > 0)
        {
            addComponentType(NpcInfoType.SPEED_MULTIPLIER);
        }

        if ((npc.getLeftHandItem() > 0) || (npc.getRightHandItem() > 0))
        {
            addComponentType(NpcInfoType.EQUIPPED);
        }

        if (npc.getChampion() > 0)
        {
            addComponentType(NpcInfoType.TEAM);
        }

        if (npc.getDisplayEffect() > 0)
        {
            addComponentType(NpcInfoType.DISPLAY_EFFECT);
        }

        if (npc.isInsideZone(L2Character.ZONE_WATER) || npc.isFlying())
        {
            addComponentType(NpcInfoType.SWIM_OR_FLY);
        }

        if (npc.isFlying())
        {
            addComponentType(NpcInfoType.FLYING);
        }

        if (npc.isClone())
        {
            addComponentType(NpcInfoType.CLONE);
            addComponentType(NpcInfoType.CLONE2);
        }

        if (npc.getMaxHp() > 0)
        {
            addComponentType(NpcInfoType.MAX_HP);
        }

        if (npc.getMaxMp() > 0)
        {
            addComponentType(NpcInfoType.MAX_MP);
        }

        if (npc.getCurrentHp() <= npc.getMaxHp())
        {
            addComponentType(NpcInfoType.CURRENT_HP);
        }

        if (npc.getCurrentMp() <= npc.getMaxMp())
        {
            addComponentType(NpcInfoType.CURRENT_MP);
        }

        if (npc.getTemplate().getIdTemplate() != npc.getTemplate().getNpcId())
        {
            addComponentType(NpcInfoType.NAME);
        }

        if (npc.getEnchantEffect() > 0)
        {
            addComponentType(NpcInfoType.ENCHANT);
        }

        if (npc.isMonster())
        {
            addComponentType(NpcInfoType.TRANSFORMATION);
        }

        if (npc.isOctavisRaid())
        {
            addComponentType(NpcInfoType.OCTAVIS_RAID);
        }

        if (npc.isInsideZone(L2Character.ZONE_TOWN) && (npc.getCastle() != null) && (Config.SHOW_CREST_WITHOUT_QUEST || npc.getCastle().getShowNpcCrest()) && (npc.getCastle().getOwnerId() != 0))
        {
            int townId = TownManager.getTown(npc.getX(), npc.getY(), npc.getZ()).getTownId();
            if ((townId != 33) && (townId != 22))
            {
                L2Clan clan = ClanTable.getInstance().getClan(npc.getCastle().getOwnerId());
                _clanId = clan.getClanId();
                _clanCrest = clan.getCrestId();
                _clanLargeCrest = clan.getCrestLargeId();
                _allyCrest = clan.getAllyCrestId();
                _allyId = clan.getAllyId();

                addComponentType(NpcInfoType.CLAN);
            }
        }

        addComponentType(NpcInfoType.UNKNOWN8);

        if (!npc.isTargetable() && !npc.isShowName())
        {
            _statusMask |= 0x00;
        }
        else if (npc.isInCombat() && !npc.isTargetable() && !npc.isShowName())
        {
            _statusMask |= 0x01;
        }
        else if (npc.isAlikeDead() && !npc.isTargetable() && !npc.isShowName())
        {
            _statusMask |= 0x02;
        }
        else if (npc.isDead() && !npc.isTargetable() && !npc.isShowName())
        {
            _statusMask |= 0x03;
        }
        else if (npc.isTargetable() && !npc.isShowName())
        {
            _statusMask |= 0x04;
        }
        else if (npc.isInCombat() && npc.isTargetable() && !npc.isShowName())
        {
            _statusMask |= 0x05;
        }
        else if (npc.isAlikeDead() && npc.isTargetable() && !npc.isShowName())
        {
            _statusMask |= 0x06;
        }
        else if (npc.isDead() && npc.isTargetable() && !npc.isShowName())
        {
            _statusMask |= 0x07;
        }
        else if (npc.isShowName() && !npc.isTargetable())
        {
            _statusMask |= 0x08;
        }
        else if (npc.isInCombat() && !npc.isTargetable() && npc.isShowName())
        {
            _statusMask |= 0x09;
        }
        else if (npc.isAlikeDead() && !npc.isTargetable() && npc.isShowName())
        {
            _statusMask |= 0x0A;
        }
        else if (npc.isDead() && !npc.isTargetable() && npc.isShowName())
        {
            _statusMask |= 0x0B;
        }
        else if (npc.isInCombat() && npc.isShowName() && npc.isTargetable())
        {
            _statusMask |= 0x0D;
        }
        else if (npc.isAlikeDead() && npc.isTargetable() && npc.isShowName())
        {
            _statusMask |= 0x0E;
        }
        else if (npc.isDead() && npc.isTargetable() && npc.isShowName())
        {
            _statusMask |= 0x0F;
        }
        else
        {
            _statusMask |= 0x0C;
        }

        if (_statusMask != 0)
        {
            addComponentType(NpcInfoType.VISUAL_STATE);
        }
    }

    public void common(L2Trap trap)
    {
        if (trap.getTemplate().getIdTemplate() != trap.getTemplate().getNpcId())
        {
            _masks[2] |= 0x10;
        }

        addComponentType(NpcInfoType.ATTACKABLE, NpcInfoType.UNKNOWN1, NpcInfoType.ID, NpcInfoType.POSITION, NpcInfoType.ALIVE, NpcInfoType.RUNNING);

        if (trap.getHeading() > 0)
        {
            addComponentType(NpcInfoType.HEADING);
        }

        if ((trap.getStat().getPAtkSpd() > 0) || (trap.getStat().getMAtkSpd() > 0))
        {
            addComponentType(NpcInfoType.ATK_CAST_SPEED);
        }

        if (trap.getRunSpeed() > 0)
        {
            addComponentType(NpcInfoType.SPEED_MULTIPLIER);
        }

        if (trap.getChampion() > 0)
        {
            addComponentType(NpcInfoType.TEAM);
        }

        if (trap.isInsideZone(L2Character.ZONE_WATER) || trap.isFlying())
        {
            addComponentType(NpcInfoType.SWIM_OR_FLY);
        }

        if (trap.isFlying())
        {
            addComponentType(NpcInfoType.FLYING);
        }

        if (trap.isClone())
        {
            addComponentType(NpcInfoType.CLONE);
            addComponentType(NpcInfoType.CLONE2);
        }

        if (trap.getMaxHp() > 0)
        {
            addComponentType(NpcInfoType.MAX_HP);
        }

        if (trap.getMaxMp() > 0)
        {
            addComponentType(NpcInfoType.MAX_MP);
        }

        if (trap.getCurrentHp() <= trap.getMaxHp())
        {
            addComponentType(NpcInfoType.CURRENT_HP);
        }

        if (trap.getCurrentMp() <= trap.getMaxMp())
        {
            addComponentType(NpcInfoType.CURRENT_MP);
        }

        if (trap.getTemplate().getIdTemplate() != trap.getTemplate().getNpcId())
        {
            addComponentType(NpcInfoType.NAME);
        }

        if (trap.isMonster())
        {
            addComponentType(NpcInfoType.TRANSFORMATION);
        }

        if (trap.isOctavisRaid())
        {
            addComponentType(NpcInfoType.OCTAVIS_RAID);
        }

        addComponentType(NpcInfoType.UNKNOWN8);

        /** TODO: Confirm me
         0x00 просто стоит без ника и титула нельзя взять в таргет
         0x01 в боевом режиме без ника и титула нельзя взять в таргет
         0x02 мертвы понарошку без ника и титула нельзя взять в таргет
         0x03 мертвы без ника и титула нельзя взять в таргет
         0x04 просто стоит без ника и титула можно взять в таргет
         0x05 в боевом режиме без ника и титула можно взять в таргет
         0x06 мертвы понарошку без ника и титула можно взять в таргет
         0x07 мертвы без ника и титула можно взять в таргет
         0x08 просто стоит, есть ник и титул, нельзя взятьв  таргет
         0x09 в боевом режиме с ником и титулом нельзя взять в таргет
         0x0A мертвы понарошку с ником и титулом нельзя взять в таргет
         0x0B мертвы с ником и титулом нельзя взять в таргет
         0x0C просто стоит с ником и титулом можно взять в  трагет
         0x0D в боевом режиме с ником и титулом можно взять в  трагет
         0x0E dead с ником и титулом можно взять в  трагет
         0x0F dead с ником и титулом можно взять в  трагет
         */
        if (trap.isInCombat())
        {
            _statusMask |= 0x01;
        }
        if (trap.isDead())
        {
            _statusMask |= 0x02;
        }

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
        calcBlockSize(_npc, component);
    }

    private void calcBlockSize(L2Npc npc, NpcInfoType type)
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
                _initSize += type.getBlockLength() + (npc.getTitle().length() * 2);
                break;
            }
            case NAME:
            {
                _blockSize += type.getBlockLength() + (npc.getName().length() * 2);
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
        if (_trap != null)
        {
            writeD(_trap.getObjectId());
            writeC(_trap.isShowSummonAnimation() ? 0x01 : 0x00);
            writeH(37); // 37 bits
            writeB(_masks);

            // Block 1
            writeC(_initSize);

            if (containsMask(NpcInfoType.ATTACKABLE))
            {
                writeC(_trap.isAttackable() ? 0x01 : 0x00);
            }
            if (containsMask(NpcInfoType.UNKNOWN1))
            {
                writeD(0x00); // unknown
            }
            if (containsMask(NpcInfoType.TITLE))
            {
                writeS(_trap.getTitle());
            }

            // Block 2
            writeH(_blockSize);
            if (containsMask(NpcInfoType.ID))
            {
                writeD(_trap.getTemplate().getIdTemplate() + 1000000);
            }
            if (containsMask(NpcInfoType.POSITION))
            {
                writeD(_trap.getX());
                writeD(_trap.getY());
                writeD(_trap.getZ());
            }
            if (containsMask(NpcInfoType.HEADING))
            {
                writeD(_trap.getHeading());
            }
            if (containsMask(NpcInfoType.UNKNOWN2))
            {
                writeD(0x00); // Unknown
            }
            if (containsMask(NpcInfoType.ATK_CAST_SPEED))
            {
                writeD(_trap.getPAtkSpd());
                writeD(_trap.getMAtkSpd());
            }
            if (containsMask(NpcInfoType.SPEED_MULTIPLIER))
            {
                _buf.putFloat(_trap.getStat().getMovementSpeedMultiplier());
                _buf.putFloat(_trap.getStat().getAttackSpeedMultiplier());
            }
            if (containsMask(NpcInfoType.ALIVE))
            {
                writeC(_trap.isDead() ? 0x00 : 0x01);
            }
            if (containsMask(NpcInfoType.RUNNING))
            {
                writeC(_trap.isRunning() ? 0x01 : 0x00);
            }
            if (containsMask(NpcInfoType.SWIM_OR_FLY))
            {
                writeC(_trap.isInsideZone(L2Character.ZONE_WATER) ? 0x01 : _trap.isFlying() ? 0x02 : 0x00);
            }
            if (containsMask(NpcInfoType.TEAM))
            {
                writeC(_trap.getChampion());
            }
            if (containsMask(NpcInfoType.FLYING))
            {
                writeD(_trap.isFlying() ? 0x01 : 0x00);
            }
            if (containsMask(NpcInfoType.UNKNOWN8))
            {
                writeD(0x00);
            }
            if (containsMask(NpcInfoType.TRANSFORMATION))
            {
                writeD(((L2MonsterInstance) _npc).getTransformationId());
            }
            if (containsMask(NpcInfoType.CURRENT_HP))
            {
                writeD((int) _trap.getCurrentHp());
            }
            if (containsMask(NpcInfoType.CURRENT_MP))
            {
                writeD((int) _trap.getCurrentMp());
            }
            if (containsMask(NpcInfoType.MAX_HP))
            {
                writeD(_trap.getMaxHp());
            }
            if (containsMask(NpcInfoType.MAX_MP))
            {
                writeD(_trap.getMaxMp());
            }
            if (containsMask(NpcInfoType.NAME))
            {
                writeS(_trap.getName());
            }
            if (containsMask(NpcInfoType.NAME_NPCSTRINGID))
            {
                writeD(-1); // NPCStringId for name
            }
            if (containsMask(NpcInfoType.TITLE_NPCSTRINGID))
            {
                writeD(-1); // NPCStringId for title
            }
            if (containsMask(NpcInfoType.NAME_COLOR))
            {
                writeD(0x00); // Name color
            }
            if (containsMask(NpcInfoType.VISUAL_STATE))
            {
                writeC(_statusMask);
            }
        }
        else
        {
            writeD(_npc.getObjectId());
            writeC(_npc.isShowSummonAnimation() ? 0x01 : 0x00);
            writeH(37); // 37 bits
            writeB(_masks);

            // Block 1
            writeC(_initSize);

            if (containsMask(NpcInfoType.ATTACKABLE))
            {
                writeC(_npc.isAttackable() && !(_npc instanceof L2GuardInstance) ? 0x01 : 0x00);
            }
            if (containsMask(NpcInfoType.UNKNOWN1))
            {
                writeD(0x00); // unknown
            }
            if (containsMask(NpcInfoType.TITLE))
            {
              writeS( _npc.getTitle() );
            }

            // Block 2
            writeH(_blockSize);
            if (containsMask(NpcInfoType.ID))
            {
                writeD(_npc.getTemplate().getIdTemplate() + 1000000);
            }
            if (containsMask(NpcInfoType.POSITION))
            {
                writeD(_npc.getX());
                writeD(_npc.getY());
                writeD(_npc.getZ());
            }
            if (containsMask(NpcInfoType.HEADING))
            {
                writeD(_npc.getHeading());
            }
            if (containsMask(NpcInfoType.UNKNOWN2))
            {
                writeD(0x00); // Unknown
            }
            if (containsMask(NpcInfoType.ATK_CAST_SPEED))
            {
                writeD(_npc.getPAtkSpd());
                writeD(_npc.getMAtkSpd());
            }
            if (containsMask(NpcInfoType.SPEED_MULTIPLIER))
            {
                _buf.putFloat(_npc.getStat().getMovementSpeedMultiplier());
                _buf.putFloat(_npc.getStat().getAttackSpeedMultiplier());
            }
            if (containsMask(NpcInfoType.EQUIPPED))
            {
                writeD(_npc.getRightHandItem());
                writeD(_npc.getArmorId());
                writeD(_npc.getLeftHandItem());
            }
            if (containsMask(NpcInfoType.ALIVE))
            {
                writeC(_npc.isDead() ? 0x00 : 0x01);
            }
            if (containsMask(NpcInfoType.RUNNING))
            {
                writeC(_npc.isRunning() ? 0x01 : 0x00);
            }
            if (containsMask(NpcInfoType.SWIM_OR_FLY))
            {
                writeC(_npc.isInsideZone(L2Character.ZONE_WATER) ? 0x01 : _npc.isFlying() ? 0x02 : 0x00);
            }
            if (containsMask(NpcInfoType.TEAM))
            {
                writeC(_trap.getChampion());
            }
            if (containsMask(NpcInfoType.ENCHANT))
            {
                writeD(_npc.getEnchantEffect());
            }
            if (containsMask(NpcInfoType.FLYING))
            {
                writeD(_npc.isFlying() ? 0x01 : 0x00);
            }
            if (containsMask(NpcInfoType.CLONE))
            {
                writeD(_npc.getOwner().getObjectId()); // Player ObjectId with Clone
            }
            if (containsMask(NpcInfoType.UNKNOWN8))
            {
                writeD(0x00);
            }
            if (containsMask(NpcInfoType.DISPLAY_EFFECT))
            {
                writeD(_npc.getDisplayEffect());
            }
            if (containsMask(NpcInfoType.TRANSFORMATION))
            {
                writeD(((L2MonsterInstance) _npc).getTransformationId());
            }
            if (containsMask(NpcInfoType.CURRENT_HP))
            {
                writeD((int) _npc.getCurrentHp());
            }
            if (containsMask(NpcInfoType.CURRENT_MP))
            {
                writeD((int) _npc.getCurrentMp());
            }
            if (containsMask(NpcInfoType.MAX_HP))
            {
                writeD(_npc.getMaxHp());
            }
            if (containsMask(NpcInfoType.MAX_MP))
            {
                writeD(_npc.getMaxMp());
            }
            if (containsMask(NpcInfoType.CLONE2))
            {
                writeC(_npc.isClone() ? 0x02 : 0x00);
            }
            if (containsMask(NpcInfoType.OCTAVIS_RAID))
            {
                writeD(_npc.isOctavisRaid() ? _npc.getObjectId() : 0x00); //привязка львов на октависе TODO
                writeD(_npc.isOctavisRaid() ? 500 : 0x00); //ренж до нее
            }
            if (containsMask(NpcInfoType.NAME))
            {
                writeS(_npc.getName());
            }
            if (containsMask(NpcInfoType.NAME_NPCSTRINGID))
            {
                writeD(-1); // NPCStringId for name
            }
            if (containsMask(NpcInfoType.TITLE_NPCSTRINGID))
            {
                writeD(-1); // NPCStringId for title
            }
            if (containsMask(NpcInfoType.PVP_FLAG))
            {
                writeC(_npc.getPvPFlagController().getStateValue()); // PVP flag
            }
            if (containsMask(NpcInfoType.NAME_COLOR))
            {
                writeD(0x00); // Name color
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
        }
    }
}