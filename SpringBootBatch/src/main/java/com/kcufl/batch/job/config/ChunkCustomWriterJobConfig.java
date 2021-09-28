package com.kcufl.batch.job.config;

import java.util.List;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kcufl.batch.job.model.Member;
import com.kcufl.batch.job.model.Member2;

import lombok.extern.slf4j.Slf4j;

/**
 * 전체 금액이 10,000원 이상인 회원들에게 1,000원 캐시백을 주는 배치
 * @param <T>
 */

@Slf4j
@Configuration
@EnableBatchProcessing
public class ChunkCustomWriterJobConfig
{

	private final Logger log = LoggerFactory.getLogger(ChunkCustomWriterJobConfig.class);
	private final String JOB_NAME = "ChunkCustomWriter";

	@Value("${chunkSize:1}") private int chunkSize;
	@Autowired private JobBuilderFactory jobBuilderFactory;
	@Autowired private StepBuilderFactory stepBuilderFactory;
	@Autowired private SqlSessionFactory sqlSessionFactory;


	@Bean(name=JOB_NAME + "Job")
	public Job job()
	{

		return jobBuilderFactory
				.get(JOB_NAME + "Job")
				.start(step())
				.build();

	}


	@Bean(name=JOB_NAME + "Step")
	public Step step()
	{

		return stepBuilderFactory
				.get(JOB_NAME + "Step")
				.<Member, Member2>chunk(chunkSize)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.build();

	}


	@Bean(name=JOB_NAME + "Reader")
	public ItemReader<Member> reader()
	{

		log.info(JOB_NAME + "Reader START ========================================>>");

		return new MyBatisPagingItemReaderBuilder<Member>()
				.pageSize(10)
				.sqlSessionFactory(sqlSessionFactory)
				//Mapper안에서도 Paging 처리 시 OrderBy는 필수!
				.queryId("com.batch.job.repository.Member.selectAllMember")
				.build();

	}


	@Bean(name=JOB_NAME + "Processor")
	public ItemProcessor<Member, Member2> processor()
	{

		return Member -> new Member2(Member.getName(), Member.getCountry(), Member.getPopulation());

	}


	@Bean(name=JOB_NAME + "Writer")
	public ItemWriter<Member2> writer()
	{

		return new ItemWriter<Member2>()
		{
			@Override
			public void write(List<? extends Member2> items) throws Exception
			{
				log.info(JOB_NAME + "Writer START ========================================>>");

				for (Member2 item : items)
				{
					System.out.println("────────────────────────────────────────────────────────");
					System.out.println(item.getId());
					System.out.println(item.getName());
					System.out.println(item.getCountry());
					System.out.println(item.getPopulation());
					System.out.println(item.getAmount());
					System.out.println("────────────────────────────────────────────────────────");
				}
			}
		};

	}
}