package com.kcufl.batch.job.config;

/*
public class Rollback
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
					.noRollback(NullPointerException.class) // NullPointerException 발생  rollback이 되지 않게 설정
					.build();
		}
}
*/