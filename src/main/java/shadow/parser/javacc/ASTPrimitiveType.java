/* Generated By:JJTree: Do not edit this line. ASTPrimitiveType.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package shadow.parser.javacc;

import org.apache.logging.log4j.Logger;

import shadow.Loggers;
import shadow.typecheck.type.Type;

public
@SuppressWarnings("all")
class ASTPrimitiveType extends SimpleNode {
  private static final Logger logger = Loggers.TYPE_CHECKER;
  
  public ASTPrimitiveType(int id) {
    super(id);
  }

  public ASTPrimitiveType(ShadowParser p, int id) {
    super(p, id);
  }

	public void setImage(String image) {
		this.image = image;
		
		if( image.equals("boolean"))
			type = Type.BOOLEAN;
		else if( image.equals("byte"))
			type = Type.BYTE;
		else if( image.equals("code"))
			type = Type.CODE;
		else if( image.equals("short"))
			type = Type.SHORT;
		else if( image.equals("int"))
			type = Type.INT;
		else if( image.equals("long"))
			type = Type.LONG;
		else if( image.equals("float"))
			type = Type.FLOAT;
		else if( image.equals("boolean"))
			type = Type.BOOLEAN;
		else if( image.equals("double"))
			type = Type.DOUBLE;
		else if( image.equals("ubyte"))
			type = Type.UBYTE;
		else if( image.equals("uint"))
			type = Type.UINT;
		else if( image.equals("ulong"))
			type = Type.ULONG;
		else if( image.equals("ushort"))
			type = Type.USHORT;
		else
			type = Type.UNKNOWN;		
	}

    public void dump(String prefix) {
    	String className = this.getClass().getSimpleName();
    	if(type == null)
    		logger.debug(prefix + className + "(" + lineStart + ":" + columnStart + ")");
    	else
    		logger.debug(prefix + className + "(" + lineStart + ":" + columnStart + "): " + type.getTypeName());
        dumpChildren(prefix);
    }

  /** Accept the visitor. **/
  public Object jjtAccept(ShadowParserVisitor visitor, Boolean secondVisit) throws ShadowException {
    return visitor.visit(this, secondVisit);
  }
}
/* JavaCC - OriginalChecksum=7784e844d05b94f77bb9201999721b64 (do not edit this line) */
