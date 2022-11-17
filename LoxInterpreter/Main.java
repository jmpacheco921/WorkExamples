import interpreter.*;

public class Main {
  
	public static void main(String[] args) {
    //if (args != null && args.length > 0 && args[0] != null) {
        String inputFile = "basicTest.Lisp";
		Interpreter interpreter = new Interpreter(Tokenizer.tokenize(inputFile));
		interpreter.interpret();

    //}
	}
}