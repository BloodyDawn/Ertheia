package dwo.scripts.npc;

import dwo.gameserver.instancemanager.GraciaSeedsManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 26.04.13
 * Time: 11:49
 */

public class VigilImmortality extends Quest
{
	private static final int[] NPCs = {32539, 32540};

	// Переменные из скриптов
	int inzone_id1 = 121;
	int inzone_id2 = 122;
	int return_x = -212836;
	int return_y = 209824;
	int return_z = 4288;
	String fnHiEnter1 = "vigil_immortality002a.htm";
	String fnHiEnter2 = "vigil_immortality002b.htm";
	String fnHiEnterFail = "vigil_immortality003.htm";
	int loc_x01 = -179537;
	int loc_y01 = 209551;
	int loc_z01 = -15504;
	int loc_x02 = -179779;
	int loc_y02 = 212540;
	int loc_z02 = -15520;
	int loc_x03 = -177028;
	int loc_y03 = 211135;
	int loc_z03 = -15520;
	int loc_x04 = -176355;
	int loc_y04 = 208043;
	int loc_z04 = -15520;
	int loc_x05 = -179284;
	int loc_y05 = 205990;
	int loc_z05 = -15520;
	int loc_x06 = -182268;
	int loc_y06 = 208218;
	int loc_z06 = -15520;
	int loc_x07 = -182069;
	int loc_y07 = 211140;
	int loc_z07 = -15520;
	int loc_x08 = -176036;
	int loc_y08 = 210002;
	int loc_z08 = -11948;
	int loc_x09 = -176039;
	int loc_y09 = 208203;
	int loc_z09 = -11949;
	int loc_x10 = -183288;
	int loc_y10 = 208205;
	int loc_z10 = -11939;
	int loc_x11 = -183290;
	int loc_y11 = 210004;
	int loc_z11 = -11939;
	int loc_x12 = -187776;
	int loc_y12 = 205696;
	int loc_z12 = -9536;
	int loc_x13 = -186327;
	int loc_y13 = 208286;
	int loc_z13 = -9536;
	int loc_x14 = -184429;
	int loc_y14 = 211155;
	int loc_z14 = -9536;
	int loc_x15 = -182811;
	int loc_y15 = 213871;
	int loc_z15 = -9504;
	int loc_x16 = -180921;
	int loc_y16 = 216789;
	int loc_z16 = -9536;
	int loc_x17 = -177264;
	int loc_y17 = 217760;
	int loc_z17 = -9536;
	int loc_x18 = -173727;
	int loc_y18 = 218169;
	int loc_z18 = -9536;

	public VigilImmortality()
	{
		addAskId(NPCs, -1002);
		addAskId(NPCs, -1004);
		addAskId(NPCs, -1006);
		addAskId(NPCs, -1008);
	}

	public static void main(String[] args)
	{
		new VigilImmortality();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ArrayUtils.contains(NPCs, npc.getNpcId()))
		{
			int soiState = GraciaSeedsManager.getInstance().getSoIState(); // TODO
			int i5 = 0;
			int i6 = 0;
			int i7 = 0;
			if(ask == -1002 && reply == 1003)
			{
				if(soiState == 1)
				{
					return fnHiEnterFail;
				}
				else
				{
					return soiState == 2 || soiState == 5 ? fnHiEnter2 : fnHiEnter1;
				}
			}
			else if(ask == -1004 && reply == 1005)
			{
				player.teleToLocation(return_x, return_y, return_z);
			}
			else if(ask == -1006 && reply == 1007)
			{
				if(soiState == 3)
				{
					int i1 = Rnd.get(18) + 1;
					switch(i1)
					{
						case 1:
							i5 = loc_x01;
							i6 = loc_y01;
							i7 = loc_z01;
							break;
						case 2:
							i5 = loc_x02;
							i6 = loc_y02;
							i7 = loc_z02;
							break;
						case 3:
							i5 = loc_x03;
							i6 = loc_y03;
							i7 = loc_z03;
							break;
						case 4:
							i5 = loc_x04;
							i6 = loc_y04;
							i7 = loc_z04;
							break;
						case 5:
							i5 = loc_x05;
							i6 = loc_y05;
							i7 = loc_z05;
							break;
						case 6:
							i5 = loc_x06;
							i6 = loc_y06;
							i7 = loc_z06;
							break;
						case 7:
							i5 = loc_x07;
							i6 = loc_y07;
							i7 = loc_z07;
							break;
						case 8:
							i5 = loc_x08;
							i6 = loc_y08;
							i7 = loc_z08;
							break;
						case 9:
							i5 = loc_x09;
							i6 = loc_y09;
							i7 = loc_z09;
							break;
						case 10:
							i5 = loc_x10;
							i6 = loc_y10;
							i7 = loc_z10;
							break;
						case 11:
							i5 = loc_x11;
							i6 = loc_y11;
							i7 = loc_z11;
							break;
						case 12:
							i5 = loc_x12;
							i6 = loc_y12;
							i7 = loc_z12;
							break;
						case 13:
							i5 = loc_x13;
							i6 = loc_y13;
							i7 = loc_z13;
							break;
						case 14:
							i5 = loc_x14;
							i6 = loc_y14;
							i7 = loc_z14;
							break;
						case 15:
							i5 = loc_x15;
							i6 = loc_y15;
							i7 = loc_z15;
							break;
						case 16:
							i5 = loc_x16;
							i6 = loc_y16;
							i7 = loc_z16;
							break;
						case 17:
							i5 = loc_x17;
							i6 = loc_y17;
							i7 = loc_z17;
							break;
						case 18:
							i5 = loc_x18;
							i6 = loc_y18;
							i7 = loc_z18;
							break;
					}
					player.teleToLocation(i5, i6, i7);
				}
				else if(soiState == 4)
				{
					int i1 = Rnd.get(7) + 1;

					switch(i1)
					{
						case 1:
							i5 = loc_x01;
							i6 = loc_y01;
							i7 = loc_z01;
							break;
						case 2:
							i5 = loc_x02;
							i6 = loc_y02;
							i7 = loc_z02;
							break;
						case 3:
							i5 = loc_x03;
							i6 = loc_y03;
							i7 = loc_z03;
							break;
						case 4:
							i5 = loc_x04;
							i6 = loc_y04;
							i7 = loc_z04;
							break;
						case 5:
							i5 = loc_x05;
							i6 = loc_y05;
							i7 = loc_z05;
							break;
						case 6:
							i5 = loc_x06;
							i6 = loc_y06;
							i7 = loc_z06;
							break;
						case 7:
							i5 = loc_x07;
							i6 = loc_y07;
							i7 = loc_z07;
							break;
					}
					player.teleToLocation(i5, i6, i7);
				}
				else
				{
					return fnHiEnterFail;
				}
			}
			else if(ask == -1008 && reply == 1009)
			{
				// TODO: Вход в инстансы в зависимости от стадии СоИ
				if(soiState == 2 || soiState == 5)
				{
					//myself->InstantZone_Enter(talker,inzone_id1,2);
				}
				else
				{
					return fnHiEnterFail;
				}
			}
		}
		return null;
	}
}