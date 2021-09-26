package com.batch.job.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableBatchProcessing
public class TaskletFlowJobConfig
{

	private final Logger log = LoggerFactory.getLogger(TaskletFlowJobConfig.class);
	private final String JOB_NAME = "TaskletFlow";

	@Autowired public JobBuilderFactory jobBuilderFactory;
	@Autowired public StepBuilderFactory stepBuilderFactory;


	@Bean(name=JOB_NAME + "Job")
	public Job ExampleJob3()
	{

		Job exampleJob = jobBuilderFactory
				.get(JOB_NAME + "Job")
				.start(startStep())
					.on("FAILED")			// startStep의 ExitStatus가 FAILED일 경우
					.to(failOverStep())		// failOver Step을 실행 시킨다.
					.on("*")				// failOver Step의 결과와 상관없이
					.to(writeStep())		// write Step을 실행 시킨다.
					.on("*")				// write Step의 결과와 상관없 이
					.end()					// Flow를 종료시킨다.

				.from(startStep())			// startStep이 FAILED가 아니고
					.on("COMPLETED")		// COMPLETED일 경우
					.to(processStep())		// process Step을 실행 시킨다
					.on("*")				// process Step의 결과와 상관없이
					.to(writeStep())		// write Step을 실행 시킨다.
					.on("*")				// wrtie Step의 결과와 상관없이
					.end()					// Flow를 종료 시킨다.

				.from(startStep())			// startStep의 결과가 FAILED, COMPLETED가 아닌
					.on("*")				// 모든 경우
					.to(writeStep())		// write Step을 실행시킨다.
					.on("*")				// write Step의 결과와 상관없이
					.end()					// Flow를 종료시킨다.
				.end()
				.build();

		return exampleJob;

	}


	@Bean(name=JOB_NAME + "StartStep")
	public Step startStep()
	{

		return stepBuilderFactory
				.get(JOB_NAME + "StartStep")
				.tasklet((contribution, chunkContext) ->
				{
					log.info("Start Step!");

					String result = "COMPLETED";
					//String result = "FAIL";
					//String result = "UNKNOWN";

					//Flow에서 on은 RepeatStatus가 아닌 ExitStatus를 바라본다.
					if(result.equals("COMPLETED"))
						contribution.setExitStatus(ExitStatus.COMPLETED);
					else if(result.equals("FAIL"))
						contribution.setExitStatus(ExitStatus.FAILED);
					else if(result.equals("UNKNOWN"))
						contribution.setExitStatus(ExitStatus.UNKNOWN);

					return RepeatStatus.FINISHED;
				})
				.build();

	}


	@Bean(name=JOB_NAME + "FailOverStep")
	public Step failOverStep()
	{

		return stepBuilderFactory
				.get(JOB_NAME + "FailOverStep")
				.tasklet((contribution, chunkContext) ->
				{
					log.info("FailOver Step!");
					return RepeatStatus.FINISHED;
				})
				.build();

	}


	@Bean(name=JOB_NAME + "ProcessStep")
	public Step processStep()
	{

		return stepBuilderFactory
				.get(JOB_NAME + "ProcessStep")
				.tasklet((contribution, chunkContext) ->
				{
					log.info("Process Step!");
					return RepeatStatus.FINISHED;
				})
				.build();

	}


	@Bean(name=JOB_NAME + "WriteStep")
	public Step writeStep()
	{

		return stepBuilderFactory
				.get(JOB_NAME + "WriteStep")
				.tasklet((contribution, chunkContext) ->
				{
					log.info("Write Step!");
					return RepeatStatus.FINISHED;
				})
				.build();

	}

}