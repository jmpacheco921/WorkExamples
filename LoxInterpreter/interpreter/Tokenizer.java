package interpreter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import static interpreter.Token.TokenType.*;

public class Tokenizer {
	
	public static ArrayList<Token> tokenize(String filename) {
	    File inFile = new File(filename); 
	    try {
		    Scanner scanner = new Scanner(inFile);
		  	String line;
			int index, length;
			char character;
			ArrayList<Token> tokens = new ArrayList<>();
			while(scanner.hasNext()) {
				line = scanner.next();
				index = 0;
				length = line.length();
				while (index < length) {
					character = line.charAt(index);
					switch (character) {
						case '(': tokens.add(new Token(LEFT_PAREN)); break;
						case ')': tokens.add(new Token(RIGHT_PAREN)); break;
						case '+': tokens.add(new Token(PLUS)); break;
						case '-': tokens.add(new Token(MINUS)); break;
						case '*': tokens.add(new Token(STAR)); break;
						case '/': tokens.add(new Token(SLASH)); break;
						case '<': tokens.add(new Token(LESS)); break;
						case '>': tokens.add(new Token(GREATER)); break;
						case '=': tokens.add(new Token(EQUAL)); break;
						default:
							if (Character.isDigit(character) || character == '.') { // Handle numbers
								int startIndex = index;
								int decimalPoints = 0; // counter for decimals
								while (index < length && Character.isDigit(character)) {
									index++;
									if (index != length)
										character = line.charAt(index);
								}
								if (character == '.' && index + 1 < length) {
									decimalPoints = 1;
									index++;
									if (index != length)
										character = line.charAt(index);
									while (index < length && Character.isDigit(character)) {
										index++;
										if (index != length)
											character = line.charAt(index);
									}
								}
								if(Character.isDigit(character))
									tokens.add(new Token(NUMBER, line.substring(startIndex, index)));
									//tokens.add(new Token(NUMBER, line.substring(startIndex, index + 1)));
								else {
									if (character == '.' && decimalPoints == 1) { // check for more than one decimal
										System.out.println("Error: more than one decimal point in " + line.substring(startIndex, index + 1));
										System.exit(0);
									}
									tokens.add(new Token(NUMBER, line.substring(startIndex, index)));
									index--;
								}
							} else if (Character.isAlphabetic(character)) { // Handle identifiers and keywords
								int startIndex = index;
								while (index + 1 < length && (Character.isAlphabetic(character) || Character.isDigit(character))) {
									index++;
									character = line.charAt(index);
								}
								String identifier = line.substring(startIndex, index + 1);
								if (Character.isAlphabetic(character) || Character.isDigit(character) || character == '?') {
									if (!isKeyword(identifier, tokens))
										tokens.add(new Token(IDENTIFIER, identifier));
								} else if (character == '(' || character == ')' || character == '"' || character == '<' || character == '>' || character == '=' || character == '+' || character == '-' || character == '*' || character == '/') {
									if (!isKeyword(line.substring(startIndex, index), tokens))
										tokens.add(new Token(IDENTIFIER, line.substring(startIndex, index)));
									index--;
								} else 
									tokens.add(new Token(IDENTIFIER, identifier));
							} else if (character == '"') { // Handle Strings/Symbols
								int startIndex = index + 1;
								String symbol = "";
								character = ';'; // placeholder for loop
								while (index + 1 < length && character != '"') {
									index++;
									character = line.charAt(index);
									if (index + 1 == length && character != '"') {
										symbol += line.substring(startIndex, index + 1) + " ";
										if (scanner.hasNext()) {
											line = scanner.next();
											index = 0;
											startIndex = 0;
											length = line.length();
										} else {
											System.out.println("Error: Unclosed quotation mark");
											System.exit(0);
										}
									}
								}
								symbol += line.substring(startIndex, index);
								tokens.add(new Token(SYMBOL, symbol));
							} else {
								System.out.println("Error: Unexpected character: " + line.charAt(index));
								System.exit(0);
							}
							//index--;
							break;
					}
					index++;
				}
				index = 0;
			}
			/*
					for (Token token : tokens)
						System.out.println(token.getType().toString() + ": " + token.getLexeme());
			/**/
			scanner.close();
			return tokens;
			
		} catch (FileNotFoundException e) {
			System.out.println("Error: File " + filename + " not found.");
			System.exit(0);
		}
	    return null;
	}
	
	// Helper function, adds a keyword token to the list if the string is a keyword, otherwise returns false
	private static boolean isKeyword(String identifier, ArrayList<Token> tokens) {
		if (identifier.equalsIgnoreCase("if")) tokens.add(new Token(IF));
		else if (identifier.equalsIgnoreCase("while")) tokens.add(new Token(WHILE));
		else if (identifier.equalsIgnoreCase("set")) tokens.add(new Token(SET));
		else if (identifier.equalsIgnoreCase("begin")) tokens.add(new Token(BEGIN));
		else if (identifier.equalsIgnoreCase("cons")) tokens.add(new Token(CONS));
		else if (identifier.equalsIgnoreCase("car")) tokens.add(new Token(CAR));
		else if (identifier.equalsIgnoreCase("cdr")) tokens.add(new Token(CDR));
		else if (identifier.equalsIgnoreCase("print")) tokens.add(new Token(PRINT));
		else if (identifier.equalsIgnoreCase("T")) tokens.add(new Token(T));
		else if (identifier.equalsIgnoreCase("define")) tokens.add(new Token(DEFINE));
		else if (identifier.equalsIgnoreCase("number?")) tokens.add(new Token(NUMBERQ));
		else if (identifier.equalsIgnoreCase("symbol?")) tokens.add(new Token(SYMBOLQ));
		else if (identifier.equalsIgnoreCase("list?")) tokens.add(new Token(LISTQ));
		else if (identifier.equalsIgnoreCase("null?")) tokens.add(new Token(NULLQ));
		else return false;
		return true;
	}

}