package ghareeb.smsplus.encoding;

import java.util.BitSet;

public class BitsHelper
{

	public static BitSet convertFromByteArray(byte[] bytes)
	{
		BitSet bits = new BitSet();
		for (int i = 0; i < bytes.length * 8; i++)
		{
			// if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0)
			if ((bytes[i / 8] & (1 << (i % 8))) > 0)
			{
				bits.set(i);
			}
		}
		return bits;
	}

	public static byte[] convertToByteArray(BitSet bits)
	{
		byte[] bytes = new byte[(bits.length() + 7) / 8];

		for (int i = 0; i < bits.length(); i++)
		{
			if (bits.get(i))
			{
				// bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);

				bytes[i / 8] |= 1 << (i % 8);
			}
		}

		return bytes;
	}

	public static int convertToDecimal(BitSet bitSet, int startIndex, int length)
	{
		int multiplier = 1;
		int result = 0;

		for (int i = 1; i <= length; i++)
		{
			if (bitSet.get(startIndex))
				result += multiplier;

			multiplier *= 2;
			startIndex++;
		}

		return result;
	}

	public static void convertToBinary(int toEncode, BitSet bitSet, int startIndex, int length)
	{
		int counter = 0;

		while (toEncode > 0)
		{
			if (toEncode % 2 == 1)
				bitSet.set(startIndex);
			else
				bitSet.clear(startIndex);

			toEncode /= 2;
			startIndex++;
			counter++;
		}

		if (counter < length)
		{
			bitSet.clear(startIndex, startIndex + (length - counter - 1));
		}
	}

	public static void copy(BitSet source, int sourceStartIndex, BitSet destination, int destinationStartIndex, int copyLength)
	{
		for (int i = sourceStartIndex, j = destinationStartIndex; i < sourceStartIndex + copyLength; i++, j++)
		{
			if (source.get(i))
			{
				destination.set(j);
			}
		}
	}

	public static byte[] shiftLeft(byte[] original, int shiftPlaces)
	{
		final byte maskOfCarry = (byte) 0x80;
		boolean carry = false;
		byte[] temp;
		byte[] result = new byte[original.length];
		System.arraycopy(result, 0, original, 0, original.length);

		for (int step = 1; step <= shiftPlaces; ++step)
		{

			for (int i = 0; i < result.length; ++i)
			{
				if (carry)
				{
					carry = ((result[i] & maskOfCarry) != 0);
					result[i] <<= 1;
					++result[i];
				}
				else
				{
					carry = ((result[i] & maskOfCarry) != 0);
					result[i] <<= 1;
				}
			}

			if (carry)
			{
				temp = new byte[result.length + 1];
				System.arraycopy(result, 0, temp, 0, result.length);
				++temp[result.length];
				result = temp;
			}
		}

		return result;
	}
}
