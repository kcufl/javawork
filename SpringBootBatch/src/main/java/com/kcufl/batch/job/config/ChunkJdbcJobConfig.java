package com.kcufl.batch.job.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

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
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.kcufl.batch.job.model.Member;

import lombok.extern.slf4j.Slf4j;

/**
 * Chunk 방식 + JDBC
 */

@Slf4j
@Configuration
@EnableBatchProcessing
public class ChunkJdbcJobConfig
{

	private final Logger log = LoggerFactory.getLogger(ChunkJdbcJobConfig.class);
	private final String JOB_NAME = "ChunkJdbc";

	@Value("${chunkSize:10}") private int chunkSize;
	@Autowired private JobBuilderFactory jobBuilderFactory;
	@Autowired private StepBuilderFactory stepBuilderFactory;
	@Autowired private DataSource dataSource;


	@Bean(name=JOB_NAME + "Job")
	public Job JdbcExampleJob() throws Exception
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
	public JdbcPagingItemReader<Member> reader() throws Exception
	{

		Map<String,Object> parameterValues = new HashMap<>();
		parameterValues.put("amount", "10000");

		//pageSize와 fethSize는 동일하게 설정
		return new JdbcPagingItemReaderBuilder<Member>()
				.name(JOB_NAME + "Reader")
				.pageSize(10)
				.fetchSize(10)
				.dataSource(dataSource)
				.rowMapper(new BeanPropertyRowMapper<>(Member.class))
				.queryProvider(customQueryProvider())
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


	@StepScope
	@Bean(name=JOB_NAME + "Writer")
	public JdbcBatchItemWriter<Member> writer()
	{

		return new JdbcBatchItemWriterBuilder<Member>()
				.dataSource(dataSource)
				.sql("UPDATE MEMBER SET AMOUNT = :amount WHERE ID = :id")
				.beanMapped()
				.build();

	}


	public PagingQueryProvider customQueryProvider() throws Exception
	{

		SqlPagingQueryProviderFactoryBean queryProviderFactoryBean = new SqlPagingQueryProviderFactoryBean();

		queryProviderFactoryBean.setDataSource(dataSource);
		queryProviderFactoryBean.setSelectClause("SELECT ID, NAME, EMAIL, NICK_NAME, STATUS, AMOUNT ");
		queryProviderFactoryBean.setFromClause("FROM MEMBER ");
		queryProviderFactoryBean.setWhereClause("WHERE AMOUNT >= :amount");

		Map<String,Order> sortKey = new HashMap<>();
		sortKey.put("id", Order.ASCENDING);
		queryProviderFactoryBean.setSortKeys(sortKey);

		return queryProviderFactoryBean.getObject();

	}

}