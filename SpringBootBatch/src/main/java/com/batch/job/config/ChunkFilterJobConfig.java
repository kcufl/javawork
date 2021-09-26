package com.batch.job.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.batch.job.model.Member;

import lombok.extern.slf4j.Slf4j;

/**
 * 필터처리방식. Writer에 값을 넘길지 말지를 Processor에서 판단
 * 아래의 코드는 Member 의 id가 짝수일 경우 필터링 하는 예제
 *
 */

@Slf4j
@Configuration
@EnableBatchProcessing
public class ChunkFilterJobConfig
{

	private final Logger log = LoggerFactory.getLogger(ChunkFilterJobConfig.class);
	private final String JOB_NAME = "ChunkFilter";

	@Value("${chunkSize:1000}") private int chunkSize;
	@Autowired private JobBuilderFactory jobBuilderFactory;
	@Autowired private StepBuilderFactory stepBuilderFactory;
	@Autowired private SqlSessionFactory sqlSessionFactory;


	@Bean(name=JOB_NAME + "Job")
	public Job job() throws Exception
	{

		return jobBuilderFactory
				.get(JOB_NAME + "Job")
				.preventRestart()
				.start(step())
				.build();

	}


	@JobScope
	@Bean(name=JOB_NAME + "Step")
	public Step step() throws Exception
	{

		return stepBuilderFactory
				.get(JOB_NAME + "Step")
				.<Member, Member>chunk(chunkSize)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.build();

	}


	@Bean(name=JOB_NAME + "Reader")
	public ItemReader reader() throws Exception
	{

		log.info(JOB_NAME + "Reader START ========================================>>");

		return new MyBatisPagingItemReaderBuilder<Member>()
				.pageSize(10)
				.sqlSessionFactory(sqlSessionFactory)
				//Mapper안에서도 Paging 처리 시 OrderBy는 필수!
				.queryId("com.batch.job.repository.Member.selectAllMemberMap")
				.build();

	}


	@Bean(name=JOB_NAME + "Processor")
	public ItemProcessor<Member, Member> processor()
	{

		return Member ->
		{
			boolean isIgnoreTarget = Member.getId() % 2 == 0L;
			if(isIgnoreTarget)
			{
				log.info(">>>>>>>>> Member name={}, isIgnoreTarget={}", Member.getName(), isIgnoreTarget);
				return null;
			}

			return Member;
		};

	}


	@Bean(name=JOB_NAME + "Writer")
	public ItemWriter<Member> writer()
	{

		return items ->
		{
			for (Member item : items)
			{
				log.info("Member Name={}", item.getName());
			}
		};

	}
}