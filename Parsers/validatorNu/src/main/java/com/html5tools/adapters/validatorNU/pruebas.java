package com.html5tools.adapters.validatorNU;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class pruebas {
	public static void main(String[] args) {
		List<String> as = new ArrayList<String>();
		as.add("=");
		as.add(":");
		
		Collections.sort(as);
		
		for(String a:as){
			System.out.println(a);
		}
	}
}
