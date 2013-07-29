package com.patrick_vane.unrealscript.editor.parser;

import java.util.ArrayList;
import com.patrick_vane.unrealscript.editor.constants.WordConstant;


public class CodeBlock implements Code
{
	protected CodeBlock parent;
	protected ArrayList<Code> childs = new ArrayList<Code>();
	protected ArrayList<CodeWord> lineBeforeBlock;
	protected boolean function;
	
	protected CodeBlockCode currentCode;
	protected boolean closed = false;
	
	
	protected CodeBlock( CodeBlock parent )
	{
		currentCode = new CodeBlockCode( this );
		childs.add( currentCode );
		
		if( parent == null )
		{
			lineBeforeBlock = new ArrayList<CodeWord>();
		}
		else
		{
			lineBeforeBlock = parent.getLastLine();
			if( lineBeforeBlock.size() > 0 )
			{
				function = WordConstant.FUNCTION_KEYWORDS_HASHSET.contains( lineBeforeBlock.get(0).getWord().toLowerCase() );
			}
		}
		
		this.parent = parent;
		if( parent != null )
			parent.addChild( this );
	}
	
	
	@Override
	public boolean isNewWordOpen()
	{
		if( currentCode != null )
			return currentCode.isNewWordOpen();
		return false;
	}
	
	
	@Override
	public void addCharacter( int characterPosition, char character )
	{
		if( currentCode != null )
			currentCode.addCharacter( characterPosition, character );
	}
	@Override
	public void closeWord( int characterPosition )
	{
		if( currentCode != null )
			currentCode.closeWord( characterPosition );
	}
	@Override
	public void newLine()
	{
		if( currentCode != null )
			currentCode.newLine();
	}
	
	
	private void addChild( CodeBlock child )
	{
		if( currentCode != null )
		{
			currentCode.close();
			currentCode = null;
		}
		childs.add( child );
	}
	
	private void closedChild( CodeBlock child )
	{
		if( currentCode != null )
		{
			currentCode.close();
			currentCode = null;
		}
		currentCode = new CodeBlockCode( this );
		childs.add( currentCode );
	}
	
	
	@Override
	public boolean isClosed()
	{
		return closed;
	}
	
	@Override
	public void close()
	{
		if( !closed )
		{
			closed = true;
			
			if( currentCode != null )
			{
				currentCode.close();
				currentCode = null;
			}
			
			if( parent != null )
			{
				parent.closedChild( this );
			}
		}
	}
	
	
	@Override
	public ArrayList<CodeWord> getLastLine()
	{
		ArrayList<CodeWord> line = new ArrayList<CodeWord>();
		int i = 1;
		while( ((line == null) || (line.size() <= 0)) && (childs.size() >= i) )
		{
			line = childs.get( childs.size()-i ).getLastLine();
			i++;
		}
		return line;
	}
	@Override
	public CodeWord getLastCompletedWord()
	{
		ArrayList<CodeWord> lastLine = getLastLine();
		if( (lastLine != null) && (lastLine.size() > 0) )
			return lastLine.get( lastLine.size() - 1 );
		return null;
	}
	
	/** Is this block a function? */
	public boolean isFunction()
	{
		return function;
	}
	@Override
	public CodeBlock getParent()
	{
		return parent;
	}
	public ArrayList<Code> getChilds()
	{
		return childs;
	}
	public ArrayList<CodeWord> getLineBeforeBlock()
	{
		return lineBeforeBlock;
	}
	
	/** Is this block inside a function? (is a parent block of this block a function?) */
	public boolean isInFunction()
	{
		CodeBlock block = parent;
		while( block != null )
		{
			if( block.isFunction() )
				return true;
			block = block.getParent();
		}
		return false;
	}
	
	@Override
	public int getDepth()
	{
		int depth = 0;
		CodeBlock block = parent;
		while( block != null )
		{
			depth++;
			block = block.getParent();
		}
		return depth;
	}
	
	public boolean isParentOf( Code block )
	{
		return childs.contains( block );
	}
	public boolean isChildOf( CodeBlock block )
	{
		return block.isParentOf( this );
	}
	
	/** Starts with 1, returns -1 if it is not a child of this block. */
	public int getChildNumber( Code block )
	{
		for( int i=0; i<childs.size(); i++ )
		{
			if( childs.get(i) == block )
			{
				return i+1;
			}
		}
		return -1;
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((childs == null) ? 0 : childs.hashCode());
		result = prime * result + (function ? 1231 : 1237);
		return result;
	}
	
	@Override
	public boolean equals( Object obj )
	{
		if( this == obj )
			return true;
		if( obj == null )
			return false;
		if( getClass() != obj.getClass() )
			return false;
		CodeBlock other = (CodeBlock) obj;
		if( childs == null )
		{
			if( other.childs != null )
				return false;
		}
		else if( !childs.equals( other.childs ) )
			return false;
		if( function != other.function )
			return false;
		return true;
	}
}
