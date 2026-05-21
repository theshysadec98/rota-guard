/* Nhom I */
package com.rotaguard.config;

import com.rotaguard.rule.FatigueRule;
import com.rotaguard.rule.impl.ConsecutiveNightRule;
import com.rotaguard.rule.impl.QuickReturnRule;
import com.rotaguard.rule.impl.WeeklyHoursRule;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuleConfig {

  @Bean
  List<FatigueRule> fatigueRules(
      QuickReturnRule quickReturnRule,
      ConsecutiveNightRule consecutiveNightRule,
      WeeklyHoursRule weeklyHoursRule) {
    return List.of(quickReturnRule, consecutiveNightRule, weeklyHoursRule);
  }
}
