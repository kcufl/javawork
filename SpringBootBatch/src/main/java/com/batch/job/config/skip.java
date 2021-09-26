package com.batch.job.config;

/*
public class skip
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
				.skipLimit(1) // skip 허용 횟수, 해당 횟수 초과시 Error 발생, Skip 사용시 필수 설정
				.skip(NullPointerException.class)// NullPointerException에 대해선 Skip
				.noSkip(SQLException.class) // SQLException에 대해선 noSkip
				//.skipPolicy(new CustomSkipPolilcy) // 사용자가 커스텀하며 Skip Policy 설정 가능
				.build();
	}
}
*/