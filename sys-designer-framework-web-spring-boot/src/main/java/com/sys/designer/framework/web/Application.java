package com.sys.designer.framework.web;

import com.sys.designer.framework.PackageInfo;
import com.sys.designer.framework.common.FullModelBeanNameGenerator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackageClasses = {
        PackageInfo.class
}, nameGenerator = FullModelBeanNameGenerator.class)
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Application {
}
