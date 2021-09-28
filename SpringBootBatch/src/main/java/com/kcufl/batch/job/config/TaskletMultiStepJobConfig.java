package com.kcufl.batch.job.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class TaskletMultiStepJobConfig
{

	private final Logger log = LoggerFactory.getLogger(TaskletMultiStepJobConfig.class);
	private final String JOB_NAME = "TaskletMultiStep";

	@Autowired public JobBuilderFactory jobBuilderFactory;
	@Autowired public StepBuilderFactory stepBuilderFactory;


	@Bean(name=JOB_NAME + "Job")
	public Job job()
	{

		Job exampleJob = jobBuilderFactory
				.get(JOB_NAME + "Job")
				.start(startStep())
				.next(nextStep())
				.next(lastStep())
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
					return RepeatStatus.FINISHED;
				})
				.build();

	}


	@Bean(name=JOB_NAME + "NextStep")
	public Step nextStep()
	{

		return stepBuilderFactory
				.get(JOB_NAME + "NextStep")
				.tasklet((contribution, chunkContext) ->
				{
					log.info("Next Step!");
					return RepeatStatus.FINISHED;
				})
				.build();

	}


	@Bean(name=JOB_NAME + "LastStep")
	public Step lastStep()
	{

		return stepBuilderFactory
				.get(JOB_NAME + "LastStep")
				.tasklet((contribution, chunkContext) ->
				{
					log.info("Last Step!!");
					return RepeatStatus.FINISHED;
				})
				.build();

	}

}