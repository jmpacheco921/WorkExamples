package interpreter;

public class Token {
	
	private TokenType type;
	private String lexeme; // used for identifiers, numbers, and symbols
	
	public enum TokenType {
		// single characters and operators
		LEFT_PAREN, RIGHT_PAREN, PLUS, MINUS, STAR, SLASH, LESS, GREATER, EQUAL,
		// keywords
		IF, WHILE, SET, BEGIN, CONS, CAR, CDR, PRINT, T, DEFINE, 
		// literals
		IDENTIFIER, NUMBER, SYMBOL,
		// functions
		NUMBERQ, SYMBOLQ, LISTQ, NULLQ
	}
	
	public Token(TokenType type, String lexeme) {
		this.setType(type);
		this.setLexeme(lexeme);
	}
	
	public Token(TokenType type) {
		this.setType(type);
		this.setLexeme(null);
	}
	
	public String getLexeme() {
		return lexeme;
	}

	public void setLexeme(String lexeme) {
		this.lexeme = lexeme;
	}

	public TokenType getType() {
		return type;
	}

	public void setType(TokenType type) {
		this.type = type;
	}

}