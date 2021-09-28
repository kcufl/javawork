package com.kcufl.batch.job.config;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

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
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kcufl.batch.job.model.Member;

import lombok.extern.slf4j.Slf4j;

/**
 * Chunk 방식 + JPA
 */

@Slf4j
@Configuration
@EnableBatchProcessing
public class ChunkJPAJobConfig
{

	private final Logger log = LoggerFactory.getLogger(ChunkJPAJobConfig.class);
	private final String JOB_NAME = "ChunkJPA";

	@Value("${chunkSize:10}") private int chunkSize;
	@Autowired private JobBuilderFactory jobBuilderFactory;
	@Autowired private StepBuilderFactory stepBuilderFactory;
	@Autowired private EntityManagerFactory entityManagerFactory;


	@Bean(name=JOB_NAME + "Job")
	public Job job() throws Exception
	{

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

		return stepBuilderFactory
				.get(JOB_NAME + "Step")
				.<Member,Member>chunk(chunkSize)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.build();

	}


	@StepScope
	@Bean(name=JOB_NAME + "Reader")
	public JpaPagingItemReader<Member> reader() throws Exception
	{

		Map<String,Object> parameterValues = new HashMap<>();
		parameterValues.put("amount", 10000);

		return new JpaPagingItemReaderBuilder<Member>()
				.name(JOB_NAME + "Reader")
				.pageSize(10)
				.parameterValues(parameterValues)
				.queryString("SELECT p FROM Member p WHERE p.amount >= :amount ORDER BY id ASC")
				.entityManagerFactory(entityManagerFactory)
				.build();

	}


	@StepScope
	@Bean(name=JOB_NAME + "Processor")
	public ItemProcessor<Member, Member> processor()
	{

		return new ItemProcessor<Member, Member>()
		{
			@Override
			public Member process(Member member) throws Exception
			{

				//1000원 추가 적립
				member.setAmount(member.getAmount() + 1000);

				return member;
			}
		};

	}


	@StepScope
	@Bean(name=JOB_NAME + "Writer")
	public JpaItemWriter<Member> writer()
	{

		return new JpaItemWriterBuilder<Member>()
				.entityManagerFactory(entityManagerFactory)
				.build();

	}

}