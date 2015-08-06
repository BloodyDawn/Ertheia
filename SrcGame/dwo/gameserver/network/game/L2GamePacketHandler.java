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
package dwo.gameserver.network.game;

import dwo.config.Config;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.L2GameClient.GameClientState;
import dwo.gameserver.network.game.clientpackets.*;
import dwo.gameserver.network.game.clientpackets.packet.ChangeAttribute.RequestChangeAttributeCancel;
import dwo.gameserver.network.game.clientpackets.packet.ChangeAttribute.RequestChangeAttributeItem;
import dwo.gameserver.network.game.clientpackets.packet.ChangeAttribute.SendChangeAttributeTargetItem;
import dwo.gameserver.network.game.clientpackets.packet.Commission.*;
import dwo.gameserver.network.game.clientpackets.packet.CuriousHouse.*;
import dwo.gameserver.network.game.clientpackets.packet.ability.*;
import dwo.gameserver.network.game.clientpackets.packet.alchemy.RequestAlchemyConversion;
import dwo.gameserver.network.game.clientpackets.packet.alchemy.RequestAlchemySkillList;
import dwo.gameserver.network.game.clientpackets.packet.alchemy.RequestAlchemyTryMixCube;
import dwo.gameserver.network.game.clientpackets.packet.beautyShop.NotifyExitBeautyShop;
import dwo.gameserver.network.game.clientpackets.packet.beautyShop.RequestRegistBeauty;
import dwo.gameserver.network.game.clientpackets.packet.beautyShop.RequestShowBeautyList;
import dwo.gameserver.network.game.clientpackets.packet.compound.*;
import dwo.gameserver.network.game.clientpackets.packet.divide.RequestDivideAdena;
import dwo.gameserver.network.game.clientpackets.packet.divide.RequestDivideAdenaCancel;
import dwo.gameserver.network.game.clientpackets.packet.divide.RequestDivideAdenaStart;
import dwo.gameserver.network.game.clientpackets.packet.enchant.item.*;
import dwo.gameserver.network.game.clientpackets.packet.enchant.skill.RequestExEnchantSkill;
import dwo.gameserver.network.game.clientpackets.packet.enchant.skill.RequestExEnchantSkillInfo;
import dwo.gameserver.network.game.clientpackets.packet.enchant.skill.RequestExEnchantSkillInfoDetail;
import dwo.gameserver.network.game.clientpackets.packet.pcCafe.ExPCCafeRequestOpenWindowWithoutNPC;
import dwo.gameserver.network.game.clientpackets.packet.pledge.clan.*;
import dwo.gameserver.network.game.clientpackets.packet.pledge.clanSearch.*;
import dwo.gameserver.network.game.clientpackets.packet.primeshop.RequestBR_BuyProduct;
import dwo.gameserver.network.game.clientpackets.packet.primeshop.RequestBR_GamePoint;
import dwo.gameserver.network.game.clientpackets.packet.primeshop.RequestBR_ProductInfo;
import dwo.gameserver.network.game.clientpackets.packet.primeshop.RequestBR_ProductList;
import dwo.gameserver.network.game.clientpackets.packet.recipe.*;
import dwo.gameserver.network.game.clientpackets.packet.shapeShifting.RequestExCancelShapeShiftingItem;
import dwo.gameserver.network.game.clientpackets.packet.shapeShifting.RequestExTryToPutShapeShiftingEnchantSupportItem;
import dwo.gameserver.network.game.clientpackets.packet.shapeShifting.RequestExTryToPutShapeShiftingTargetItem;
import dwo.gameserver.network.game.clientpackets.packet.shapeShifting.RequestShapeShiftingItem;
import dwo.gameserver.network.mmocore.*;
import dwo.gameserver.util.Util;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;

/**
 * Stateful Packet Handler<BR>
 * The Stateful approach prevents the server from handling inconsistent packets, examples:<BR>
 * <li>Clients sends a MoveToLocation packet without having a character attached. (Potential errors handling the packet).</li>
 * <li>Clients sends a RequestAuthLogin being already authed. (Potential exploit).</li>
 * <BR><BR>
 * Note: If for a given exception a packet needs to be handled on more then one state, then it should be added to all these states.
 *
 * @author KenM
 */
public class L2GamePacketHandler implements IPacketHandler<L2GameClient>, IClientFactory<L2GameClient>, IMMOExecutor<L2GameClient>
{
	private static final Logger _log = LogManager.getLogger(L2GamePacketHandler.class);

	// implementation

	@Override
	public ReceivablePacket<L2GameClient> handlePacket(ByteBuffer buf, L2GameClient client)
	{
		if(client.dropPacket())
		{
			return null;
		}

		int opcode = buf.get() & 0xFF;
		int id3;

		ReceivablePacket<L2GameClient> msg = null;
		GameClientState state = client.getState();

        switch(state)
        {
            case CONNECTED:
                switch(opcode)
                {
                    case 0x0e:
                        msg = new ProtocolVersion();
                        break;
                    case 0x2b:
                        msg = new AuthLogin();
                        break;
                    default:
                        printDebug(opcode, buf, state, client);
                        break;
                }
                break;
            case AUTHED:
                switch(opcode)
                {
                    case 0x00:
                        msg = new Logout();
                        break;
                    case 0x0c:
                        msg = new CharacterCreate();
                        break;
                    case 0x0d:
                        msg = new CharacterDelete();
                        break;
                    case 0x12:
                        msg = new CharacterSelect();
                        break;
                    case 0x13:
                        msg = new NewCharacter();
                        break;
                    case 0x7b:
                        msg = new CharacterRestore();
                        break;
                    case 0xd0:
                        int id2;
                        if(buf.remaining() >= 2)
                        {
                            id2 = buf.getShort() & 0xffff;
                        }
                        else
                        {
                            if(Config.PACKET_HANDLER_DEBUG)
                            {
                                _log.log(Level.DEBUG, "Client: " + client + " sent a 0xd0 without the second opcode.");
                            }
                            break;
                        }
                        switch(id2)
                        {
                            case 0x33:
                                msg = new RequestGotoLobby();
                                break;
                            case 0xa6:
                                msg = new RequestEx2ndPasswordCheck();
                                break;
                            case 0xa7:
                                msg = new RequestEx2ndPasswordVerify();
                                break;
                            case 0xa8:
                                msg = new RequestEx2ndPasswordReq();
                                break;
                            case 0xa9:
                                msg = new RequestCharacterNameCreatable();
                                break;
                            default:
                                printDebugDoubleOpcode(opcode, id2, buf, state, client);
                        }
                        break;
                    default:
                        printDebug(opcode, buf, state, client);
                        break;
                }
                break;
            case IN_GAME:
                switch(opcode)
                {
                    case 0x00:
                        msg = new Logout(); // В дампе нету
                        break;
                    case 0x01:
                        msg = new Atk(); // В дампе нету
                        break;
                    case 0x03:
                        msg = new RequestStartPledgeWar();
                        break;
                    case 0x04:
                        msg = new RequestReplyStartPledgeWar();
                        break;
                    case 0x05:
                        msg = new RequestStopPledgeWar(); // RequestServerList ?
                        break;
                    case 0x06:
                        msg = new RequestReplyStopPledgeWar(); // В дампе нету
                        break;
                    case 0x07:
                        msg = new RequestSurrenderPledgeWar(); // ResponseAuthGameGuard ?
                        break;
                    case 0x08:
                        msg = new RequestReplySurrenderPledgeWar(); // В дампе нету
                        break;
                    case 0x09:
                        msg = new RequestSetPledgeCrest();
                        break;
                    case 0x0b:
                        msg = new RequestGiveNickName();
                        break;
                    case 0x0c:
                        // msg = new RequestCharacterCreate(); ?
                        break;
                    case 0x0d:
                        // msg = new RequestCharacterDelete(); ?
                        break;
                    case 0x0e:
                        // msg = new RequestPIAgreementCheck(); ?
                        break;
                    case 0x0f:
                        msg = new MoveBackwardToLocation();
                        break;
                    case 0x11:
                        msg = new EnterWorld();
                        break;
                    case 0x12:
                        // msg = new RequestGameStart(); ?
                        break;
                    case 0x13:
                        // msg = new RequestNewCharacter(); ?
                        break;
                    case 0x14:
                        msg = new RequestItemList();
                        break;
                    case 0x15:
                        // В дампе нету
                        break;
                    case 0x16:
                        msg = new RequestUnEquipItem();
                        break;
                    case 0x17:
                        msg = new RequestDropItem();
                        break;
                    case 0x19:
                        msg = new UseItem();
                        break;
                    case 0x1a:
                        msg = new RequestTrade();
                        break;
                    case 0x1b:
                        msg = new RequestAddTradeItem();
                        break;
                    case 0x1c:
                        msg = new RequestTradeDone();
                        break;
                    case 0x1f:
                        msg = new Action(); // RequestAttack ?
                        break;
                    case 0x22:
                        msg = new RequestLinkHtml();
                        break;
                    case 0x23:
                        msg = new RequestBypassToServer();
                        break;
                    case 0x24:
                        msg = new RequestBBSwrite();
                        break;
                    case 0x25:
                        // В дампе нету
                        break;
                    case 0x26:
                        msg = new RequestJoinPledge();
                        break;
                    case 0x27:
                        msg = new RequestAnswerJoinPledge();
                        break;
                    case 0x28:
                        msg = new RequestWithdrawalPledge();
                        break;
                    case 0x29:
                        msg = new RequestOustPledgeMember();
                        break;
                    case 0x2c:
                        msg = new RequestGetItemFromPet();
                        break;
                    case 0x2e:
                        msg = new RequestAllyInfo();
                        break;
                    case 0x2f:
                        msg = new RequestCrystallizeItem();
                        break;
                    case 0x30:
                        msg = new RequestPrivateStoreManageSell(); // В дампе нету
                        break;
                    case 0x31:
                        msg = new SetPrivateStoreList();
                        break;
                    case 0x32:
                        msg = new Atk(); // RequestPrivateStoreManageCancel ?
                        break;
                    case 0x33:
                        // В дампе нету
                        break;
                    case 0x34:
                        // msg = new SocialAction(); ?
                        break;
                    case 0x35:
                        // msg = new ChangeMoveType(); ?
                        break;
                    case 0x36:
                        //msg = new ChangeWaitType(); ?
                        break;
                    case 0x37:
                        msg = new RequestSellItem();
                        break;
                    case 0x38:
                        // В дампе нету
                        break;
                    case 0x39:
                        msg = new RequestMagicSkillUse();
                        break;
                    case 0x3a: // SendApperingPacket
                        msg = new SAP();
                        break;
                    case 0x3b:
                        if(Config.ALLOW_WAREHOUSE)
                        {
                            msg = new SendWareHouseDepositList();
                        }
                        break;
                    case 0x3c:
                        msg = new SendWareHouseWithDrawList();
                        break;
                    case 0x3d:
                        msg = new RequestShortCutReg();
                        break;
                    case 0x3f:
                        msg = new RequestShortCutDel();
                        break;
                    case 0x40:
                        msg = new RequestBuyItem();
                        break;
                    case 0x41:
                        // В дампе нету
                        break;
                    case 0x42:
                        msg = new RequestJoinParty();
                        break;
                    case 0x43:
                        msg = new RequestAnswerJoinParty();
                        break;
                    case 0x44:
                        msg = new RequestWithDrawalParty();
                        break;
                    case 0x45:
                        msg = new RequestOustPartyMember();
                        break;
                    case 0x46:
                        // msg = new RequestDismissParty(); ?
                        break;
                    case 0x47:
                        msg = new CannotMoveAnymore(); // В дампе нету
                        break;
                    case 0x48:
                        msg = new RequestTargetCanceld();
                        break;
                    case 0x49:
                        msg = new Say2c();
                        break;
                    case 0x4a: // В дампе нету
                        int id_2;
                        if(buf.remaining() >= 2)
                        {
                            id_2 = buf.getShort() & 0xffff;
                        }
                        else
                        {
                            if(Config.PACKET_HANDLER_DEBUG)
                            {
                                _log.log(Level.DEBUG, "Client: " + client + " sent a 0x4a without the second opcode.");
                            }
                            break;
                        }
                        switch(id_2)
                        {
                            case 0x00:
                                // SuperCmdCharacterInfo
                                break;
                            case 0x01:
                                // SuperCmdSummonCmd
                                break;
                            case 0x02:
                                // SuperCmdServerStatus
                                break;
                            case 0x03:
                                // SendL2ParamSetting
                                break;
                            default:
                                printDebugDoubleOpcode(opcode, id_2, buf, state, client);
                                break;
                        }
                        break;
                    case 0x4d:
                        msg = new RequestPledgeMemberList();
                        break;
                    case 0x4f:
                        // В дампе нету
                        break;
                    case 0x50:
                        msg = new RequestSkillList();
                        break;
                    case 0x52:
                        msg = new MoveWithDelta();
                        break;
                    case 0x53:
                        msg = new RequestGetOnVehicle();
                        break;
                    case 0x54:
                        msg = new RequestGetOffVehicle();
                        break;
                    case 0x55:
                        msg = new AnswerTradeRequest();
                        break;
                    case 0x56:
                        msg = new RequestActionUse();
                        break;
                    case 0x57:
                        msg = new RequestRestart();
                        break;
                    case 0x58:
                        msg = new RequestSiegeInfo(); // В дампе нету
                        break;
                    case 0x59:
                        msg = new ValidatePosition();
                        break;
                    case 0x5a:
                        // msg = new RequestSEKCustom(); ?
                        break;
                    case 0x5b:
                        msg = new StartRotating();
                        break;
                    case 0x5c:
                        msg = new FinishRotating();
                        break;
                    case 0x5e:
                        msg = new RequestShowBoard();
                        break;
                    case 0x5f:
                        msg = new RequestEnchantItem();
                        break;
                    case 0x60:
                        msg = new RequestDestroyItem();
                        break;
                    case 0x62:
                        msg = new RequestQuestList(); // В дампе нету
                        break;
                    case 0x63:
                        msg = new RequestQuestAbort();
                        break;
                    case 0x65:
                        msg = new RequestPledgeInfo();
                        break;
                    case 0x66:
                        msg = new RequestPledgeExtendedInfo();
                        break;
                    case 0x67:
                        msg = new RequestPledgeCrest();
                        break;
                    case 0x6A:
                        msg = new RequestFriendInfoList();
                        break;
                    case 0x6b:
                        msg = new RequestSendFriendMsg();
                        break;
                    case 0x6c:
                        msg = new RequestShowMiniMap();
                        break;
                    case 0x6d:
                        // msg = new RequestSendMsnChatLog(); ?
                        break;
                    case 0x6e:
                        msg = new RequestReload();
                        break;
                    case 0x6f:
                        msg = new RequestHennaEquip();
                        break;
                    case 0x70:
                        msg = new RequestHennaRemoveList();
                        break;
                    case 0x71:
                        msg = new RequestHennaItemRemoveInfo();
                        break;
                    case 0x72:
                        msg = new RequestHennaRemove();
                        break;
                    case 0x73:
                        msg = new RequestAcquireSkillInfo();
                        break;
                    case 0x74:
                        msg = new SendBypassBuildCmd();
                        break;
                    case 0x75:
                        msg = new RequestMoveToLocationInVehicle(); // В дампе нету
                        break;
                    case 0x76:
                        msg = new CannotMoveAnymore();
                        break;
                    case 0x77:
                        msg = new RequestFriendInvite();
                        break;
                    case 0x78:
                        msg = new RequestAnswerFriendInvite();
                        break;
                    case 0x79:
                        msg = new RequestFriendList(); // В дампе нету
                        break;
                    case 0x7a:
                        msg = new RequestFriendDel();
                        break;
                    case 0x7b:
                        // msg = new RequestCharacterRestore(); ?
                        break;
                    case 0x7c:
                        msg = new RequestAcquireSkill();
                        break;
                    case 0x7d:
                        msg = new RequestRestartPoint();
                        break;
                    case 0x7e:
                        msg = new RequestGMCommand();
                        break;
                    case 0x7f:
                        msg = new RequestPartyMatchConfig();
                        break;
                    case 0x80:
                        msg = new RequestPartyMatchList();
                        break;
                    case 0x81:
                        msg = new RequestPartyMatchDetail();
                        break;
                    case 0x83:
                        msg = new RequestPrivateStoreBuy();
                        break;
                    case 0x85:
                        msg = new RequestTutorialLinkHtml();
                        break;
                    case 0x86:
                        msg = new RequestTutorialPassCmdToServer();
                        break;
                    case 0x87:
                        msg = new RequestTutorialQuestionMark();
                        break;
                    case 0x88:
                        msg = new RequestTutorialClientEvent();
                        break;
                    case 0x89:
                        msg = new RequestPetition();
                        break;
                    case 0x8a:
                        msg = new RequestPetitionCancel();
                        break;
                    case 0x8b:
                        msg = new RequestGmList();
                        break;
                    case 0x8c:
                        msg = new RequestJoinAlly();
                        break;
                    case 0x8d:
                        msg = new RequestAnswerJoinAlly();
                        break;
                    case 0x8e:
                        msg = new AllyLeave();
                        break;
                    case 0x8f:
                        msg = new AllyDismiss();
                        break;
                    case 0x90:
                        msg = new RequestDismissAlly();
                        break;
                    case 0x91:
                        msg = new RequestSetAllyCrest();
                        break;
                    case 0x92:
                        msg = new RequestAllyCrest();
                        break;
                    case 0x93:
                        msg = new RequestChangePetName();
                        break;
                    case 0x94:
                        msg = new RequestPetUseItem();
                        break;
                    case 0x95:
                        msg = new RequestGiveItemToPet();
                        break;
                    case 0x96:
                        msg = new RequestPrivateStoreQuitSell();
                        break;
                    case 0x97:
                        msg = new SetPrivateStoreMsgSell();
                        break;
                    case 0x98:
                        msg = new RequestPetGetItem();
                        break;
                    case 0x99:
                        msg = new RequestPrivateStoreManageBuy(); // В дампе нету
                        break;
                    case 0x9a:
                        msg = new SetPrivateStoreListBuy();
                        break;
                    case 0x9c:
                        msg = new RequestPrivateStoreQuitBuy();
                        break;
                    case 0x9d:
                        msg = new SetPrivateStoreMsgBuy();
                        break;
                    case 0x9f:
                        msg = new RequestPrivateStoreSell();
                        break;
                    case 0xa6:
                        // msg = new RequestSkillCoolTime(); ?
                        break;
                    case 0xa7:
                        msg = new RequestPackageSendableItemList();
                        break;
                    case 0xa8:
                        msg = new RequestPackageSend();
                        break;
                    case 0xa9:
                        msg = new RequestBlock();
                        break;
                    case 0xaa:
                        msg = new RequestSiegeInfo();
                        break;
                    case 0xab:
                        msg = new RequestSiegeAttackerList();
                        break;
                    case 0xac:
                        msg = new RequestSiegeDefenderList();
                        break;
                    case 0xad:
                        msg = new RequestJoinSiege();
                        break;
                    case 0xae:
                        msg = new RequestConfirmSiegeWaitingList();
                        break;
                    case 0xaf:
                        // msg = new RequestSetCastleSiegeTime(); ?
                        break;
                    case 0xb0:
                        msg = new MultiSellChoose();
                        break;
                    case 0xb2:
                        // msg = new RequestRemainTime(); ?
                        break;
                    case 0xb3:
                        msg = new BypassUserCmd();
                        break;
                    case 0xb4:
                        msg = new SnoopQuit();
                        break;
                    case 0xb5:
                        msg = new RequestRecipeBookOpen();
                        break;
                    case 0xb6: // RequestRecipeItemDelete
                        msg = new RequestRecipeBookDestroy();
                        break;
                    case 0xb7:
                        msg = new RequestRecipeItemMakeInfo();
                        break;
                    case 0xb8:
                        msg = new RequestRecipeItemMakeSelf();
                        break;
                    case 0xb9:
                        // В дампе нету
                        break;
                    case 0xba:
                        msg = new RequestRecipeShopMessageSet();
                        break;
                    case 0xbb:
                        msg = new RequestRecipeShopListSet();
                        break;
                    case 0xbc:
                        msg = new RequestRecipeShopManageQuit();
                        break;
                    case 0xbd:
                        // msg = new RequestRecipeShopManageCancel();
                        break;
                    case 0xbe:
                        msg = new RequestRecipeShopMakeInfo();
                        break;
                    case 0xbf: // RequestRecipeShopMakeDo
                        msg = new RequestRecipeShopMakeItem();
                        break;
                    case 0xc0: // RequestRecipeShopSellList
                        msg = new RequestRecipeShopManagePrev();
                        break;
                    case 0xc1: // RequestObserverEndPacket
                        msg = new ObserverReturn();
                        break;
                    case 0xc2:
                        // Unused (RequestEvaluate/VoteSociality)
                        break;
                    case 0xc3:
                        msg = new RequestHennaDrawList();
                        break;
                    case 0xc4:
                        msg = new RequestHennaItemDrawInfo();
                        break;
                    case 0xc5:
                        msg = new RequestBuySeed();
                        break;
                    case 0xc6:
                        msg = new DlgAnswer();
                        break;
                    case 0xc7:
                        msg = new RequestPreviewItem(); // В дампе нету
                        break;
                    case 0xc8:
                        // msg = new RequestSSQStatus(); ?
                        break;
                    case 0xc9:
                        msg = new RequestPetitionFeedback();
                        break;
                    case 0xcb:
                        msg = new GameGuardReply();
                        break;
                    case 0xcc:
                        msg = new RequestPledgePower();
                        break;
                    case 0xcd:
                        msg = new RequestMakeMacro();
                        break;
                    case 0xce:
                        msg = new RequestDeleteMacro();
                        break;
                    case 0xcf:
                        // В дампе нету
                        break;
                    case 0xd0:
                        int id2;
                        if(buf.remaining() >= 2)
                        {
                            id2 = buf.getShort() & 0xffff;
                        }
                        else
                        {
                            if(Config.PACKET_HANDLER_DEBUG)
                            {
                                _log.log(Level.DEBUG, "Client: " + client + " sent a 0xd0 without the second opcode.");
                            }
                            break;
                        }
                        switch(id2)
                        {
                            case 0x01:
                                msg = new RequestManorList();
                                break;
                            case 0x02:
                                msg = new RequestProcureCropList();
                                break;
                            case 0x03:
                                msg = new RequestSetSeed();
                                break;
                            case 0x04:
                                msg = new RequestSetCrop();
                                break;
                            case 0x05:
                                msg = new RequestWriteHeroWords();
                                break;
                            case 0x06:
                                msg = new RequestExAskJoinMPCC();
                                break;
                            case 0x07:
                                msg = new RequestExAcceptJoinMPCC();
                                break;
                            case 0x08:
                                msg = new RequestExOustFromMPCC();
                                break;
                            case 0x09:
                                msg = new RequestOustFromPartyRoom();
                                break;
                            case 0x0a:
                                msg = new RequestDismissPartyRoom();
                                break;
                            case 0x0b:
                                msg = new RequestWithdrawPartyRoom();
                                break;
                            case 0x0c:
                                msg = new RequestHandOverPartyMaster();
                                break;
                            case 0x0d:
                                msg = new RequestAutoSoulShot();
                                break;
                            case 0x0e:
                                msg = new RequestExEnchantSkillInfo();
                                break;
                            case 0x0f:
                                msg = new RequestExEnchantSkill();
                                break;
                            case 0x10:
                                msg = new RequestExPledgeCrestLarge();
                                break;
                            case 0x11:
                                int id9 = buf.getInt();
                                switch (id9)
                                {
                                    // TODO
                                    case 0x00:
                                    {
                                        msg = new RequestExSetPledgeCrestLarge();// 0
                                        break;
                                    }
                                    case 0x01:
                                    {
                                        msg = new RequestExSetPledgeCrestLarge();// 1
                                        break;
                                    }
                                    case 0x02:
                                    {
                                        msg = new RequestExSetPledgeCrestLarge();// 2
                                        break;
                                    }
                                    case 0x03:
                                    {
                                        msg = new RequestExSetPledgeCrestLarge();// 3
                                        break;
                                    }
                                    case 0x04:
                                    {
                                        msg = new RequestExSetPledgeCrestLarge();// 4
                                        break;
                                    }
                                    default:
                                    {
                                        printDebugDoubleOpcode(opcode, id9, buf, state, client);
                                    }
                                }
                                break;
                            case 0x12:
                                msg = new RequestPledgeSetAcademyMaster();
                                break;
                            case 0x13:
                                msg = new RequestPledgePowerGradeList();
                                break;
                            case 0x14:
                                msg = new RequestPledgeMemberPowerInfo();
                                break;
                            case 0x15:
                                msg = new RequestPledgeSetMemberPowerGrade();
                                break;
                            case 0x16:
                                msg = new RequestPledgeMemberInfo();
                                break;
                            case 0x17:
                                msg = new RequestPledgeWarList();
                                break;
                            case 0x18:
                                msg = new RequestExFishRanking();
                                break;
                            case 0x19:
                                msg = new RequestPCCafeCouponUse();
                                break;
                            case 0x1b:
                                msg = new RequestDuelStart();
                                break;
                            case 0x1c:
                                msg = new RequestDuelAnswerStart();
                                break;
                            case 0x1d:
                                // msg = new RequestExSetTutorial();
                                break;
                            case 0x1e:
                                msg = new RequestExRqItemLink();
                                break;
                            case 0x1f:
                                // В дампе нету
                                break;
                            case 0x20:
                                msg = new MoveToLocationInAirShip(); // В дампе нету
                                break;
                            case 0x21:
                                //								msg = new RequestKeyMapping();
                                break;
                            case 0x22:
                                msg = new RequestSaveKeyMapping();
                                break;
                            case 0x23:
                                msg = new RequestExRemoveItemAttribute();
                                break;
                            case 0x24:
                                msg = new RequestSaveInventoryOrder();
                                break;
                            case 0x25:
                                msg = new RequestExitPartyMatchingWaitingRoom();
                                break;
                            case 0x26:
                                msg = new RequestConfirmTargetItem();
                                break;
                            case 0x27:
                                msg = new RequestConfirmRefinerItem();
                                break;
                            case 0x28:
                                msg = new RequestConfirmGemStone();
                                break;
                            case 0x29:
                                msg = new RequestOlympiadObserverEnd();
                                break;
                            case 0x2a:
                                msg = new RequestCursedWeaponList();
                                break;
                            case 0x2b:
                                msg = new RequestCursedWeaponLocation();
                                break;
                            case 0x2c:
                                msg = new RequestPledgeReorganizeMember();
                                break;
                            case 0x2d:
                                msg = new RequestExMPCCShowPartyMembersInfo();
                                break;
                            case 0x2e:
                                msg = new RequestOlympiadMatchList(); // В дампе нету
                                break;
                            case 0x2f:
                                msg = new RequestAskJoinPartyRoom();
                                break;
                            case 0x30:
                                msg = new AnswerJoinPartyRoom();
                                break;
                            case 0x31:
                                msg = new RequestListPartyMatchingWaitingRoom();
                                break;
                            case 0x32:
                                msg = new RequestExEnchantItemAttribute();
                                break;
                            case 0x35:
                                msg = new MoveToLocationAirShip(); // В дампе нету
                                break;
                            case 0x36:
                                msg = new RequestBidItemAuction();
                                break;
                            case 0x37:
                                msg = new RequestInfoItemAuction();
                                break;
                            case 0x38:
                                msg = new RequestExChangeName();
                                break;
                            case 0x39:
                                msg = new RequestAllCastleInfo();
                                break;
                            case 0x3a:
                                //								msg = new RequestAllFortressInfo();
                                break;
                            case 0x3b:
                                msg = new RequestAllAgitInfo();
                                break;
                            case 0x3c:
                                msg = new RequestFortressSiegeInfo();
                                break;
                            case 0x3d:
                                msg = new RequestGetBossRecord();
                                break;
                            case 0x3e:
                                msg = new RequestRefine();
                                break;
                            case 0x3f:
                                msg = new RequestConfirmCancelItem();
                                break;
                            case 0x40:
                                msg = new RequestRefineCancel();
                                break;
                            case 0x41:
                                msg = new RequestExMagicSkillUseGround();
                                break;
                            case 0x42:
                                msg = new RequestDuelSurrender();
                                break;
                            case 0x43:
                                msg = new RequestExEnchantSkillInfoDetail();
                                break;
                            case 0x45:
                                msg = new RequestFortressMapInfo();
                                break;
                            case 0x46:
                                msg = new RequestPVPMatchRecord();
                                break;
                            case 0x48:
                                msg = new RequestDispel();
                                break;
                            case 0x49:
                                msg = new RequestExTryToPutEnchantTargetItem();
                                break;
                            case 0x4a:
                                msg = new RequestExTryToPutEnchantSupportItem();
                                break;
                            case 0x4b:
                                msg = new RequestExCancelEnchantItem();
                                break;
                            case 0x4c:
                                msg = new RequestChangeNicknameColor();
                                break;
                            case 0x4d:
                                msg = new RequestResetNickname();
                                break;
                            case 0x4e:
                                if(buf.remaining() >= 4)
                                {
                                    id3 = buf.getInt();
                                }
                                else
                                {
                                    _log.log(Level.WARN, "Client: " + client + " sent a 0xd0:0x4e without the third opcode.");
                                    break;
                                }
                                switch(id3)
                                {
                                    case 0x00:
                                        msg = new RequestBookMarkSlotInfo();
                                        break;
                                    case 0x01:
                                        msg = new RequestSaveBookMarkSlot();
                                        break;
                                    case 0x02:
                                        msg = new RequestModifyBookMarkSlot();
                                        break;
                                    case 0x03:
                                        msg = new RequestDeleteBookMarkSlot();
                                        break;
                                    case 0x04:
                                        msg = new RequestTeleportBookMark();
                                        break;
                                    case 0x05:
                                        msg = new RequestChangeBookMarkSlot();
                                        break;
                                    default:
                                        printDebugDoubleOpcode(opcode, id3, buf, state, client);
                                        break;
                                }
                                break;
                            case 0x4f:
                                msg = new RequestWithDrawPremiumItem();
                                break;
                            case 0x50:
                                // msg = new RequestJump(); TODO
                                break;
                            case 0x51:
                                // msg = new RequestStartShowCrataeCubeRank(); ?
                                break;
                            case 0x52:
                                // msg = new RequestStopShowCrataeCubeRank();  ?
                                break;
                            case 0x53:
                                // msg = new NotifyStartMiniGame(); ?
                                break;
//                            case 0x54: Выпелено в последующих тронах
//                                msg = new RequestJoinDominionWar();
//                                break;
//                            case 0x55:
//                                msg = new RequestDominionInfo();
//                                break;
                            case 0x56:
                                // msg = new RequestExCleftEnter(); ?
                                break;
                            case 0x57:
                                msg = new RequestExBlockGameEnter();
                                break;
                            case 0x58:
                                msg = new EndScenePlayer();
                                break;
                            case 0x59:
                                msg = new RequestExBlockGameVote();
                                break;
                            case 0x5a:
                                msg = new RequestListMpccWaiting();
                                break;
                            case 0x5b:
                                msg = new RequestManageMpccRoom();
                                break;
                            case 0x5c:
                                msg = new RequestJoinMpccRoom();
                                break;
                            case 0x5d:
                                msg = new RequestOustFromMpccRoom();
                                break;
                            case 0x5e:
                                msg = new RequestDismissMpccRoom();
                                break;
                            case 0x5f:
                                //msg = new RequestWithdrawMpccRoom(); ?
                                break;
                            case 0x60:
                                msg = new RequestSeedPhase();
                                break;
                            case 0x61:
                                //msg = new RequestMpccPartymasterList(); ?
                                break;
                            case 0x62:
                                msg = new RequestPostItemList();
                                break;
                            case 0x63:
                                msg = new RequestSendPost();
                                break;
                            case 0x64:
                                msg = new RequestRequestReceivedPostList();
                                break;
                            case 0x65:
                                msg = new RequestDeleteReceivedPost();
                                break;
                            case 0x66:
                                msg = new RequestRequestReceivedPost();
                                break;
                            case 0x67:
                                msg = new RequestReceivePost();
                                break;
                            case 0x68:
                                msg = new RequestRejectPost();
                                break;
                            case 0x69:
                                msg = new RequestRequestSentPostList();
                                break;
                            case 0x6a:
                                msg = new RequestDeleteSentPost();
                                break;
                            case 0x6b:
                                msg = new RequestRequestSentPost();
                                break;
                            case 0x6c:
                                msg = new RequestCancelSentPost();
                                break;
                            case 0x6d:
                                // msg = new RequestShowNewUserPetition(); ?
                                break;
                            case 0x6e:
                                // msg = new RequestShowStepTwo(); ?
                                break;
                            case 0x6f:
                                // msg = new RequestShowStepThree(); ?
                                break;
                            case 0x72:
                                msg = new RequestRefundItem();
                                break;
                            case 0x73:
                                msg = new RequestBuySellUIClose();
                                break;
                            case 0x74:
                                // msg = new RequestEventMatchObserverEnd(); ?
                                break;
                            case 0x75:
                                msg = new RequestPartyLootingModify();
                                break;
                            case 0x76:
                                msg = new RequestPartyLootingModifyAgreement();
                                break;
                            case 0x77:
                                msg = new AnswerCoupleAction();
                                break;
                            case 0x78:
                                msg = new RequestBR_EventRankerList();
                                break;
                            case 0x79:
                                //msg = new RequestAskMemberShip(); ?
                                break;
                            case 0x7a:
                                msg = new RequestAddExpandQuestAlarm();
                                break;
                            case 0x7b:
                                msg = new NewVoteSociality();
                                break;
                            case 0x7c:
                                msg = new GetOnShuttle();
                                break;
                            case 0x7d:
                                msg = new GetOffShuttle();
                                break;
                            case 0x80:
                                int id4;
                                if(buf.remaining() >= 2)
                                {
                                    id4 = buf.getShort() & 0xffff;
                                }
                                else
                                {
                                    if(Config.PACKET_HANDLER_DEBUG)
                                    {
                                        _log.log(Level.DEBUG, "Client: " + client + " sent a 0xd0:0х80 without the second opcode.");
                                    }
                                    break;
                                }
                                switch(id4)
                                {
                                    case 0x01:
                                        // msg = new RequestExAgitInitialize(); ?
                                        break;
                                    case 0x02:
                                        // msg = new RequestExAgitDetailInfo(); ?
                                        break;
                                    case 0x03:
                                        // msg = new RequestExMyAgitState(); ?
                                        break;
                                    case 0x04:
                                        // msg = new RequestExRegisterAgitForBidStep1(); ?
                                        break;
                                    case 0x05:
                                        // msg = new RequestExRegisterAgitForBidStep2(); ?
                                        break;
                                    case 0x07:
                                        // msg = new RequestExConfirmCancelRegisteringAgit(); ?
                                        break;
                                    case 0x08:
                                        // msg = new RequestExProceedCancelRegisteringAgit(); ?
                                        break;
                                    case 0x09:
                                        // msg = new RequestExConfirmCancelAgitLot(); ?
                                        break;
                                    case 0x0a:
                                        // msg = new RequestExProceedCancelAgitLot(); ?
                                        break;
                                    case 0x0d:
                                        // msg = new RequestExApplyForBidStep1(); ?
                                        break;
                                    case 0x0e:
                                        // msg = new RequestExApplyForBidStep2(); ?
                                        break;
                                    case 0x0f:
                                        // msg = new RequestExApplyForBidStep3(); ?
                                        break;
                                    case 0x10:
                                        // msg = new RequestExReBid(); ?
                                        break;
                                    case 0x11:
                                        // msg = new RequestExAgitListForLot(); ?
                                        break;
                                    case 0x12:
                                        // msg = new RequestExApplyForAgitLotStep1(); ?
                                        break;
                                    case 0x13:
                                        // msg = new RequestExApplyForAgitLotStep2(); ?
                                        break;
                                    case 0x14:
                                        // msg = new RequestExAgitListForBid(); ?
                                        break;
                                    default:
                                        printDebugDoubleOpcode(opcode, id4, buf, state, client);
                                        break;
                                }
                                break;
                            case 0x81:
                                msg = new RequestExAddPostFriendForPostBox();
                                break;
                            case 0x82:
                                msg = new RequestExDeletePostFriendForPostBox();
                                break;
                            case 0x83:
                                msg = new RequestExShowPostFriendListForPostBox();
                                break;
                            case 0x85:
                                msg = new RequestOlympiadMatchList();
                                break;
                            case 0x86:
                                msg = new RequestBR_GamePoint();
                                break;
                            case 0x87:
                                msg = new RequestBR_ProductList();
                                break;
                            case 0x88:
                                msg = new RequestBR_ProductInfo();
                                break;
                            case 0x89:
                                msg = new RequestBR_BuyProduct();
                                break;
                            case 0x8a:
                                // msg = new RequestBR_RecentProductList(); ?
                                break;
                            case 0x8b:
                                // msg = new RequestBR_MinigameLoadScores(); ?
                                break;
                            case 0x8c:
                                // msg = new RequestBR_MinigameInsertScore(); ?
                                break;
                            case 0x8d:
                                // msg = new RequstBR_LectureMark(); ?
                                break;
                            case 0x8e:
                                msg = new RequestCrystallizeEstimate();
                                break;
                            case 0x8f:
                                msg = new RequestCrystallizeItemCancel();
                                break;
                            case 0x90:
                                msg = new RequestExEscapeScene();
                                break;
                            case 0x91:
                                msg = new RequestFlyMove();
                                break;
                            case 0x92:
                                // msg = new RequestSurrenderPledgeWarEX(); ?
                                break;
                            case 0x93:
                                if(buf.remaining() >= 4)
                                {
                                    id3 = buf.get();
                                }
                                else
                                {
                                    _log.log(Level.WARN, "Client: " + client + " sent a 0xD0:0x93 without the third opcode.");
                                    break;
                                }
                                switch(id3)
                                {
                                    case 0x02:
                                        msg = new RequestDynamicQuestProgressInfo();
                                        break;
                                    case 0x03:
                                        msg = new RequestDynamicQuestScoreBoard();
                                        break;
                                    case 0x04:
                                        msg = new RequestDynamicQuestHTML();
                                        break;
                                    default:
                                        printDebugDoubleOpcode(opcode, id3, buf, state, client);
                                        break;
                                }
                                break;
                            case 0x94:
                                msg = new RequestFriendDetailInfo();
                                break;
                            case 0x95:
                                msg = new RequestUpdateFriendMemo();
                                break;
                            case 0x96:
                                msg = new RequestUpdateBlockMemo();
                                break;
                            case 0x97:
                                msg = new RequestInzonePartyInfoHistory();
                                break;
                            case 0x98:
                                msg = new RequestCommissionRegistrableItemList();
                                break;
                            case 0x99:
                                msg = new RequestCommissionInfo();
                                break;
                            case 0x9a:
                                msg = new RequestCommissionRegister();
                                break;
                            case 0x9b:
                                msg = new RequestCommissionCancel();
                                break;
                            case 0x9c:
                                msg = new RequestCommissionDelete();
                                break;
                            case 0x9d:
                                msg = new RequestCommissionList();
                                break;
                            case 0x9e:
                                msg = new RequestCommissionBuyInfo();
                                break;
                            case 0x9f:
                                msg = new RequestCommissionBuyItem();
                                break;
                            case 0xa0:
                                msg = new RequestCommissionRegisteredItem();
                                break;
                            case 0xa1:
                                msg = new RequestCallToChangeClass();
                                break;
                            case 0xa2:
                                msg = new RequestChangeToAwakenedClass();
                                break;
                            case 0xa3:
                                msg = new RequestWorldStatistics();
                                break;
                            case 0xa4:
                                msg = new RequestUserStatistics();
                                break;
                            case 0xa5:
                                // msg = new RequestWebSessionID(); ?
                                break;
                            case 0xaa:
                                msg = new RequestGoodsInventoryInfo();
                                break;
                            case 0xab:
                                msg = new RequestUseGoodsInventoryItem();
                                break;
                            case 0xac:
                                msg = new RequestFirstPlayStart();
                                break;
                            case 0xad:
                                msg = new RequestFlyMoveStart();
                                break;
                            case 0xae:
                                msg = new RequestHardWareInfo();
                                break;
                            case 0xb0:
                                msg = new SendChangeAttributeTargetItem();
                                break;
                            case 0xb1:
                                msg = new RequestChangeAttributeItem();
                                break;
                            case 0xb2:
                                msg = new RequestChangeAttributeCancel();
                                break;
                            case 0xb3:
                                // msg = new RequestBR_PresentBuyProduct(); ?
                                break;
                            case 0xb4:
                                msg = new ConfirmMenteeAdd();
                                break;
                            case 0xb5:
                                msg = new RequestMentorCancel();
                                break;
                            case 0xb6:
                                msg = new RequestMentorList();
                                break;
                            case 0xb7:
                                msg = new RequestMenteeAdd();
                                break;
                            case 0xb8:
                                msg = new RequestMenteeWaitingList();
                                break;
                            case 0xb9:
                                msg = new RequestJoinPledgeByName();
                                break;
                            case 0xba:
                                msg = new RequestInzoneWaitingTime();
                                break;
                            case 0xbb:
                                msg = new RequestJoinCuriousHouse();
                                break;
                            case 0xbc:
                                msg = new RequestCancelCuriousHouse();
                                break;
                            case 0xbd:
                                msg = new RequestLeaveCuriousHouse();
                                break;
                            case 0xbe:
                                msg = new RequestObservingListCuriousHouse();
                                break;
                            case 0xbf:
                                msg = new RequestObservingCuriousHouse();
                                break;
                            case 0xc0:
                                msg = new RequestLeaveObservingCuriousHouse();
                                break;
                            case 0xc1:
                                msg = new RequestCuriousHouseHtml();
                                break;
                            case 0xc2:
                                msg = new RequestCuriousHouseRecord();
                                break;
                            case 0xc4:
                                msg = new RequestExTryToPutShapeShiftingEnchantSupportItem(); // RequestExTryToPut_Shape_Shifting_EnchantSupportItem ?
                                break;
                            case 0xc5:
                                msg = new RequestExTryToPutShapeShiftingTargetItem(); // RequestExTryToPut_Shape_Shifting_TargetItem ?
                                break;
                            case 0xc6:
                                msg = new RequestExCancelShapeShiftingItem(); // RequestExCancelShape_Shifting_Item ?
                                break;
                            case 0xc7:
                                msg = new RequestShapeShiftingItem(); // RequestShape_Shifting_Item ?
                                break;
                            case 0xc8:
                                // msg = new NCGuardSendDataToServer(); ?
                                break;
                            case 0xc9:
                                //msg = new RequestEventKalieToken();
                                break;
                            case 0xca:
                                msg = new RequestShowBeautyList();
                                break;
                            case 0xcb:
                                msg = new RequestRegistBeauty();
                                break;
                            case 0xcd:
                                // msg = new RequestShowResetShopList(); ?
                                break;
                            case 0xce:
                                msg = new NetPing();
                                break;

                            case 0xcf:
                                // msg = new RequestBR_AddBasketProductInfo(); ?
                                break;
                            case 0xd0:
                                // msg = new RequestBR_DeleteBasketProductInfo(); ?
                                break;
                            case 0xd1:
                                // msg = new RequestBR_NewIConCashBtnWnd(); ?
                                break;
                            case 0xd2:
                                // msg = new RequestExEvent_Campaign_Info(); ?
                                break;
                            case 0xd3:
                                msg = new RequestPledgeRecruitInfo();
                                break;
                            case 0xd4:
                                msg = new RequestPledgeRecruitBoardSearch();
                                break;
                            case 0xd5:
                                msg = new RequestPledgeRecruitBoardAccess();
                                break;
                            case 0xd6:
                                msg = new RequestPledgeRecruitBoardDetail();
                                break;
                            case 0xd7:
                                msg = new RequestPledgeWaitingApply();
                                break;
                            case 0xd8:
                                msg = new RequestPledgeWaitingApplied();
                                break;
                            case 0xd9:
                                msg = new RequestPledgeWaitingList();
                                break;
                            case 0xda:
                                msg = new RequestPledgeWaitingUser();
                                break;
                            case 0xdb:
                                msg = new RequestPledgeWaitingUserAccept();
                                break;
                            case 0xdc:
                                msg = new RequestPledgeDraftListSearch();
                                break;
                            case 0xdd:
                                msg = new RequestPledgeDraftListApply();
                                break;
                            case 0xde:
                                msg = new RequestPledgeRecruitApplyInfo();
                                break;
                            case 0xdf:
                                msg = new RequestPledgeJoinSys();
                                break;
                            case 0xe0:
                                msg = new ResponsePetitionAlarm();
                                break;
                            case 0xe1:
                                msg = new NotifyExitBeautyShop();
                                break;
                            case 0xe2:
                                msg = new RequestRegisterXMasWishCard();
                                break;
                            case 0xe3:
                                msg = new RequestExAddEnchantScrollItem();
                                break;
                            case 0xe4:
                                msg = new RequestExRemoveEnchantSupportItem();
                                break;
                            case 0xe5:
                                msg = new RequestCardReward();
                                break;
                            case 0xe6:
                                msg = new RequestDivideAdenaStart();
                                break;
                            case 0xe7:
                                msg = new RequestDivideAdenaCancel();
                                break;
                            case 0xe8:
                                msg = new RequestDivideAdena();
                                break;
                            case 0xe9:
                                msg = new RequestAcquireAbilityList();
                                break;
                            case 0xea:
                                msg = new RequestAbilityList();
                                break;
                            case 0xeb:
                                msg = new RequestResetAbilityPoint();
                                break;
                            case 0xec:
                                msg = new RequestChangeAbilityPoint();
                                break;
                            case 0xed:
                                // msg = new RequestStopMove(); ?
                                break;
                            case 0xee:
                                msg = new RequestAbilityWndOpen();
                                break;
                            case 0xef:
                                msg = new RequestAbilityWndClose();
                                break;
                            case 0xf0:
                                msg = new ExPCCafeRequestOpenWindowWithoutNPC();
                                break;
                            case 0xf2:
                                // msg = new RequestLuckyGamePlay(); ?
                                break;
                            case 0xf3:
                                // msg = new NotifyTrainingRoomEnd(); ?
                                break;
                            case 0xf4:
                                msg = new RequestNewEnchantPushOne();
                                break;
                            case 0xf5:
                                msg = new RequestNewEnchantRemoveOne();
                                break;
                            case 0xf6:
                                msg = new RequestNewEnchantPushTwo();
                                break;
                            case 0xf7:
                                msg = new RequestNewEnchantRemoveTwo();
                                break;
                            case 0xf8:
                                msg = new RequestNewEnchantClose();
                                break;
                            case 0xf9:
                                msg = new RequestNewEnchantTry();
                                break;
                            case 0xfe:
                                // msg = new ExSendSelectedQuestZoneID(); ?
                                break;
                            case 0xff:
                                msg = new RequestAlchemySkillList();
                                break;
                            case 0x100:
                                msg = new RequestAlchemyTryMixCube();
                                break;
                            case 0x101:
                                msg = new RequestAlchemyConversion();
                                break;
                            case 0x102:
                                // msg = new SendExecutedUIEventsCount();
                                break;
                            case 0x103:
                                // msg = new ExSendClientINI();
                                break;
                            case 0x104:
                                // msg = new RequestExAutoFish();
                                break;
                            default:
                                printDebugDoubleOpcode(opcode, id2, buf, state, client);
                                break;
                        }
                        break;
                    default:
                        printDebug(opcode, buf, state, client);
                        break;
                }
                break;
        }
        return msg;
    }

    private void printDebug(int opcode, ByteBuffer buf, GameClientState state, L2GameClient client)
    {
        client.onUnknownPacket();
        if(!Config.PACKET_HANDLER_DEBUG)
        {
            return;
        }

        int size = buf.remaining();
        _log.log(Level.DEBUG, "Unknown Packet: 0x" + Integer.toHexString(opcode) + " on State: " + state.name() + " Client: " + client);
        byte[] array = new byte[size];
        buf.get(array);
        _log.log(Level.DEBUG, Util.printData(array, size));
    }

    private void printDebugDoubleOpcode(int opcode, int id2, ByteBuffer buf, GameClientState state, L2GameClient client)
    {
        client.onUnknownPacket();
        if(!Config.PACKET_HANDLER_DEBUG)
        {
            return;
        }

        int size = buf.remaining();
        _log.log(Level.DEBUG, "Unknown Packet: 0x" + Integer.toHexString(opcode) + ":0x" + Integer.toHexString(id2) + " on State: " + state.name() + " Client: " + client);
        byte[] array = new byte[size];
        buf.get(array);
        _log.log(Level.DEBUG, Util.printData(array, size));
    }

    @Override
    public L2GameClient create(MMOConnection<L2GameClient> con)
    {
        return new L2GameClient(con);
    }

    @Override
    public void execute(ReceivablePacket<L2GameClient> rp)
    {
        rp.getClient().execute(rp);
    }
}
