import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    // 处理根路径 / 的请求
    @GetMapping("/")
    public String index() {
        return "认证服务运行中（端口：8081）";
    }
}