package co.fatboa.backsystem.service.Impl;

import co.fatboa.backsystem.BackSystemApplication;
import co.fatboa.backsystem.domain.entity.User;
import co.fatboa.backsystem.restcontroller.UserRestController;
import co.fatboa.backsystem.service.IUserService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.net.URI;


/**
 * @Auther: hl
 * @Date: 2018/9/6 15:00
 * @Description:
 * @Modified By:
 * @Version 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackSystemApplication.class)
@AutoConfigureMockMvc
//@WebAppConfiguration
//@EnableWebMvc
public class UserServiceTest {
    @Autowired
    private MockMvc mockMvc; //mockmvc测试对象

    @Before
    public void before() {
    }

    /**
     * 测试新增用户
     */
    @Test
    public void save() throws Exception {
        User user = new User();
        user.setPassword("123456");
        user.setName("admin");
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        String json = gson.toJson(user);
        String res = mockMvc.perform
                (
                        MockMvcRequestBuilders.post("/japi/backsystem/user/save")
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .content(json)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()//返回mvcresult
                .getResponse().getContentAsString();//转换成字符串打印
        System.out.println(res);
    }
}