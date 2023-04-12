package com.kxzhu.timing_food_delivery.controller;

import com.kxzhu.timing_food_delivery.common.R;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

/**
 * @ClassName CommonController
 * @Description TODO
 * 后端过程：
 * 上传：
 * 浏览器访问localhost:8080/backend/page/demo/upload.html页面，选择图片上传后，会发送POST请求到http://localhost:8080/common/upload，由controller处理
 * controller中的upload方法处理该post请求。通过MultipartFile类型的形参，接收上传的文件。
 * （上传文件时，elementUI会自动生成"Content-Disposition: form-data; name="file"; filename="fileName.jpeg" "
 *   注意此处的name，要和控制器方法形参同名。而类型固定为MultipartFile，这样可以使用spring框架封装的代码。）
 *  upload()还要生成文件名fileName、将临时文件file转存到服务器指定位置。在转存前还需要检查该文件目录是否存在（如不存在，就要创建），最后返回文件名给前端（封装在R中）
 *
 *  下载：
 *  前端发出下载请求（GET）/common/download?name=${response.data}，带着文件名参数来服务器，由controller中的download方法处理。
 *  到服务器的指定目录下（basePath+filename），通过输入流读取文件 到输入流对象fileInputStream，再通过响应response得到输出流对象outputStream 将文件写回浏览器
 *
 * @Author zhukexin
 * @Date 2023-02-22 15:42
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {
    //注解：@Value 是org.springframework.beans.factory.annotation包下的。可以将配置文件中的值赋值给Java对象中的属性。
    //此处将配置文件中定义的timing_food_delivery.path属性值注入给basePath
    @Value("${timing_food_delivery.path}")
    private String basePath;

    /**
     * 文件上传
     * @param file 上传文件时，form表单中 input子标签的 name值
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        //参数：类型为MultipartFile。名字和(浏览器请求体form-data处的)name值一致
        //log.info(file.toString());

        //文件名的设置：随机UUID名(避免重名造成覆盖) + 后缀
        String originalFilename = file.getOriginalFilename();//原始文件名
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));//文件名的后缀
        String fileName = UUID.randomUUID().toString() + suffix;

        // 如果basePath在硬盘中是不存在的，会报java.io.FileNotFoundException
        // 希望如果不存在的化，自动创建这个目录
        File dir = new File(basePath);  //创建目录对象
        if(! dir.exists() ){    //不存在这个目录
            dir.mkdirs();   //则创建目录（含所有不存在的parent目录）
            //mkdir(): 创建目录。如果父目录不存在/当前目录已存在，则创建失败
            //mkdirs(): 创建目录。如果父目录不存在，也同时创建父目录。
        }

        //file 是一个临时文件(tmp)，需要转存到指定位置，否则请求完成后，临时文件会删除
        try {
            file.transferTo(new File(basePath + fileName));//将临时文件转存到指定位置
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success(fileName);//前端需要。写回文件名
    }


    /**
     * 文件下载
     * 文件上传完成后，回调handleAvatarSuccess方法，即紧接着发送/common/download请求，并将name传给服务器。
     * /common/download?name=xxxxx请求传来之后，会执行本方法。到指定目录下，通过输入流读取name这个文件。再通过输出流写给浏览器。
     * 浏览器就可以显示刚上传的图片
     * @param name
     * @param response
     */
    //浏览器发出的请求是http://localhost:8080/common/download?name=undefined（get请求）
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        //为什么返回值是void：通过输出流，向浏览器写回二进制数据即可，不需要返回值
        //参数：name：获取请求参数name。response：输出流需要通过response来获得

        try {
            //输入流，通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));//basePath + name是要读取的文件
            //输出流，通过输出流将文件写回浏览器（用response），在浏览器中展示图片
            ServletOutputStream outputStream = response.getOutputStream();
            //设置响应回去的文件是什么类型。"image/jpeg"是固定的，代表图片文件
            response.setContentType("image/jpeg");

            //读到哪去：
            int len = 0;
            byte[] bytes = new byte[1024];
            while ( (len = fileInputStream.read(bytes)) != -1 ){ // ==-1就是读完了。没读完就继续读
                outputStream.write(bytes, 0, len);  //把bytes中内容写进去，从0开始写len这么多
                outputStream.flush();   //刷新
            }
            //fileInputStream & outputStream是局部变量，只能在try中关。不能在finally中关
            fileInputStream.close();
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
