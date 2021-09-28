package com.kcufl.batch.job.config;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableBatchProcessing
public class TaskletDecideJobConfig
{

	private static final Logger log = LoggerFactory.getLogger(TaskletDecideJobConfig.class);
	private final String JOB_NAME = "TaskletDecide";

	@Autowired public JobBuilderFactory jobBuilderFactory;
	@Autowired public StepBuilderFactory stepBuilderFactory;


	@Bean(name=JOB_NAME + "Job")
	public Job deciderJob()
	{

		return jobBuilderFactory
				.get("deciderJob")
				.start(startStep())
				.next(decider())		// 홀수 | 짝수 구분
				.from(decider())		// decider의 상태가
					.on("ODD")			// ODD라면
					.to(oddStep())		// oddStep로 간다.
				.from(decider())		// decider의 상태가
					.on("EVEN")			// ODD라면
					.to(evenStep())		// evenStep로 간다.
				.end()					// builder 종료
				.build();

	}


	@Bean(name=JOB_NAME + "StartStep")
	public Step startStep()
	{

		return stepBuilderFactory
				.get("startStep")
				.tasklet((contribution, chunkContext) -> {
					log.info(">>>>> Start!");
					return RepeatStatus.FINISHED;
				})
				.build();

	}


	@Bean(name=JOB_NAME + "EvenStep")
	public Step evenStep()
	{

		return stepBuilderFactory
				.get("evenStep")
				.tasklet((contribution, chunkContext) -> {
					log.info(">>>>> 짝수입니다.");
					return RepeatStatus.FINISHED;
				})
				.build();

	}


	@Bean(name=JOB_NAME + "OddStep")
	public Step oddStep()
	{

		return stepBuilderFactory
				.get("oddStep")
				.tasklet((contribution, chunkContext) -> {
					log.info(">>>>> 홀수입니다.");
					return RepeatStatus.FINISHED;
				})
				.build();

	}


	@Bean(name=JOB_NAME + "Decider")
	public JobExecutionDecider decider()
	{

		return new OddDecider();

	}


	public static class OddDecider implements JobExecutionDecider
	{

		@Override
		public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution)
		{
			Random rand = new Random();

			int randomNumber = rand.nextInt(50) + 1;
			log.info("랜덤숫자: {}", randomNumber);

			if(randomNumber % 2 == 0)
			{
				return new FlowExecutionStatus("EVEN");
			}
			else
			{
				return new FlowExecutionStatus("ODD");
			}
		}

	}

}