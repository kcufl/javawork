package com.batch.job.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.batch.job.service.BusinessTasklet;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableBatchProcessing
public class TaskletClassJobConfig
{

	private final Logger log = LoggerFactory.getLogger(TaskletClassJobConfig.class);
	private final String JOB_NAME = "TaskletClass";

	@Autowired public JobBuilderFactory jobBuilderFactory;
	@Autowired public StepBuilderFactory stepBuilderFactory;


	@Bean(name=JOB_NAME + "Job")
	public Job job()
	{

		Job customJob = jobBuilderFactory
				.get(JOB_NAME + "Job")
				.start(step())
				.build();

		return customJob;

	}


	@Bean(name=JOB_NAME + "Step")
	public Step step()
	{

		return stepBuilderFactory
				.get(JOB_NAME + "Step")
				.tasklet(new BusinessTasklet())
				.build();

	}

}