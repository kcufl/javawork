package com.kcufl.comm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MessageManager {

	@Cacheable(value="message")
	public List getMessage(String psMessageId, String psIo) throws JsonParseException, JsonMappingException, IOException
	{
		ClassLoader classLoader = MessageManager.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("message/" + psMessageId + ".json");
		ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        HashMap<String, Object> struct = objectMapper.readValue(inputStream, new TypeReference<HashMap<String, Object>>(){});
/*
		//InputStream inputStream = TypeReference.class.getResourceAsStream("/message/" + psMessageId + ".json");
		ClassPathResource cpr = new ClassPathResource("course.txt");
		HashMap<String, Object> struct = (HashMap<String, Object>) mapper.readValue(cpr.getInputStream(),typeReference);
		*/
		System.out.println("JSON String" + struct.get(psIo).toString() );

		return (ArrayList)((HashMap)struct.get(psIo)).get("fields");
	}

	public String parsingInputMessage(String psMessageId, Map input) throws JsonParseException, JsonMappingException, IOException
	{
		List fields = getMessage(psMessageId, "input");
		Map field = null;

		StringBuffer sb = new StringBuffer();
		String lsName = "";
		String lsType = "";
		Integer liLength = 0;
		String lsPadding = "";
		String lsalignment = "";
		String lsDefault = "";
		String lsValue = "";
		Object loValue = null;

		for ( int i = 0 ; i < fields.size() ; i++)
		{
			field = (HashMap) fields.get(i);

			System.out.println("field" + i + " :: " + field.toString());

			lsName = (String) field.get("name");
			lsType = (String) field.get("type");
			 liLength = (Integer) field.get("length");
			lsPadding = (String) field.get("padding");
			lsalignment = (String) field.get("alignment");
			lsDefault = (String) field.get("default");
			lsValue = "";
			loValue = input.get(lsName);

			if ( loValue != null )
			{
				lsValue = (String)loValue;
			}
			else
			{
				lsValue = lsDefault;
			}

			if (StringUtils.equals(lsalignment, "left"))
			{
				lsValue = StringUtils.leftPad(lsValue,liLength,lsPadding);
			}
			else if (StringUtils.equals(lsalignment, "right"))
			{
				lsValue = StringUtils.rightPad(lsValue,liLength,lsPadding);
			}

			System.out.println("value :: [" + lsValue + "]");

			sb.append(lsValue);
		}

		return sb.toString();
	}

	public Map parsingOutputMessage(String psMessageId, String output) throws JsonParseException, JsonMappingException, IOException
	{
		List fields = getMessage(psMessageId, "output");
		Map field = null;
		Map result = new HashMap();

		StringBuffer sb = new StringBuffer();
		String lsName = "";
		String lsType = "";
		Integer liLength = 0;
		String lsPadding = "";
		String lsalignment = "";
		String lsDefault = "";
		String lsValue = "";
		Object loValue = null;
		int point = 0;
		int offset = 0;

		for ( int i = 0 ; i < fields.size() ; i++)
		{
			point = 0;
			field = (HashMap) fields.get(i);

			System.out.println("field" + i + " :: " + field.toString());

			lsName = (String) field.get("name");
			lsType = (String) field.get("type");
			liLength = (Integer) field.get("length");
			lsPadding = (String) field.get("padding");
			lsalignment = (String) field.get("alignment");
			lsDefault = (String) field.get("default");
			lsValue = output.substring(offset, offset + liLength);

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

		return result;
	}

}
