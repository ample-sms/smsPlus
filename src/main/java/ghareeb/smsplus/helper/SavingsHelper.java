package ghareeb.smsplus.helper;

import ghareeb.smsplus.common.StatisticsBundle;
import ghareeb.smsplus.database.SmsPlusDatabaseHelper;
import ghareeb.smsplus.database.entities.SentMessage;

import java.util.ArrayList;

import android.content.Context;
import android.telephony.SmsManager;

public class SavingsHelper
{
	public static StatisticsBundle calculateSavings(Context context)
	{
		StatisticsBundle result = new StatisticsBundle();
		SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(context);

		result.setDateOfFirstSentMessage(helper.SentMessage_getSendDateOfFirstSentMessage(false));
		process(result, helper);
		
		return result;
	}

	private static void process(StatisticsBundle output, SmsPlusDatabaseHelper helper)
	{
		ArrayList<SentMessage> allSentMessages = helper.SentMessage_getMessageBodyAndPartsCountOfAllSentMessages(false);
		int totalActualParts = 0;
		int totalOrdinaryParts = 0;
		
		for(SentMessage current:allSentMessages)
		{
			totalActualParts += current.getPartsCount();
			totalOrdinaryParts += getOrdinaryPartsCountOfText(current.getBody());
		}
		
		output.setNumberOfOrdinarySentMessageParts( totalOrdinaryParts);
		output.setNumberOfSentMessageActualParts(totalActualParts);
		output.setNumberOfSentMessages(allSentMessages.size());
	}
	
	private static int getOrdinaryPartsCountOfText(String messageBody)
	{
		int smiliesCount = SmiliesHelper.getCountOfSmiliesPatterns(messageBody);
		StringBuilder builder = new StringBuilder(messageBody);
		
		//Adds characters to match ordinary smilies
		for(int i = 1; i <= smiliesCount; i++)
		{
			builder.append('a');
		}
		
		SmsManager manager = SmsManager.getDefault();
		ArrayList<String> parts = manager.divideMessage(builder.toString());

		return parts.size();
	}

}
