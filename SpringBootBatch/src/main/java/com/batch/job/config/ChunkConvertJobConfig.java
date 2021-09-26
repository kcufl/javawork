package com.batch.job.config;

import javax.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.batch.job.model.Member;

import lombok.extern.slf4j.Slf4j;

/**
 * Reader에서 읽은 타입을 변환하여 Writer에 전달해주는 것을 얘기합니다.
 * 아래 코드는 Teacher라는 도메인 클래스를 읽어와 Name 필드 (String 타입)을 Wrtier에 넘겨주도록 구성한 코드입니다.
 *
 */

@Slf4j
@Configuration
@EnableBatchProcessing
public class ChunkConvertJobConfig
{

	private final Logger log = LoggerFactory.getLogger(ChunkConvertJobConfig.class);
	private final String JOB_NAME = "ChunkConvert";

	@Value("${chunkSize:1000}") private int chunkSize;
	@Autowired private JobBuilderFactory jobBuilderFactory;
	@Autowired private StepBuilderFactory stepBuilderFactory;
	@Autowired private EntityManagerFactory entityManagerFactory;


	@Bean(name=JOB_NAME + "Job")
	public Job job()
	{
		return jobBuilderFactory
				.get(JOB_NAME + "Job")
				.preventRestart()
				.start(step())
				.build();
	}


	@Bean(name=JOB_NAME + "Step")
	@JobScope
	public Step step()
	{
		return stepBuilderFactory
				.get(JOB_NAME + "Step")
				.<Member, String>chunk(chunkSize)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.build();
	}


	@Bean(name=JOB_NAME + "Reader")
	public JpaPagingItemReader<Member> reader()
	{
		return new JpaPagingItemReaderBuilder<Member>()
				.name(JOB_NAME + "Reader")
				.entityManagerFactory(entityManagerFactory)
				.pageSize(chunkSize)
				.queryString("SELECT t FROM Member t")
				.build();
	}


	@Bean(name=JOB_NAME + "Processor")
	public ItemProcessor<Member, String> processor()
	{
		return Member ->
		{
			return Member.getName();
		};
	}


	@Bean(name=JOB_NAME + "Writer")
	public ItemWriter<String> writer()
	{
		return items ->
		{
			for (String item : items)
			{
				log.info("Member Name={}", item);
			}
		};
	}
}