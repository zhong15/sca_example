/*
 * MIT License
 *
 * Copyright (c) 2024 Zhong
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package zhong.first.service.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import io.seata.spring.annotation.GlobalTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import zhong.first.service.api.FirstService;
import zhong.second.service.api.SecondService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Zhong
 * @since 0.0.1
 */
@RestController
public class FirstServiceController implements FirstService {
    private static final Logger log = LoggerFactory.getLogger(FirstServiceController.class);

    @Value("${db.username}")
    private String dbUsername;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private SecondService secondService;

    @SentinelResource(value = "/echo", fallback = "echoFallback")
    @Override
    public String echo(@PathVariable("s") String s) {
        log.info("echo " + s);
        return "hello " + s;
    }

    public String echoFallback(String s) {
        return "fallback";
    }

    @SentinelResource(value = "/getNacosConfig", fallback = "getNacosConfigFallback")
    @GetMapping("/getNacosConfig")
    public Map<String, String> getNacosConfig() {
        Map<String, String> map = new HashMap<>();
        map.put("dbUsername", dbUsername);
        return map;
    }

    public Map<String, String> getNacosConfigFallback() {
        Map<String, String> map = new HashMap<>();
        map.put("fallback", "fallback");
        return map;
    }

    @GlobalTransactional(timeoutMills = 3000, name = "first-service-tx")
    @Override
    public Integer updateAddressWeather(String address, String day, String weather) {
        log.info("updateAddressWeather address: {}, day: {}, weather: {}", address, day, weather);

        /*
         * 操作 test1 库：address 表数据必须存在
         */
        List<Long> idList = jdbcTemplate.queryForList("SELECT id FROM address WHERE address = ?", Long.class, address);
        Long id = idList == null || idList.size() < 1 ? null : idList.get(0);
        if (id == null) {
            log.info("id not found");
            return null;
        }

        /*
         * 操作 test1 库：如果是当天，则更新 address 表的 weather 字段
         */
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (Objects.equals(day, dateFormat.format(new Date()))) {
            int rows = jdbcTemplate.update("UPDATE address SET weather = ? WHERE id = ?", weather, id);
            log.info("update address rows: {}", rows);
            if (rows == 0) {
                return null;
            }
        }

        /*
         * 操作 test2 库：插入或更新 weather 表的
         */
        Long weatherId = secondService.saveOrUpdateWeather(id, address, day, weather);
        log.info("saveOrUpdateWeather weatherId: {}", weatherId);

        /*
         * 通过 weatherId 字段奇偶性抛出异常测试分布式事务回滚 test1 库、test2 库
         */
        if (weatherId != null && weatherId % 2 == 0) {
            throw new IllegalStateException("测试分布式事务回滚");
        }
        return weatherId == null ? 0 : 1;
    }
}
