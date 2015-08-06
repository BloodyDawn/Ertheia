package dwo.gameserver.network.game.clientpackets;

import dwo.config.network.ConfigGuardEngine;
import dwo.gameserver.engine.guardengine.GuardHwidManager;
import dwo.gameserver.network.game.serverpackets.LoginFail;
import dwo.gameserver.util.crypt.MD5;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 11.09.12
 * Time: 4:25
 */
public class RequestHardWareInfo extends L2GameClientPacket
{
	private String _mac;
	private String _cpu;
	private String _vgaName;
	private String _driverVersion;
	private int _windowsPlatformId;
	private int _windowsMajorVersion;
	private int _windowsMinorVersion;
	private int _windowsBuildNumber;
	private int _DXVersion;
	private int _DXRevision;
	private int _cpuSpeed;
	private int _cpuCoreCount;
	private int _unk8;
	private int _unk9;
	private int _PhysMemory1;
	private int _PhysMemory2;
	private int _unk12;
	private int _videoMemory;
	private int _unk14;
	private int _vgaVersion;

	@Override
	protected void readImpl()
	{
		_mac = readS(); // mac адрес сетевой карты.
		_windowsPlatformId = readD();
		_windowsMajorVersion = readD();
		_windowsMinorVersion = readD();
		_windowsBuildNumber = readD(); // версия операционной системы.
		_DXVersion = readD();
		_DXRevision = readD();
		_cpu = readS(); // cpu установленный в пк.
		_cpuSpeed = readD();
		_cpuCoreCount = readD();
		_unk8 = readD(); // unk
		_unk9 = readD(); // unk
		_PhysMemory1 = readD();
		_PhysMemory2 = readD();
		_unk12 = readD(); // unk
		_videoMemory = readD();
		_unk14 = readD(); // unk
		_vgaVersion = readD();
		_vgaName = readS(); // видеокарта.
		_driverVersion = readS(); // версия драйверов.
	}

	@Override
	protected void runImpl()
	{
		if(ConfigGuardEngine.GUARD_ENGINE_ENABLE)
		{
			if(_mac != null && !_mac.isEmpty())
			{
				if(_driverVersion != null && !_driverVersion.isEmpty())
				{
					// Проверяем на наличие ключа в дллке
					if(!_driverVersion.equals(ConfigGuardEngine.GUARD_ENGINE_STATIC_KEY))
					{
						sendMessageAndClose();
						return;
					}

					// Проверяем на забанненый HWID
					String hwid = MD5.getHash(_mac.replaceAll("-", ""));
					if(GuardHwidManager.getInstance().addAuthorizedClient(getClient(), hwid))
					{
						getClient().setHWID(hwid);
					}
				}
			}
			else
			{
				sendMessageAndClose();
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:B5 RequestHardWareInfo";
	}

	private void sendMessageAndClose()
	{
		getClient().getActiveChar().sendMessage("Для данного ПК вход в игру запрещен.");
		getClient().close(new LoginFail(LoginFail.ACCESS_FAILED_TRY_LATER));
	}
}