package com.batch.job.config;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnhanceMybatisLanguageDriver extends XMLLanguageDriver
{

	public EnhanceMybatisLanguageDriver()
	{
	}

	@Override
	public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql)
	{
		try
	{
			addDebuggingComment(boundSql);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
	{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return super.createParameterHandler(mappedStatement, parameterObject, boundSql);
	}

	@Override
	public SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType)
	{
		return super.createSqlSource(configuration, script, parameterType);
	}

	@Override
	public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType)
	{
		return super.createSqlSource(configuration, script, parameterType);
	}

	@SneakyThrows
	private void addDebuggingComment(BoundSql boundSql) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		Field sqlField = BoundSql.class.getDeclaredField("sql");
		sqlField.setAccessible(true);

		String sql = (String) sqlField.get(boundSql);
		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
		sql = addParameterComment(sql, parameterMappings);

		sqlField.set(boundSql, sql);
	}

	private String addParameterComment(String sql, List<ParameterMapping> parameters)
	{
		StringBuilder sqlInternalStringBuilder = new StringBuilder(sql);

		int paramReverseIndex = parameters.size() - 1;
		for (int idx = sql.length() - 1; idx > 0; idx--)
	{
			char c = sql.charAt(idx);
			if (c == '?')
	{
				String commentedString = toCommentString(parameters.get(paramReverseIndex).getProperty());

				sqlInternalStringBuilder.insert(idx + 1, commentedString);
				paramReverseIndex = paramReverseIndex - 1;
			}
		}

		return sqlInternalStringBuilder.toString();
	}

	private String toCommentString(String comment)
	{
		return " /*" + comment + "*/ ";
	}

}

