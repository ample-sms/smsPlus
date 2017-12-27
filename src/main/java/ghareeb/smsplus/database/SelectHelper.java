package ghareeb.smsplus.database;

public class SelectHelper
{
	public String getFullyQualifiedCols(Table t)
	{
		StringBuilder builder = new StringBuilder();
		String[] cols = t.getColumnNames();
		String table = t.getTableName();
		
		for(int i = 0; i < cols.length; i++)
		{
			builder.append(table);
			builder.append(".");
			builder.append(cols[i]);
			
			if(i < cols.length - 1)
				builder.append(",");
		}
		
		return builder.toString();
	}
}
