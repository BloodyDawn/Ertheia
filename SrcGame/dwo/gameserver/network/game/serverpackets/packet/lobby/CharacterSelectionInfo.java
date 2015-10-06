package dwo.gameserver.network.game.serverpackets.packet.lobby;

import dwo.config.Config;
import dwo.gameserver.datatables.sql.AccountShareDataTable;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.datatables.xml.ExperienceTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.CursedWeaponsManager;
import dwo.gameserver.model.items.itemcontainer.Inventory;
import dwo.gameserver.model.player.AccountShareData;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.game.components.CharSelectInfoPackage;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CharacterSelectionInfo extends L2GameServerPacket
{
    private static final int[] PAPERDOLL_ORDER_VISUAL_ID = new int[]
    {
            Inventory.PAPERDOLL_RHAND,
            Inventory.PAPERDOLL_LHAND,
            Inventory.PAPERDOLL_GLOVES,
            Inventory.PAPERDOLL_CHEST,
            Inventory.PAPERDOLL_LEGS,
            Inventory.PAPERDOLL_FEET,
            Inventory.PAPERDOLL_HAIR,
            Inventory.PAPERDOLL_HAIR2,
    };

    private String _loginName;
    private int _sessionId;
    private int _activeId;
    private CharSelectInfoPackage[] _characterPackages;

    /**
     * @param loginName имя аккаунта
     * @param sessionId ID сессии
     */
    public CharacterSelectionInfo(String loginName, int sessionId)
    {
        _sessionId = sessionId;
        _loginName = loginName;
        _characterPackages = loadCharacterSelectInfo(_loginName);
        _activeId = -1;
    }

    public CharacterSelectionInfo(String loginName, int sessionId, int activeId)
    {
        _sessionId = sessionId;
        _loginName = loginName;
        _characterPackages = loadCharacterSelectInfo(_loginName);
        _activeId = activeId;
    }

    private static CharSelectInfoPackage[] loadCharacterSelectInfo(String loginName)
    {
        CharSelectInfoPackage charInfopackage;
        List<CharSelectInfoPackage> characterList = new ArrayList<>();

        ThreadConnection con = null;
        FiltredPreparedStatement statement = null;
        ResultSet charList = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(Characters.SELECT_CHARACTERS);
            statement.setString(1, loginName);
            charList = statement.executeQuery();

            while(charList.next())// fills the package
            {
                charInfopackage = restoreChar(charList);
                if(charInfopackage != null)
                {
                    characterList.add(charInfopackage);
                }
            }
            return characterList.toArray(new CharSelectInfoPackage[characterList.size()]);
        }
        catch(Exception e)
        {
            _log.log(Level.ERROR, "Could not restore char info: " + e.getMessage(), e);
        }
        finally
        {
            DatabaseUtils.closeDatabaseCSR(con, statement, charList);
        }

        return new CharSelectInfoPackage[0];
    }

    private static void loadCharacterSubclassInfo(CharSelectInfoPackage charInfopackage, int ObjectId, int activeClassId)
    {
        ThreadConnection con = null;
        FiltredPreparedStatement statement = null;
        ResultSet charList = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(Characters.SELECT_CHARACTER_SUBCLASSES_BY_CHARID);
            statement.setInt(1, ObjectId);
            statement.setInt(2, activeClassId);
            charList = statement.executeQuery();

            if(charList.next())
            {
                charInfopackage.setExp(charList.getLong("exp"));
                charInfopackage.setSp(charList.getInt("sp"));
                charInfopackage.setLevel(charList.getInt("level"));
            }
        }
        catch(Exception e)
        {
            _log.log(Level.ERROR, "Could not restore char subclass info: " + e.getMessage(), e);
        }
        finally
        {
            DatabaseUtils.closeDatabaseCSR(con, statement, charList);
        }
    }

    private static CharSelectInfoPackage restoreChar(ResultSet chardata) throws Exception
    {
        int objectId = chardata.getInt("charId");
        String name = chardata.getString("char_name");

        // See if the char must be deleted
        long deletetime = chardata.getLong("deletetime");
        if(deletetime > 0)
        {
            if(System.currentTimeMillis() > deletetime)
            {
                L2Clan clan = ClanTable.getInstance().getClan(chardata.getInt("clanid"));
                if(clan != null)
                {
                    clan.removeClanMember(objectId, 0);
                }

                L2GameClient.deleteCharByObjId(objectId);
                return null;
            }
        }

        CharSelectInfoPackage charInfopackage = new CharSelectInfoPackage(objectId, name);
        charInfopackage.setLevel(chardata.getInt("level"));
        charInfopackage.setMaxHp(chardata.getInt("maxhp"));
        charInfopackage.setCurrentHp(chardata.getDouble("curhp"));
        charInfopackage.setMaxMp(chardata.getInt("maxmp"));
        charInfopackage.setCurrentMp(chardata.getDouble("curmp"));
        charInfopackage.setReputation(chardata.getInt("reputation"));
        charInfopackage.setPkKills(chardata.getInt("pkkills"));
        charInfopackage.setPvPKills(chardata.getInt("pvpkills"));

        // Allow custom styles to be displayed in char select window
        int face;
        int hairStyle;
        int hairColor;
        if((face = chardata.getInt("custom_face")) > 0)
        {
            charInfopackage.setFace(face);
        }
        else
        {
            charInfopackage.setFace(chardata.getInt("face"));
        }

        if((hairStyle = chardata.getInt("custom_hair_style")) > 0)
        {
            charInfopackage.setHairStyle(hairStyle);
        }
        else
        {
            charInfopackage.setHairStyle(chardata.getInt("hairstyle"));
        }

        if((hairColor = chardata.getInt("custom_hair_color")) > 0)
        {
            charInfopackage.setHairColor(hairColor);
        }
        else
        {
            charInfopackage.setHairColor(chardata.getInt("haircolor"));
        }

        charInfopackage.setSex(chardata.getInt("sex"));

        charInfopackage.setExp(chardata.getLong("exp"));
        charInfopackage.setSp(chardata.getInt("sp"));
        charInfopackage.setClanId(chardata.getInt("clanid"));

        charInfopackage.setRace(chardata.getInt("race"));

        int baseClassId = chardata.getInt("base_class");
        int activeClassId = chardata.getInt("classid");

        charInfopackage.setX(chardata.getInt("x"));
        charInfopackage.setY(chardata.getInt("y"));
        charInfopackage.setZ(chardata.getInt("z"));

        // if is in subclass, load subclass exp, sp, lvl info
        if(baseClassId != activeClassId)
        {
            loadCharacterSubclassInfo(charInfopackage, objectId, activeClassId);
        }

        charInfopackage.setClassId(activeClassId);

        // Get the augmentation id for equipped weapon
        int weaponObjId = charInfopackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND);
        if(weaponObjId < 1)
        {
            weaponObjId = charInfopackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND);
        }

        // Check Transformation
        int cursedWeaponId = CursedWeaponsManager.getInstance().checkOwnsWeaponId(objectId);
        if(cursedWeaponId > 0)
        {
            // cursed weapon transformations
            if(cursedWeaponId == 8190)
            {
                charInfopackage.setTransformId(301);
            }
            else if(cursedWeaponId == 8689)
            {
                charInfopackage.setTransformId(302);
            }
            else
            {
                charInfopackage.setTransformId(0);
            }
        }
        else if(chardata.getInt("transform_id") > 0)
        {
            charInfopackage.setTransformId(chardata.getInt("transform_id"));
        }
        else
        {
            charInfopackage.setTransformId(0);
        }

        if(weaponObjId > 0)
        {
            ThreadConnection con = null;
            FiltredPreparedStatement statement = null;
            ResultSet result = null;
            try
            {
                con = L2DatabaseFactory.getInstance().getConnection();
                statement = con.prepareStatement("SELECT augAttributes FROM item_attributes WHERE itemId=?");
                statement.setInt(1, weaponObjId);
                result = statement.executeQuery();
                if(result.next())
                {
                    int augment = result.getInt("augAttributes");
                    charInfopackage.setAugmentationId(augment == -1 ? 0 : augment);
                }
            }
            catch(Exception e)
            {
                _log.log(Level.ERROR, "Could not restore augmentation info: " + e.getMessage(), e);
            }
            finally
            {
                DatabaseUtils.closeDatabaseCSR(con, statement, result);
            }
        }

        /*
		   * Check if the base class is set to zero and alse doesn't match
		   * with the current active class, otherwise send the base class ID.
		   *
		   * This prevents templates created before base class was introduced
		   * from being displayed incorrectly.
		   */
        if(baseClassId == 0 && activeClassId > 0)
        {
            charInfopackage.setBaseClassId(activeClassId);
        }
        else
        {
            charInfopackage.setBaseClassId(baseClassId);
        }

        charInfopackage.setDeleteTimer(deletetime);
        charInfopackage.setLastAccess(chardata.getLong("lastAccess"));
        return charInfopackage;
    }

    public CharSelectInfoPackage[] getCharInfo()
    {
        return _characterPackages;
    }

    @Override
    protected void writeImpl()
    {
        int size = _characterPackages.length;

        writeD(size);

        writeD(Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT); // максимальное кол-во персонажей на аккаунте, 7 - предел клиента
        writeC(size == Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT ? 0x01 : 0x00); // Ertheia: приходит 0х00
        writeC(0x01); // play mode, if 1 can create only 2 char in regular lobby
        writeD(0x02); // if 1, korean client
        writeC(0x00); // if 1 suggest premium account

        long lastAccess = 0L;

        if(_activeId == -1)
        {
            for(int i = 0; i < size; i++)
            {
                if(lastAccess < _characterPackages[i].getLastAccess())
                {
                    lastAccess = _characterPackages[i].getLastAccess();
                    _activeId = i;
                }
            }
        }

        for(int i = 0; i < size; i++)
        {
            CharSelectInfoPackage charInfoPackage = _characterPackages[i];

            writeS(charInfoPackage.getName());
            writeD(charInfoPackage.getCharId());
            writeS(_loginName);
            writeD(_sessionId);
            writeD(charInfoPackage.getClanId());
            writeD(0x00); // ??

            writeD(charInfoPackage.getSex());
            writeD(charInfoPackage.getRace());

            writeD(charInfoPackage.getBaseClassId());

            writeD(0x01); // active ??

            writeD(charInfoPackage.getX()); // x
            writeD(charInfoPackage.getY()); // y
            writeD(charInfoPackage.getZ()); // z

            writeF(charInfoPackage.getCurrentHp()); // hp cur
            writeF(charInfoPackage.getCurrentMp()); // mp cur

            writeQ(charInfoPackage.getSp());
            writeQ(charInfoPackage.getExp());
            writeF((float) (charInfoPackage.getExp() - ExperienceTable.getInstance().getExpForLevel(charInfoPackage.getLevel())) / (ExperienceTable.getInstance().getExpForLevel(charInfoPackage.getLevel() + 1) - ExperienceTable.getInstance().getExpForLevel(charInfoPackage.getLevel()))); // High Five exp %
            writeD(charInfoPackage.getLevel());

            writeD(charInfoPackage.getReputation()); // Reputation
            writeD(charInfoPackage.getPkKills());
            writeD(charInfoPackage.getPvPKills());

            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);

            writeD(0x00);
            writeD(0x00);

            for(int slot : getPaperdollOrder())
            {
                writeD(charInfoPackage.getPaperdollItemId(slot));
            }

            for (int slot : getPaperdollOrderVisualId())
            {
                writeD(charInfoPackage.getPaperdollItemSkinByItemId(slot));
            }

            writeD(0x00); // ??
            writeD(0x00); // ??
            writeD(0x00); // ??
            writeH(0x00); // ??

            writeD(charInfoPackage.getHairStyle());
            writeD(charInfoPackage.getHairColor());
            writeD(charInfoPackage.getFace());

            // GOD
            writeF(charInfoPackage.getMaxHp()); // hp max
            writeF(charInfoPackage.getMaxMp()); // mp max

            long deleteTime = charInfoPackage.getDeleteTimer();
            int deletedays = 0;
            if(deleteTime > 0)
            {
                deletedays = (int) ((deleteTime - System.currentTimeMillis()) / 1000);
            }
            writeD(deletedays); // days left before

            writeD(charInfoPackage.getClassId());
            writeD(i == _activeId ? 0x01 : 0x00);

            writeC(charInfoPackage.getEnchantEffect() > 127 ? 127 : charInfoPackage.getEnchantEffect());
            writeD(charInfoPackage.getAugmentationId());

            // Freya by Vistall:
            writeD(0x00); // npdid - 16024    Tame Tiny Baby Kookaburra        A9E89C
            writeD(0x00); // level
            writeD(0x00); // ?
            writeD(0x00); // food? - 1200
            writeD(0x00);

            writeF(0x00); // max Hp
            writeF(0x00); // cur Hp

            AccountShareData data = AccountShareDataTable.getInstance().getAccountData(_loginName, "player_vitality_points", String.valueOf(Config.STARTING_VITALITY_POINTS));
            writeD(data.getIntValue());    // Количество виталити-очков
            writeD(data.getIntValue() >= 1 ? (int) Config.RATE_VITALITY * 100 : 0); // Glory Days: бонус от виталити очков.
            data = AccountShareDataTable.getInstance().getAccountData(_loginName, "player_vitality_items_left", "5");
            writeD(data.getIntValue(5)); // Glory Days: количество итемов которыми можно повысить уровень виталити.
            writeD(charInfoPackage.getAccessLevel() == -100 ? 0x00 : 0x01); //?
            writeC(0x00); //?
            writeC(0x00); //IsHero
            writeC(0x01); //TODO isHairAccessoryEnabled
        }
    }

    @Override
    protected int[] getPaperdollOrderVisualId()
    {
        return PAPERDOLL_ORDER_VISUAL_ID;
    }
}