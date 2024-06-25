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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Zhong
 * @since 0.0.1
 */
@RequestMapping("/hello")
@RestController
public class HelloController {
    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    @Value("${db.username}")
    private String dbUsername;

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
}
