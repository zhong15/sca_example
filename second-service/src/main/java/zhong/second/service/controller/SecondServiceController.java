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

package zhong.second.service.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import zhong.second.service.api.SecondService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Zhong
 * @since 0.0.1
 */
@RestController
public class SecondServiceController implements SecondService {
    private static final Logger log = LoggerFactory.getLogger(SecondServiceController.class);

    @Value("${db.username}")
    private String dbUsername;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @SentinelResource(value = "/echo", fallback = "echoFallback")
    @GetMapping("/echo/{s}")
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

    @Override
    public Long saveOrUpdateWeather(Long addressId, String address, String day, String weather) {
        log.info("updateAddressWeather address: {}, day: {}, weather: {}", address, day, weather);

        java.sql.Date theDay = parseSqlDate(day);
        if (theDay == null) {
            return null;
        }

        /*
         * 操作 test2 库：查询 weather 表数据是否存在
         */
        List<Long> idList = jdbcTemplate.queryForList("SELECT id FROM weather WHERE address_id = ? AND the_day = ?",
                Long.class, addressId, theDay);
        Long id = idList == null || idList.size() < 1 ? null : idList.get(0);
        log.info("id: {}", id);

        /*
         * 操作 test2 库：weather 表数据不存在则插入，存在则更新
         */
        if (id == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            int rows = jdbcTemplate.update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement pStmt = connection.prepareStatement(
                            "INSERT INTO weather (address_id, address, the_day, weather) VALUES (?, ?, ?, ?)",
                            PreparedStatement.RETURN_GENERATED_KEYS);
                    pStmt.setLong(1, addressId);
                    pStmt.setString(2, address);
                    pStmt.setDate(3, theDay);
                    pStmt.setString(4, weather);
                    return pStmt;
                }
            }, keyHolder);

            log.info("insert weather rows: {}, id: {}", rows, keyHolder.getKey());

            return rows == 1 ? keyHolder.getKey().longValue() : null;
        } else {
            int rows = jdbcTemplate.update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement pStmt = connection.prepareStatement(
                            "UPDATE weather SET weather = ? WHERE id = ?",
                            PreparedStatement.RETURN_GENERATED_KEYS);
                    pStmt.setString(1, weather);
                    pStmt.setLong(2, id);
                    return pStmt;
                }
            });

            log.info("update weather rows: {}", rows);

            return rows == 1 ? id : null;
        }
    }

    private static java.sql.Date parseSqlDate(String day) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return new java.sql.Date(dateFormat.parse(day).getTime());
        } catch (ParseException e) {
            log.warn("day parse error: {}", day, e);
            return null;
        }
    }
}
