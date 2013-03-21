package com.patrick_vane.unrealscript.editor.parser;

import com.patrick_vane.unrealscript.editor.UnrealScriptEditor;
import com.patrick_vane.unrealscript.editor.class_hierarchy.parser.UnrealScriptClass;


public class UnrealScriptAttributeParser
{
	public static UnrealScriptAttributes parseAttributes( String className )
	{
		UnrealScriptAttributes attributes = new UnrealScriptAttributes();
		try
		{
			UnrealScriptClass thisClass = UnrealScriptEditor.getUnrealScriptClass( className );
			if( thisClass != null )
			{
				attributes.addAttributes(  UnrealScriptParser.getAttributes( thisClass.getName(), UnrealScriptParser.parse(thisClass.getFile()) )  );
				
				UnrealScriptClass parentClass = thisClass.getParent();
				while( parentClass.getParent() != null )
				{
					attributes.addAttributes(  UnrealScriptParser.getAttributes( parentClass.getName(), UnrealScriptParser.parse(parentClass.getFile()) )  );
					parentClass = parentClass.getParent();
				}
			}
		}
		catch( Exception e )
		{
		}
		return attributes;
	}
}