package com.batch.job.config;
/*
public class Retry
	{
	@Bean
	@JobScope
	public Step Step() throws Exception
	{
		return stepBuilderFactory.get("Step")
				.<Member,Member>chunk(10)
				.reader(reader(null))
				.processor(processor(null))
				.writer(writer(null))
				.faultTolerant()
				.retryLimit(1) //retry 횟수, retry 사용시 필수 설정, 해당 Retry 이후 Exception시 Fail 처리
				.retry(SQLException.class) // SQLException에 대해선 Retry 수행
				.noRetry(NullPointerException.class) // NullPointerException에 no Retry
				//.retryPolicy(new CustomRetryPolilcy) // 사용자가 커스텀하며 Retry Policy 설정 가능
				.build();
	}

}
*/