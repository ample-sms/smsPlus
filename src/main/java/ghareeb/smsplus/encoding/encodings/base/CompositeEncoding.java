package ghareeb.smsplus.encoding.encodings.base;

import ghareeb.smsplus.encoding.InvalidCharException;
import ghareeb.smsplus.encoding.InvalidCodeException;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public abstract class CompositeEncoding implements IEncoding {
    private ArrayList<IEncoding> children = new ArrayList<IEncoding>();

    public void addAllEncodings(List<IEncoding> list)
    {
	children.addAll(list);
    }
    
    public void addEncoding(IEncoding encoding) {
	children.add(encoding);
    }

    public void removeEncoding(IEncoding encoding) {
	children.remove(encoding);
    }

    public IEncoding getChildAt(int index) {
	return children.get(index);
    }
    
    public boolean hasEncoding(IEncoding encoding)
    {
	return children.contains(encoding);
    }
    
    public List<IEncoding> getAllChildren()
    {
	return children;
    }

    @Override
    public void encode(char toEncode, BitSet bitSet, int startIndex,
	    int charLength, int offset) throws InvalidCharException {
	
	for(int i = 0; i < children.size(); i++)
	{
	    if(children.get(i).isRecognized(toEncode))
	    {
		children.get(i).encode(toEncode, bitSet, startIndex, charLength, offset + getOffsetOfChildEncoding(i));
		
		return;
	    }
	}
	
	throw new InvalidCharException();
    }

    @Override
    public char decode(BitSet bitSet, int startIndex, int charLength, int offset)
	    throws InvalidCodeException {
	
	char result;
	
	for(int i = 0; i < children.size(); i++)
	{
	    try
	    {
		result = children.get(i).decode(bitSet, startIndex, charLength,  offset + getOffsetOfChildEncoding(i));
		
		return result;
	    }
	    catch(Exception e)
	    {
		
	    }
	}
	
	throw new InvalidCodeException();
    }

    @Override
    public int getMinimumBitsCountPerChar() {
	int charsCount = getCharsCount();

	return (int) Math.ceil(Math.log(charsCount) / Math.log(2.0));
    }

    @Override
    public int getCharsCount() {
	int result = 0;

	for (IEncoding e : children) {
	    result += e.getCharsCount();
	}

	return result;
    }

    @Override
    public boolean isRecognized(char character) {
	for (IEncoding e : children)
	    if (e.isRecognized(character))
		return true;

	return false;
    }
    
    private int getOffsetOfChildEncoding(int childIndex)
    {
	int offset = 0;
	
	for(int i = 0; i < childIndex; i++)
	    offset += children.get(i).getCharsCount();
	
	return offset;
    }
}
