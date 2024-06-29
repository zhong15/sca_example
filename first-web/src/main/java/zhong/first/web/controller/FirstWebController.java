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

package zhong.first.web.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import zhong.first.service.api.FirstService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Zhong
 * @since 0.0.1
 */
@RefreshScope
@RequestMapping("/test")
@RestController
public class FirstWebController {
    private static final Logger log = LoggerFactory.getLogger(FirstWebController.class);

    @Value("${db.username}")
    private String dbUsername;
    @Autowired
    private FirstService firstService;
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/getNacosConfig")
    @SentinelResource(value = "/getNacosConfig", fallback = "getNacosConfigFallback")
    public Map<String, String> getNacosConfig() {
//        log.info(restTemplate.getForObject("http://first-service/hello/echo/11111", String.class));
        log.info(firstService.echo("123"));
        Map<String, String> map = new HashMap<>();
        map.put("dbUsername", dbUsername);
        return map;
    }

    public Map<String, String> getNacosConfigFallback() {
        Map<String, String> map = new HashMap<>();
        map.put("fallback", "fallback");
        return map;
    }

    /**
     * 更新城市的天气
     * <p>每隔一次会成功一次、失败一次
     *
     * @param address 测试
     * @param day     日期，格式：yyyy-MM-dd
     * @param weather 天气
     * @return 1 如果成功
     */
    @GetMapping("/updateAddressWeather")
    public Integer updateAddressWeather(@RequestParam(name = "address") String address,
                                        @RequestParam(name = "day") String day,
                                        @RequestParam(name = "weather") String weather) {
        log.info("updateAddressWeather address: {}, day: {}, weather: {}", address, day, weather);
        return firstService.updateAddressWeather(address, day, weather);
    }
}
