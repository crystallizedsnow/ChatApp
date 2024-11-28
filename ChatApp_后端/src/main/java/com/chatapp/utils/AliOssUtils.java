package com.chatapp.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyuncs.exceptions.ClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Component
@Slf4j
public class AliOssUtils {
    private String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";
    public String upload(MultipartFile file) throws IOException, ClientException {
        //获取上传的文件的输入流
        InputStream inputStream=file.getInputStream();
       //避免文件覆盖
        String originalFileName=file.getOriginalFilename();
        String fileName= UUID.randomUUID().toString()+originalFileName.substring(originalFileName.lastIndexOf("."));
        //上传文件到OSS
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        OSS ossClient=new OSSClientBuilder().build(endpoint, credentialsProvider);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream);
        PutObjectResult result = ossClient.putObject(putObjectRequest);
       //文件访问路径
        String url=endpoint.split("//")[0]+"//"+bucketName+"."+endpoint.split("//")[1]+"/"+fileName;
        ossClient.shutdown();
        return url;
    }
    public void deleteImg(String url) throws ClientException {
        // 填写文件完整路径。文件完整路径中不能包含Bucket名称。
        String fileName = url.substring(url.lastIndexOf("/")+1);
        // 创建OSSClient实例。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);
        // 删除文件或目录。如果要删除目录，目录必须为空。
        ossClient.deleteObject(bucketName, fileName);
//        // 关闭OSSClient。
        ossClient.shutdown();
   }

