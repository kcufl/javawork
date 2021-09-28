package com.kcufl.batch.job.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomService {

	private final static Logger log = LoggerFactory.getLogger(CustomService.class);

    public void businessLogic() {
        //비즈니스 로직
        for(int idx = 0; idx < 10; idx ++){
            log.info("[idx] = " + idx);
        }
    }
}