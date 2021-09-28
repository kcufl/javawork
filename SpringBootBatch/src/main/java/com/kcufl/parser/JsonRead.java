package com.kcufl.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;



public class JsonRead {

	public static void main(String[] args) throws JsonParseException, JsonMappingException, FileNotFoundException, IOException {

		Map struct = MessageManagergetMessage("M030", "in");

		List al = (ArrayList) struct.get("fields");

		HashMap test = new HashMap();
		test.put("Occupation11","AAAAAA");
		test.put("Gender111","Male");

		StringBuffer sb = new StringBuffer();

		for ( int i = 0 ; i < al.size() ; i++)
		{
			Map list = (HashMap) al.get(i);

			/*
			String name = (String) test.get(list.get("name"));
			 */

			String lsType = (String) list.get("type");
			Integer liLength = (Integer) list.get("length");
			String lsPadding = (String) list.get("padding");
			String lsalignment = (String) list.get("alignment");
			String lsDefault = (String) list.get("default");
			String lsValue = "";

			if (StringUtils.equals(lsalignment, "left"))
			{
				lsValue = StringUtils.leftPad(lsDefault,liLength,lsPadding);
			}
			else if (StringUtils.equals(lsalignment, "right"))
			{
				lsValue = StringUtils.rightPad(lsDefault,liLength,lsPadding);
			}

			System.out.println("value :: [" + lsValue + "]");
		}

	}

}
