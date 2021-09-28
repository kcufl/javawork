package com.kcufl.batch.job.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
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
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.kcufl.batch.job.model.Member;

import lombok.extern.slf4j.Slf4j;

/**
 * 전체 금액이 10,000원 이상인 회원들에게 1,000원 캐시백을 주는 배치
 * @param <T>
 */

@Slf4j
@Configuration
@EnableBatchProcessing
public class ChunkCompositeWriterJobConfig2<T>
{

	private final Logger log = LoggerFactory.getLogger(ChunkCompositeWriterJobConfig2.class);
	private final String JOB_NAME = "ChunkCompositeWriter2";

	@Value("${chunkSize:1000}") private int chunkSize;
	@Autowired private JobBuilderFactory jobBuilderFactory;
	@Autowired private StepBuilderFactory stepBuilderFactory;
	@Autowired private SqlSessionFactory sqlSessionFactory;
	@Autowired private DataSource dataSource;


	@Bean(name=JOB_NAME + "Job")
	public Job job() throws Exception
	{

		Job exampleJob = jobBuilderFactory
				.get(JOB_NAME + "Job")
				.start(step(dataSource))
				.build();

		return exampleJob;

	}


	@JobScope
	@Bean(name=JOB_NAME + "Step")
	protected Step step(DataSource datasource) throws Exception
	{
		return this.stepBuilderFactory
				.get(JOB_NAME + "Step")
				.<Member, Member>chunk(chunkSize)
				.reader(reader(datasource))
				.processor(processor())
				.writer(writer(datasource))
				.build();

	}


	@StepScope
	@Bean(name=JOB_NAME + "Reader")
	public JdbcPagingItemReader<Member> reader(DataSource datasource) throws Exception
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


	@Bean(name=JOB_NAME + "Writer")
	public CompositeItemWriter<Member> writer(DataSource dataSource)
	{

		CompositeItemWriter<Member> compositeItemWriter = new CompositeItemWriter<>();
		compositeItemWriter.setDelegates(Arrays.asList( writer1(dataSource), writer2(dataSource)));
		return compositeItemWriter;

	}


	public JdbcBatchItemWriter<Member> writer1(DataSource dataSource)
	{

		return new JdbcBatchItemWriterBuilder<Member>()
				.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
				.sql("UPDATE xxxx")
				.dataSource(dataSource)
				.build();

	}


	public JdbcBatchItemWriter<Member> writer2(DataSource dataSource)
	{

		return new JdbcBatchItemWriterBuilder<Member>()
				.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
				.sql("UPDATE yyyyy")
				.dataSource(dataSource)
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