package com.batch.job.config;

public class test {

@BeforeStep
public void initializeValues(StepExecution stepExecution) {
	String value = stepExecution.getJobExecution().getExecutionContext().getString("MY_VALUE");
}
	
}
