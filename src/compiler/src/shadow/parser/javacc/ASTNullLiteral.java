/* Generated By:JJTree: Do not edit this line. ASTNullLiteral.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package shadow.parser.javacc;

public
@SuppressWarnings("all")
class ASTNullLiteral extends SimpleNode {
  public ASTNullLiteral(int id) {
    super(id);
  }

  public ASTNullLiteral(ShadowParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ShadowParserVisitor visitor, Boolean secondVisit) throws ShadowException {
    return visitor.visit(this, secondVisit);
  }
  
  public void dump(String prefix) {
	System.out.println(prefix + "ASTNullLiteral: null");
	dumpChildren(prefix);
  }

}
/* JavaCC - OriginalChecksum=42dd46adc904b5375853fe5a3b87d6fa (do not edit this line) */
