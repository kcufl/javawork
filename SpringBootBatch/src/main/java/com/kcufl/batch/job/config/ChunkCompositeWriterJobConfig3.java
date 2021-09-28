package com.kcufl.batch.job.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
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
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kcufl.batch.job.model.Member;

import lombok.extern.slf4j.Slf4j;

/**
 * 전체 금액이 10,000원 이상인 회원들에게 1,000원 캐시백을 주는 배치
 * @param <T>
 */

@Slf4j
@Configuration
@EnableBatchProcessing
public class ChunkCompositeWriterJobConfig3<T>
{

	private final Logger log = LoggerFactory.getLogger(ChunkCompositeWriterJobConfig3.class);
	private final String JOB_NAME = "ChunkCompositeWriter3";

	@Value("${chunkSize:1000}") private int chunkSize;
	@Autowired private JobBuilderFactory jobBuilderFactory;
	@Autowired private StepBuilderFactory stepBuilderFactory;
	@Autowired private SqlSessionFactory sqlSessionFactory;


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
	public MyBatisPagingItemReader<Member> reader() throws Exception
	{

		Map<String,Object> parameterValues = new HashMap<>();
		parameterValues.put("amount", "10000");

		return new MyBatisPagingItemReaderBuilder<Member>()
				.pageSize(10)
				.sqlSessionFactory(sqlSessionFactory)
				//Mapper안에서도 Paging 처리 시 OrderBy는 필수!
				.queryId("com.finnq.batch.db.mapper.memberMapper.selectMemberInfo")
				.parameterValues(parameterValues)
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


	@Bean(name=JOB_NAME + "Writer")
	public CompositeItemWriter<Member> writer() throws Exception
	{

		List<ItemWriter<? super Member>> member = new ArrayList<>();
		member.add(writer1());
		member.add(writer2());

		CompositeItemWriter<Member> compositeItemWriter = new CompositeItemWriter<>();
		compositeItemWriter.setDelegates(member);
		compositeItemWriter.afterPropertiesSet();

		return compositeItemWriter;

	}


	public MyBatisBatchItemWriter<Member> writer1()
	{

		return new MyBatisBatchItemWriterBuilder<Member>()
				//.assertUpdates(false)
				.sqlSessionFactory(sqlSessionFactory)
				.statementId("com.finnq.batch.db.mapper.memberMapper.insertMember")
				.build();

	}


	public MyBatisBatchItemWriter<Member> writer2()
	{

		return new MyBatisBatchItemWriterBuilder<Member>()
				//.assertUpdates(false)
				.sqlSessionFactory(sqlSessionFactory)
				.statementId("com.finnq.batch.db.mapper.memberMapper.insertMember")
				.build();

	}
}