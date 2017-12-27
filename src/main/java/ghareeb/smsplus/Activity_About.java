package ghareeb.smsplus;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class Activity_About extends Activity
{
	//TODO enhance emoticons bar
	//TODO messages backup
	
	//Potential Languages
	//1-Greek
	//2-Coptic
	//3-Russian (and other Cyrillic)
	//4-Armenian
	//5-Hebrew
	//6-Syriac
	//7-Thai
	//8-Indic (Several)
	//9-Lao
	//10-Georgian
	//11-Ethiopic
	//12-Khmer
	//13-Mongolian
	//14-Hiragana (and Katakana) Japanese
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		TextView faceBookTV = (TextView)findViewById(R.id.textView3);
		faceBookTV.setMovementMethod(LinkMovementMethod.getInstance());
	}
}
