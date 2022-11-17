package interpreter;

import java.util.*;
import java.lang.*;
import interpreter.Token.TokenType;
import static interpreter.Token.TokenType.*;


public class Interpreter {

  private ArrayList<String> definitions = new ArrayList<String>(); // function identifiers
  private ArrayList<ArrayList<Token>> functions = new ArrayList<ArrayList<Token>>(); // parallel function bodies
  private ArrayList<String> identifiers = new ArrayList<String>(); // atom/list identifiers
  private ArrayList<ArrayList<Object>> values = new ArrayList<ArrayList<Object>>(); // parallel atom/list values

  // List of all input tokens
  private ArrayList<Token> masterTokens = new ArrayList<Token>();
  
  public Interpreter(ArrayList<Token> tokenList) {
	  masterTokens = tokenList;
  }
  
  public void setTokens(ArrayList<Token> tokenList){
    masterTokens = tokenList;
  }

  public void interpret() {
    int numTokens = masterTokens.size();
    for (int[] index = new int[] {0}; index[0] < numTokens; index[0]++) { // mutable pass by reference index
      evaluateExpr(masterTokens, index);
    }
  }

  private Object evaluateExpr(ArrayList<Token> tokens, int index[]) {
    if (index[0] >= tokens.size())
    	return null;
	TokenType currentType = tokens.get(index[0]).getType();
    //		System.out.println("TOKEN: " + tokens.get(index[0]).getType());
    index[0]++;
    while (currentType != RIGHT_PAREN) {
	    if (currentType == LEFT_PAREN)
	      return createList(tokens, index);
	    else if (currentType == DEFINE)
	      index[0] = defineFunction(tokens, index[0]);
	    else if (currentType == SET) {
	      index[0]++;
	      Token name = tokens.get(index[0] - 1);
	      Object value = evaluateExpr(tokens, index);
	      if (value instanceof Token)
	    	  setExpr(name, new ArrayList<Object>(Arrays.asList(value)));
	      else
	    	  setExpr(name, (ArrayList<Object>) value);
	      
	      
	      /*} else if (currentType == IF)
	      return ifExpr(null, null, null); // TODO: change arguments
	    else if (currentType == WHILE)
	      return whileExpr(null, null); // TODO: change arguments
	      */
	      
	      
	      /*
	    } else if (currentType == BEGIN) {
	    	Object list = evaluateExpr(tokens, index);
	    	if (list instanceof ArrayList<?>)
	    		return beginExpr((ArrayList<Object>) list);
	    	else {
	    		System.out.println("Error evaluating begin expr");
	    		System.exit(0);
	    	}
	    } else if (currentType == CONS) {
	    	  Object list1 = evaluateExpr(tokens, index);
	    	  Object list2 = evaluateExpr(tokens, index);
	    	  if (list1 instanceof ArrayList<?> && list2 instanceof ArrayList<?>)
	    		  return consExpr((ArrayList<Object>) list1, (ArrayList<Object>) list2);
	    	  else {
	    		  System.out.println("Error evaluating = expr");
	    		  System.exit(0);
	    	  }
	    } else if (currentType == CAR) {
	    	Object list = evaluateExpr(tokens, index);
	    	if (list instanceof ArrayList<?>)
	    		return carExpr((ArrayList<Object>) list);
	    	else {
	    		System.out.println("Error evaluating car");
	    		System.exit(0);
	    	}
	    */
	      
	      
	    } else if (currentType == CDR) {
	    	Object list = evaluateExpr(tokens, index);
	    	if (list instanceof ArrayList<?>)
	    		return cdrExpr((ArrayList<Object>) list);
	    	else {
	    		System.out.println("Error evaluating number? expr");
	    		System.exit(0);
	    	}
    	} else if (currentType == NUMBERQ) {
	    	Object list = evaluateExpr(tokens, index);
	    	if (list instanceof ArrayList<?>)
	    		return numberCheckExpr((ArrayList<Object>) list);
	    	else {
	    		System.out.println("Error evaluating number? expr");
	    		System.exit(0);
	    	}
	    } else if (currentType == SYMBOLQ) {
	    	Object list = evaluateExpr(tokens, index);
	    	if (list instanceof ArrayList<?>)
	    		return sysmbolCheckExpr((ArrayList<Object>) list);
	    	else {
	    		System.out.println("Error evaluating symbol? expr");
	    		System.exit(0);
	    	}
    	} else if (currentType == LISTQ) {
	    	Object list = evaluateExpr(tokens, index);
	    	if (list instanceof ArrayList<?>)
	    		return listCheckExpr((ArrayList<Object>) list);
	    	else {
	    		System.out.println("Error evaluating list? expr");
	    		System.exit(0);
	    	}
	    /*
	    } else if (currentType == NULLQ) {
	    	Object list = evaluateExpr(tokens, index);
	    	if (list instanceof ArrayList<?>)
	    		return nullCheckExpr((ArrayList<Object>) list);
	    	else {
	    		  System.out.println("Error evaluating null? expr");
	    		  System.exit(0);
	    	  }
	    */
	    } else if (currentType == PRINT) {
	    	Object toPrint = evaluateExpr(tokens, index);
	    	if (toPrint instanceof Token)
	    		printAtom((Token) toPrint);
	    	else if (toPrint instanceof ArrayList<?>) {
	    	    if (((ArrayList<Object>) toPrint).size() > 1) {
	    	    	System.out.print("(");
	    	    	printList((ArrayList<Object>) toPrint);
	    	    	System.out.print(")");
	    	    } else
	    	    	printList((ArrayList<Object>) toPrint);
	    	} else // TODO: Test for returning nil?
	    		return null;
	    } else if (currentType == PLUS) {
	    	Token token1 = parseAtomToken(evaluateExpr(tokens, index));
        	Token token2 = parseAtomToken(evaluateExpr(tokens, index));
        	return plusExpr(token1, token2);
      } else if (currentType == MINUS) {
    	  Token token1 = parseAtomToken(evaluateExpr(tokens, index));
    	  Token token2 = parseAtomToken(evaluateExpr(tokens, index));
	      return minusExpr(token1, token2);
      } else if (currentType == STAR){
    	  Token token1 = parseAtomToken(evaluateExpr(tokens, index));
    	  Token token2 = parseAtomToken(evaluateExpr(tokens, index));
	      return multiplyExpr(token1, token2);
      } else if (currentType == SLASH) {
    	  Token token1 = parseAtomToken(evaluateExpr(tokens, index));
    	  Token token2 = parseAtomToken(evaluateExpr(tokens, index));
	      return divideExpr(token1, token2);
      } else if (currentType == LESS) {
    	  Token token1 = parseAtomToken(evaluateExpr(tokens, index));
    	  Token token2 = parseAtomToken(evaluateExpr(tokens, index));
	      return lessThanExpr(token1, token2);
      } else if (currentType == GREATER) {
    	  Token token1 = parseAtomToken(evaluateExpr(tokens, index));
    	  Token token2 = parseAtomToken(evaluateExpr(tokens, index));
    	  return greaterThanExpr(token1, token2);
      /*
          } else if (currentType == EQUAL) {
    	  Object list1 = evaluateExpr(tokens, index);
    	  Object list2 = evaluateExpr(tokens, index);
    	  if (list1 instanceof ArrayList<?> && list2 instanceof ArrayList<?>)
    		  return compareExpr((ArrayList<Object>) list1, (ArrayList<Object>) list2);
    	  else {
    		  System.out.println("Error evaluating = expr");
    		  System.exit(0);
    	  }
    	*/
      } else if (currentType == IDENTIFIER) {
    	  String id = tokens.get(index[0] - 1).getLexeme();
    	  int defIndex = definitions.indexOf(id);
          if (defIndex != -1) {
        	ArrayList<Object> arguments = new ArrayList<Object>();
        	Token token = tokens.get(index[0]);
        	int leftParentheses = 1, rightParentheses = 0;
        	while (leftParentheses != rightParentheses) {
        		TokenType type = token.getType();
        		index[0]++;
        		if (type == IDENTIFIER || type == SYMBOL || type == NUMBER)
        			arguments.add(token);
        		else if (type == LEFT_PAREN) {
        			leftParentheses++;
        			createList(tokens, index);
        		} else if (type == RIGHT_PAREN)
        			rightParentheses++;
        		if (index[0] < tokens.size())
        			token = tokens.get(index[0]);
        	}
            return callFunction(functions.get(defIndex), arguments);
          }
	      return getIdValue(id);
      } else if (currentType == NUMBER || currentType == SYMBOL)
	      return tokens.get(index[0] - 1);
      else if (currentType == T)
    	  return true;
	  else
	      System.out.println("Error: invalid Token" + tokens.get(index[0] - 1).toString());
	    if (index[0] < tokens.size())
	    	currentType = tokens.get(index[0]).getType();
	    index[0]++;
	    	
    }
    return null;
  }

  // index is an array that holds one mutable value for keeping track of position
  private ArrayList<Object> createList(ArrayList<Token> tokenList, int[] index) {
    TokenType currentType = tokenList.get(index[0]).getType();
    ArrayList<Object> newList = new ArrayList<Object>();
    while (currentType != RIGHT_PAREN) {
      if (currentType == LEFT_PAREN) {
    	  index[0]++;
        newList.add(createList(tokenList, index));
      } else if (currentType == NUMBER || currentType == SYMBOL || currentType == IDENTIFIER) {
    	  //newList.add(tokenList.get(index[0]));
        newList.add(evaluateExpr(tokenList, index));
        index[0]--;
        if (tokenList.get(index[0]).getType() == RIGHT_PAREN) {
      	  index[0]++;
      	  return newList;
        }
    } else {
        Object eval = evaluateExpr(tokenList, index);
        if (eval != null) {
          newList.add(eval);
          if (tokenList.get(index[0]).getType() == RIGHT_PAREN) {
        	  index[0]++;
        	  return newList;
          }
        } else
        	return newList; // TODO: TEST to see if this needs to be replaced by index[0]++ in if branch
      }
      index[0]++;
      if (index[0] < tokenList.size())
    	  currentType = tokenList.get(index[0]).getType();
      else
    	  return newList;
    }
    return newList;
  }
  
  // Helper for retrieving tokens from lists
  private Token parseAtomToken(Object object) {
	  if (object instanceof Token)
		  return (Token) object;
	  else if (object instanceof ArrayList<?>) {
		  ArrayList<Object> list = (ArrayList<Object>) object;
		  while (list.size() == 1) {
			  if (list.get(0) instanceof Token)
				  return (Token) list.get(0);
			  else if (list.get(0) instanceof ArrayList<?>)
				  list = (ArrayList<Object>)list.get(0);
		  }
	  }
	  System.out.println("Error: operation can only be used on atoms.");
	  System.exit(0);
	  return null;
  }

  private int defineFunction(ArrayList<Token> tokenList, int index) {
    Token id = (Token) tokenList.get(index);
    index++;
    Token token = (Token) tokenList.get(index);
    index++;
    if (id.getType() == IDENTIFIER) {
      if (token.getType() == LEFT_PAREN) {
        int leftParentheses = 2, rightParentheses = 0;
        definitions.add(id.getLexeme());
        int defIndex = definitions.indexOf(id.getLexeme());
        functions.add(new ArrayList<Token>());
        functions.get(defIndex).add(token);
        while (leftParentheses != rightParentheses) {
          token = (Token) tokenList.get(index);
          index++;
          functions.get(defIndex).add(token);
          if (token.getType() == RIGHT_PAREN)
            rightParentheses++;
          else if (token.getType() == LEFT_PAREN)
            leftParentheses++;
        }
      } else {
        System.out.println("Error: '(' needed after define " + id.getLexeme());
        System.exit(0);
      }
    } else {
      System.out.println("Error: function identifier needed after define keyword.");
      System.exit(0);
    }
    return index;
  }

  private void printList(ArrayList<Object> expr) {
    int length = expr.size();
    for (int i = 0; i < length; i++) {
    	if (expr.get(i) instanceof ArrayList<?>) {
    		if (((ArrayList<Object>) expr.get(i)).size() == 1 && ((ArrayList<Object>) expr.get(i)).get(0) instanceof Token)
        		printList((ArrayList<Object>) expr.get(i));
    		else {
    			System.out.print("(");
	    		printList((ArrayList<Object>) expr.get(i));
		    	System.out.print(")");
    		}
	    	//printList((ArrayList<Object>) toPrint);
    	} else if (expr.get(i) instanceof Token) {
    		Token token = (Token) expr.get(i);
    		TokenType type = token.getType();
    		if (type == NUMBER || type == SYMBOL)
    			printAtom(token);
    		else if (type == IDENTIFIER) {
    			ArrayList<Object> value = getIdValue(token.getLexeme());
    			if (value != null) {
    				printList(value);
    			} else {
    				System.out.println("Error: " + token.getLexeme() + " not initialized.");
    				System.exit(0);
    			}
    		} else {
    			System.out.println("Error: Attempted to print something that is not a list or atom.");
        		System.exit(0);
    		}
    	} else {
    		System.out.println("Error: Attempted to print something that is not a list or atom.");
    		System.exit(0);
    	}
    	if (length > 1 && i + 1 != length)
    		System.out.print(" ");
    }
  }

  private void printAtom(Token expr) {
	String lexeme = expr.getLexeme();
    if (lexeme != null) {
      System.out.print(lexeme.replace("\\n", "\n"));
    } else {
      System.out.println("Error: print called on unexpected token " + expr.getType().toString());
      System.exit(0);
    }
  }
  
  private void ifExpr(ArrayList<Token> condExpr, ArrayList<Token> trueExpr, ArrayList<Token> falseExpr){
		int []help;
		boolean bruh = (boolean)evaluateExpr(condExpr, help);
		if (bruh) evaluateExpr(trueExpr, help);
		else evaluateExpr(falseExpr, help);
		/*switch(condExpr.get(1).getType()){
			case LESS:
				/*String help = lessThanExpr(condExpr.get(2), condExpr.get(3));
				if(help=="T"){
evaluateExpr(trueExpr)
}else{ evaluateExpr(falseExpr)
}
				break;
			case GREATER:
				break;
			case EQUAL:
				break;
			case NUMBERQ:
				break;
			case SYMBOLQ:
				break;
			case LISTQ:
				break;
			case NULLQ:
				break;
			default:
				System.exit(0);
				break;
		}*/
  }

  private void whileExpr(ArrayList<Object> cond, ArrayList<Token> exprBody){
    while(!nullCheckExpr(cond)){
      beginExpr(exprBody);
    }
  }
	
  private void setExpr(Token identifier, ArrayList<Object> value) {
	if (identifier.getType() == IDENTIFIER) {
		if (getIdValue(identifier.getLexeme()) == null) {
	        identifiers.add(identifier.getLexeme());
	        if (value.size() == 1 && value.get(0) instanceof Token) {
	        	Token token = (Token) value.get(0);
	        	if (token.getType() == IDENTIFIER)
	        		values.add(values.get(identifiers.indexOf(token.getLexeme())));
	        	else 
	        		values.add(value);
	        } else
	        	values.add(value);
	      } else {
		      if (value.size() == 1 && value.get(0) instanceof Token) {
			     Token token = (Token) value.get(0);
			   	 if (token.getType() == IDENTIFIER)
			   		values.set(identifiers.indexOf(identifier.getLexeme()), values.get(identifiers.indexOf(token.getLexeme())));
			   	 else 
			      	values.set(identifiers.indexOf(identifier.getLexeme()), value);
		      } else
		      		values.set(identifiers.indexOf(identifier.getLexeme()), value);
	      }
      } else {
      System.out.println("Error: invalid identifier to be set: " + identifier.toString());
      System.exit(0);
    }
  }

  // Retrieves values for identifiers or makes function calls
  private ArrayList<Object> getIdValue(String identifier) {
    ArrayList<Object> value = null;
    int index = identifiers.indexOf(identifier);
    if (index != -1) 
      value = values.get(index);
    else {
      index = definitions.indexOf(identifier);
      if (index != -1) {
        Object object = evaluateExpr(functions.get(index), new int[] {0});
        if (object instanceof Token)
          value = new ArrayList<Object>(Arrays.asList(object));
        else if (object instanceof ArrayList<?>)
          return (ArrayList<Object>) object;
      }
    }
    return value;
  }
  
  private Object callFunction(ArrayList<Token> tokens, ArrayList<Object> arguments) {
	  Token token = tokens.get(0);
	  int index = 1;
	  int oldIdsSize = identifiers.size();
	  if (token.getType() == LEFT_PAREN) {
		  token = tokens.get(index);
		  while (token.getType() != RIGHT_PAREN) {
			  Object argument = arguments.get(index - 1);
			  if (argument instanceof Token)
				  setExpr(token, new ArrayList<Object>(Arrays.asList(argument)));
			  else if (argument instanceof ArrayList<?>)
				  setExpr(token, (ArrayList<Object>) argument);
			  index++;
			  token = tokens.get(index);
		  }
		  Object evaluation = evaluateExpr(tokens, new int[] {index + 1});
		  int newIdsSize;
		  for (newIdsSize = identifiers.size(); newIdsSize > oldIdsSize; newIdsSize--) {
			  identifiers.remove(newIdsSize - 1);
			  values.remove(newIdsSize - 1);
		  }
		  return evaluation;
	  } else {
		  System.out.println("Error: Function defined improperly.");
		  System.exit(0);
	  }
	  return null;
  }

  private void beginExpr(ArrayList<Token> exprs){
    int numToken = exprs.size();
    for (int[] index = new int[] {0}; index[0] < numTokens; index[0]++)
      evaluateExpr(exprs,index);
  }
  
  private Token plusExpr(Token num1, Token num2){
		double help = 0, help2 = 0;
		try{
			help = Double.parseDouble(num1.getLexeme());
			help2 = Double.parseDouble(num2.getLexeme());
    }catch(Exception e){
			System.out.println(e);
			System.exit(0);
		}
		Token sender = new Token (NUMBER, String.valueOf(help+help2));
		return sender;
  }

  private Token minusExpr(Token num1, Token num2){
    double help = 0, help2 = 0;
    try{
      help = Double.parseDouble(num1.getLexeme());
			help2 = Double.parseDouble(num2.getLexeme());
    }catch(Exception e){
			System.out.println(e);
			System.exit(0);
    }
    Token sender = new Token (NUMBER, String.valueOf(help-help2));
		return sender;
  }

  private Token multiplyExpr(Token num1, Token num2){
    double help = 0, help2 = 0;
		try{
			help = Double.parseDouble(num1.getLexeme());
			help2 = Double.parseDouble(num2.getLexeme());
    }catch(Exception e){
			System.out.println(e);
			System.exit(0);
		}
		Token sender = new Token (NUMBER, String.valueOf(help*help2));
		return sender;
  }
  
  private Token divideExpr(Token num1, Token num2){
    double help = 0, help2 = 0;
    try{
      help = Double.parseDouble(num1.getLexeme());
			help2 = Double.parseDouble(num2.getLexeme());
    }catch(Exception e){
			System.out.println(e);
			System.exit(0);
    }
    Token sender = new Token (NUMBER, String.valueOf(help/help2));
		return sender;
  }

	private int countList(ArrayList<Token> tlist){
		int count = 0, paren = 0;
		if (tlist!=null){
			switch (tlist.get(0).getType()){
				case LEFT_PAREN:
					for (int i=0;i<tlist.size();i++){
						if(paren!=0){
							if (tlist.get(i).getType()==LEFT_PAREN) paren++;
							else if (tlist.get(i).getType()==RIGHT_PAREN) paren--;
						}else{
							if (tlist.get(i).getType()==LEFT_PAREN) paren++;
							count++;
						}
					}
					break;
				case NUMBER:
					count++; break;
				case SYMBOL: count++; break;
				default: System.out.println("Unexpected token"); System.exit(0); break;
			}
		}
		return count;
	}
	
  private boolean compareExpr(ArrayList<Token> list1, ArrayList<Token> list2){
		ArrayList<Token> helper1 = new ArrayList<Token>();
		getIdValue(list1.get(0).getLexeme());
		ArrayList<Token> helper2 = new ArrayList<Token>();
		getIdValue(list2.get(0).getLexeme());
		int count1 = 0, count2 = 0;
		
		switch (list1.get(0).getType()){
			case IDENTIFIER:
				if (helper1!=null) count1 = countList(helper1);
				else count1 = 1;
				break;
				
			case LEFT_PAREN: count1 = countList(list1); break;
				
			case NUMBER: count1 = 1; break;

			case SYMBOL: count1 = 1; break;
				
			default: break;
		}
		switch (list2.get(0).getType()){
			case IDENTIFIER:
				if (helper2!=null) count2 = countList(helper2);
				else count2 = 1;
				break;
				
			case LEFT_PAREN: count2 = countList(list2); break;
				
			case NUMBER: count2 = 1; break;

			case SYMBOL: count2 = 1; break;
				
			default: break;
		}
		
		if (count1 == count2) return true;
		return false;
  }

  private boolean lessThanExpr(Token num1, Token num2){
		double help = 0;
		double help2 = 0;
		try{
			help = Double.parseDouble(num1.getLexeme());
			help2 = Double.parseDouble(num2.getLexeme());
		}catch(Exception e){
			System.out.println(e);
			System.exit(0);
		}
		if(help < help2) return true;
    return false;
  }

  private boolean greaterThanExpr(Token num1, Token num2){
    double help = 0;
		double help2 = 0;
		try{
			help = Double.parseDouble(num1.getLexeme());
			help2 = Double.parseDouble(num2.getLexeme());
		}catch(Exception e){
			System.out.println(e);
			System.exit(0);
		}
		if(help > help2) return true;
    return false;
  }

  private ArrayList<Object> consExpr(ArrayList<Object> newCar, ArrayList<Object> newCdr){
    ArrayList<Object> returnVal = new ArrayList<Object>;
    for(int i = 0; i < newCar.size(); i++){
      if(newCar.get(i) instanceof Token){
        Token token = newCar.get(i);
        if(token.getType == IDENTIFIER){
          if(getIdValue(token.getLexeme()) == null){
            System.out.println("Element of newCar: " + token.getLexeme() + " is an uninitialized variable");
            System.exit(0);
          }
        }
      }
    } // Make this a method if this becomes an issue
    returnVal.add(newCar);
    for(int i = 0; i < newCdr.size(); i++){
      if(newCdr.get(i) instanceof Token){
        Token token = newCdr.get(i);
        if(token.getType == IDENTIFIER){
          if(getIdValue(token.getLexeme()) == null){
            System.out.println("Element of newCdr: " + token.getLexeme() + " is an uninitialized variable");
            System.exit(0);
          }
        }
      }
    }
    returnVal.add(newCdr);
    
    return returnVal
  }
  
  private ArrayList<Object> carExpr(ArrayList<Object> expr){
    if (expr.size() == 0)
      return new ArrayList<Object>();
    return expr.get(0);
  }

  private ArrayList<Object> cdrExpr(ArrayList<Object> expr){
    ArrayList<Object> cdr = new ArrayList<Object>();
    if(expr.size() >= 2){
      for(int i = 1; i < expr.size(); i++)
        cdr.add(expr.get(i));
    }
    return cdr;
  }

  private boolean numberCheckExpr(ArrayList<Object> exprs){
    if(exprs.size() != 1 || exprs.get(0) instanceof ArrayList)
      return false;
    Token expr = (Token)exprs.get(0);
    if(expr.getType() == NUMBER)
      return true;
    if (expr.getType() == IDENTIFIER){
      ArrayList<Object> temp  = getIdValue(expr.getLexeme());
      if(temp == null){
        System.out.println("Call to uninitialized variable: "+ expr.getLexeme());
        System.exit(0);
      }
      if(temp.size() != 1  || temp.get(0) instanceof ArrayList)
        return false;
      Token atom = (Token)temp.get(0);
      if(atom.getType() == NUMBER)
        return true;
    }
    return false;
  }

  private boolean sysmbolCheckExpr(ArrayList<Object> exprs){
    if(exprs.size() != 1 || exprs.get(0) instanceof ArrayList)
      return false;
    Token expr = (Token)exprs.get(0);
    if(expr.getType() == SYMBOL)
      return true;
    if (expr.getType() == IDENTIFIER){
      ArrayList<Object> temp  = getIdValue(expr.getLexeme());
      if(temp == null){
        System.out.println("Call to uninitialized variable: "+ expr.getLexeme());
        System.exit(0);
      }
      if(temp.size() != 1  || temp.get(0) instanceof ArrayList)
        return false;
      Token atom = (Token)temp.get(0);
      if(atom.getType() == SYMBOL)
        return true;
    }
    return false;
  }

  private boolean listCheckExpr(ArrayList<Object> expr){
    if(expr.size() == 0 ||expr.get(0) instanceof ArrayList || expr.size() > 1)
      return true;
    Token token = (Token)expr.get(0);
    if((token.getType() == NUMBER || token.getType() == SYMBOL))
      return false;
    if(token.getType() == IDENTIFIER){
      ArrayList<Object> temp  = getIdValue(token.getLexeme());
      if(temp == null){
        System.out.println("Call to uninitialized variable: "+ token.getLexeme());
        System.exit(0);
      }
      if(expr.size() == 0 ||expr.get(0) instanceof ArrayList || expr.size() > 1)
        return true;
    }
    return false;
  }
  private boolean nullCheckExpr(ArrayList<Object> expr){
    for(int i = 0; i < expr.size(); i++){
      if(expr.get(i) instanceof ArrayList){
        if(nullCheckExpr(expr.get(i)) == false)
          return false;
      }else if(expr.get(i) instanceof Token)
        return false;
    }
    return true;
  }
}