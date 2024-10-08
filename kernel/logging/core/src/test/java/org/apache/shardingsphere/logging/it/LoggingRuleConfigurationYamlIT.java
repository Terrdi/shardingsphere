/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.logging.it;

import org.apache.shardingsphere.logging.config.LoggingRuleConfiguration;
import org.apache.shardingsphere.logging.logger.ShardingSphereAppender;
import org.apache.shardingsphere.logging.logger.ShardingSphereLogger;
import org.apache.shardingsphere.test.it.yaml.YamlRuleConfigurationIT;

import java.util.Collections;

class LoggingRuleConfigurationYamlIT extends YamlRuleConfigurationIT {
    
    LoggingRuleConfigurationYamlIT() {
        super("yaml/logging-rule.yaml", getExpectedRuleConfiguration());
    }
    
    private static LoggingRuleConfiguration getExpectedRuleConfiguration() {
        ShardingSphereLogger logger = new ShardingSphereLogger("foo_logger", "INFO", true, "foo_appender");
        logger.getProps().put("k0", "v0");
        logger.getProps().put("k1", "v1");
        ShardingSphereAppender appender = new ShardingSphereAppender("foo_appender", "foo_appender_class", "sss");
        appender.setFile("foo_file");
        return new LoggingRuleConfiguration(Collections.singletonList(logger), Collections.singletonList(appender));
    }
}
