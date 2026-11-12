package com.aircookies.aircookeisairobotspringboot.aspect;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface ApiOperationLog {

    /**
     * API描述
     *
     * @return
     */
    String description() default "";

}
