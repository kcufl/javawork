package com.kcufl.batch.job.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kcufl.batch.job.service.CustomService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableBatchProcessing
public class TaskletMethodInvokingJobConfig
{

	private final Logger log = LoggerFactory.getLogger(TaskletMethodInvokingJobConfig.class);
	private final String JOB_NAME = "TaskletMethodInvoking";

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
				.tasklet(adapter())
				.build();

	}


	@Bean(name=JOB_NAME + "Adapter")
	public MethodInvokingTaskletAdapter adapter()
	{

		MethodInvokingTaskletAdapter adapter = new MethodInvokingTaskletAdapter();

		adapter.setTargetObject(service());
		adapter.setTargetMethod("businessLogic");

		return adapter;

	}


	@Bean(name=JOB_NAME + "Service")
	public CustomService service()
	{

		return new CustomService ();

	}

}