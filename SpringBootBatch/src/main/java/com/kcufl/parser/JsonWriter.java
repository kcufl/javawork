package com.kcufl.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;



public class JsonWriter {

	public static void main(String[] args) throws JsonParseException, JsonMappingException, FileNotFoundException, IOException {

		ObjectMapper mapper = new ObjectMapper();
		TypeReference<Map<String, Object>> typeReference = new TypeReference<Map<String, Object>>(){};
		InputStream inputStream = TypeReference.class.getResourceAsStream("/conf/M030.json");

		String message = "                test00male  ";

		try {
			HashMap<String, Object> users = (HashMap<String, Object>) mapper.readValue(inputStream,typeReference);
			System.out.println("JSON String" + users.toString() );
			List al = (ArrayList) users.get("fields");

			StringBuffer sb = new StringBuffer();

			int offset = 0;

			HashMap result = new HashMap();

			for ( int i = 0 ; i < al.size() ; i++)
			{
				Map list = (HashMap) al.get(i);

				String lsName = (String) list.get("name");
				String lsType = (String) list.get("type");
				Integer liLength = (Integer) list.get("length");
				String lsPadding = (String) list.get("padding");
				String lsalignment = (String) list.get("alignment");
				String lsDefault = (String) list.get("default");
				String lsValue = "";

				lsValue = message.substring(offset,offset+liLength);
				System.out.println("Origin Value :: [" + lsValue + "]");

				offset += liLength;

				if ( StringUtils.equals (lsalignment,"left") )
				{
					System.out.println("lastIndexOf == " + lsValue.lastIndexOf(lsPadding) );
				}
				else
				{
					System.out.println("indexOf == " + lsValue.indexOf(lsPadding) );
				}

				if ( StringUtils.equals (lsType,"String") )
				{
					if ( StringUtils.equals (lsalignment,"left") )
					{
						lsValue = lsValue.substring(lsValue.lastIndexOf(lsPadding)+1);
					}
					else
					{
						lsValue = lsValue.substring(0,lsValue.lastIndexOf(lsPadding)-1);
					}

					System.out.println("value :: [" + lsValue + "]");
					result.put(lsName, lsValue);
				}
				else if ( StringUtils.equals (lsType,"Integer"))
				{
					System.out.println("value :: [" + Integer.valueOf(lsValue) + "]");
					result.put(lsName, Integer.valueOf(lsValue));
				}
			}

			System.out.println("value :: [" + result.toString() + "]");

		} catch (IOException e){
			System.out.println("Unable to save users: " + e.getMessage());
		}


	}

}
