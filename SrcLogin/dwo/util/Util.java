package dwo.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class Util
{
	public static boolean isInternalIP(String ipAddress)
	{
		InetAddress addr;
		try
		{
			addr = InetAddress.getByName(ipAddress);
			return addr.isSiteLocalAddress() || addr.isLoopbackAddress();
		}
		catch(UnknownHostException e)
		{
			StackTrace.displayStackTraceInformation(e);
		}
		return false;
	}

	public static String printData(byte[] data, int len)
	{
		StringBuilder result = new StringBuilder(len << 2);

		int counter = 0;

		for(int i = 0; i < len; i++)
		{
			if(counter % 16 == 0)
			{
				result.append(fillHex(i, 4)).append(": ");
			}

			result.append(fillHex(data[i] & 0xff, 2)).append(' ');
			counter++;
			if(counter == 16)
			{
				result.append("   ");

				int charpoint = i - 15;
				for(int a = 0; a < 16; a++)
				{
					int t1 = 0xFF & data[charpoint++];
					if(t1 > 0x1f && t1 < 0x80)
					{
						result.append((char) t1);
					}
					else
					{
						result.append('.');
					}
				}

				result.append('\n');
				counter = 0;
			}
		}

		int rest = data.length % 16;
		if(rest > 0)
		{
			for(int i = 0; i < 17 - rest; i++)
			{
				result.append("   ");
			}

			int charpoint = data.length - rest;
			for(int a = 0; a < rest; a++)
			{
				int t1 = 0xFF & data[charpoint++];
				if(t1 > 0x1f && t1 < 0x80)
				{
					result.append((char) t1);
				}
				else
				{
					result.append('.');
				}
			}

			result.append('\n');
		}

		return result.toString();
	}

	public static String fillHex(int data, int digits)
	{
		String number = Integer.toHexString(data);

		for(int i = number.length(); i < digits; i++)
		{
			number = '0' + number;
		}

		return number;
	}

	/**
	 * @param raw
	 * @return
	 */
	public static String printData(byte[] raw)
	{
		return printData(raw, raw.length);
	}

	public static String printData(ByteBuffer buf)
	{
		byte[] data = new byte[buf.remaining()];
		buf.get(data);
		String hex = printData(data, data.length);
		buf.position(buf.position() - data.length);
		return hex;
	}

	public static byte[] generateHex(int size)
	{
		byte[] array = new byte[size];
		Rnd.nextBytes(array);
		return array;
	}

	public static String getStackTrace(Throwable t)
	{
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
}
