package net.shopin.history.annotation;

/**
 * @title: History
 * @description: TODO(用一句话描述该文件做什么)
 * @author: qmg
 * @date: 2020/6/8 13:17
 * @version: V1.0
 */

import java.lang.annotation.*;

/**
 * @title: History
 * @description: 开启注解后 对 该实体操作记录历史信息
 * @author: qiaomg
 * @date: 2020/6/8 10:51
 * @version: V1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface History {
    String value() default "_history";
}
