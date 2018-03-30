package cop5556fa17;

import java.util.HashMap;

import cop5556fa17.AST.Declaration;

public class SymbolTable{
	HashMap<String, Declaration> a = new HashMap<String, Declaration>();
	
	Declaration lookupType(String name){
		if(name == null){
			return null;
		}
		else{
			return a.get(name);
		}
	}
	
	void insert(String name, Declaration d){
		a.put(name, d);
	}
}
