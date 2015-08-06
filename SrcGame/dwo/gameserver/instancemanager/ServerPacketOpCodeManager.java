package dwo.gameserver.instancemanager;

import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * L2GOD Team
 * User: Bacek, ANZO
 * Date: 28.09.12
 * Time: 21:18
 */

public class ServerPacketOpCodeManager
{
    private static final Logger _log = LogManager.getLogger(ServerPacketOpCodeManager.class);

    private static final Map<Integer, Integer> _serverOpCodes = new HashMap<>(ServerPacketEnum.values().length);

    private ServerPacketOpCodeManager()
    {
        hashServerPackets();
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _serverOpCodes.size() + " active server packet opcodes.");
    }

    /***
     * @param packageName путь до пеккеджа с классами
     * @return список классов в указанном пеккедже
     */
    public static List<Class> getClassesForPackage(String packageName) throws ClassNotFoundException
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL packageURL;
        List<Class> names = new FastList<>();

        String packetPath = packageName.replace(".", "/");
        packageURL = classLoader.getResource(packetPath);

        if(packageURL != null)
        {
            if(packageURL.getProtocol().equals("jar"))
            {
                String jarFileName = null;
                JarFile jf;
                Enumeration<JarEntry> jarEntries = null;
                String entryName;

                try
                {
                    jarFileName = URLDecoder.decode(packageURL.getFile(), "UTF-8");
                }
                catch(UnsupportedEncodingException e)
                {
                    _log.error("Failed to get JAR name.", e);
                }

                if(jarFileName != null)
                {
                    jarFileName = jarFileName.substring(5, jarFileName.indexOf('!'));
                    try
                    {
                        jf = new JarFile(jarFileName);
                        jarEntries = jf.entries();
                    }
                    catch(IOException e)
                    {
                        _log.error("Failed to load JAR File.", e);
                    }

                    if(jarEntries != null)
                    {
                        while(jarEntries.hasMoreElements())
                        {
                            entryName = jarEntries.nextElement().getName();

                            if(entryName.endsWith(".class"))
                            {
                                if(entryName.startsWith(packetPath) && entryName.length() > packetPath.length() + 5)
                                {
                                    entryName = entryName.substring(packetPath.length() + 1, entryName.lastIndexOf('.'));
                                    names.add(Class.forName(packageName + '.' + entryName.replace("/", ".")));
                                }
                            }
                            else
                            {
                                names.addAll(getClassesForPackage(packageName + '.' + entryName.replace("/", ".")));
                            }
                        }
                    }
                }
            }
            else
            {
                File folder = new File(packageURL.getFile());
                File[] fileList = folder.listFiles();

                if(fileList != null)
                {
                    String entryName;
                    for(File actual : fileList)
                    {
                        entryName = actual.getName();

                        if(entryName.endsWith(".class"))
                        {
                            entryName = entryName.substring(0, entryName.lastIndexOf('.'));
                            names.add(Class.forName(packageName + '.' + entryName));
                        }
                        else
                        {
                            names.addAll(getClassesForPackage(packageName + '.' + entryName));
                        }
                    }
                }
            }
        }
        return names;
    }

    public static ServerPacketOpCodeManager getInstance()
    {
        return SingletonHolder._instance;
    }

    private void hashServerPackets()
    {
        try
        {
            List<Class> classes = getClassesForPackage("dwo.gameserver.network.game.serverpackets");
            for(Class<?> clazz : classes)
            {
                for(ServerPacketEnum t : ServerPacketEnum.values())
                {
                    if(t.name().equals(clazz.getSimpleName()))
                    {
                        _serverOpCodes.put(clazz.hashCode(), t.ordinal());
                        //                      Debug
                        //						if(t.ordinal() > 254)
                        //						{
                        //							StringBuilder sb = new StringBuilder();
                        //							sb.append(Integer.toHexString(t.ordinal() - 255));
                        //							if (sb.length() < 2) {
                        //								sb.insert(0, '0'); // pad with leading zero if needed
                        //							}
                        //							String hex = sb.toString();
                        //							System.out.println(clazz.getName() + " " + hex);
                        //						}
                        break;
                    }
                }
            }
        }
        catch(ClassNotFoundException e)
        {
            _log.log(Level.ERROR, getClass().getSimpleName() + " : Error while reflecting server packet classes!", e);
        }
    }

    /***
     * @param hash хеш класса серверного пакеты
     * @return опкод для указанного хеша пакетв
     */
    public int getOpCodeForPacketHash(int hash)
    {
        return _serverOpCodes.get(hash);
    }

    static enum ServerPacketEnum
    {
        /*0x00*/ Die,
        /*0x01*/ Revive,
        /*0x02*/ AttackOutofRange,
        /*0x03*/ AttackinCoolTime,
        /*0x04*/ AttackDeadTarget,
        /*0x05*/ SpawnItem,
        /*0x06*/ SellList,
        /*0x07*/ BuyList,
        /*0x08*/ DeleteObject,
        /*0x09*/ CharacterSelectionInfo,
        /*0x0A*/ LoginFail,
        /*0x0B*/ CharacterSelected,
        /*0x0C*/ NpcInfo,
        /*0x0D*/ NewCharacterSuccess,
        /*0x0E*/ NewCharacterFail,
        /*0x0F*/ CharacterCreateSuccess,
        /*0x10*/ CharacterCreateFail,
        /*0x11*/ ItemList,
        /*0x12*/ SunRise,
        /*0x13*/ SunSet,
        /*0x14*/ TradeStart,
        /*0x15*/ TradeStartOk,
        /*0x16*/ DropItem,
        /*0x17*/ GetItem,
        /*0x18*/ StatusUpdate,
        /*0x19*/ NpcHtmlMessage,
        /*0x1A*/ TradeOwnAdd,
        /*0x1B*/ TradeOtherAdd,
        /*0x1C*/ TradeDone,
        /*0x1D*/ CharacterDeleteSuccess,
        /*0x1E*/ CharacterDeleteFail,
        /*0x1F*/ ActionFail,
        /*0x20*/ SeverClose,
        /*0x21*/ InventoryUpdate,
        /*0x22*/ TeleportToLocation,
        /*0x23*/ TargetSelected,
        /*0x24*/ TargetUnselected,
        /*0x25*/ AutoAttackStart,
        /*0x26*/ AutoAttackStop,
        /*0x27*/ SocialAction,
        /*0x28*/ ChangeMoveType,
        /*0x29*/ ChangeWaitType,
        /*0x2A*/ ManagePledgePower,
        /*0x2B*/ CreatePledge,
        /*0x2C*/ AskJoinPledge,
        /*0x2D*/ JoinPledge,
        /*0x2E*/ VersionCheck,
        /*0x2F*/ MTL,
        /*0x30*/ NS,
        /*0x31*/ CI,
        /*0x32*/ UI,
        /*0x33*/ Attack,
        /*0x34*/ WithdrawalPledge,
        /*0x35*/ OustPledgeMember,
        /*0x36*/ SetOustPledgeMember,
        /*0x37*/ DismissPledge,
        /*0x38*/ SetDismissPledge,
        /*0x39*/ AskJoinParty,
        /*0x3A*/ JoinParty,
        /*0x3B*/ WithdrawalParty,
        /*0x3C*/ OustPartyMember,
        /*0x3D*/ SetOustPartyMember,
        /*0x3E*/ DismissParty,
        /*0x3F*/ SetDismissParty,
        /*0x40*/ MagicAndSkillList,
        /*0x41*/ WareHouseDepositList,
        /*0x42*/ WareHouseWithdrawList,
        /*0x43*/ WareHouseDone,
        /*0x44*/ ShortCutRegister,
        /*0x45*/ ShortCutInit,
        /*0x46*/ ShortCutDelete,
        /*0x47*/ StopMove,
        /*0x48*/ MagicSkillUse,
        /*0x49*/ MagicSkillCanceled,
        /*0x4A*/ Say2,
        /*0x4B*/ NpcInfoAbnormalVisualEffect,
        /*0x4C*/ DoorInfo,
        /*0x4D*/ DoorStatusUpdate,
        /*0x4E*/ PartySmallWindowAll,
        /*0x4F*/ PartySmallWindowAdd,
        /*0x50*/ PartySmallWindowDeleteAll,
        /*0x51*/ PartySmallWindowDelete,
        /*0x52*/ PartySmallWindowUpdate,
        /*0x53*/ TradePressOwnOk,
        /*0x54*/ MagicSkillLaunched,
        /*0x55*/ FriendAddRequestResult,
        /*0x56*/ FriendAdd,
        /*0x57*/ FriendRemove,
        /*0x58*/ FriendList,
        /*0x59*/ FriendStatus,
        /*0x5A*/ PledgeShowMemberListAll,
        /*0x5B*/ PledgeShowMemberListUpdate,
        /*0x5C*/ PledgeShowMemberListAdd,
        /*0x5D*/ PledgeShowMemberListDelete,
        /*0x5E*/ MagicList,
        /*0x5F*/ SkillList,
        /*0x60*/ VehicleInfo,
        /*0x61*/ FinishRotating,
        /*0x62*/ SystemMessage,
        /*0x63*/ StartPledgeWar,
        /*0x64*/ ReplyStartPledgeWar,
        /*0x65*/ StopPledgeWar,
        /*0x66*/ ReplyStopPledgeWar,
        /*0x67*/ SurrenderPledgeWar,
        /*0x68*/ ReplySurrenderPledgeWar,
        /*0x69*/ SetPledgeCrest,
        /*0x6A*/ PledgeCrest,
        /*0x6B*/ SetupGauge,
        /*0x6C*/ VehicleDeparture,
        /*0x6D*/ VehicleCheckLocation,
        /*0x6E*/ GetOnVehicle,
        /*0x6F*/ GetOffVehicle,
        /*0x70*/ TradeRequest,
        /*0x71*/ RestartResponse,
        /*0x72*/ MoveToPawn,
        /*0x73*/ SSQInfo,
        /*0x74*/ GameGuardQuery,
        /*0x75*/ L2FriendList,
        /*0x76*/ L2Friend,
        /*0x77*/ L2FriendStatus,
        /*0x78*/ L2FriendSay,
        /*0x79*/ ValidateLocation,
        /*0x7A*/ StartRotating,
        /*0x7B*/ ShowBoard,
        /*0x7C*/ ChooseInventoryItem,
        /*0x7D*/ Dummy,
        /*0x7E*/ MoveToLocationInVehicle,
        /*0x7F*/ StopMoveInVehicle,
        /*0x80*/ ValidateLocationInVehicle,
        /*0x81*/ TradeUpdate,
        /*0x82*/ TradePressOtherOk,
        /*0x83*/ FriendAddRequest,
        /*0x84*/ LogOutOk,
        /*0x85*/ AbnormalStatusUpdate,
        /*0x86*/ QuestList,
        /*0x87*/ EnchantResult,
        /*0x88*/ PledgeShowMemberListDeleteAll,
        /*0x89*/ PledgeInfo,
        /*0x8A*/ PledgeExtendedInfo,
        /*0x8B*/ SummonInfo,
        /*0x8C*/ Ride,
        /*0x8D*/ Dummy1,
        /*0x8E*/ PledgeShowInfoUpdate,
        /*0x8F*/ ClientAction,
        /*0x90*/ AcquireSkillList,
        /*0x91*/ AcquireSkillInfo,
        /*0x92*/ ServerObjectInfo,
        /*0x93*/ GMHide,
        /*0x94*/ AcquireSkillDone,
        /*0x95*/ GMViewCharacterInfo,
        /*0x96*/ GMViewPledgeInfo,
        /*0x97*/ GMViewSkillInfo,
        /*0x98*/ GMViewMagicInfo,
        /*0x99*/ GMViewQuestInfo,
        /*0x9A*/ GMViewItemList,
        /*0x9B*/ GMViewWarehouseWithdrawList,
        /*0x9C*/ ListPartyWating,
        /*0x9D*/ PartyRoomInfo,
        /*0x9E*/ PlaySound,
        /*0x9F*/ StaticObject,
        /*0xA0*/ PrivateStoreManageList,
        /*0xA1*/ PrivateStoreList,
        /*0xA2*/ PrivateStoreMsg,
        /*0xA3*/ ShowMinimap,
        /*0xA4*/ ReviveRequest,
        /*0xA5*/ AbnormalVisualEffect,
        /*0xA6*/ TutorialShowHtml,
        /*0xA7*/ TutorialShowQuestionMark,
        /*0xA8*/ TutorialEnableClientEvent,
        /*0xA9*/ TutorialCloseHtml,
        /*0xAA*/ ShowRadar,
        /*0xAB*/ WithdrawAlliance,
        /*0xAC*/ OustAllianceMemberPledge,
        /*0xAD*/ DismissAlliance,
        /*0xAE*/ SetAllianceCrest,
        /*0xAF*/ AllianceCrest,
        /*0xB0*/ ServerCloseSocket,
        /*0xB1*/ PetStatusShow,
        /*0xB2*/ PetInfo,
        /*0xB3*/ PetItemList,
        /*0xB4*/ PetInventoryUpdate,
        /*0xB5*/ AllianceInfo,
        /*0xB6*/ PetStatusUpdate,
        /*0xB7*/ PetDelete,
        /*0xB8*/ DeleteRadar,
        /*0xB9*/ MyTargetSelected,
        /*0xBA*/ PartyMemberPosition,
        /*0xBB*/ AskJoinAlliance,
        /*0xBC*/ JoinAlliance,
        /*0xBD*/ PrivateStoreBuyManageList,
        /*0xBE*/ PrivateStoreBuyList,
        /*0xBF*/ PrivateStoreBuyMsg,
        /*0xC0*/ VehicleStart,
        /*0xC1*/ NpcInfoState,
        /*0xC2*/ StartAllianceWar,
        /*0xC3*/ ReplyStartAllianceWar,
        /*0xC4*/ StopAllianceWar,
        /*0xC5*/ ReplyStopAllianceWar,
        /*0xC6*/ SurrenderAllianceWar,
        /*0xC7*/ SkillCoolTime,
        /*0xC8*/ PackageToList,
        /*0xC9*/ CastleSiegeInfo,
        /*0xCA*/ CastleSiegeAttackerList,
        /*0xCB*/ CastleSiegeDefenderList,
        /*0xCC*/ NickNameChanged,
        /*0xCD*/ PledgeStatusChanged,
        /*0xCE*/ RelationChanged,
        /*0xCF*/ EventTrigger,
        /*0xD0*/ MultiSellList,
        /*0xD1*/ SetSummonRemainTime,
        /*0xD2*/ PackageSendableList,
        /*0xD3*/ EarthQuake,
        /*0xD4*/ FlyToLocation,
        /*0xD5*/ BlockList,
        /*0xD6*/ SpecialCamera,
        /*0xD7*/ NormalCamera,
        /*0xD8*/ SkillRemainSec,
        /*0xD9*/ NetPing,
        /*0xDA*/ Dice,
        /*0xDB*/ Snoop,
        /*0xDC*/ RecipeBookItemList,
        /*0xDD*/ RecipeItemMakeInfo,
        /*0xDE*/ RecipeShopManageList,
        /*0xDF*/ RecipeShopSellList,
        /*0xE0*/ RecipeShopItemInfo,
        /*0xE1*/ RecipeShopMsg,
        /*0xE2*/ ShowCalc,
        /*0xE3*/ MonRaceInfo,
        /*0xE4*/ HennaItemInfo,
        /*0xE5*/ HennaInfo,
        /*0xE6*/ HennaUnequipList,
        /*0xE7*/ HennaUnequipInfo,
        /*0xE8*/ MacroList,
        /*0xE9*/ BuyListSeed,
        /*0xEA*/ ShowTownMap,
        /*0xEB*/ ObserverStart,
        /*0xEC*/ ObserverEnd,
        /*0xED*/ ChairSit,
        /*0xEE*/ HennaEquipList,
        /*0xEF*/ SellListProcure,
        /*0xF0*/ GMHennaInfo,
        /*0xF1*/ RadarControl,
        /*0xF2*/ ClientSetTime,
        /*0xF3*/ ConfirmDlg,
        /*0xF4*/ PartySpelled,
        /*0xF5*/ ShopPreviewList,
        /*0xF6*/ ShopPreviewInfo,
        /*0xF7*/ CameraMode,
        /*0xF8*/ ShowXMasSeal,
        /*0xF9*/ EtcStatusUpdate,
        /*0xFA*/ ShortBuffStatusUpdate,
        /*0xFB*/ SSQStatus,
        /*0xFC*/ PetitionVote,
        /*0xFD*/ AgitDecoInfo,
        /*0xFE*/ Dummy2, //TODO
        /*0xFE*/ ExDummy,//0x00),
        /*0xFE*/ ExRegenMax,//0x01),
        /*0xFE*/ ExEventMatchUserInfo,//0x02),
        /*0xFE*/ ExColosseumFenceInfo,//0x03),
        /*0xFE*/ ExEventMatchSpelledInfo,//0x04),
        /*0xFE*/ ExEventMatchFirecracker,//0x05),
        /*0xFE*/ ExEventMatchTeamUnlocked,//0x06),
        /*0xFE*/ ExEventMatchGMTest,//0x07),
        /*0xFE*/ ExPartyRoomMember,//0x08),
        /*0xFE*/ ExClosePartyRoom,//0x09),
        /*0xFE*/ ExManagePartyRoomMember,//0xA),
        /*0xFE*/ ExEventMatchLockResult,//0xB),
        /*0xFE*/ ExAutoSoulShot,//0xC),
        /*0xFE*/ ExEventMatchList,//0xD),
        /*0xFE*/ ExEventMatchObserver,//0xE),
        /*0xFE*/ ExEventMatchMessage,//0xF),
        /*0xFE*/ ExEventMatchScore,//0x10),
        /*0xFE*/ ExServerPrimitive,//0x11),
        /*0xFE*/ ExOpenMPCC,//0x12),
        /*0xFE*/ ExCloseMPCC,//0x13),
        /*0xFE*/ ExShowCastleInfo,//0x14),
        /*0xFE*/ ExShowFortressInfo,//0x15),
        /*0xFE*/ ExShowAgitInfo,//0x16),
        /*0xFE*/ ExShowFortressSiegeInfo,//0x17),
        /*0xFE*/ ExPartyPetWindowAdd,//0x18),
        /*0xFE*/ ExPartyPetWindowUpdate,//0x19),
        /*0xFE*/ ExAskJoinMPCC,//0x1A),
        /*0xFE*/ ExPledgeEmblem,//0x1B),
        /*0xFE*/ ExEventMatchTeamInfo,//0x1C),
        /*0xFE*/ ExEventMatchCreate,//0x1D),
        /*0xFE*/ ExFishingStart,//0x1E),
        /*0xFE*/ ExFishingEnd,//0x1F),
        /*0xFE*/ ExShowQuestInfo,//0x20),
        /*0xFE*/ ExShowQuestMark,//0x21),
        /*0xFE*/ ExSendManorList,//0x22),
        /*0xFE*/ ExShowSeedInfo,//0x23),
        /*0xFE*/ ExShowCropInfo,//0x24),
        /*0xFE*/ ExShowManorDefaultInfo,//0x25),
        /*0xFE*/ ExShowSeedSetting,//0x26),
        /*0xFE*/ ExFishingStartCombat,//0x27),
        /*0xFE*/ ExFishingHpRegen,//0x28),
        /*0xFE*/ ExEnchantSkillList,//0x29),
        /*0xFE*/ ExEnchantSkillInfo,//0x2A),
        /*0xFE*/ ExShowCropSetting,//0x2B),
        /*0xFE*/ ExShowSellCropList,//0x2C),
        /*0xFE*/ ExOlympiadMatchEnd,//0x2D),
        /*0xFE*/ ExMailArrived,//0x2E),
        /*0xFE*/ ExStorageMaxCount,//0x2F),
        /*0xFE*/ ExEventMatchManage,//0x30),
        /*0xFE*/ ExMultiPartyCommandChannelInfo,//0x31),
        /*0xFE*/ ExPCCafePointInfo,//0x32),
        /*0xFE*/ ExSetCompassZoneCode,//0x33),
        /*0xFE*/ ExGetBossRecord,//0x34),
        /*0xFE*/ ExAskJoinPartyRoom,//0x35),
        /*0xFE*/ ExListPartyMatchingWaitingRoom,//0x36),
        /*0xFE*/ ExSetMpccRouting,//0x37),
        /*0xFE*/ ExShowAdventurerGuideBook,//0x38),
        /*0xFE*/ ExShowScreenMessage,//0x39),
        /*0xFE*/ PledgeSkillList,//0x3A),
        /*0xFE*/ PledgeSkillListAdd,//0x3B),
        /*0xFE*/ PledgeSkillListRemove,//0x3C),
        /*0xFE*/ PledgePowerGradeList,//0x3D),
        /*0xFE*/ PledgeReceivePowerInfo,//0x3E),
        /*0xFE*/ PledgeReceiveMemberInfo,//0x3F),
        /*0xFE*/ PledgeReceiveWarList,//0x40),
        /*0xFE*/ PledgeReceiveSubPledgeCreated,//0x41),
        /*0xFE*/ ExRedSky,//0x42),
        /*0xFE*/ PledgeReceiveUpdatePower,//0x43),
        /*0xFE*/ FlySelfDestination,//0x44),
        /*0xFE*/ ShowPCCafeCouponShowUI,//0x45),
        /*0xFE*/ ExSearchOrc,//0x46),
        /*0xFE*/ ExCursedWeaponList,//0x47),
        /*0xFE*/ ExCursedWeaponLocation,//0x48),
        /*0xFE*/ ExRestartClient,//0x49),
        /*0xFE*/ ExRequestHackShield,//0x4A),
        /*0xFE*/ ExUseSharedGroupItem,//0x4B),
        /*0xFE*/ ExMPCCShowPartyMemberInfo,//0x4C),
        /*0xFE*/ ExDuelAskStart,//0x4D),
        /*0xFE*/ ExDuelReady,//0x4E),
        /*0xFE*/ ExDuelStart,//0x4F),
        /*0xFE*/ ExDuelEnd,//0x50),
        /*0xFE*/ ExDuelUpdateUserInfo,//0x51),
        /*0xFE*/ ExShowVariationMakeWindow,//0x52),
        /*0xFE*/ ExShowVariationCancelWindow,//0x53),
        /*0xFE*/ ExPutItemResultForVariationMake,//0x54),
        /*0xFE*/ ExPutIntensiveResultForVariationMake,//0x55),
        /*0xFE*/ ExPutCommissionResultForVariationMake,//0x56),
        /*0xFE*/ ExVariationResult,//0x57),
        /*0xFE*/ ExPutItemResultForVariationCancel,//0x58),
        /*0xFE*/ ExVariationCancelResult,//0x59),
        /*0xFE*/ ExDuelEnemyRelation,//0x5A),
        /*0xFE*/ ExPlayAnimation,//0x5B),
        /*0xFE*/ ExMPCCPartyInfoUpdate,//0x5C),
        /*0xFE*/ ExPlayScene,//0x5D),
        /*0xFE*/ ExSpawnEmitter,//0x5E),
        /*0xFE*/ ExEnchantSkillInfoDetail,//0x5F),
        /*0xFE*/ ExBasicActionList,//0x60),
        /*0xFE*/ ExAirShipInfo,//0x61),
        /*0xFE*/ ExAttributeEnchantResult,//0x62),
        /*0xFE*/ ExChooseInventoryAttributeItem,//0x63),
        /*0xFE*/ ExGetOnAirShip,//0x64),
        /*0xFE*/ ExGetOffAirShip,//0x65),
        /*0xFE*/ ExMoveToLocationAirShip,//0x66),
        /*0xFE*/ ExStopMoveAirShip,//0x67),
        /*0xFE*/ ExShowTrace,//0x68),
        /*0xFE*/ ExItemAuctionInfo,//0x69),
        /*0xFE*/ ExNeedToChangeName,//0x6A),
        /*0xFE*/ ExPartyPetWindowDelete,//0x6B),
        /*0xFE*/ ExTutorialList,//0x6C),
        /*0xFE*/ ExRpItemLink,//0x6D),
        /*0xFE*/ ExMoveToLocationInAirShip,//0x6E),
        /*0xFE*/ ExStopMoveInAirShip,//0x6F),
        /*0xFE*/ ExValidateLocationInAirShip,//0x70),
        /*0xFE*/ ExUISetting,//0x71),
        /*0xFE*/ ExMoveToTargetInAirShip,//0x72),
        /*0xFE*/ ExAttackInAirShip,//0x73),
        /*0xFE*/ ExMagicSkillUseInAirShip,//0x74),
        /*0xFE*/ ExShowBaseAttributeCancelWindow,//0x75),
        /*0xFE*/ ExBaseAttributeCancelResult,//0x76),
        /*0xFE*/ ExSubPledgetSkillAdd,//0x77),
        /*0xFE*/ ExResponseFreeServer,//0x78),
        /*0xFE*/ ExShowProcureCropDetail,//0x79),
        /*0xFE*/ ExHeroList,//0x7A),
        /*0xFE*/ ExOlympiadUserInfo,//0x7B),
        /*0xFE*/ ExOlympiadSpelledInfo,//0x7C),
        /*0xFE*/ ExOlympiadMode,//0x7D),
        /*0xFE*/ ExShowFortressMapInfo,//0x7E),
        /*0xFE*/ ExPVPMatchRecord,//0x7F),
        /*0xFE*/ ExPVPMatchUserDie,//0x80),
        /*0xFE*/ ExPrivateStoreWholeMsg,//0x81),
        /*0xFE*/ ExPutEnchantTargetItemResult,//0x82),
        /*0xFE*/ ExPutEnchantSupportItemResult,//0x83),
        /*0xFE*/ ExChangeNicknameNColor,//0x84),
        /*0xFE*/ ExGetBookMarkInfo,//0x85),
        /*0xFE*/ ExNotifyPremiumItem,//0x86),
        /*0xFE*/ ExGetPremiumItemList,//0x87),
        /*0xFE*/ ExPeriodicItemList,//0x88),
        /*0xFE*/ ExJumpToLocation,//0x89),
        /*0xFE*/ ExPVPMatchCCRecord,//0x8A),
        /*0xFE*/ ExPVPMatchCCMyRecord,//0x8B),
        /*0xFE*/ ExPVPMatchCCRetire,//0x8C),
        /*0xFE*/ ExShowTerritory,//0x8D),
        /*0xFE*/ ExNpcQuestHtmlMessage,//0x8E),
        /*0xFE*/ ExSendUIEvent,//0x8F),
        /*0xFE*/ ExNotifyBirthDay,//0x90),
        /*0xFE*/ ExShowDominionRegistry,//0x91),
        /*0xFE*/ ExReplyRegisterDominion,//0x92),
        /*0xFE*/ ExReplyDominionInfo,//0x93),
        /*0xFE*/ ExShowOwnthingPos,//0x94),
        /*0xFE*/ ExCleftList,//0x95),
        /*0xFE*/ ExCleftState,//0x96),
        /*0xFE*/ ExDominionChannelSet,//0x97),
        /*0xFE*/ ExBlockUpSetList,//0x98),
        /*0xFE*/ ExBlockUpSetState,//0x99),
        /*0xFE*/ ExStartScenePlayer,//0x9A),
        /*0xFE*/ ExAirShipTeleportList,//0x9B),
        /*0xFE*/ ExMpccRoomInfo,//0x9C),
        /*0xFE*/ ExListMpccWaiting,//0x9D),
        /*0xFE*/ ExDissmissMpccRoom,//0x9E),
        /*0xFE*/ ExManageMpccRoomMember,//0x9F),
        /*0xFE*/ ExMpccRoomMember,//0xA0),
        /*0xFE*/ ExVitalityPointInfo,//0xA1),
        /*0xFE*/ ExShowSeedMapInfo,//0xA2),
        /*0xFE*/ ExMpccPartymasterList,//0xA3),
        /*0xFE*/ ExDominionWarStart,//0xA4),
        /*0xFE*/ ExDominionWarEnd,//0xA5),
        /*0xFE*/ ExShowLines,//0xA6),
        /*0xFE*/ ExPartyMemberRenamed,//0xA7),
        /*0xFE*/ ExEnchantSkillResult,//0xA8),
        /*0xFE*/ ExRefundList,//0xA9),
        /*0xFE*/ ExNoticePostArrived,//0xAA),
        /*0xFE*/ ExShowReceivedPostList,//0xAB),
        /*0xFE*/ ExReplyReceivedPost,//0xAC),
        /*0xFE*/ ExShowSentPostList,//0xAD),
        /*0xFE*/ ExReplySentPost,//0xAE),
        /*0xFE*/ ExResponseShowStepOne,//0xAF),
        /*0xFE*/ ExResponseShowStepTwo,//0xB0),
        /*0xFE*/ ExResponseShowContents,//0xB1),
        /*0xFE*/ ExShowPetitionHtml,//0xB2),
        /*0xFE*/ ExReplyPostItemList,//0xB3),
        /*0xFE*/ ExChangePostState,//0xB4),
        /*0xFE*/ ExReplyWritePost,//0xB5),
        /*0xFE*/ ExInitializeSeed,//0xB6),
        /*0xFE*/ ExRaidReserveResult,//0xB7),
        /*0xFE*/ ExBuySellList,//0xB8),
        /*0xFE*/ ExCloseRaidSocket,//0xB9),
        /*0xFE*/ ExPrivateMarketList,//0xBA),
        /*0xFE*/ ExRaidCharacterSelected,//0xBB),
        /*0xFE*/ ExAskCoupleAction,//0xBC),
        /*0xFE*/ ExBrBroadcastEventState,//0xBD),
        /*0xFE*/ ExBR_LoadEventTopRankers,//0xBE),
        /*0xFE*/ ExChangeNPCState,//0xBF),
        /*0xFE*/ ExAskModifyPartyLooting,//0xC0),
        /*0xFE*/ ExSetPartyLooting,//0xC1),
        /*0xFE*/ ExRotation,//0xC2),
        /*0xFE*/ ExChangeClientEffectInfo,//0xC3),
        /*0xFE*/ ExMembershipInfo,//0xC4),
        /*0xFE*/ ExReplyHandOverPartyMaster,//0xC5),
        /*0xFE*/ ExQuestNpcLogList,//0xC6),
        /*0xFE*/ ExQuestItemList,//0xC7),
        /*0xFE*/ ExGMViewQuestItemList,//0xC8),
        /*0xFE*/ ExResartResponse,//0xC9),
        /*0xFE*/ ExVoteSystemInfo,//0xCA),
        /*0xFE*/ ExShuttuleInfo,//0xCB),
        /*0xFE*/ ExSuttleGetOn,//0xCC),
        /*0xFE*/ ExSuttleGetOff,//0xCD),
        /*0xFE*/ ExSuttleMove,//0xCE),
        /*0xFE*/ ExMTLInSuttle,//0xCF),
        /*0xFE*/ ExStopMoveInShuttle,//0xD0),
        /*0xFE*/ ExValidateLocationInShuttle,//0xD1),
        /*0xFE*/ ExAgitAuctionCmd,//0xD2),
        /*0xFE*/ ExConfirmAddingPostFriend,//0xD3),
        /*0xFE*/ ExReceiveShowPostFriend,//0xD4),
        /*0xFE*/ ExReceiveOlympiad,//0xD5),
        /*0xFE*/ ExBR_GamePoint,//0xD6),
        /*0xFE*/ ExBR_ProductList,//0xD7),
        /*0xFE*/ ExBR_ProductInfo,//0xD8),
        /*0xFE*/ ExBR_BuyProduct,//0xD9),
        /*0xFE*/ ExBR_PremiumState,//0xDA),
        /*0xFE*/ ExBrExtraUserInfo,//0xDB),
        /*0xFE*/ ExBrBuffEventState,//0xDC),
        /*0xFE*/ ExBR_RecentProductList,//0xDD),
        /*0xFE*/ ExBR_MinigameLoadScores,//0xDE),
        /*0xFE*/ ExBR_AgathionEnergyInfo,//0xDF),
        /*0xFE*/ ExShowChannelingEffect,//0xE0),
        /*0xFE*/ ExGetCrystalizingEstimation,//0xE1),
        /*0xFE*/ ExGetCrystalizingFail,//0xE2),
        /*0xFE*/ ExNavitAdventPointInfo,//0xE3),
        /*0xFE*/ ExNavitAdventEffect,//0xE4),
        /*0xFE*/ ExNavitAdventTimeChange,//0xE5),
        /*0xFE*/ ExAbnormalStatusUpdateFromTarget,//0xE6),
        /*0xFE*/ ExStopScenePlayer,//0xE7),
        /*0xFE*/ ExFlyMove,//0xE8),
        /*0xFE*/ ExDynamicQuest,//0xE9),
        /*0xFE*/ ExSubjobInfo,//0xEA),
        /*0xFE*/ ExChangeMPCost,//0xEB),
        /*0xFE*/ ExFriendDetailInfo,//0xEC),
        /*0xFE*/ ExBlockAddResult,//0xED),
        /*0xFE*/ ExBlockRemoveResult,//0xEE),
        /*0xFE*/ ExBlockDefailInfo,//0xEF),
        /*0xFE*/ ExLoadInzonePartyHistory,//0xF0),
        /*0xFE*/ ExFriendNotifyNameChange,//0xF1),
        /*0xFE*/ ExShowCommission,//0xF2),
        /*0xFE*/ ExResponseCommissionItemList,//0xF3),
        /*0xFE*/ ExResponseCommissionInfo,//0xF4),
        /*0xFE*/ ExResponseCommissionRegister,//0xF5),
        /*0xFE*/ ExResponseCommissionDelete,//0xF6),
        /*0xFE*/ ExResponseCommissionList,//0xF7),
        /*0xFE*/ ExResponseCommissionBuyInfo,//0xF8),
        /*0xFE*/ ExResponseCommissionBuyItem,//0xF9),
        /*0xFE*/ ExAcquirableSkillListByClass,//0xFA),
        /*0xFE*/ ExMagicAttackInfo,//0xFB),
        /*0xFE*/ ExAcquireSkillInfo,//0xFC),
        /*0xFE*/ ExNewSkillToLearnByLevelUp,//0xFD),
        /*0xFE*/ ExCallToChangeClass,//0xFE),
        /*0xFE*/ ExChangeToAwakenedClass,//0xFF),
        /*0xFE*/ ExTacticalSign,//0x100),
        /*0xFE*/ ExLoadStatWorldRank,//0x101),
        /*0xFE*/ ExLoadStatUser,//0x102),
        /*0xFE*/ ExLoadStatHotLink,//0x103),
        /*0xFE*/ ExGetWebSessionID,//0x104),
        /*0xFE*/ Ex2NDPasswordCheck,//0x105),
        /*0xFE*/ Ex2NDPasswordVerify,//0x106),
        /*0xFE*/ Ex2NDPasswordAck,//0x107),
        /*0xFE*/ ExFlyMoveBroadcast,//0x108),
        /*0xFE*/ ExShowUsm,//0x109),
        /*0xFE*/ ExShowStatPage,//0x10A),
        /*0xFE*/ ExIsCharNameCreatable,//0x10B),
        /*0xFE*/ ExGoodsInventoryChangedNoti,//0x10C),
        /*0xFE*/ ExGoodsInventoryInfo,//0x10D),
        /*0xFE*/ ExGoodsInventoryResult,//0x10E),
        /*0xFE*/ ExAlterSkillRequest,//0x10F),
        /*0xFE*/ ExNotifyFlyMoveStart,//0x110),
        /*0xFE*/ ExDummy1,//0x111),
        /*0xFE*/ ExCloseCommission,//0x112),
        /*0xFE*/ ExChangeAttributeItemList,//0x113),
        /*0xFE*/ ExChangeAttributeInfo,//0x114),
        /*0xFE*/ ExChangeAttributeOk,//0x115),
        /*0xFE*/ ExChangeAttributeFail,//0x116),
        /*0xFE*/ ExLightingCandleEvent,//0x117),
        /*0xFE*/ ExVitalityEffectInfo,//0x118),
        /*0xFE*/ ExLoginVitalityEffectInfo,//0x119),
        /*0xFE*/ ExBR_PresentBuyProduct,//0x11A),
        /*0xFE*/ ExMentorList,//0x11B),
        /*0xFE*/ ExMentorAdd,//0x11C),
        /*0xFE*/ ListMenteeWaiting,//0x11D),
        /*0xFE*/ ExInzoneWaitingInfo,//0x11E),
        /*0xFE*/ ExCuriousHouseState,//0x11F),
        /*0xFE*/ ExCuriousHouseEnter,//0x120),
        /*0xFE*/ ExCuriousHouseLeave,//0x121),
        /*0xFE*/ ExCuriousHouseMemberList,//0x122),
        /*0xFE*/ ExCuriousHouseMemberUpdate,//0x123),
        /*0xFE*/ ExCuriousHouseRemainTime,//0x124),
        /*0xFE*/ ExCuriousHouseResult,//0x125),
        /*0xFE*/ ExCuriousHouseObserveList,//0x126),
        /*0xFE*/ ExCuriousHouseObserveMode,//0x127),
        /*0xFE*/ ExSysstring,//0x128),
        /*0xFE*/ ExChoose_Shape_Shifting_Item,//0x129),
        /*0xFE*/ ExPut_Shape_Shifting_Target_Item_Result,//0x12A),
        /*0xFE*/ ExPut_Shape_Shifting_Extraction_Item_Result,//0x12B) ,
        /*0xFE*/ ExShape_Shifting_Result,//0x12C),
        /*0xFE*/ ExCastleState,//0x12D),
        /*0xFE*/ ExNCGuardReceiveDataFromServer,//0x12E),
        /*0xFE*/ ExKalieEvent,//0x12F),
        /*0xFE*/ ExKalieEventJackpotUser,//0x130),
        /*0xFE*/ ExAbnormalVisualEffectInfo,//0x131),
        /*0xFE*/ ExNpcSpeedInfo,//0x132),
        /*0xFE*/ ExSetPledgeEmblemAck,//0x133),
        /*0xFE*/ ExShowBeautyMenu,//0x134),
        /*0xFE*/ ExResponseBeautyList,//0x135),
        /*0xFE*/ ExResponseBeautyRegistReset,//0x136),
        /*0xFE*/ ExResponseResetList,//0x137),
        /*0xFE*/ ExShuffleSeedAndPublicKey,//0x138),
        /*0xFE*/ ExCheck_SpeedHack,//0x139),
        /*0xFE*/ ExBR_NewIConCashBtnWnd,//0x13A),
        /*0xFE*/ ExEvent_Campaign_Info,//0x13B),
        /*0xFE*/ ExUnReadMailCount,//0x13C),
        /*0xFE*/ ExPledgeCount,//0x13D),
        /*0xFE*/ ExAdenaInvenCount,//0x13E),
        /*0xFE*/ ExPledgeRecruitInfo,//0x13F),
        /*0xFE*/ ExPledgeRecruitApplyInfo,//0x140),
        /*0xFE*/ ExPledgeRecruitBoardSearch,//0x141),
        /*0xFE*/ ExPledgeRecruitBoardDetail,//0x142),
        /*0xFE*/ ExPledgeWaitingListApplied,//0x143),
        /*0xFE*/ ExPledgeWaitingList,//0x144),
        /*0xFE*/ ExPledgeWaitingUser,//0x145),
        /*0xFE*/ ExPledgeDraftListSearch,//0x146),
        /*0xFE*/ ExPledgeWaitingListAlarm,//0x147),
        /*0xFE*/ ExValidateActiveCharacter,//0x148),
        /*0xFE*/ ExCloseCommissionRegister,//0x149),
        /*0xFE*/ ExTeleportToLocationActivate,//0x14A),
        /*0xFE*/ ExNotifyWebPetitionReplyAlarm,//0x14B),
        /*0xFE*/ ExEventShowXMasWishCard,//0x14C),
        /*0xFE*/ ExInvitation_Event_UI_Setting,//0x14D),
        /*0xFE*/ ExInvitation_Event_Ink_Energy,//0x14E),
        /*0xFE*/ Ex_Check_using,//0x14F),
        /*0xFE*/ ExGMVitalityEffectInfo,//0x150),
        /*0xFE*/ ExPathToAwakeningAlarm,//0x151),
        /*0xFE*/ ExPutEnchantScrollItemResult,//0x152),
        /*0xFE*/ ExRemoveEnchantSupportItemResult,//0x153),
        /*0xFE*/ ExShowCardRewardList,//0x154),
        /*0xFE*/ ExGmViewCharacterInfo,//0x155),
        /*0xFE*/ ExUserInfoEquipSlot,//0x156),
        /*0xFE*/ ExUserInfoCubic,//0x157),
        /*0xFE*/ ExUserInfoAbnormalVisualEffect,//0x158),
        /*0xFE*/ ExUserInfoFishing,//0x159),
        /*0xFE*/ ExPartySpelledInfoUpdate,//0x15A),
        /*0xFE*/ ExDivideAdenaStart,//0x15B),
        /*0xFE*/ ExDivideAdenaCancel,//0x15C),
        /*0xFE*/ ExDivideAdenaDone,//0x15D),
        /*0xFE*/ ExPetInfo,//0x15E),
        /*0xFE*/ ExAcquireAPSkillList,//0x15F),
        /*0xFE*/ ExStartLuckyGame,//0x160), TODO
        /*0xFE*/ ExBettingLuckyGameResult,//0x161), TODO
        /*0xFE*/ ExTrainingZone_Admission,//0x162), TODO
        /*0xFE*/ ExTrainingZone_Leaving,//0x163), TODO
        /*0xFE*/ ExPeriodicHenna,//0x164),
        /*0xFE*/ ExShowAPListWnd,//0x165),
        /*0xFE*/ ExUserInfoInvenWeight,//0x166),
        /*0xFE*/ ExCloseAPListWnd,//0x167),
        /*0xFE*/ ExEnchantOneOK,//0x168),
        /*0xFE*/ ExEnchantOneFail,//0x169),
        /*0xFE*/ ExEnchantOneRemoveOK,//0x16A),
        /*0xFE*/ ExEnchantOneRemoveFail,//0x16B),
        /*0xFE*/ ExEnchantTwoOK,//0x16C),
        /*0xFE*/ ExEnchantTwoFail,//0x16D),
        /*0xFE*/ ExEnchantTwoRemoveOK,//0x16E),
        /*0xFE*/ ExEnchantTwoRemoveFail,//0x16F),
        /*0xFE*/ ExEnchantSucess,//0x170),
        /*0xFE*/ ExEnchantFail,//0x171),
        /*0xFE*/ ExAccountAttendanceInfo,//0x172), TODO
        /*0xFE*/ ExWorldChatCnt,//0x173),
        /*0xFE*/ ExAlchemySkillList,//0x174),
        /*0xFE*/ ExTryMixCube,//0x175), TODO
        /*0xFE*/ ExAlchemyConversion,//0x176), TODO
        /*0xFE*/ ExBeautyItemList,//0x177); TODO
    }

    private static class SingletonHolder
    {
        protected static final ServerPacketOpCodeManager _instance = new ServerPacketOpCodeManager();
    }
}