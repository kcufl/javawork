package com.kcufl.batch.main;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.converter.JobParametersConverter;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.ExitCodeMapper;
import org.springframework.batch.core.launch.support.JvmSystemExiter;
import org.springframework.batch.core.launch.support.SimpleJvmExitCodeMapper;
import org.springframework.batch.core.launch.support.SystemExiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@SpringBootApplication(scanBasePackages = "com.kcufl.batch.job.*")
@MapperScan(basePackages = "com.kcufl.batch.job*")
@ComponentScan(basePackages = "com.kcufl")
public class SpringBootBatchApplication implements CommandLineRunner {

	private final static Logger logger = LoggerFactory.getLogger(SpringBootBatchApplication.class);

	@Autowired
	private Collection<Job> jobs = Collections.emptySet();

	@Autowired
	private JobLauncher jobLauncher;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SpringBootBatchApplication.class);
		app.run(args);
	}

	@Override
	public void run(String... args) throws Exception {
		ExitCodeMapper exitCodeMapper = new SimpleJvmExitCodeMapper();
		SystemExiter exiter = new JvmSystemExiter();


		try {
			JobParametersConverter parameters = new DefaultJobParametersConverter();
			JobParameters jobParameters = parameters.getJobParameters(StringUtils.splitArrayElementsIntoProperties(args, "="));

			Assert.isTrue((args == null) || (args.length == 0) || (!jobParameters.isEmpty()), "Invalid JobParameters " + Arrays.asList(parameters) + ". If parameters are provided they should be in the form name=value (no whitespace).");

			String jobName = jobParameters.getString("-batch.name");

			Optional<Job> job = jobs.stream().filter(s -> s.getName().equals(jobName)).findFirst();

			JobExecution jobExecution = jobLauncher.run(job.get(), jobParameters);
			ExitStatus exitStatis = jobExecution.getExitStatus();

			int exitCode = exitCodeMapper.intValue(exitStatis.getExitCode());

			exiter.exit(exitCode);
		} catch (Exception e) {
			logger.error("Job Terminated in error", e);
			System.out.println("Job Terminated in error :: " + e.getMessage());

			exiter.exit(exitCodeMapper.intValue(ExitStatus.FAILED.getExitCode()));

			throw new Exception(e);
		}
	}

}
