package com.batch.job.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
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
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.batch.job.model.Member;

import lombok.extern.slf4j.Slf4j;

/**
 * Chunk 방식 + Composite Processor
 */

@Slf4j
@Configuration
@EnableBatchProcessing
public class ChunkCompositeProcessorJobConfig
{

	private final Logger log = LoggerFactory.getLogger(ChunkCompositeProcessorJobConfig.class);
	private final String JOB_NAME = "ChunkCompositeProcessor";

	@Value("${chunkSize:1000}") private int chunkSize;
	@Autowired private JobBuilderFactory jobBuilderFactory;
	@Autowired private StepBuilderFactory stepBuilderFactory;
	@Autowired private SqlSessionFactory sqlSessionFactory;


	@Bean(JOB_NAME + "Job")
	public Job job() throws Exception
	{

		log.info(JOB_NAME + "Job START ========================================>>");

		return jobBuilderFactory
				.get(JOB_NAME + "Job")
				.preventRestart()
				.start(step())
				.build();

	}


	@SuppressWarnings("unchecked")
	@JobScope
	@Bean(JOB_NAME + "Step")
	public Step step() throws Exception
	{

		log.info(JOB_NAME + "Step START ========================================>>");

		return stepBuilderFactory
				.get(JOB_NAME + "Step")
				.<Member, String>chunk(chunkSize)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.build();

	}


	@StepScope
	@Bean(name=JOB_NAME + "Reader")
	public MyBatisPagingItemReader<Member> reader() throws Exception
	{

		log.info(JOB_NAME + "Reader START ========================================>>");

		Map<String,Object> parameterValues = new HashMap<>();
		parameterValues.put("id", 1);

		return new MyBatisPagingItemReaderBuilder<Member>()
				.pageSize(10)
				.sqlSessionFactory(sqlSessionFactory)
				//Mapper안에서도 Paging 처리 시 OrderBy는 필수!
				.queryId("com.batch.job.repository.Member.selectMemberById")
				.parameterValues(parameterValues)
				.build();

	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean(JOB_NAME + "Processor")
	public CompositeItemProcessor processor()
	{

		log.info(JOB_NAME + "Processor START ========================================>>");

		List<ItemProcessor> delegates = new ArrayList<>(2);
		delegates.add(processor1());
		delegates.add(processor2());

		CompositeItemProcessor processor = new CompositeItemProcessor<>();
		processor.setDelegates(delegates);

		return processor;

	}


	@Bean(JOB_NAME + "Writer")
	public ItemWriter<String> writer()
	{

		log.info(JOB_NAME + "Writer START ========================================>>");

		return items ->
		{
			for (String item : items)
			{
				log.info("Member Name={}", item);
			}
		};

	}


	public ItemProcessor<Member, String> processor1()
	{

		log.info(JOB_NAME + "Processor1 START ========================================>>");
		return Member::getName;

	}


	public ItemProcessor<String, String> processor2()
	{

		log.info(JOB_NAME + "Processor2 START ========================================>>");
		return name -> "안녕하세요. "+ name + "입니다.";

	}

}