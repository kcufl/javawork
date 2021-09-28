package com.kcufl.batch.job.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import lombok.extern.slf4j.Slf4j;

/**
 * Chunk 방식 + MaBatis
 */

@Slf4j
@Configuration
@EnableBatchProcessing
public class ChunkMyBatisJobConfig
{

	private final Logger log = LoggerFactory.getLogger(ChunkMyBatisJobConfig.class);
	private final String JOB_NAME = "ChunkMyBatis";

	@Value("${chunkSize:10}") private int chunkSize;
	@Autowired private JobBuilderFactory jobBuilderFactory;
	@Autowired private StepBuilderFactory stepBuilderFactory;
	@Autowired private SqlSessionFactory sqlSessionFactory;


	@Bean(name=JOB_NAME + "Job")
	public Job ExampleJob() throws Exception
	{

		log.info(JOB_NAME + "Job START ========================================>>");

		Job exampleJob = jobBuilderFactory
				.get(JOB_NAME + "Job")
				.start(Step())
				.build();

		return exampleJob;

	}


	@JobScope
	@Bean(name=JOB_NAME + "Step")
	public Step Step() throws Exception
	{

		log.info(JOB_NAME + "Step START ========================================>>");

		return stepBuilderFactory
				.get(JOB_NAME + "Step")
				.<HashMap<String, Object>,HashMap<String, Object>>chunk(chunkSize)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.build();

	}


	@StepScope
	@Bean(name=JOB_NAME + "Reader")
	public ItemReader<HashMap<String, Object>> reader() throws Exception
	{

		log.info(JOB_NAME + "Reader START ========================================>>");

		return new MyBatisPagingItemReaderBuilder<HashMap<String, Object>>()
				.pageSize(10)
				.sqlSessionFactory(sqlSessionFactory)
				//Mapper안에서도 Paging 처리 시 OrderBy는 필수!
				.queryId("com.batch.job.repository.Member.selectAllMemberMap")
				.build();

	}


	@StepScope
	@Bean(name=JOB_NAME + "Processor")
	public ItemProcessor<HashMap<String, Object>, HashMap<String, Object>> processor()
	{

		return new ItemProcessor<HashMap<String, Object>, HashMap<String, Object> >()
		{
			@Override
			public HashMap<String, Object> process(HashMap<String, Object> member) throws Exception
			{
				log.info(JOB_NAME + "Processor START ========================================>>");
				log.info(member.toString());

				//1000원 추가 적립
				member.put("AMOUNT", (Long)member.get("AMOUNT") + 1000 );

				return member;
			}
		};

	}


	@StepScope
	@Bean(name=JOB_NAME + "Writer")
	public MyBatisBatchItemWriter<HashMap<String, Object>> writer()
	{

		log.info(JOB_NAME + "Writer START ========================================>>");

		return new MyBatisBatchItemWriterBuilder<HashMap<String, Object>>()
				//.assertUpdates(false)
				.sqlSessionFactory(sqlSessionFactory)
				.statementId("com.batch.job.repository.Member.insertMember")
				.itemToParameterConverter(createItemToParameterMapConverter())
				.build();

	}

	public static <T> Converter<T, Map<String, Object>> createItemToParameterMapConverter()
	{

		return item -> {
			Map<String, Object> parameter = new HashMap<>();
			parameter.put("name", ((HashMap)item).get("NAME"));
			parameter.put("country", ((HashMap)item).get("COUNTRY"));
			parameter.put("population", ((HashMap)item).get("POPULATION"));
			return parameter;
		};

	}

}