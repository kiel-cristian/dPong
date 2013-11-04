package cl.dcc.cc5303;

import java.util.HashMap;

import cl.dcc.cc5303.Utils.Pair;

public class CommandLineParser {
	private HashMap<String, String> optionTypes;
	private HashMap<String, String> defaultVals;
	private HashMap<String, Pair<Integer, String>> lessThanRules;
	private HashMap<String, Pair<Integer, String>> greaterThanRules;
	private CommandLineOptions options;
	private String[] args;
	private boolean parsed;

	public CommandLineParser(String[] args) {
		optionTypes = new HashMap<String, String>();
		defaultVals = new HashMap<String, String>();
		lessThanRules = new HashMap<String, Pair<Integer, String>>();
		greaterThanRules = new HashMap<String, Pair<Integer, String>>();
		options = new CommandLineOptions();
		this.args = args;
	}
	
	public void addIntegerOption(String mark) {
		addIntegerOption(mark, null);
	}
	
	public void addIntegerOption(String mark, Integer defaultVal) {
		addOption(mark, "int", defaultVal.toString());
	}
	
	public void addStringOption(String mark) {
		addStringOption(mark, null);
	}
	
	public void addStringOption(String mark, String defaultVal) {
		addOption(mark, "string", defaultVal.toString());
	}
	
	private void addOption(String mark, String type, String defaultVal) {
		assert(validType(type));
		optionTypes.put(mark, type);
		if (defaultVal != null) {
			defaultVals.put(mark, defaultVal);
		}
	}
	
	public void greaterThanRule(String mark, int value, String errorMsg) {
		greaterThanRules.put(mark, new Pair<Integer, String>(value, errorMsg));
	}
	
	public void lessThanRule(String mark, int value, String errorMsg) {
		lessThanRules.put(mark, new Pair<Integer, String>(value, errorMsg));
	}
	
	public int getInt(String mark, int ifMissing) {
		assert(parsed);
		Integer val = options.integerVals.get(mark);
		if (val == null) return ifMissing;
		else return val;
	}
	
	public String getString(String mark, String ifMissing) {
		assert(parsed);
		String val = options.stringVals.get(mark);
		if (val == null) return ifMissing;
		else return val;
	}
	
	public boolean containsInt(String mark) {
		assert(parsed);
		return options.integerVals.containsKey(mark);
	}
	
	public boolean containsString(String mark) {
		assert(parsed);
		return options.stringVals.containsKey(mark);
	}
	
	private boolean validType(String type) {
		return type.equals("string") || type.equals("int");
	}
	
	public void parse() throws ParserException {
		for (int i=0; i<args.length; i++) {
			String p = args[i];
			String opt = null;
			
			if (p.startsWith("-")) {
				opt = p.substring(1);
			}
			else {
				throw new ParserException("Las opciones deben antecederse con -");
			}
			
			if (args.length > (i+1) && !args[i+1].startsWith("-")) {
				addValue(opt, args[i+1]);
				i++;
			}
			else {
				String defaultVal = defaultVals.get(opt);
				if (defaultVal == null) {
					throw new ParserException("Debe proveer un valor para la opción " + p);
				}
				addValue(opt, defaultVal);
			}
		}
		parsed = true;
	}
	
	private void addValue(String opt, String val) throws ParserException {
		String type = optionTypes.get(opt);
		if (type.equals("int")) {
			int intVal = Integer.parseInt(val);
			checkIntRules(opt, intVal);
			options.integerVals.put(opt, intVal);
		}
		if (type.equals("string")) {
			options.stringVals.put(opt, val);
		}
	}
	
	private void checkIntRules(String opt, int value) throws ParserException {
		checkLessThanRule(opt, value);
		checkGreaterThanRule(opt, value);
	}
	
	private void checkLessThanRule(String opt, int value) throws ParserException {
		Pair<Integer, String> rule = lessThanRules.get(opt);
		if (rule != null) {
			if (!(value < rule.left())) {
				throw new ParserException(rule.right());
			}
		}
	}
	
	private void checkGreaterThanRule(String opt, int value) throws ParserException {
		Pair<Integer, String> rule = greaterThanRules.get(opt);
		if (rule != null) {
			if (!(value > rule.left())) {
				throw new ParserException(rule.right());
			}
		}
	}
	
	public static class ParserException extends Exception {
		private static final long serialVersionUID = 9180470665370371523L;

		public ParserException(String s) {
			super(s);
		}
	}
	
	private class CommandLineOptions {
		private HashMap<String, Integer> integerVals;
		private HashMap<String, String> stringVals;
		
		public CommandLineOptions() {
			integerVals = new HashMap<String, Integer>();
			stringVals = new HashMap<String, String>();
		}
	}
}
